package com.hfw.nrc.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.NrcHrPost;
import com.hfw.nrc.domain.NrcHrSanctioned;
import com.hfw.nrc.domain.Rcho;
import com.hfw.nrc.dto.NrcHrSubmitRequest;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcHrPostRepository;
import com.hfw.nrc.repository.NrcHrSanctionedRepository;
import com.hfw.nrc.repository.RchoRepository;
import com.hfw.nrc.util.NrcUserPrincipal;

/**
 * All NRC HR business logic - designation configuration, the hire/vacate/
 * approve state machine, and the table builders behind the HR page. Ported
 * faithfully from KABHI_BACKEND's KabhiHrService (the real, currently-used
 * KABHI HR module - not the legacy HRController/HREntity one). See
 * db/migration/V17 for the one deliberate deviation (sanctioned counts are
 * per-hospital, not global-per-designation - NRC hospitals aren't uniformly
 * staffed the way KABHI districts are).
 *
 * MODEL: a "post" is identified by (hId, designation, slotNo) - unlike
 * KABHI, NRC designations don't need a postGroup grouping layer (no NRC
 * designation shares a sanctioned pool with another the way KABHI's 5
 * doctor titles share one "DOCTOR" group), so designation itself is the
 * group. Every submission is a new NrcHrPost row; the slot's live state is
 * whichever row is isCurrent = true and approvalStatus = APPROVED. A
 * PENDING row sits alongside, not yet affecting the live state, until an
 * admin approves it - every NRC HR submission goes through approval (unlike
 * KABHI's admin-own-STATE-team auto-approve rule, which has no NRC
 * equivalent since NRC's admin has no hospital of their own).
 */
@Service
public class NrcHrPostService {

	@Autowired private NrcHrPostRepository hrRepo;
	@Autowired private NrcHrSanctionedRepository sanctionedRepo;
	@Autowired private HospitalRepository hospitalRepo;
	@Autowired private RchoRepository rchoRepo;

	/** District an RCHO principal is scoped to, or null if they aren't (or aren't actually an RCHO). */
	private Integer districtIdForRcho(NrcUserPrincipal principal) {
		if (!principal.isRcho()) {
			return null;
		}
		return rchoRepo.findByUId((int) principal.getUserId()).map(Rcho::getDistrictId).orElse(null);
	}

	private List<Integer> hospitalIdsInDistrict(int districtId) {
		return hospitalRepo.findAll().stream()
				.filter(h -> h.getDistrictId() != null && h.getDistrictId() == districtId)
				.map(Hospital::getHId)
				.collect(Collectors.toList());
	}

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");

	// ===================== Dropdown option lists =====================

	/**
	 * NRC's own five designations. MSW was in the original admin.component.ts
	 * form (see V15's note on why it was added there) but is dropped from
	 * this list per explicit request - existing MSW nrc_hr_post/
	 * nrc_hr_sanctioned rows are cleaned up separately (see V18), the old
	 * counts-only nr.nrc_hr table's msw_* columns (real historical data back
	 * to 2020) are untouched.
	 */
	public static final List<String> DESIGNATIONS = Arrays.asList(
			"Paediatrician / Medical Officer", "Staff Nurse", "Dietician", "Cook", "Attender");

	/** These two designations often have no formal email address at all - Email Id is optional for them, still required (and format-checked) for everyone else. */
	public static final List<String> EMAIL_OPTIONAL_DESIGNATIONS = Arrays.asList("Cook", "Attender");

	public static final List<String> STATUS_OPTIONS = Arrays.asList("Working", "Vacant");

	public static final List<String> CONSULTATION_DAYS = Arrays.asList(
			"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

	private static boolean isVacantStatus(String employmentStatus) {
		return "Vacant".equalsIgnoreCase(employmentStatus);
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	// ===================== Form metadata / availability =====================

	public Map<String, Object> getMeta(NrcUserPrincipal principal) {
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("role", principal.isAdmin() ? "ADMIN" : (principal.isRcho() ? "RCHO" : "HOSPITAL_USER"));
		meta.put("designations", DESIGNATIONS);
		meta.put("statuses", STATUS_OPTIONS);
		meta.put("consultationDays", CONSULTATION_DAYS);
		meta.put("emailOptionalDesignations", EMAIL_OPTIONAL_DESIGNATIONS);

		LocalDate today = LocalDate.now();
		meta.put("dojMin", today.minusDays(3).toString());
		meta.put("dojMax", today.plusDays(3).toString());

		if (!principal.isAdmin()) {
			Integer hId = principal.getHospitalId();
			Optional<Hospital> hospital = hId != null ? hospitalRepo.findById(hId) : Optional.empty();
			String hospitalName = hospital.map(Hospital::getName).orElse("");
			meta.put("hId", hId);
			meta.put("hospitalName", hospitalName);
			meta.put("title", "NRC " + hospitalName + " HR");
		} else {
			meta.put("title", "NRC Human Resources");
		}
		return meta;
	}

	/** Per-designation occupancy, used by the Add HR form to lock the Status field when a post is already filled. */
	public List<Map<String, Object>> getAvailability(NrcUserPrincipal principal) {
		Integer hId = principal.getHospitalId();
		if (hId == null) {
			return new ArrayList<>();
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (String designation : DESIGNATIONS) {
			List<NrcHrPost> current = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(hId, designation);
			List<NrcHrPost> occupiedRows = current.stream()
					.filter(r -> !isVacantStatus(r.getEmploymentStatus())).collect(Collectors.toList());
			boolean pending = hrRepo.findByHIdAndDesignation(hId, designation).stream()
					.anyMatch(r -> "PENDING".equals(r.getApprovalStatus()) || "RCHO_APPROVED".equals(r.getApprovalStatus()));
			int sanctioned = getSanctionedCount(hId, designation);

			Map<String, Object> m = new LinkedHashMap<>();
			m.put("designation", designation);
			m.put("occupied", !occupiedRows.isEmpty());
			m.put("currentPersonName", occupiedRows.isEmpty() ? null : occupiedRows.get(0).getPersonName());
			m.put("pending", pending);
			m.put("hasFreeSlot", occupiedRows.size() < sanctioned);
			out.add(m);
		}
		return out;
	}

	// ===================== Submit / approve / reject =====================

	private void validate(NrcHrSubmitRequest req, String designation) {
		if (isBlank(req.getPersonName())) {
			throw new IllegalArgumentException("Name of the person is required.");
		}
		if (isBlank(req.getEmploymentStatus()) || !STATUS_OPTIONS.contains(req.getEmploymentStatus().trim())) {
			throw new IllegalArgumentException("Please select a valid Status.");
		}
		if (isBlank(req.getDateOfJoining())) {
			throw new IllegalArgumentException("Date of Joining is required.");
		}
		LocalDate doj;
		try {
			doj = LocalDate.parse(req.getDateOfJoining().trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Date of Joining format, expected yyyy-MM-dd.");
		}
		LocalDate today = LocalDate.now();
		if (doj.isBefore(today.minusDays(3)) || doj.isAfter(today.plusDays(3))) {
			throw new IllegalArgumentException("Date of Joining must be within 3 days before or after today.");
		}
		// Required for every designation except Cook/Attender (see
		// EMAIL_OPTIONAL_DESIGNATIONS) - those two often have no formal email
		// address at all. Format-checked either way when a value IS provided.
		boolean emailOptional = EMAIL_OPTIONAL_DESIGNATIONS.contains(designation);
		if (!emailOptional && isBlank(req.getEmailId())) {
			throw new IllegalArgumentException("Email Id is required for this designation.");
		}
		if (!isBlank(req.getEmailId()) && !EMAIL_PATTERN.matcher(req.getEmailId().trim()).matches()) {
			throw new IllegalArgumentException("Email Id must be a valid email address.");
		}
		if (isBlank(req.getContactNo()) || !MOBILE_PATTERN.matcher(req.getContactNo().trim()).matches()) {
			throw new IllegalArgumentException("Contact No must be a valid 10-digit mobile number.");
		}
		if ("On-Consultation".equalsIgnoreCase(req.getEmploymentStatus())
				&& (req.getDaysOfConsultation() == null || req.getDaysOfConsultation().isEmpty())) {
			throw new IllegalArgumentException("Select at least one Day of Consultation.");
		}
	}

	public NrcHrPost submit(NrcHrSubmitRequest req, NrcUserPrincipal principal) {
		Integer hId = principal.getHospitalId();
		if (hId == null) {
			throw new IllegalStateException("This account has no hospital associated with it.");
		}

		String designation = req.getDesignation() == null ? null : req.getDesignation().trim();
		if (designation == null || !DESIGNATIONS.contains(designation)) {
			throw new IllegalArgumentException("Invalid designation.");
		}

		validate(req, designation);

		boolean vacating = isVacantStatus(req.getEmploymentStatus());

		boolean alreadyPending = hrRepo.findByHIdAndDesignation(hId, designation).stream()
				.anyMatch(r -> "PENDING".equals(r.getApprovalStatus()) || "RCHO_APPROVED".equals(r.getApprovalStatus()));
		if (alreadyPending) {
			throw new IllegalStateException("A submission for this post is already pending approval.");
		}

		List<NrcHrPost> currentRows = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(hId, designation);
		int sanctioned = getSanctionedCount(hId, designation);

		int slotNo;
		if (vacating) {
			NrcHrPost occupied = currentRows.stream()
					.filter(r -> !isVacantStatus(r.getEmploymentStatus()))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No active person found in this post to mark vacant."));
			slotNo = occupied.getSlotNo();
		} else {
			Set<Integer> occupiedSlots = currentRows.stream()
					.filter(r -> !isVacantStatus(r.getEmploymentStatus()))
					.map(NrcHrPost::getSlotNo)
					.collect(Collectors.toSet());
			Integer freeSlot = null;
			for (int s = 1; s <= sanctioned; s++) {
				if (!occupiedSlots.contains(s)) {
					freeSlot = s;
					break;
				}
			}
			if (freeSlot == null) {
				throw new IllegalStateException(
						"All sanctioned posts for this designation are already filled. "
								+ "Submit as Vacant against the existing post to record a resignation.");
			}
			slotNo = freeSlot;
		}

		NrcHrPost entity = new NrcHrPost();
		entity.setHId(hId);
		entity.setDesignation(designation);
		entity.setSlotNo(slotNo);
		entity.setPersonName(req.getPersonName().trim());
		entity.setDateOfJoining(Date.valueOf(LocalDate.parse(req.getDateOfJoining().trim())));
		entity.setEmailId(isBlank(req.getEmailId()) ? null : req.getEmailId().trim());
		entity.setContactNo(req.getContactNo().trim());
		entity.setEmploymentStatus(req.getEmploymentStatus().trim());
		entity.setDaysOfConsultation("On-Consultation".equalsIgnoreCase(req.getEmploymentStatus())
				&& req.getDaysOfConsultation() != null ? String.join(",", req.getDaysOfConsultation()) : null);
		entity.setSubmittedBy(principal.getUsername());
		entity.setSubmittedDate(LocalDateTime.now());

		// Every NRC HR submission requires admin approval - unlike KABHI, NRC's
		// admin has no hospital of their own, so there is no "own team,
		// auto-approve" case here.
		entity.setApprovalStatus("PENDING");
		entity.setCurrent(false);

		return hrRepo.save(entity);
	}

	/** RCHO tier: first review, moves PENDING -> RCHO_APPROVED. Does not flip isCurrent - only the final admin APPROVED does that. */
	public NrcHrPost rchoApprove(int hrPostId, NrcUserPrincipal rcho) {
		Integer districtId = districtIdForRcho(rcho);
		if (districtId == null) {
			throw new SecurityException("Only an RCHO can perform this action.");
		}
		NrcHrPost row = hrRepo.findById(hrPostId).orElseThrow(() -> new NoSuchElementException("Submission not found."));
		Hospital hospital = hospitalRepo.findById(row.getHId()).orElseThrow(() -> new NoSuchElementException("Hospital not found."));
		if (hospital.getDistrictId() == null || hospital.getDistrictId() != districtId) {
			throw new SecurityException("This submission is not in your district.");
		}
		if (!"PENDING".equals(row.getApprovalStatus())) {
			throw new IllegalStateException("Only pending submissions can be reviewed.");
		}
		row.setApprovalStatus("RCHO_APPROVED");
		row.setRchoApprovedBy(rcho.getUsername());
		row.setRchoApprovedDate(LocalDateTime.now());
		return hrRepo.save(row);
	}

	/** RCHO tier: reject, terminal - never reaches the admin queue. */
	public NrcHrPost rchoReject(int hrPostId, String remarks, NrcUserPrincipal rcho) {
		Integer districtId = districtIdForRcho(rcho);
		if (districtId == null) {
			throw new SecurityException("Only an RCHO can perform this action.");
		}
		NrcHrPost row = hrRepo.findById(hrPostId).orElseThrow(() -> new NoSuchElementException("Submission not found."));
		Hospital hospital = hospitalRepo.findById(row.getHId()).orElseThrow(() -> new NoSuchElementException("Hospital not found."));
		if (hospital.getDistrictId() == null || hospital.getDistrictId() != districtId) {
			throw new SecurityException("This submission is not in your district.");
		}
		if (!"PENDING".equals(row.getApprovalStatus())) {
			throw new IllegalStateException("Only pending submissions can be reviewed.");
		}
		row.setApprovalStatus("REJECTED");
		row.setRchoApprovedBy(rcho.getUsername());
		row.setRchoApprovedDate(LocalDateTime.now());
		row.setRchoRemarks(remarks);
		return hrRepo.save(row);
	}

	/** Admin (final) tier - only acts on submissions the district's RCHO has already passed through. */
	public NrcHrPost approve(int hrPostId, NrcUserPrincipal admin) {
		if (!admin.isAdmin()) {
			throw new SecurityException("Only ADMIN can approve HR submissions.");
		}
		NrcHrPost row = hrRepo.findById(hrPostId).orElseThrow(() -> new NoSuchElementException("Submission not found."));
		if (!"RCHO_APPROVED".equals(row.getApprovalStatus())) {
			throw new IllegalStateException("Only submissions already approved by the RCHO can be given final approval.");
		}

		List<NrcHrPost> currentRows = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(row.getHId(), row.getDesignation());
		for (NrcHrPost prev : currentRows) {
			if (prev.getSlotNo() == row.getSlotNo()) {
				prev.setCurrent(false);
				hrRepo.save(prev);
			}
		}

		row.setApprovalStatus("APPROVED");
		row.setApprovedBy(admin.getUsername());
		row.setApprovedDate(LocalDateTime.now());
		row.setCurrent(true);
		return hrRepo.save(row);
	}

	public NrcHrPost reject(int hrPostId, String remarks, NrcUserPrincipal admin) {
		if (!admin.isAdmin()) {
			throw new SecurityException("Only ADMIN can reject HR submissions.");
		}
		NrcHrPost row = hrRepo.findById(hrPostId).orElseThrow(() -> new NoSuchElementException("Submission not found."));
		if (!"RCHO_APPROVED".equals(row.getApprovalStatus())) {
			throw new IllegalStateException("Only submissions already approved by the RCHO can be reviewed here.");
		}
		row.setApprovalStatus("REJECTED");
		row.setApprovedBy(admin.getUsername());
		row.setApprovedDate(LocalDateTime.now());
		row.setRemarks(remarks);
		return hrRepo.save(row);
	}

	/**
	 * Per-hospital HR status table, for an RCHO drilling into one of their
	 * district's hospitals (the "linked DNRC/TNRC" buttons on the RCHO's HR
	 * page). Unlike admin (any hospital) or a hospital user (always their
	 * own, via SecurityUtils.resolveHospitalId), an RCHO may request any
	 * hId - but only one actually in their own district; requesting another
	 * district's hospital is rejected rather than silently narrowed, same
	 * "never trust the client value, verify it" spirit as RchoScopeResolver.
	 */
	public Map<String, Object> getHospitalTableForRcho(int hId, NrcUserPrincipal rcho) {
		Integer districtId = districtIdForRcho(rcho);
		if (districtId == null) {
			throw new SecurityException("Only an RCHO can use this.");
		}
		if (!hospitalIdsInDistrict(districtId).contains(hId)) {
			throw new SecurityException("This hospital is not in your district.");
		}
		return getHospitalTable(hId);
	}

	/** RCHO's own approval queue (their district's PENDING submissions), each row enriched with the hospital name for display - the plain NrcHrPost list has only h_id, and RCHO's queue spans many hospitals (unlike admin's own per-hospital approval modal, which already knows the hospital name from the row it was opened from). */
	public List<Map<String, Object>> getRchoQueue(NrcUserPrincipal rcho) {
		Integer districtId = districtIdForRcho(rcho);
		if (districtId == null) {
			throw new SecurityException("Only an RCHO can view this queue.");
		}
		List<Integer> hIds = hospitalIdsInDistrict(districtId);
		Map<Integer, String> hospitalNames = hospitalRepo.findAll().stream()
				.filter(h -> hIds.contains(h.getHId()))
				.collect(Collectors.toMap(Hospital::getHId, Hospital::getName));

		return hrRepo.findByApprovalStatus("PENDING").stream()
				.filter(r -> hIds.contains(r.getHId()))
				.sorted(Comparator.comparing(NrcHrPost::getSubmittedDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.map(r -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("hrPostId", r.getHrPostId());
					m.put("hId", r.getHId());
					m.put("hospitalName", hospitalNames.getOrDefault(r.getHId(), "Hospital #" + r.getHId()));
					m.put("designation", r.getDesignation());
					m.put("personName", r.getPersonName());
					m.put("dateOfJoining", r.getDateOfJoining());
					m.put("emailId", r.getEmailId());
					m.put("contactNo", r.getContactNo());
					m.put("employmentStatus", r.getEmploymentStatus());
					m.put("daysOfConsultation", r.getDaysOfConsultation());
					m.put("submittedBy", r.getSubmittedBy());
					m.put("submittedDate", r.getSubmittedDate());
					return m;
				})
				.collect(Collectors.toList());
	}

	/** Role-aware actionable queue: an RCHO's own district's PENDING submissions, or the admin's district-wide RCHO_APPROVED (ready for final sign-off) ones. */
	public List<NrcHrPost> getPending(Integer hId, NrcUserPrincipal principal) {
		Integer rchoDistrictId = districtIdForRcho(principal);
		if (rchoDistrictId != null) {
			List<Integer> hIds = hospitalIdsInDistrict(rchoDistrictId);
			return hrRepo.findByApprovalStatus("PENDING").stream()
					.filter(r -> hIds.contains(r.getHId()) && (hId == null || hId.equals(r.getHId())))
					.collect(Collectors.toList());
		}
		if (hId != null) {
			return hrRepo.findByHIdAndApprovalStatus(hId, "RCHO_APPROVED");
		}
		return hrRepo.findByApprovalStatus("RCHO_APPROVED");
	}

	public List<NrcHrPost> search(Integer hId, String designation, String personName,
			String approvalStatus, String employmentStatus, NrcUserPrincipal principal) {
		List<NrcHrPost> base;
		Integer rchoDistrictId = districtIdForRcho(principal);
		if (rchoDistrictId != null) {
			List<Integer> hIds = hospitalIdsInDistrict(rchoDistrictId);
			base = hrRepo.findAll().stream().filter(r -> hIds.contains(r.getHId())).collect(Collectors.toList());
		} else if (!principal.isAdmin()) {
			base = hrRepo.findByHId(principal.getHospitalId());
		} else if (hId != null) {
			base = hrRepo.findByHId(hId);
		} else {
			base = hrRepo.findAll();
		}
		return base.stream()
				.filter(r -> isBlank(designation) || designation.equalsIgnoreCase(r.getDesignation()))
				.filter(r -> isBlank(personName) || (r.getPersonName() != null
						&& r.getPersonName().toLowerCase().contains(personName.toLowerCase())))
				.filter(r -> isBlank(approvalStatus) || approvalStatus.equalsIgnoreCase(r.getApprovalStatus()))
				.filter(r -> isBlank(employmentStatus) || employmentStatus.equalsIgnoreCase(r.getEmploymentStatus()))
				.sorted(Comparator.comparing(NrcHrPost::getSubmittedDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());
	}

	// ===================== Sanctioned counts (admin-editable, per hospital) =====================

	public int getSanctionedCount(int hId, String designation) {
		return sanctionedRepo.findByHIdAndDesignation(hId, designation)
				.map(NrcHrSanctioned::getSanctionedCount)
				.orElseGet(() -> {
					seedSanctioned(hId, designation, 1);
					return 1;
				});
	}

	private void seedSanctioned(int hId, String designation, int count) {
		NrcHrSanctioned s = new NrcHrSanctioned();
		s.setHId(hId);
		s.setDesignation(designation);
		s.setSanctionedCount(count);
		s.setUpdatedBy("SYSTEM");
		s.setUpdatedDate(LocalDateTime.now());
		sanctionedRepo.save(s);
	}

	public NrcHrSanctioned updateSanctioned(int hId, String designation, int count, NrcUserPrincipal admin) {
		if (!admin.isAdmin()) {
			throw new SecurityException("Only ADMIN can change sanctioned post counts.");
		}
		if (count < 0) {
			throw new IllegalArgumentException("Sanctioned count cannot be negative.");
		}
		NrcHrSanctioned s = sanctionedRepo.findByHIdAndDesignation(hId, designation).orElseGet(() -> {
			seedSanctioned(hId, designation, 1);
			return sanctionedRepo.findByHIdAndDesignation(hId, designation).get();
		});
		s.setSanctionedCount(count);
		s.setUpdatedBy(admin.getUsername());
		s.setUpdatedDate(LocalDateTime.now());
		return sanctionedRepo.save(s);
	}

	// ===================== Table builders =====================

	/** null/blank hospitalLevel = every hospital; "DISTRICT"/"TALUKA" = just that level. */
	private List<Hospital> hospitalsForLevel(String hospitalLevel) {
		List<Hospital> all = hospitalRepo.findAll();
		if (isBlank(hospitalLevel)) {
			return all;
		}
		return all.stream()
				.filter(h -> hospitalLevel.equalsIgnoreCase(h.getHospitalLevel()))
				.collect(Collectors.toList());
	}

	/** One hospital's own HR status table (self-view for a hospital user, or drill-down for admin). */
	public Map<String, Object> getHospitalTable(int hId) {
		Optional<Hospital> hospital = hospitalRepo.findById(hId);
		String hospitalName = hospital.map(Hospital::getName).orElse("Hospital #" + hId);

		List<Map<String, Object>> rows = new ArrayList<>();
		int totalSanctioned = 0, totalWorking = 0, totalVacant = 0;
		LocalDateTime lastUpdated = null;
		int slNo = 1;

		for (String designation : DESIGNATIONS) {
			int sanctioned = getSanctionedCount(hId, designation);
			List<NrcHrPost> current = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(hId, designation);
			boolean hasPending = hrRepo.findByHIdAndDesignation(hId, designation).stream()
					.anyMatch(r -> "PENDING".equals(r.getApprovalStatus()) || "RCHO_APPROVED".equals(r.getApprovalStatus()));

			List<NrcHrPost> occupiedRows = current.stream()
					.filter(r -> !isVacantStatus(r.getEmploymentStatus())).collect(Collectors.toList());
			int working = occupiedRows.size();
			int vacant = sanctioned - working;
			String status = hasPending ? "Pending" : (working > 0 ? "Approved" : "Vacant");
			String personNames = occupiedRows.stream().map(NrcHrPost::getPersonName).collect(Collectors.joining(", "));

			for (NrcHrPost r : current) {
				if (r.getApprovedDate() != null && (lastUpdated == null || r.getApprovedDate().isAfter(lastUpdated))) {
					lastUpdated = r.getApprovedDate();
				}
			}

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("slNo", slNo++);
			row.put("designation", designation);
			row.put("sanctioned", sanctioned);
			row.put("working", working);
			row.put("vacant", vacant);
			row.put("status", status);
			row.put("personName", personNames);
			rows.add(row);

			totalSanctioned += sanctioned;
			totalWorking += working;
			totalVacant += vacant;
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("hId", hId);
		result.put("hospitalName", hospitalName);
		result.put("rows", rows);
		result.put("hospitalTotalSanctioned", totalSanctioned);
		result.put("hospitalTotalWorking", totalWorking);
		result.put("hospitalTotalVacant", totalVacant);
		result.put("asOfDate", (lastUpdated != null ? lastUpdated.toLocalDate() : LocalDate.now()).toString());
		return result;
	}

	/**
	 * Admin - cumulative HR table, one row per designation. hospitalLevel
	 * ("DISTRICT"/"TALUKA"/null-for-all) scopes which hospitals are summed -
	 * mirrors KABHI's own District/Admin view toggle (rule 22), using NRC's
	 * existing hospital_level split instead (NRC's admin has no separate
	 * "own team" the way KABHI's STATE scope does - see this class's
	 * javadoc - so both buttons here scope real hospitals, not a self/other
	 * split).
	 */
	public Map<String, Object> getCumulativeTable(String hospitalLevel) {
		List<Hospital> hospitals = hospitalsForLevel(hospitalLevel);

		List<Map<String, Object>> rows = new ArrayList<>();
		int slNo = 1, totalSanctioned = 0, totalWorking = 0, totalVacant = 0;

		for (String designation : DESIGNATIONS) {
			int sanctioned = 0, working = 0;
			for (Hospital h : hospitals) {
				sanctioned += getSanctionedCount(h.getHId(), designation);
				List<NrcHrPost> current = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(h.getHId(), designation);
				working += (int) current.stream().filter(r -> !isVacantStatus(r.getEmploymentStatus())).count();
			}
			int vacant = sanctioned - working;

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("slNo", slNo++);
			row.put("designation", designation);
			row.put("sanctioned", sanctioned);
			row.put("working", working);
			row.put("vacant", vacant);
			rows.add(row);

			totalSanctioned += sanctioned;
			totalWorking += working;
			totalVacant += vacant;
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", rows);
		result.put("hospitalCount", hospitals.size());
		result.put("totalSanctioned", totalSanctioned);
		result.put("totalWorking", totalWorking);
		result.put("totalVacant", totalVacant);
		result.put("asOfDate", LocalDate.now().toString());
		return result;
	}

	/** Admin - hospital-wise & designation-wise HR matrix, with a "Need to approve" flag per hospital. hospitalLevel scopes the same way getCumulativeTable does. */
	public Map<String, Object> getHospitalDesignationMatrix(String hospitalLevel) {
		List<Hospital> hospitals = hospitalsForLevel(hospitalLevel).stream()
				.sorted(Comparator.comparing(Hospital::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toList());

		List<Map<String, Object>> rows = new ArrayList<>();
		int slNo = 1;
		for (Hospital h : hospitals) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("slNo", slNo++);
			row.put("hId", h.getHId());
			row.put("hospitalName", h.getName());

			Map<String, Object> cells = new LinkedHashMap<>();
			int pendingCount = 0;
			int occupiedCount = 0;
			for (String designation : DESIGNATIONS) {
				List<NrcHrPost> current = hrRepo.findByHIdAndDesignationAndIsCurrentTrue(h.getHId(), designation);
				boolean hasPending = hrRepo.findByHIdAndDesignation(h.getHId(), designation).stream()
						.anyMatch(r -> "PENDING".equals(r.getApprovalStatus()) || "RCHO_APPROVED".equals(r.getApprovalStatus()));
				if (hasPending) {
					pendingCount++;
				}
				Optional<NrcHrPost> occupant = current.stream()
						.filter(r -> !isVacantStatus(r.getEmploymentStatus())).findFirst();
				if (occupant.isPresent()) {
					occupiedCount++;
				}

				Map<String, Object> cell = new LinkedHashMap<>();
				cell.put("occupied", occupant.isPresent());
				cell.put("personName", occupant.map(NrcHrPost::getPersonName).orElse("Vacant"));
				cell.put("pending", hasPending);
				cells.put(designation, cell);
			}
			row.put("cells", cells);
			row.put("pendingCount", pendingCount);
			// pendingCount==0 alone doesn't mean anything is actually filled - an
			// all-vacant hospital with zero submissions ever made also has
			// pendingCount==0, so occupiedCount must be checked too before calling
			// it "Up to date" (same fix already applied to the equivalent
			// hasPending ? "Pending" : (working>0 ? "Approved" : "Vacant") branch
			// in getHospitalTable() above, and to the frontend's own independent
			// re-derivation of this same status in admin.component.ts).
			row.put("status", pendingCount > 0 ? "Need to approve" : (occupiedCount > 0 ? "Up to date" : "Vacant"));
			rows.add(row);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("designations", DESIGNATIONS);
		result.put("rows", rows);
		result.put("asOfDate", LocalDate.now().toString());
		return result;
	}
}
