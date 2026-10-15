package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.NrcAdmission;
import com.hfw.nrc.domain.NrcFollowup;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.FollowupDetailDto;
import com.hfw.nrc.dto.FollowupListDto;
import com.hfw.nrc.dto.FollowupPageResponse;
import com.hfw.nrc.dto.FollowupSaveRequest;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcFollowupRepository;

/**
 * Stage 5 - the single service behind the consolidated Follow-up module
 * (replaces followup/followup-search/followup-history/followup-due's old
 * spread, backed by NRCFollowupService/FollowupController). Every "Record
 * Followup" submit inserts a new VISIT row (history table, see
 * NrcFollowup's javadoc) and bumps nr.nrc_admission.formStatus
 * 5.0 -> 5.1 -> 5.2 ..., same pattern as weight-capture/discharge.
 *
 * Named FollowupEventService, not "NrcFollowupService", deliberately - this
 * filename was originally NrcFollowupService.java, but that collided with
 * the pre-existing NRCFollowupService.java on this project's Windows
 * checkout (case-insensitive filesystem - "Nrc" and "NRC" resolve to the
 * same physical file even though Java's class-name resolution is
 * case-sensitive), which silently overwrote the old file's real
 * implementation. Renamed to something that can't collide, once that was
 * discovered and the old file was reconstructed from surviving evidence.
 */
@Service
public class FollowupEventService {

	@Autowired private NrcFollowupRepository followupRepository;
	@Autowired private NrcAdmissionRepository admissionRepository;

	/** Same +7/+15/+30/+60-day checkpoint protocol as the old NRCFollowupService#generateFollowupSched, re-verified against live data (5 sample cases, 100% consistent) - but writing SCHEDULE rows into nr.nrc_followup instead of the old nr.followup_schedule table. */
	private static final int[] FOLLOWUP_DAY_OFFSETS = { 7, 15, 30, 60 };

	// NrcFollowup.followupDate is java.time.LocalDate (used with this
	// formatter); NrcAdmission's lastDischargeDate/lastFollowupDate are
	// java.sql.Timestamp/java.util.Date (used with SimpleDateFormat below,
	// matching the rest of this codebase - java.util.Date has no built-in
	// conversion to java.time types, so mixing formatters is simpler than
	// converting every field).
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final SimpleDateFormat LEGACY_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

	public AbstractResponse recordFollowup(FollowupSaveRequest req, Integer hospitalId, boolean isAdmin, int userId) {
		Optional<NrcAdmission> found = admissionRepository.findById(req.getCaseId());
		if (!found.isPresent()) {
			return denied("Admission not found");
		}
		NrcAdmission a = found.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) {
			return denied("Not authorized for this admission");
		}
		if (a.getFormStatus() == null || a.getFormStatus().compareTo(new BigDecimal("4")) < 0) {
			return denied("This patient has not been discharged yet - follow-up is only available after discharge");
		}

		int nextSeq = followupRepository.findByCaseIdAndRecordTypeOrderByFollowupDateAsc(req.getCaseId(), "VISIT").size() + 1;

		NrcFollowup f = new NrcFollowup();
		f.setCaseId(req.getCaseId());
		f.setRecordType("VISIT");
		f.setFollowupSeq(nextSeq);
		f.setFollowupDate(parseDate(req.getFollowupDate()));
		f.setPlaceId(req.getPlaceId());
		f.setStatusId(req.getStatusId());
		f.setFollowupSd(req.getStandardDev());
		f.setDoneBy(req.getDoneBy());
		f.setRemarks(req.getRemarks());
		f.setWeightKg(req.getWeightKg());
		f.setHeightCm(req.getHeightCm());

		BigDecimal nextStatus = nextFollowupStatus(a.getFormStatus());
		f.setFormStatusAtEvent(nextStatus);
		f.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		f.setCreatedBy(userId);
		followupRepository.save(f);

		a.setFormStatus(nextStatus);
		a.setLastFollowupDate(java.sql.Date.valueOf(f.getFollowupDate()));
		a.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		a.setLastUpdatedBy(userId);
		admissionRepository.save(a);

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("New follow-up saved successfully");
		return response;
	}

	/**
	 * Called from NrcDischargeService#saveDischarge right after every discharge
	 * event (first discharge or a later edit) - deactivates any previous
	 * SCHEDULE rows for the case and generates a fresh 4-checkpoint schedule
	 * from the (possibly new) discharge date, mirroring the old
	 * NRCFollowupService#generateFollowupSched/updateFollowups pair. Closes
	 * the gap where new discharges via the Stage 4 consolidated flow
	 * previously left nr.nrc_followup with no SCHEDULE rows at all.
	 */
	public void generateSchedule(int caseId, LocalDate dischargeDate, int userId) {
		if (dischargeDate == null) return;

		List<NrcFollowup> existing = followupRepository.findByCaseIdAndRecordTypeOrderByFollowupDateAsc(caseId, "SCHEDULE");
		for (NrcFollowup s : existing) {
			s.setIsActive(false);
			s.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
			s.setLastUpdatedBy(userId);
		}
		followupRepository.saveAll(existing);

		List<NrcFollowup> toSave = new ArrayList<>();
		int seq = 1;
		for (int offset : FOLLOWUP_DAY_OFFSETS) {
			NrcFollowup s = new NrcFollowup();
			s.setCaseId(caseId);
			s.setRecordType("SCHEDULE");
			s.setFollowupSeq(seq++);
			s.setFollowupDate(dischargeDate.plusDays(offset));
			s.setIsActive(true);
			s.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			s.setCreatedBy(userId);
			toSave.add(s);
		}
		followupRepository.saveAll(toSave);
	}

	/** First follow-up after discharge starts the 5.x sequence; later ones increment by 0.1, same pattern as weight-capture (3.x) / discharge (4.x). */
	private BigDecimal nextFollowupStatus(BigDecimal current) {
		if (current == null || current.compareTo(new BigDecimal("5")) < 0) {
			return new BigDecimal("5.0");
		}
		return current.add(new BigDecimal("0.1")).setScale(1, RoundingMode.HALF_UP);
	}

	/**
	 * Follow-up Records list. Reverted back to Follow-up-is-post-discharge-only:
	 * a patient is only ever eligible for this list once they've actually been
	 * discharged (formStatus &gt;= 4) - Discharge itself is submitted from
	 * In-Patient Care (see InpatientCareComponent#discharge), never from here,
	 * so an active admission can never appear in both tables at once.
	 * formStatusBucket narrows within the always-applied floor of 4:
	 * DISCHARGED = discharged but no follow-up visit recorded yet (4.x);
	 * FOLLOWUP = at least one follow-up visit recorded (5.x); null/anything
	 * else = every discharged-or-later case.
	 */
	public FollowupPageResponse listFollowups(Integer hId, String search, Integer month, Integer year, String formStatusBucket, Integer districtId, Integer talukaId, String sort, int page, int size) {
		BigDecimal minStatus = new BigDecimal("4");
		BigDecimal maxStatus = null;
		if (formStatusBucket != null) {
			switch (formStatusBucket.toUpperCase()) {
				case "DISCHARGED": minStatus = new BigDecimal("4"); maxStatus = new BigDecimal("5"); break;
				case "FOLLOWUP": minStatus = new BigDecimal("5"); break;
				default: break;
			}
		}
		// districtId/talukaId - same admin-only cascading filter as In-Patient
		// Care (see NrcAdmissionService#listAdmissions); a no-op for a
		// hospital-scoped user, same reasoning as that page.
		Page<NrcAdmission> pageResult = admissionRepository.searchSorted(hId, search, month, year, minStatus, maxStatus, districtId, talukaId, null, sort, PageRequest.of(page, size));

		FollowupPageResponse response = new FollowupPageResponse();
		List<FollowupListDto> rows = new ArrayList<>();
		for (NrcAdmission a : pageResult.getContent()) {
			rows.add(toListDto(a));
		}
		response.setRecords(rows);
		response.setTotalElements(pageResult.getTotalElements());
		response.setTotalPages(pageResult.getTotalPages());
		response.setPage(page);
		return response;
	}

	private FollowupListDto toListDto(NrcAdmission a) {
		FollowupListDto dto = new FollowupListDto();
		dto.setCaseId(a.getCaseId());
		dto.setHId(a.getHId());
		dto.setUhId(a.getUhId());
		dto.setSamNo(a.getSamNum());
		dto.setChildName(a.getChildName());
		dto.setChildGender(NrcAdmissionService.expandGender(a.getChildGender()));
		dto.setContactNo(a.getChildMobileNumber());
		dto.setDischargeDate(a.getLastDischargeDate() != null ? LEGACY_DATE_FMT.format(a.getLastDischargeDate()) : null);
		dto.setLastFollowupDate(a.getLastFollowupDate() != null ? LEGACY_DATE_FMT.format(a.getLastFollowupDate()) : null);
		dto.setFormStatus(a.getFormStatus() != null ? a.getFormStatus().toString() : "0");
		dto.setStatusLabel(NrcAdmissionService.statusLabelFor(a.getFormStatus()));
		int visitCount = followupRepository.findByCaseIdAndRecordTypeOrderByFollowupDateAsc(a.getCaseId(), "VISIT").size();
		dto.setFollowupVisitCount(visitCount);

		List<NrcFollowup> schedule = followupRepository.findByCaseIdAndRecordTypeOrderByFollowupDateAsc(a.getCaseId(), "SCHEDULE");
		List<FollowupListDto.ScheduleItemDto> scheduleItems = new ArrayList<>();
		for (NrcFollowup s : schedule) {
			if (Boolean.TRUE.equals(s.getIsActive())) {
				FollowupListDto.ScheduleItemDto item = new FollowupListDto.ScheduleItemDto();
				item.setSeq(s.getFollowupSeq() != null ? s.getFollowupSeq() : 0);
				item.setDate(s.getFollowupDate() != null ? DATE_FMT.format(s.getFollowupDate()) : null);
				// A checkpoint is "done" once the visit with that same ordinal
				// number has actually been recorded (not just once its scheduled
				// date has passed) - an overdue-but-unvisited checkpoint should
				// read as still pending, not falsely "done".
				item.setCompleted(s.getFollowupSeq() != null && visitCount >= s.getFollowupSeq());
				scheduleItems.add(item);
			}
		}
		dto.setFollowupSchedule(scheduleItems);
		return dto;
	}

	/** "1st followup / 2nd followup / ... up to latest" View popup. */
	public FollowupDetailDto getDetail(int caseId, Integer hospitalId, boolean isAdmin) {
		Optional<NrcAdmission> found = admissionRepository.findById(caseId);
		if (!found.isPresent()) return null;
		NrcAdmission a = found.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) return null;

		FollowupDetailDto dto = new FollowupDetailDto();
		dto.setChildName(a.getChildName());
		dto.setUhId(a.getUhId());
		dto.setSamNo(a.getSamNum() != null ? String.valueOf(a.getSamNum()) : null);
		dto.setRchId(a.getChildRchId() != null ? String.valueOf(a.getChildRchId()) : null);
		dto.setGender(a.getChildGender());

		List<FollowupDetailDto.VisitDto> visits = new ArrayList<>();
		for (NrcFollowup f : followupRepository.findByCaseIdAndRecordTypeOrderByFollowupDateAsc(caseId, "VISIT")) {
			FollowupDetailDto.VisitDto v = new FollowupDetailDto.VisitDto();
			v.setSeq(f.getFollowupSeq() != null ? f.getFollowupSeq() : 0);
			v.setFollowupDate(f.getFollowupDate() != null ? DATE_FMT.format(f.getFollowupDate()) : null);
			v.setPlaceId(f.getPlaceId() != null ? String.valueOf(f.getPlaceId()) : null);
			v.setStatusId(f.getStatusId() != null ? String.valueOf(f.getStatusId()) : null);
			v.setWeightKg(f.getWeightKg());
			v.setHeightCm(f.getHeightCm());
			v.setFollowupSd(f.getFollowupSd());
			v.setDoneBy(f.getDoneBy());
			v.setRemarks(f.getRemarks());
			visits.add(v);
		}
		dto.setVisits(visits);
		return dto;
	}

	private AbstractResponse denied(String message) {
		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("1");
		response.setErrorMessage(message);
		return response;
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.isEmpty()) return null;
		try { return LocalDate.parse(value, DATE_FMT); } catch (Exception e) { return null; }
	}
}
