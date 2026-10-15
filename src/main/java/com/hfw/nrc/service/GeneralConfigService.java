package com.hfw.nrc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.AdmissionCriteriaValue;
import com.hfw.nrc.domain.AdmissionType;
import com.hfw.nrc.domain.Caste;
import com.hfw.nrc.domain.ComorbidConditionValue;
import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.EducationQualification;
import com.hfw.nrc.domain.FollowupPlace;
import com.hfw.nrc.domain.MalnutritionStatus;
import com.hfw.nrc.domain.MedicalRisk;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.domain.Outcome;
import com.hfw.nrc.domain.ReferredFrom;
import com.hfw.nrc.domain.Religion;
import com.hfw.nrc.domain.RiskCategory;
import com.hfw.nrc.domain.SocialFactor;
import com.hfw.nrc.domain.Chc;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.Phc;
import com.hfw.nrc.domain.Taluka;
import com.hfw.nrc.domain.Unit;
import com.hfw.nrc.domain.Ward;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.GenericConfigList;
import com.hfw.nrc.dto.GenericConfigModel;
import com.hfw.nrc.repository.AdmissionCriteriaValueRepository;
import com.hfw.nrc.repository.AdmissionTypeRepository;
import com.hfw.nrc.repository.CasteRepository;
import com.hfw.nrc.repository.ChcRepository;
import com.hfw.nrc.repository.ComorbidConditionValueRepository;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.EducationQualificationRepository;
import com.hfw.nrc.repository.FollowupPlaceRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.MalnutritionStatusRepository;
import com.hfw.nrc.repository.MedicalRiskRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.OutcomeRepository;
import com.hfw.nrc.repository.PhcRepository;
import com.hfw.nrc.repository.ReferredFromRepository;
import com.hfw.nrc.repository.ReligionRepository;
import com.hfw.nrc.repository.RiskCategoryRepository;
import com.hfw.nrc.repository.SocialFactorRepository;
import com.hfw.nrc.repository.TalukaRepository;
import com.hfw.nrc.repository.UnitRepository;
import com.hfw.nrc.repository.WardRepository;

/**
 * Ported from nrcapp.service.GeneralConfigServiceImpl - all the reference/
 * lookup-data endpoints (Group A) plus hospital-scoped unit/ward lookups
 * (Group B), same source service in the original app. Response shape
 * (AbstractResponse/GenericConfigList, errorCode "0"/"1") preserved exactly
 * since the already-merged Angular frontend parses this shape directly.
 */
@Service
public class GeneralConfigService {

	@Autowired
	private ReligionRepository religionRepository;
	@Autowired
	private CasteRepository casteRepository;
	@Autowired
	private AdmissionTypeRepository admissionTypeRepository;
	@Autowired
	private AdmissionCriteriaValueRepository admissionCriteriaValueRepository;
	@Autowired
	private ComorbidConditionValueRepository comorbidConditionValueRepository;
	@Autowired
	private ReferredFromRepository referredFromRepository;
	@Autowired
	private SocialFactorRepository socialFactorRepository;
	@Autowired
	private EducationQualificationRepository educationQualificationRepository;
	@Autowired
	private MedicalRiskRepository medicalRiskRepository;
	@Autowired
	private OutcomeRepository outcomeRepository;
	@Autowired
	private RiskCategoryRepository riskCategoryRepository;
	@Autowired
	private FollowupPlaceRepository followupPlaceRepository;
	@Autowired
	private MalnutritionStatusRepository malnutritionStatusRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private TalukaRepository talukaRepository;
	@Autowired
	private UnitRepository unitRepository;
	@Autowired
	private WardRepository wardRepository;
	@Autowired
	private NrcUserRepository nrcUserRepository;
	@Autowired
	private PhcRepository phcRepository;
	@Autowired
	private ChcRepository chcRepository;
	@Autowired
	private HospitalRepository hospitalRepository;

	private static AbstractResponse ok(String message, GenericConfigList list) {
		AbstractResponse res = new AbstractResponse();
		res.setErrorCode("0");
		res.setSuccessMessage(message);
		res.setResponseObject(list);
		return res;
	}

	private static AbstractResponse notInitialized(String what) {
		AbstractResponse res = new AbstractResponse();
		res.setErrorCode("1");
		res.setErrorMessage(what + " list is not initialized");
		return res;
	}

	private static GenericConfigModel config(Integer id, String code, String desc) {
		GenericConfigModel m = new GenericConfigModel();
		m.setConfigId(id);
		m.setConfigCode(code);
		m.setConfigDesc(desc);
		return m;
	}

	public AbstractResponse getAllReligion() {
		List<Religion> list = religionRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Religion");
		}
		GenericConfigList out = new GenericConfigList();
		for (Religion r : list) {
			out.getGenericList().add(config(r.getReligionId(), r.getReligion(), r.getDescription()));
		}
		return ok("Found religion list", out);
	}

	public AbstractResponse getAllCaste() {
		List<Caste> list = casteRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Caste");
		}
		GenericConfigList out = new GenericConfigList();
		for (Caste c : list) {
			out.getGenericList().add(config(c.getCasteId(), c.getCaste(), c.getDescription()));
		}
		return ok("Found caste list", out);
	}

	/** Hardcoded, not DB-backed - ported from GeneralConfigServiceImpl#getallCategory. */
	public AbstractResponse getAllCategory() {
		GenericConfigList out = new GenericConfigList();
		out.getGenericList().add(config(1, "BPL", "BPL"));
		out.getGenericList().add(config(2, "APL", "APL"));
		out.getGenericList().add(config(3, "Others", "Others"));
		return ok("Found Category list", out);
	}

	/** Hardcoded, not DB-backed - ported from GeneralConfigServiceImpl#getAllGender. */
	public AbstractResponse getAllGender() {
		GenericConfigList out = new GenericConfigList();
		out.getGenericList().add(config(1, "Male", "Male"));
		out.getGenericList().add(config(2, "Female", "Female"));
		out.getGenericList().add(config(3, "Others", "Others"));
		return ok("Found Gender list", out);
	}

	public AbstractResponse getAllAdmissionTypes() {
		List<AdmissionType> list = admissionTypeRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Admission Type");
		}
		GenericConfigList out = new GenericConfigList();
		for (AdmissionType a : list) {
			out.getGenericList().add(config(a.getAdmissionTypeId(), a.getAdmissionType(), a.getDescription()));
		}
		return ok("Found admission type list", out);
	}

	public AbstractResponse getAllAdmissionCriteria() {
		List<AdmissionCriteriaValue> list = admissionCriteriaValueRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Criteria");
		}
		GenericConfigList out = new GenericConfigList();
		for (AdmissionCriteriaValue a : list) {
			out.getGenericList().add(config(a.getCriteriaId(), a.getCriteria(), a.getDescription()));
		}
		return ok("Found criteria list", out);
	}

	public AbstractResponse getAllComorbidValues() {
		List<ComorbidConditionValue> list = comorbidConditionValueRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Comorbid");
		}
		GenericConfigList out = new GenericConfigList();
		for (ComorbidConditionValue c : list) {
			out.getGenericList().add(config(c.getComorbidId(), c.getComorbidCondition(), c.getDescription()));
		}
		return ok("Found comorbid list", out);
	}

	public AbstractResponse getAllReferredFrom() {
		List<ReferredFrom> list = referredFromRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Referred");
		}
		GenericConfigList out = new GenericConfigList();
		for (ReferredFrom r : list) {
			out.getGenericList().add(config(r.getReferredFromId(), r.getReferredFrom(), r.getDescription()));
		}
		return ok("Found Referred list", out);
	}

	public AbstractResponse getAllSocialFactors() {
		List<SocialFactor> list = socialFactorRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Social factor");
		}
		GenericConfigList out = new GenericConfigList();
		for (SocialFactor s : list) {
			out.getGenericList().add(config(s.getSocialFactorsId(), s.getSocialFactors(), s.getDescription()));
		}
		return ok("Found Social factor list", out);
	}

	public AbstractResponse getAllQualifications() {
		List<EducationQualification> list = educationQualificationRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Educational qualification");
		}
		GenericConfigList out = new GenericConfigList();
		for (EducationQualification q : list) {
			out.getGenericList().add(config(q.getEducationQualificationId(), q.getEducationQualification(), q.getDescription()));
		}
		return ok("Found educational qualification list", out);
	}

	public AbstractResponse getAllMedicalRisks() {
		List<MedicalRisk> list = medicalRiskRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Medical risk");
		}
		GenericConfigList out = new GenericConfigList();
		for (MedicalRisk m : list) {
			out.getGenericList().add(config(m.getMedicalRiskId(), m.getMedicalRisk(), m.getDescription()));
		}
		return ok("Found medical risk list", out);
	}

	public AbstractResponse getAllOutcomes() {
		List<Outcome> list = outcomeRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Outcome");
		}
		GenericConfigList out = new GenericConfigList();
		for (Outcome o : list) {
			out.getGenericList().add(config(o.getOutcomeId(), o.getOutcome(), o.getDescription()));
		}
		return ok("Found Outcome list", out);
	}

	public AbstractResponse getAllRiskCategory() {
		List<RiskCategory> list = riskCategoryRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Risk Category");
		}
		GenericConfigList out = new GenericConfigList();
		for (RiskCategory r : list) {
			out.getGenericList().add(config(r.getRiskCategoryId(), r.getRiskCategory(), r.getDescription()));
		}
		return ok("Found Risk Category list", out);
	}

	public AbstractResponse getAllPlacesForFollowup() {
		List<FollowupPlace> list = followupPlaceRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Followup places");
		}
		GenericConfigList out = new GenericConfigList();
		for (FollowupPlace p : list) {
			out.getGenericList().add(config(p.getPlaceId(), p.getPlaceName(), p.getDescription()));
		}
		return ok("Found Followup places list", out);
	}

	public AbstractResponse getAllMalStatus() {
		List<MalnutritionStatus> list = malnutritionStatusRepository.findAllOrdered();
		if (list.isEmpty()) {
			return notInitialized("Malnutrition status");
		}
		GenericConfigList out = new GenericConfigList();
		for (MalnutritionStatus s : list) {
			out.getGenericList().add(config(s.getStatusId(), s.getMalnutrition(), s.getDescription()));
		}
		return ok("Found Malnutrition status list", out);
	}

	public AbstractResponse getAllDistricts() {
		List<District> list = districtRepository.findAll();
		if (list.isEmpty()) {
			return notInitialized("District");
		}
		java.util.Set<Integer> districtsWithHospitals = new java.util.HashSet<>();
		java.util.Set<Integer> districtsWithDistrictLevelNrc = new java.util.HashSet<>();
		for (Hospital h : hospitalRepository.findAll()) {
			if (h.getDistrictId() != null) {
				districtsWithHospitals.add(h.getDistrictId());
				if ("DISTRICT".equals(h.getHospitalLevel())) {
					districtsWithDistrictLevelNrc.add(h.getDistrictId());
				}
			}
		}
		GenericConfigList out = new GenericConfigList();
		for (District d : list) {
			GenericConfigModel m = config(d.getDistrictId().intValue(), d.getDistrictCode(), d.getDistrictName());
			// Repurposed as "has at least one created NRC hospital" here (not
			// "already taken", unlike PHC/CHC) - lets filter dropdowns (Hospital
			// Records/In-Patient Care/Follow-up) show only districts with real
			// data, while the Registration form's own District dropdown still
			// shows every district (it doesn't read this flag).
			m.setAlreadyRegistered(districtsWithHospitals.contains(d.getDistrictId().intValue()));
			// Registration form only - see GenericConfigModel#hasExactLevelNrc's javadoc.
			m.setHasExactLevelNrc(districtsWithDistrictLevelNrc.contains(d.getDistrictId().intValue()));
			out.getGenericList().add(m);
		}
		return ok("Found District list", out);
	}

	/**
	 * Item 3 - cascading Taluka dropdown; districtId null returns every taluka
	 * (all 110, unfiltered). When districtId IS given, also appends one entry
	 * per PHC/CHC-linked hospital in that district (configCode="HOSPITAL",
	 * configId=h_id instead of a taluka_id, configDesc="{facility} (PHC/CHC)")
	 * so In-Patient Care/Follow-up's filter can narrow down to one specific
	 * facility within a taluka, not just the whole taluka - a taluka can host
	 * more than one hospital (a pure taluka-level one plus PHC/CHC-linked
	 * ones), and those patients aren't otherwise distinguishable in this
	 * dropdown. Real taluka rows keep configCode="TALUKA"; callers that only
	 * want real taluks (e.g. the Registration form) should filter on that.
	 */
	public AbstractResponse getAllTaluks(Integer districtId, boolean includeHospitals) {
		List<Taluka> list = districtId != null
				? talukaRepository.findByDistrictIdOrderByTalukaNameAsc(districtId)
				: talukaRepository.findAll();
		if (list.isEmpty() && districtId == null) {
			return notInitialized("Taluka");
		}
		List<Hospital> allHospitals = hospitalRepository.findAll();
		java.util.Set<Integer> talukasWithHospitals = new java.util.HashSet<>();
		java.util.Set<Integer> talukasWithPlainTalukaNrc = new java.util.HashSet<>();
		for (Hospital h : allHospitals) {
			if (h.getTalukaId() != null) {
				talukasWithHospitals.add(h.getTalukaId());
				// "Plain" = a taluka-level NRC not hosted under a specific PHC/CHC -
				// one of these per taluka is the real duplicate to block; a taluka
				// can still separately host any number of PHC/CHC-linked hospitals.
				if ("TALUKA".equals(h.getHospitalLevel()) && h.getPhcId() == null && h.getChcId() == null) {
					talukasWithPlainTalukaNrc.add(h.getTalukaId());
				}
			}
		}
		GenericConfigList out = new GenericConfigList();
		for (Taluka t : list) {
			GenericConfigModel m = config(t.getTalukaId(), "TALUKA", t.getTalukaName());
			// Same "has a created NRC hospital" meaning as getAllDistricts above -
			// a taluka can legitimately host more than one hospital (a pure
			// taluka-level one plus PHC/CHC-linked ones), so unlike PHC/CHC this
			// is never used to disable an option on the Registration form, only
			// to narrow filter dropdowns elsewhere.
			m.setAlreadyRegistered(talukasWithHospitals.contains(t.getTalukaId()));
			// Registration form only - see GenericConfigModel#hasExactLevelNrc's javadoc.
			m.setHasExactLevelNrc(talukasWithPlainTalukaNrc.contains(t.getTalukaId()));
			out.getGenericList().add(m);
		}
		if (districtId != null && includeHospitals) {
			for (Hospital h : allHospitals) {
				if (!districtId.equals(h.getDistrictId())) {
					continue;
				}
				String facilityLabel = null;
				if (h.getPhcId() != null) {
					facilityLabel = phcRepository.findById(h.getPhcId()).map(p -> p.getPhcName() + " (PHC)").orElse(null);
				} else if (h.getChcId() != null) {
					facilityLabel = chcRepository.findById(h.getChcId()).map(c -> c.getChcName() + " (CHC)").orElse(null);
				}
				if (facilityLabel != null) {
					GenericConfigModel m = config(h.getHId(), "HOSPITAL", facilityLabel);
					m.setAlreadyRegistered(true);
					out.getGenericList().add(m);
				}
			}
		}
		if (out.getGenericList().isEmpty()) {
			return notInitialized("Taluka");
		}
		return ok("Found Taluka list", out);
	}

	/**
	 * userId here is nrc_users.u_id (loginUserId in the old API) - the
	 * hospital is resolved from that user's own record, same as the source.
	 * Phase 3 controllers additionally run this through SecurityUtils so a
	 * non-admin cannot pass someone else's userId.
	 */
	/**
	 * NRC Hospital Registration form's PHC dropdown, scoped to a Taluka - already-registered PHCs are flagged so the frontend can grey them out (no two NRC hospitals under the same PHC).
	 *
	 * KNOWN DATA GAP (confirmed live against nr_live 2026-10-11, not a code
	 * bug - correctly returns empty/notInitialized for these): 15 real
	 * talukas have zero config.phc AND zero config.chc rows, so this
	 * endpoint (and getAllChcs below) legitimately has nothing to return for
	 * them - a PHC/CHC-linked NRC can't be registered there until someone
	 * supplies the real facility list (Health Department HMIS/facility
	 * registry - not something to invent here). District-level and plain
	 * Taluka-level registration in these talukas is unaffected.
	 *   Belagavi: Kudachi, Yemkanmardi
	 *   Davanagere: Harappanahalli, Mayakonda
	 *   Kalaburgi: Kalburgi Rural
	 *   Kolar: KGF
	 *   Koppal: Kanakagiri
	 *   Raichur: Maski
	 *   Ramanagara: Soluru
	 *   Uttara Kannada: Joida
	 *   Vijayapura: Chadchan
	 *   Yadgir: Shorapura
	 *   Bengaluru Urban: CV Raman Nagar, Pulikeshinagar, Mahadevapura
	 * Also found but NOT part of this gap (unused, zero hospitals reference
	 * them, likely stale/junk config.taluka rows predating this check -
	 * flagged, not touched): taluka_id 113 "Bengaluru Rural" and 118
	 * "Bengaluru Urban" (named identically to their own parent district -
	 * not real taluk names), and taluka_id 201/202 "Kotturu"/"Vijayanagara"
	 * under Ballari (Vijayanagara is actually its own real Karnataka
	 * district since 2021, carved out of Ballari - these look like
	 * pre-reorganization rows).
	 */
	public AbstractResponse getAllPhcs(Integer talukaId) {
		List<Phc> list = phcRepository.findByTalukaIdOrderByPhcNameAsc(talukaId);
		if (list.isEmpty()) {
			return notInitialized("PHC");
		}
		java.util.Set<Integer> used = new java.util.HashSet<>();
		for (Hospital h : hospitalRepository.findAll()) {
			if (h.getPhcId() != null) {
				used.add(h.getPhcId());
			}
		}
		GenericConfigList out = new GenericConfigList();
		for (Phc p : list) {
			GenericConfigModel m = config(p.getPhcId(), null, p.getPhcName());
			m.setAlreadyRegistered(used.contains(p.getPhcId()));
			out.getGenericList().add(m);
		}
		return ok("Found PHC list", out);
	}

	/** Same as getAllPhcs, for CHCs - including the same KNOWN DATA GAP documented on getAllPhcs's own javadoc above (same 15 talukas, zero config.chc rows too). */
	public AbstractResponse getAllChcs(Integer talukaId) {
		List<Chc> list = chcRepository.findByTalukaIdOrderByChcNameAsc(talukaId);
		if (list.isEmpty()) {
			return notInitialized("CHC");
		}
		java.util.Set<Integer> used = new java.util.HashSet<>();
		for (Hospital h : hospitalRepository.findAll()) {
			if (h.getChcId() != null) {
				used.add(h.getChcId());
			}
		}
		GenericConfigList out = new GenericConfigList();
		for (Chc c : list) {
			GenericConfigModel m = config(c.getChcId(), null, c.getChcName());
			m.setAlreadyRegistered(used.contains(c.getChcId()));
			out.getGenericList().add(m);
		}
		return ok("Found CHC list", out);
	}

	public AbstractResponse getAllUnits(Integer userId) {
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			AbstractResponse res = new AbstractResponse();
			res.setErrorCode("1");
			res.setErrorMessage("Invalid user ID");
			return res;
		}
		Integer hospitalId = user.getHospitalId();
		List<Unit> list = unitRepository.findByHospitalIdOrdered(hospitalId);
		if (list.isEmpty()) {
			return notInitialized("Unit");
		}
		GenericConfigList out = new GenericConfigList();
		for (Unit u : list) {
			out.getGenericList().add(config(u.getUnitId(), u.getUnit(), u.getDescription()));
		}
		return ok("Found unit list for the hospital " + hospitalId, out);
	}

	public AbstractResponse getAllWards(Integer userId) {
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			AbstractResponse res = new AbstractResponse();
			res.setErrorCode("1");
			res.setErrorMessage("Invalid user ID");
			return res;
		}
		Integer hospitalId = user.getHospitalId();
		List<Ward> list = wardRepository.findByHospitalIdOrdered(hospitalId);
		if (list.isEmpty()) {
			return notInitialized("Ward");
		}
		GenericConfigList out = new GenericConfigList();
		for (Ward w : list) {
			out.getGenericList().add(config(w.getWardId(), w.getWard(), w.getDescription()));
		}
		return ok("Found ward list for the hospital " + hospitalId, out);
	}
}
