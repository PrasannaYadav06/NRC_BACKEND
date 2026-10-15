package com.hfw.nrc.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.Chc;
import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.HospitalDetail;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.domain.Phc;
import com.hfw.nrc.domain.Taluka;
import com.hfw.nrc.domain.Unit;
import com.hfw.nrc.domain.Ward;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.AddHospitalRequestModel;
import com.hfw.nrc.dto.HospitalAdminDto;
import com.hfw.nrc.dto.HospitalCreatedDto;
import com.hfw.nrc.dto.HospitalDetailsAdminDto;
import com.hfw.nrc.dto.HospitalPageResponse;
import com.hfw.nrc.dto.HospitalRecordDto;
import com.hfw.nrc.dto.RchoCredentialsDto;
import com.hfw.nrc.dto.UpdateHospitalRequestModel;
import com.hfw.nrc.repository.ChcRepository;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.HospitalDetailRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.PhcRepository;
import com.hfw.nrc.repository.TalukaRepository;
import com.hfw.nrc.repository.UnitRepository;
import com.hfw.nrc.repository.WardRepository;
import com.hfw.nrc.util.PasswordCompat;

/**
 * Ported from nrcadmin's Group H (POST /hospital/save): NRCService +
 * HospitalService + UserService + HospitalDetailsService collapsed into
 * one class (same tables, one caller, no reason to keep 4 files). Creates
 * a Hospital row, seeds its 3 default units (A/B/C) and 5 default wards
 * (SAM/PICU/COVID/SARI/Others), creates its first login account, and seeds
 * one hospital_details row (hr_type_id=25, bed count) for the current
 * month/year - all ported verbatim from the source's own logic/order.
 */
@Service
public class HospitalAdminService {

	@Autowired
	private HospitalRepository hospitalRepository;
	@Autowired
	private UnitRepository unitRepository;
	@Autowired
	private WardRepository wardRepository;
	@Autowired
	private NrcUserRepository nrcUserRepository;
	@Autowired
	private HospitalDetailRepository hospitalDetailRepository;
	@Autowired
	private PasswordCompat passwordCompat;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private TalukaRepository talukaRepository;
	@Autowired
	private PhcRepository phcRepository;
	@Autowired
	private ChcRepository chcRepository;
	@Autowired
	private RchoService rchoService;

	private static final Pattern HCODE_SEQ_PATTERN = Pattern.compile("^[DT]NRC-(\\d+)$", Pattern.CASE_INSENSITIVE);

	/** Server-computed Hospital Code/Name/Username/Password + level - the client only ever sends District/Taluka/PHC-or-CHC selections, never these values (see NRC Hospital Registration redesign plan). */
	private static class GeneratedIdentity {
		String hCode, name, username, password, hospitalLevel;
	}

	private String slug(String s) {
		return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	/** One shared running sequence per district across DISTRICT/TALUKA/PHC/CHC alike - see plan's "Code/name/username/password generation rules". */
	private int computeNextSequence(int districtId) {
		int max = 0;
		for (Hospital h : hospitalRepository.findAll()) {
			if (h.getDistrictId() != null && h.getDistrictId() == districtId && h.getHCode() != null) {
				Matcher m = HCODE_SEQ_PATTERN.matcher(h.getHCode().trim());
				if (m.matches()) {
					max = Math.max(max, Integer.parseInt(m.group(1)));
				}
			}
		}
		return max + 1;
	}

	/**
	 * Live preview for the Registration form - same generateIdentity() logic
	 * the real POST /hospital/save uses, read-only (no DB write). The
	 * sequence number is a best-effort snapshot: if two admins register in
	 * the same district at nearly the same moment, the actual save (which
	 * re-runs this same computation at commit time) is still the source of
	 * truth and its own confirmation dialog shows what was really assigned -
	 * this preview existing to go stale between keystrokes and the real
	 * submit is a cosmetic risk, not a correctness one.
	 */
	public HospitalCreatedDto previewIdentity(int districtId, Integer talukaId, Integer phcId, Integer chcId) {
		GeneratedIdentity id = generateIdentity(districtId, talukaId, phcId, chcId);
		HospitalCreatedDto dto = new HospitalCreatedDto();
		dto.setHCode(id.hCode);
		dto.setName(id.name);
		dto.setUsername(id.username);
		dto.setPassword(id.password);
		return dto;
	}

	private GeneratedIdentity generateIdentity(int districtId, Integer talukaId, Integer phcId, Integer chcId) {
		if (phcId != null && chcId != null) {
			throw new IllegalArgumentException("A hospital can be registered under a PHC or a CHC, not both.");
		}
		District district = districtRepository.findById((long) districtId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid district."));

		boolean isTaluka = talukaId != null;
		String prefix = isTaluka ? "T" : "D";
		String placeName;
		String nameSuffix = "";
		if (!isTaluka) {
			placeName = district.getDistrictName();
		} else if (phcId != null) {
			Phc phc = phcRepository.findById(phcId).orElseThrow(() -> new IllegalArgumentException("Invalid PHC."));
			placeName = phc.getPhcName();
			nameSuffix = "(PHC)";
		} else if (chcId != null) {
			Chc chc = chcRepository.findById(chcId).orElseThrow(() -> new IllegalArgumentException("Invalid CHC."));
			placeName = chc.getChcName();
			nameSuffix = "(CHC)";
		} else {
			Taluka taluka = talukaRepository.findById(talukaId).orElseThrow(() -> new IllegalArgumentException("Invalid taluka."));
			placeName = taluka.getTalukaName();
		}

		// Hard server-side duplicate check - the frontend already disables
		// taken PHC/CHC options and the already-registered District/Taluka
		// level (GeneralConfigService#getAllDistricts/getAllTaluks's
		// hasExactLevelNrc), but this is the actual point of truth, not just
		// a UI nicety (e.g. two admins racing the same form, or a client
		// that skipped the check). Reuses the same existing-hospital row to
		// name it back to the user, not a generic "duplicate" message.
		for (Hospital existing : hospitalRepository.findByDistrictId(districtId)) {
			if (!isTaluka && "DISTRICT".equals(existing.getHospitalLevel())) {
				throw new IllegalArgumentException(
						"An NRC hospital already exists for " + district.getDistrictName() + " district: " + existing.getName() + ".");
			}
			if (isTaluka && talukaId.equals(existing.getTalukaId())) {
				if (phcId != null && phcId.equals(existing.getPhcId())) {
					throw new IllegalArgumentException("An NRC hospital is already registered under this PHC: " + existing.getName() + ".");
				}
				if (chcId != null && chcId.equals(existing.getChcId())) {
					throw new IllegalArgumentException("An NRC hospital is already registered under this CHC: " + existing.getName() + ".");
				}
				if (phcId == null && chcId == null && existing.getPhcId() == null && existing.getChcId() == null
						&& "TALUKA".equals(existing.getHospitalLevel())) {
					throw new IllegalArgumentException(
							"An NRC hospital already exists for " + placeName + " taluka: " + existing.getName() + ".");
				}
			}
		}

		int nextSeq = computeNextSequence(districtId);

		GeneratedIdentity id = new GeneratedIdentity();
		id.hospitalLevel = isTaluka ? "TALUKA" : "DISTRICT";
		id.hCode = prefix + "NRC-" + nextSeq;
		id.name = prefix + "NRC " + placeName + nameSuffix;
		id.username = computeUniqueUsername(prefix + "nrc" + slug(placeName));
		id.password = prefix + "nrc" + slug(placeName) + "@123456";
		return id;
	}

	/**
	 * "{Prefix}nrc{place}" (e.g. "Tnrcadivala") - was "{prefix}nrc{districtId}{seq}"
	 * (e.g. "tnrc107"), changed per explicit request: the digit-heavy form
	 * wasn't considered a valid/readable username. A place name alone isn't
	 * guaranteed globally unique the way district+sequence was (a village/PHC
	 * name can repeat across different districts), so an actual collision
	 * falls back to appending 2, 3, ... - the common case stays clean.
	 */
	private String computeUniqueUsername(String base) {
		if (nrcUserRepository.findByUserId(base) == null) {
			return base;
		}
		int n = 2;
		while (nrcUserRepository.findByUserId(base + n) != null) {
			n++;
		}
		return base + n;
	}

	@Transactional
	public AbstractResponse saveAllHospitalDetails(AddHospitalRequestModel commonRequest) {
		AbstractResponse response = new AbstractResponse();

		HospitalAdminDto req = commonRequest.getHospital();
		GeneratedIdentity id;
		try {
			id = generateIdentity(req.getDistrictId(), req.getTalukaId(), req.getPhcId(), req.getChcId());
		} catch (IllegalArgumentException e) {
			response.setErrorCode("1");
			response.setErrorMessage(e.getMessage());
			return response;
		}

		int newHId = saveHospital(req, id);
		saveUser(id.username, id.password, newHId);
		saveHospitalDetails(commonRequest.getHospitalDetails());

		District district = districtRepository.findById((long) req.getDistrictId()).orElse(null);
		RchoService.RchoEnsureResult rchoResult = rchoService.ensureRchoForDistrict(
				req.getDistrictId(), district != null ? district.getDistrictName() : "District " + req.getDistrictId());

		HospitalCreatedDto created = new HospitalCreatedDto();
		created.setHId(newHId);
		created.setHCode(id.hCode);
		created.setName(id.name);
		created.setUsername(id.username);
		created.setPassword(id.password);
		created.setRchoCreated(rchoResult.created);
		if (rchoResult.created) {
			RchoCredentialsDto rchoDto = new RchoCredentialsDto();
			rchoDto.setRchoId(rchoResult.rcho.getRchoId());
			rchoDto.setRchoName(rchoResult.rcho.getRchoName());
			rchoDto.setUsername(rchoResult.username);
			rchoDto.setPassword(rchoResult.plaintextPassword);
			created.setRcho(rchoDto);
		}

		response.setErrorCode("0");
		response.setSuccessMessage("Hospital Details Saved Successfully");
		response.setResponseObject(created);
		return response;
	}

	/** Ported from nrcadmin.service.HospitalService#saveHospital, now server-generating hCode/name/level/phc-chc-linkage instead of trusting the client. Returns the new hospital's h_id. */
	private int saveHospital(HospitalAdminDto hospitalRequest, GeneratedIdentity id) {
		Hospital hospital = new Hospital();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		hospital.setHCode(id.hCode);
		hospital.setName(id.name);
		hospital.setNameShort(hospitalRequest.getNameShort());
		hospital.setDistrictId(hospitalRequest.getDistrictId());
		hospital.setTalukaId(hospitalRequest.getTalukaId());
		hospital.setPhcId(hospitalRequest.getPhcId());
		hospital.setChcId(hospitalRequest.getChcId());
		hospital.setHospitalLevel(id.hospitalLevel);
		hospital.setMedicalCollege(hospitalRequest.isMedicalCollege());
		hospital.setCreatedOn(timestamp);
		hospitalRepository.save(hospital);

		// Ported as-is: re-reads "the latest hospital by created_on" rather
		// than using the just-saved row's generated id directly - matches
		// the source's own (slightly roundabout) approach.
		Optional<Hospital> hospitalDetails = hospitalRepository.findLatestHospital();
		int newHId = hospitalDetails.get().getHId();

		Unit unit1 = new Unit(); unit1.setUnit("Unit A"); unit1.setDescription("Unit A"); unit1.setCreatedOn(timestamp); unit1.setHId(newHId);
		Unit unit2 = new Unit(); unit2.setUnit("Unit B"); unit2.setDescription("Unit B"); unit2.setCreatedOn(timestamp); unit2.setHId(newHId);
		Unit unit3 = new Unit(); unit3.setUnit("Unit C"); unit3.setDescription("Unit C"); unit3.setCreatedOn(timestamp); unit3.setHId(newHId);
		unitRepository.saveAll(List.of(unit1, unit2, unit3));

		Ward ward1 = new Ward(); ward1.setWard("SAM Ward"); ward1.setDescription("SAM Ward"); ward1.setCreatedOn(timestamp); ward1.setHId(newHId);
		Ward ward2 = new Ward(); ward2.setWard("PICU Ward"); ward2.setDescription("PICU Ward"); ward2.setCreatedOn(timestamp); ward2.setHId(newHId);
		Ward ward3 = new Ward(); ward3.setWard("COVID Ward"); ward3.setDescription("COVID Ward"); ward3.setCreatedOn(timestamp); ward3.setHId(newHId);
		Ward ward4 = new Ward(); ward4.setWard("SARI Ward"); ward4.setDescription("SARI Ward"); ward4.setCreatedOn(timestamp); ward4.setHId(newHId);
		Ward ward5 = new Ward(); ward5.setWard("Others"); ward5.setDescription("Others"); ward5.setCreatedOn(timestamp); ward5.setHId(newHId);
		wardRepository.saveAll(List.of(ward1, ward2, ward3, ward4, ward5));
		return newHId;
	}

	/**
	 * Ported from nrcadmin.service.UserService#saveUser, now taking the
	 * server-generated username/password (see generateIdentity) instead of
	 * trusting client-supplied values. One deliberate improvement over the
	 * source, not a behavior change to existing data: the source stored
	 * EncodeUtil's legacy Base64 encoding for brand-new accounts too; this
	 * stores a real bcrypt hash directly via PasswordCompat (which already
	 * checks bcrypt first) since there's no existing legacy value here to
	 * stay compatible with.
	 */
	private void saveUser(String username, String plaintextPassword, int hId) {
		NrcUser user = new NrcUser();
		user.setUserId(username);
		user.setPassword(passwordCompat.rehash(plaintextPassword));
		user.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		user.setActive(true);
		// role is NOT NULL with no server-side default applied on a Hibernate
		// insert (it lists every mapped column explicitly) - every other
		// existing account defaults to HOSPITAL_USER (see
		// db/migration/V1__add_role_to_nrc_users.sql), so new hospital
		// accounts created here do too.
		user.setRole("HOSPITAL_USER");
		// New account, given an auto-generated password - flagged so the
		// frontend shows the "kindly change the password" banner until they
		// actually do.
		user.setMustChangePassword(true);

		hospitalRepository.findById(hId).ifPresent(user::setHospital);

		nrcUserRepository.save(user);
	}

	/** Ported from nrcadmin.service.HospitalDetailsService#saveHospitalDetails. */
	private void saveHospitalDetails(HospitalDetailsAdminDto hospitalDetailsRequest) {
		HospitalDetail hospitalDetail = new HospitalDetail();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		hospitalDetail.setValue(hospitalDetailsRequest.getValue());
		hospitalDetail.setCreatedOn(timestamp);

		Optional<Hospital> hospital = hospitalRepository.findLatestHospital();
		Optional<NrcUser> nrcUser = nrcUserRepository.findFirstUser();

		Calendar calendar1 = new GregorianCalendar();
		int month = calendar1.get(Calendar.MONTH) + 1; // {0 - 11}
		int year = calendar1.get(Calendar.YEAR);
		hospitalDetail.setYear(year);
		hospitalDetail.setMonth(month);
		hospitalDetail.setHrTypeId(25);
		hospital.ifPresent(hospitalDetail::setHospital);
		nrcUser.ifPresent(hospitalDetail::setCreatedBy);

		hospitalDetailRepository.save(hospitalDetail);
	}

	// ============ Stage 2: Hospital Records (list / view / edit) ============

	public HospitalPageResponse listHospitals(String search, Integer districtId, Integer talukaId, String sort, int page, int size) {
		Page<Hospital> result = hospitalRepository.searchSorted(search, districtId, talukaId, sort, PageRequest.of(page, size));

		HospitalPageResponse response = new HospitalPageResponse();
		List<HospitalRecordDto> records = new ArrayList<>();
		for (Hospital h : result.getContent()) {
			records.add(toDto(h));
		}
		response.setRecords(records);
		response.setTotalElements((int) result.getTotalElements());
		response.setTotalPages(result.getTotalPages());
		response.setPage(page);
		return response;
	}

	public HospitalRecordDto getHospitalDetail(int hId) {
		Hospital hospital = hospitalRepository.findById(hId).orElse(null);
		if (hospital == null) {
			return null;
		}
		return toDto(hospital);
	}

	@Transactional
	public AbstractResponse updateHospital(int hId, UpdateHospitalRequestModel request) {
		AbstractResponse response = new AbstractResponse();

		Hospital hospital = hospitalRepository.findById(hId).orElse(null);
		if (hospital == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Hospital not found");
			return response;
		}

		hospital.setHCode(request.getHCode());
		hospital.setName(request.getName());
		hospital.setNameShort(request.getNameShort());
		hospital.setDistrictId(request.getDistrictId());
		hospital.setPincode(request.getPincode());
		hospital.setMobileNumber(request.getMobileNumber());
		hospital.setEmailId(request.getEmailId());
		hospital.setLandLineNumber(request.getLandLineNumber());
		hospital.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		hospitalRepository.save(hospital);

		// Bed count: update the most recent hospital_details row (hr_type_id=25)
		// in place rather than inserting another one - editing hospital info
		// isn't a new HR reporting-period submission (ResourceService/HR module
		// owns that flow), it's just correcting this one field.
		if (request.getNumberOfBeds() != null) {
			List<HospitalDetail> latest = hospitalDetailRepository.findLatestForHospital(hId);
			HospitalDetail bedRow = latest.stream().filter(d -> d.getHrTypeId() == 25).findFirst().orElse(null);
			if (bedRow != null) {
				bedRow.setValue(request.getNumberOfBeds());
				bedRow.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
				hospitalDetailRepository.save(bedRow);
			} else {
				saveHospitalDetails(newBedDetailsDto(request.getNumberOfBeds()));
			}
		}

		if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
			nrcUserRepository.findFirstByHospitalId(hId).ifPresent(user -> {
				user.setPassword(passwordCompat.rehash(request.getNewPassword()));
				nrcUserRepository.save(user);
			});
		}

		response.setErrorCode("0");
		response.setSuccessMessage("Hospital updated successfully");
		return response;
	}

	private HospitalDetailsAdminDto newBedDetailsDto(int beds) {
		HospitalDetailsAdminDto dto = new HospitalDetailsAdminDto();
		dto.setValue(beds);
		return dto;
	}

	private HospitalRecordDto toDto(Hospital h) {
		HospitalRecordDto dto = new HospitalRecordDto();
		dto.setHId(h.getHId());
		dto.setHCode(h.getHCode());
		dto.setName(h.getName());
		dto.setNameShort(h.getNameShort());
		dto.setDistrictId(h.getDistrictId());
		dto.setPincode(h.getPincode());
		dto.setMobileNumber(h.getMobileNumber());
		dto.setEmailId(h.getEmailId());
		dto.setLandLineNumber(h.getLandLineNumber());
		dto.setHospitalLevel(hospitalRepository.findHospitalLevel(h.getHId()));

		if (h.getDistrictId() != null) {
			District district = districtRepository.findById(h.getDistrictId().longValue()).orElse(null);
			if (district != null) {
				dto.setDistrictName(district.getDistrictName());
			}
		}
		dto.setTalukaId(h.getTalukaId());
		if (h.getTalukaId() != null) {
			talukaRepository.findById(h.getTalukaId()).ifPresent(t -> dto.setTalukaName(t.getTalukaName()));
		}
		if (h.getPhcId() != null) {
			phcRepository.findById(h.getPhcId()).ifPresent(p -> dto.setPhcName(p.getPhcName()));
		}
		if (h.getChcId() != null) {
			chcRepository.findById(h.getChcId()).ifPresent(c -> dto.setChcName(c.getChcName()));
		}

		List<HospitalDetail> latest = hospitalDetailRepository.findLatestForHospital(h.getHId());
		latest.stream().filter(d -> d.getHrTypeId() == 25).findFirst()
				.ifPresent(bed -> dto.setNumberOfBeds(bed.getValue()));

		nrcUserRepository.findFirstByHospitalId(h.getHId()).ifPresent(u -> dto.setUsername(u.getUserId()));

		return dto;
	}
}
