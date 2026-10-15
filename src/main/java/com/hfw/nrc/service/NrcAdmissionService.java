package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.NrcAdmission;
import com.hfw.nrc.domain.NrcWeightCapture;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.AdmissionDetailDto;
import com.hfw.nrc.dto.AdmissionListDto;
import com.hfw.nrc.dto.AdmissionPageResponse;
import com.hfw.nrc.dto.AdmissionSaveRequest;
import com.hfw.nrc.dto.PatientFullHistoryDto;
import com.hfw.nrc.dto.PatientLookupDto;
import com.hfw.nrc.dto.PatientLookupPageResponse;
import com.hfw.nrc.dto.WeightCaptureRequest;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcWeightCaptureRepository;

/**
 * Stage 3 - the single service behind the consolidated In-Patient Care
 * admission module (replaces the old spread across CaseQueryService/
 * NRCOperationService writing to 11 tables). See NrcAdmission's javadoc for
 * the full table-consolidation background.
 */
@Service
public class NrcAdmissionService {

	@Autowired private NrcAdmissionRepository admissionRepository;
	@Autowired private NrcWeightCaptureRepository weightCaptureRepository;
	@Autowired private HospitalRepository hospitalRepository;
	@Autowired private NrcDischargeService dischargeService;
	@Autowired private FollowupEventService followupEventService;

	private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

	public AbstractResponse createAdmission(AdmissionSaveRequest req, Integer hospitalId, int createdBy) {
		NrcAdmission a = new NrcAdmission();
		a.setHId(hospitalId);
		applyRequest(a, req);
		a.setFormStatus(BigDecimal.ZERO);
		a.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		a.setCreatedBy(createdBy);
		admissionRepository.save(a);

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Admission submitted successfully");
		return response;
	}

	public AbstractResponse updateAdmission(int caseId, AdmissionSaveRequest req, Integer hospitalId, boolean isAdmin, int updatedBy) {
		Optional<NrcAdmission> existing = admissionRepository.findById(caseId);
		if (!existing.isPresent()) {
			return denied("Admission not found");
		}
		NrcAdmission a = existing.get();
		if (!isAdmin && !a.getHId().equals(hospitalId)) {
			return denied("Not authorized to edit this admission");
		}
		applyRequest(a, req);
		// New admission (0) -> first edit marks it "updated" (1). Later edits
		// while still in the pre-weight-capture/discharge/followup stage stay
		// at 1 - only weight-capture/discharge/followup submissions advance
		// the decimal sequence further (see recordWeightCapture below and
		// Stage 4/5's own services).
		if (a.getFormStatus() == null || a.getFormStatus().compareTo(BigDecimal.ONE) < 0) {
			a.setFormStatus(BigDecimal.ONE);
		}
		a.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		a.setLastUpdatedBy(updatedBy);
		admissionRepository.save(a);

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Admission updated successfully");
		return response;
	}

	private void applyRequest(NrcAdmission a, AdmissionSaveRequest req) {
		a.setSamNum(req.getSamNumber());
		a.setUhId(req.getUhId());
		a.setIpNum(req.getIpNum());
		a.setAdmissionTypeId(req.getAdmissionTypeId());

		a.setChildName(req.getChildName());
		// child_gender is CHAR(1) (matches the historical migrated data's
		// single-letter M/F codes) but GeneralConfigService#getAllGender's
		// reference list is hardcoded to the full word ("Male"/"Female"/
		// "Others") - inserting that as-is overflows the column (SQL Server
		// error 8152, "String or binary data would be truncated", correctly
		// rejected rather than silently truncated). Normalized to the first
		// letter here instead of widening the column, to stay consistent
		// with every already-migrated historical row.
		a.setChildGender(normalizeGender(req.getGender()));
		a.setChildCategory(req.getCategory());
		a.setChildDateOfBirth(parseDate(req.getChildDob()));
		a.setChildAgeInMonths(sumAgeInMonths(req.getChildAgeinYears(), req.getChildAgeinMonths()));
		a.setChildReligionId(req.getReligionId());
		a.setChildCasteId(req.getCasteId());
		a.setChildMobileNumber(req.getMobileNumber());
		a.setChildAlternateMobileNumber(req.getAlternateNumber());
		a.setChildAddressLine1(req.getAddressLine1());
		a.setChildAddressLine2(req.getAddressLine2());
		a.setChildAddressLine3(req.getAddressLine3());
		a.setChildPincode(req.getPinCode());
		a.setChildDistrictId(req.getDistrictId());
		a.setChildRchId(req.getRchId());
		a.setChildHasGuardian(req.getHasGuardian());
		a.setChildGuardianName(req.getGuardianName());

		a.setAdmissionDate(parseTimestamp(req.getAdmissionDate()));
		a.setTargetWeight(req.getTargetWeight());
		a.setAdmissionSd(req.getAdmissionSd() != null ? new BigDecimal(req.getAdmissionSd()) : null);

		a.setMotherName(req.getMotherName());
		a.setMotherDateOfBirth(parseDate(req.getMotherDob()));
		a.setMotherAge(req.getMotherAge());
		a.setMotherEducationQualificationId(req.getEducationQualificationId());
		a.setMotherFamilyPlanning(req.getFamilyPlanning());
		a.setMotherRchId(req.getMotherRchId());

		if (req.getCaseAllocations() != null && !req.getCaseAllocations().isEmpty()) {
			AdmissionSaveRequest.CaseAllocationItem alloc = req.getCaseAllocations().get(0);
			a.setUnitId(alloc.getUnitId());
			a.setWardId(alloc.getWardId());
		}
		if (req.getCaseReferrals() != null && !req.getCaseReferrals().isEmpty()) {
			AdmissionSaveRequest.CaseReferralItem ref = req.getCaseReferrals().get(0);
			a.setReferredFromId(ref.getReferredFromId());
			a.setReferredBy(ref.getReferredBy());
			a.setReferredByMNumb(ref.getReferredByNumber());
		}
		if (req.getChildGrowths() != null && !req.getChildGrowths().isEmpty()) {
			AdmissionSaveRequest.ChildGrowthItem cg = req.getChildGrowths().get(0);
			a.setAdmissionWeightKg(cg.getWeightKg());
			a.setAdmissionHeightCm(cg.getHeightCm());
			// Admission-time weight/date also seeds "last measured" until the
			// first real daily weight capture supersedes it.
			if (a.getLastMeasuredWeight() == null) {
				a.setLastMeasuredWeight(cg.getWeightKg());
				a.setLastMeasuredDate(parseDate(cg.getMeasurementDate()));
			}
		}
		if (req.getCaseMotherDetails() != null && !req.getCaseMotherDetails().isEmpty()) {
			AdmissionSaveRequest.CaseMotherDetailItem md = req.getCaseMotherDetails().get(0);
			a.setBreastFeedingAtPresent(md.getBreastFeedingAtPresent());
			a.setDrugsPrescribed(md.getDrugsPrescribed());
			a.setDrugsPrescribedDetails(md.getDrugsPrescribedDetails());
			a.setLastMenstruationDate(parseDate(md.getLastMenstruationDate()));
			a.setMedicalProblem(md.getMedicalProblem());
			a.setMedicalProblemDetails(md.getMedicalProblemDetails());
			a.setNoOfChildren(md.getNoOfChildren());
			a.setObstetricHistory(md.getObstetricHistory());
			a.setNoOfMealsPerDay(md.getNoOfMealsPerDay());
			a.setPregnantAtPresent(md.getPregnantAtPresent());
			a.setNumberOfPregnancyMonths(md.getNumberOfPregnancyMonths());
			a.setPresentDietaryChoice(md.getPresentDietaryChoice());
			a.setRegisteredForAnc(md.getRegisteredForAnc());
			a.setRiskCategoryId(md.getRiskCategoryId());
		}
		if (req.getCaseMotherGrowths() != null && !req.getCaseMotherGrowths().isEmpty()) {
			AdmissionSaveRequest.CaseMotherGrowthItem mg = req.getCaseMotherGrowths().get(0);
			a.setMotherBpDiaAdmission(mg.getBloodPressureDia());
			a.setMotherBpSysAdmission(mg.getBloodPressureSys());
			a.setMotherBloodSugarFastingAdmission(mg.getBloodSugarFasting());
			a.setMotherBloodSugarRandomAdmission(mg.getBloodSugarRandom());
			a.setMotherBmiAdmission(mg.getBmi());
			a.setMotherGravida(mg.getGravida());
			a.setMotherHeightCmAdmission(mg.getHeightCm());
			a.setMotherHemoglobinAdmission(mg.getHemoglobin());
			a.setMotherMuacCmAdmission(mg.getMuacCm());
			a.setMotherNeckCircumferenceCmAdmission(mg.getNeckCircumferenceCm());
			a.setMotherPara(mg.getPara());
			a.setMotherWeightKgAdmission(mg.getWeightKg());
			a.setMotherGrowthAdviceAdmission(mg.getAdvice());
		}

		// Flatten criteria/comorbid/social-factor checkbox arrays into the
		// ac_*/cc_*/sf_* boolean columns (see V4 migration for the id->column mapping).
		resetCriteria(a);
		if (req.getAdmissionCriterias() != null) {
			for (AdmissionSaveRequest.CriteriaItem c : req.getAdmissionCriterias()) {
				applyCriteria(a, c.getCriteriaId(), c.getOthersValue());
			}
		}
		resetComorbid(a);
		if (req.getComorbidConditions() != null) {
			for (AdmissionSaveRequest.ComorbidItem c : req.getComorbidConditions()) {
				applyComorbid(a, c.getComorbidId(), c.getOthersValue());
			}
		}
		resetSocialFactors(a);
		if (req.getCaseMotherSocialFactors() != null) {
			for (AdmissionSaveRequest.SocialFactorItem s : req.getCaseMotherSocialFactors()) {
				applySocialFactor(a, s.getSocialFactorsId(), s.getOtherValue());
			}
		}
	}

	private void resetCriteria(NrcAdmission a) {
		a.setAcWflWfhLt3z(false); a.setAcWfaLt3z(false); a.setAcMuacLt115(false);
		a.setAcSevereWasting(false); a.setAcBlEdema(false); a.setAcOthers(false); a.setAcOthersValue(null);
		a.setAcMuac115125(false); a.setAcWflWfhLt2z(false); a.setAcWflWfhLt1z(false);
	}

	private void applyCriteria(NrcAdmission a, Integer criteriaId, String othersValue) {
		if (criteriaId == null) return;
		switch (criteriaId) {
			case 1: a.setAcWflWfhLt3z(true); break;
			case 2: a.setAcWfaLt3z(true); break;
			case 3: a.setAcMuacLt115(true); break;
			case 4: a.setAcSevereWasting(true); break;
			case 5: a.setAcBlEdema(true); break;
			case 6: a.setAcOthers(true); a.setAcOthersValue(othersValue); break;
			case 7: a.setAcMuac115125(true); break;
			case 8: a.setAcWflWfhLt2z(true); break;
			case 9: a.setAcWflWfhLt1z(true); break;
			default: break;
		}
	}

	private void resetComorbid(NrcAdmission a) {
		a.setCcPneumonia(false); a.setCcGastroenteritis(false); a.setCcSepsis(false); a.setCcChd(false);
		a.setCcCp(false); a.setCcRenalDisease(false); a.setCcMeningitis(false); a.setCcIcs(false);
		a.setCcIem(false); a.setCcTuberculosis(false); a.setCcOthers(false); a.setCcOthersValue(null); a.setCcCovid19(false);
	}

	private void applyComorbid(NrcAdmission a, Integer comorbidId, String othersValue) {
		if (comorbidId == null) return;
		switch (comorbidId) {
			case 1: a.setCcPneumonia(true); break;
			case 2: a.setCcGastroenteritis(true); break;
			case 3: a.setCcSepsis(true); break;
			case 4: a.setCcChd(true); break;
			case 5: a.setCcCp(true); break;
			case 6: a.setCcRenalDisease(true); break;
			case 7: a.setCcMeningitis(true); break;
			case 8: a.setCcIcs(true); break;
			case 9: a.setCcIem(true); break;
			case 10: a.setCcTuberculosis(true); break;
			case 11: a.setCcOthers(true); a.setCcOthersValue(othersValue); break;
			case 12: a.setCcCovid19(true); break;
			default: break;
		}
	}

	private void resetSocialFactors(NrcAdmission a) {
		a.setSfPoverty(false); a.setSfIgnorance(false); a.setSfIlliteracy(false);
		a.setSfMigrants(false); a.setSfWorkingMother(false); a.setSfOthers(false); a.setSfOthersValue(null);
	}

	private void applySocialFactor(NrcAdmission a, Integer socialFactorsId, String othersValue) {
		if (socialFactorsId == null) return;
		switch (socialFactorsId) {
			case 1: a.setSfPoverty(true); break;
			case 2: a.setSfIgnorance(true); break;
			case 3: a.setSfIlliteracy(true); break;
			case 4: a.setSfMigrants(true); break;
			case 5: a.setSfWorkingMother(true); break;
			case 6: a.setSfOthers(true); a.setSfOthersValue(othersValue); break;
			default: break;
		}
	}

	public AdmissionPageResponse listAdmissions(Integer hId, String search, Integer month, Integer year, String statusBucket,
			Integer districtId, Integer talukaId, String sort, int page, int size) {
		// In-Patient Care only ever lists active (not-yet-discharged) admissions -
		// discharged cases moved to the Follow-up list's Discharge action - so
		// maxStatus is always capped at 4, even with no bucket selected.
		BigDecimal minStatus = new BigDecimal("0"), maxStatus = new BigDecimal("4");
		// NEW/MONITORING used to split on the form_status 0-3/3-4 range, but a
		// large share of this table's real data predates the weight-capture
		// form_status bump (bulk-migrated admissions whose captures were never
		// recorded through recordWeightCapture) - form_status stays 0 for those
		// rows even with real capture history, which silently misfiled them as
		// "Admitted" forever. requireCaptures checks the actual capture table
		// instead, so this bucket is correct for old and new data alike.
		Integer requireCaptures = null;
		if (statusBucket != null) {
			switch (statusBucket.toUpperCase()) {
				case "NEW": requireCaptures = 0; break;
				case "MONITORING": requireCaptures = 1; break;
				default: break;
			}
		}
		Page<NrcAdmission> pageResult = admissionRepository.searchSorted(hId, search, month, year, minStatus, maxStatus,
				districtId, talukaId, requireCaptures, sort, PageRequest.of(page, size));

		AdmissionPageResponse response = new AdmissionPageResponse();
		List<AdmissionListDto> rows = new ArrayList<>();
		for (NrcAdmission a : pageResult.getContent()) {
			rows.add(toListDto(a));
		}
		response.setRecords(rows);
		response.setTotalElements(pageResult.getTotalElements());
		response.setTotalPages(pageResult.getTotalPages());
		response.setPage(page);
		return response;
	}

	private AdmissionListDto toListDto(NrcAdmission a) {
		AdmissionListDto dto = new AdmissionListDto();
		dto.setCaseId(a.getCaseId());
		dto.setHId(a.getHId());
		dto.setHospitalName(hospitalName(a.getHId()));
		dto.setUhId(a.getUhId());
		dto.setChildName(a.getChildName());
		// Raw a.getChildGender() is a single letter (M/F/O, see normalizeGender) -
		// expanded to the full word so the list's Gender column and the frontend's
		// PatientNamePipe (which keys "female"/"male" for the Ms./Master prefix,
		// item 2) both get a real word instead of silently falling through blank.
		dto.setChildGender(expandGender(a.getChildGender()));
		dto.setTargetWeight(a.getTargetWeight());
		dto.setAdmissionDate(formatTimestamp(a.getAdmissionDate()));
		dto.setLastMeasuredWeight(a.getLastMeasuredWeight());
		dto.setLastMeasuredDate(formatDate(a.getLastMeasuredDate()));
		dto.setFormStatus(a.getFormStatus() != null ? a.getFormStatus().toString() : "0");
		// Derived from the actual nr.nrc_weight_capture rows, NOT form_status -
		// see NrcAdmissionRepositoryCustom#searchSorted's requireCaptures javadoc
		// for why form_status alone silently mislabels a lot of real (mostly
		// migrated) data as "Admitted" despite having real capture history.
		int captureCount = weightCaptureRepository.findByCaseIdOrderByCaptureDateAsc(a.getCaseId()).size();
		dto.setWeightCaptureCount(captureCount);
		dto.setStatusLabel(captureCount > 0 ? "Under Weight Capture" : "Admitted");
		return dto;
	}

	private String hospitalName(Integer hId) {
		if (hId == null) return null;
		Optional<Hospital> h = hospitalRepository.findById(hId);
		return h.map(Hospital::getName).orElse(null);
	}

	/** Item 13 - Search Patient page's search bar. Small fixed page size since this is a picker, not the full records table. */
	public PatientLookupPageResponse searchPatients(Integer hId, String search, int page, int size) {
		PatientLookupPageResponse response = new PatientLookupPageResponse();
		if (search == null || search.trim().isEmpty()) {
			response.setRecords(new ArrayList<>());
			return response;
		}
		Page<NrcAdmission> pageResult = admissionRepository.searchForPatientLookup(hId, search.trim(), PageRequest.of(page, size));
		List<PatientLookupDto> rows = new ArrayList<>();
		for (NrcAdmission a : pageResult.getContent()) {
			PatientLookupDto dto = new PatientLookupDto();
			dto.setCaseId(a.getCaseId());
			dto.setUhId(a.getUhId());
			dto.setSamNum(a.getSamNum());
			dto.setChildName(a.getChildName());
			dto.setChildGender(expandGender(a.getChildGender()));
			dto.setMotherName(a.getMotherName());
			dto.setHospitalName(hospitalName(a.getHId()));
			dto.setFormStatus(a.getFormStatus() != null ? a.getFormStatus().toString() : "0");
			dto.setStatusLabel(statusLabelFor(a.getFormStatus()));
			rows.add(dto);
		}
		response.setRecords(rows);
		response.setTotalElements(pageResult.getTotalElements());
		response.setTotalPages(pageResult.getTotalPages());
		response.setPage(page);
		return response;
	}

	/**
	 * Item 13 - the Search Patient page's single-A4-sheet full history:
	 * admission (incl. its own weight-capture timeline) + latest discharge
	 * (null if never discharged) + every follow-up visit (null/empty if none
	 * yet). Composes the three existing view-popup DTOs rather than
	 * reinventing their field sets - see PatientFullHistoryDto's javadoc.
	 */
	public PatientFullHistoryDto getFullHistory(int caseId, Integer hospitalId, boolean isAdmin) {
		AdmissionDetailDto admission = getDetail(caseId, hospitalId, isAdmin);
		if (admission == null) {
			return null;
		}
		PatientFullHistoryDto dto = new PatientFullHistoryDto();
		dto.setAdmission(admission);
		dto.setDischarge(dischargeService.getLatestForCase(caseId, hospitalId, isAdmin));
		dto.setFollowup(followupEventService.getDetail(caseId, hospitalId, isAdmin));
		dto.setStatusLabel(statusLabelFor(admission.getFormStatus() != null ? new BigDecimal(admission.getFormStatus()) : null));
		return dto;
	}

	/**
	 * "Active" (not yet discharged) / "Discharged" (no follow-up visit yet) /
	 * "Under Follow-up" (at least one recorded) - same 0/4/5 thresholds as
	 * listAdmissions/listFollowups. Package-visible (same reasoning as
	 * expandGender above) so FollowupEventService#toListDto can reuse it
	 * instead of a second copy of these thresholds.
	 */
	static String statusLabelFor(BigDecimal formStatus) {
		if (formStatus == null) return "Active";
		if (formStatus.compareTo(new BigDecimal("5")) >= 0) return "Under Follow-up";
		if (formStatus.compareTo(new BigDecimal("4")) >= 0) return "Discharged";
		return "Active";
	}

	public AdmissionDetailDto getDetail(int caseId, Integer hospitalId, boolean isAdmin) {
		Optional<NrcAdmission> found = admissionRepository.findById(caseId);
		if (!found.isPresent()) {
			return null;
		}
		NrcAdmission a = found.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) {
			return null; // scoped out - treated as not-found by the controller
		}

		AdmissionDetailDto dto = new AdmissionDetailDto();
		dto.setCaseId(a.getCaseId());
		dto.setHId(a.getHId());
		dto.setHospitalName(hospitalName(a.getHId()));
		dto.setIpNum(a.getIpNum());
		dto.setUhId(a.getUhId());
		dto.setSamNum(a.getSamNum());
		dto.setAdmissionTypeId(a.getAdmissionTypeId());
		dto.setAdmissionDate(formatTimestamp(a.getAdmissionDate()));
		dto.setTargetWeight(a.getTargetWeight());
		dto.setAdmissionSd(a.getAdmissionSd());

		dto.setChildName(a.getChildName());
		dto.setChildDateOfBirth(formatDate(a.getChildDateOfBirth()));
		dto.setChildAgeInMonths(a.getChildAgeInMonths());
		// Stored as a single letter (see normalizeGender) but the Edit form's
		// gender <select> options are keyed by the full word ("Male"/
		// "Female"/"Others", from GeneralConfigService#getAllGender) -
		// expanded back out here so patchValue() on the Edit form actually
		// finds a matching option instead of leaving the dropdown blank.
		dto.setChildGender(expandGender(a.getChildGender()));
		dto.setChildAddressLine1(a.getChildAddressLine1());
		dto.setChildAddressLine2(a.getChildAddressLine2());
		dto.setChildAddressLine3(a.getChildAddressLine3());
		dto.setChildMobileNumber(a.getChildMobileNumber());
		dto.setChildAlternateMobileNumber(a.getChildAlternateMobileNumber());
		dto.setChildCategory(a.getChildCategory());
		dto.setChildReligionId(a.getChildReligionId());
		dto.setChildCasteId(a.getChildCasteId());
		dto.setChildPincode(a.getChildPincode());
		dto.setChildDistrictId(a.getChildDistrictId());
		dto.setChildRchId(a.getChildRchId());
		dto.setChildHasGuardian(a.getChildHasGuardian());
		dto.setChildGuardianName(a.getChildGuardianName());

		dto.setMotherName(a.getMotherName());
		dto.setMotherDateOfBirth(formatDate(a.getMotherDateOfBirth()));
		dto.setMotherAge(a.getMotherAge());
		dto.setMotherEducationQualificationId(a.getMotherEducationQualificationId());
		dto.setMotherFamilyPlanning(a.getMotherFamilyPlanning());
		dto.setMotherRchId(a.getMotherRchId());

		dto.setUnitId(a.getUnitId());
		dto.setWardId(a.getWardId());
		dto.setReferredFromId(a.getReferredFromId());
		dto.setReferredBy(a.getReferredBy());
		dto.setReferredByMNumb(a.getReferredByMNumb());

		dto.setRiskCategoryId(a.getRiskCategoryId());
		dto.setNoOfChildren(a.getNoOfChildren());
		dto.setObstetricHistory(a.getObstetricHistory());
		dto.setMedicalProblem(a.getMedicalProblem());
		dto.setMedicalProblemDetails(a.getMedicalProblemDetails());
		dto.setDrugsPrescribed(a.getDrugsPrescribed());
		dto.setDrugsPrescribedDetails(a.getDrugsPrescribedDetails());
		dto.setPresentDietaryChoice(a.getPresentDietaryChoice());
		dto.setNoOfMealsPerDay(a.getNoOfMealsPerDay());
		dto.setPregnantAtPresent(a.getPregnantAtPresent());
		dto.setNumberOfPregnancyMonths(a.getNumberOfPregnancyMonths());
		dto.setRegisteredForAnc(a.getRegisteredForAnc());
		dto.setBreastFeedingAtPresent(a.getBreastFeedingAtPresent());
		dto.setLastMenstruationDate(formatDate(a.getLastMenstruationDate()));

		dto.setMotherWeightKgAdmission(a.getMotherWeightKgAdmission());
		dto.setMotherHeightCmAdmission(a.getMotherHeightCmAdmission());
		dto.setMotherBmiAdmission(a.getMotherBmiAdmission());
		dto.setMotherMuacCmAdmission(a.getMotherMuacCmAdmission());
		dto.setMotherHemoglobinAdmission(a.getMotherHemoglobinAdmission());
		dto.setMotherNeckCircumferenceCmAdmission(a.getMotherNeckCircumferenceCmAdmission());
		dto.setMotherGravida(a.getMotherGravida());
		dto.setMotherPara(a.getMotherPara());
		dto.setMotherBloodSugarFastingAdmission(a.getMotherBloodSugarFastingAdmission());
		dto.setMotherBloodSugarRandomAdmission(a.getMotherBloodSugarRandomAdmission());
		dto.setMotherBpSysAdmission(a.getMotherBpSysAdmission());
		dto.setMotherBpDiaAdmission(a.getMotherBpDiaAdmission());

		dto.setAdmissionWeightKg(a.getAdmissionWeightKg());
		dto.setAdmissionHeightCm(a.getAdmissionHeightCm());

		List<Integer> criteriaIds = new ArrayList<>();
		if (Boolean.TRUE.equals(a.getAcWflWfhLt3z())) criteriaIds.add(1);
		if (Boolean.TRUE.equals(a.getAcWfaLt3z())) criteriaIds.add(2);
		if (Boolean.TRUE.equals(a.getAcMuacLt115())) criteriaIds.add(3);
		if (Boolean.TRUE.equals(a.getAcSevereWasting())) criteriaIds.add(4);
		if (Boolean.TRUE.equals(a.getAcBlEdema())) criteriaIds.add(5);
		if (Boolean.TRUE.equals(a.getAcOthers())) criteriaIds.add(6);
		if (Boolean.TRUE.equals(a.getAcMuac115125())) criteriaIds.add(7);
		if (Boolean.TRUE.equals(a.getAcWflWfhLt2z())) criteriaIds.add(8);
		if (Boolean.TRUE.equals(a.getAcWflWfhLt1z())) criteriaIds.add(9);
		dto.setSelectedCriteriaIds(criteriaIds);
		dto.setCriteriaOthersValue(a.getAcOthersValue());

		List<Integer> comorbidIds = new ArrayList<>();
		if (Boolean.TRUE.equals(a.getCcPneumonia())) comorbidIds.add(1);
		if (Boolean.TRUE.equals(a.getCcGastroenteritis())) comorbidIds.add(2);
		if (Boolean.TRUE.equals(a.getCcSepsis())) comorbidIds.add(3);
		if (Boolean.TRUE.equals(a.getCcChd())) comorbidIds.add(4);
		if (Boolean.TRUE.equals(a.getCcCp())) comorbidIds.add(5);
		if (Boolean.TRUE.equals(a.getCcRenalDisease())) comorbidIds.add(6);
		if (Boolean.TRUE.equals(a.getCcMeningitis())) comorbidIds.add(7);
		if (Boolean.TRUE.equals(a.getCcIcs())) comorbidIds.add(8);
		if (Boolean.TRUE.equals(a.getCcIem())) comorbidIds.add(9);
		if (Boolean.TRUE.equals(a.getCcTuberculosis())) comorbidIds.add(10);
		if (Boolean.TRUE.equals(a.getCcOthers())) comorbidIds.add(11);
		if (Boolean.TRUE.equals(a.getCcCovid19())) comorbidIds.add(12);
		dto.setSelectedComorbidIds(comorbidIds);
		dto.setComorbidOthersValue(a.getCcOthersValue());

		List<Integer> socialIds = new ArrayList<>();
		if (Boolean.TRUE.equals(a.getSfPoverty())) socialIds.add(1);
		if (Boolean.TRUE.equals(a.getSfIgnorance())) socialIds.add(2);
		if (Boolean.TRUE.equals(a.getSfIlliteracy())) socialIds.add(3);
		if (Boolean.TRUE.equals(a.getSfMigrants())) socialIds.add(4);
		if (Boolean.TRUE.equals(a.getSfWorkingMother())) socialIds.add(5);
		if (Boolean.TRUE.equals(a.getSfOthers())) socialIds.add(6);
		dto.setSelectedSocialFactorIds(socialIds);
		dto.setSocialFactorOthersValue(a.getSfOthersValue());

		dto.setLastMeasuredWeight(a.getLastMeasuredWeight());
		dto.setLastMeasuredDate(formatDate(a.getLastMeasuredDate()));
		dto.setFormStatus(a.getFormStatus() != null ? a.getFormStatus().toString() : "0");

		List<AdmissionDetailDto.WeightCaptureDto> captures = new ArrayList<>();
		for (NrcWeightCapture wc : weightCaptureRepository.findByCaseIdOrderByCaptureDateAsc(caseId)) {
			AdmissionDetailDto.WeightCaptureDto wcDto = new AdmissionDetailDto.WeightCaptureDto();
			wcDto.setWeightKg(wc.getWeightKg());
			wcDto.setDiet(wc.getDiet());
			wcDto.setComment(wc.getComment());
			wcDto.setCaptureDate(formatDate(wc.getCaptureDate()));
			captures.add(wcDto);
		}
		dto.setWeightCaptures(captures);

		return dto;
	}

	/**
	 * "Daily Weight capture" popup submit (spec): form_status sequence
	 * 3, 3.1, 3.2, ... incrementing per capture. Refused once the case has
	 * already moved past monitoring (formStatus &gt;= 4, i.e. discharged) -
	 * weight capture is a pre-discharge concern only.
	 */
	public AbstractResponse recordWeightCapture(int caseId, WeightCaptureRequest req, Integer hospitalId, boolean isAdmin, int createdBy) {
		Optional<NrcAdmission> found = admissionRepository.findById(caseId);
		if (!found.isPresent()) {
			return denied("Admission not found");
		}
		NrcAdmission a = found.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) {
			return denied("Not authorized for this admission");
		}
		if (a.getFormStatus() != null && a.getFormStatus().compareTo(new BigDecimal("4")) >= 0) {
			return denied("This patient has already been discharged - weight capture is no longer available");
		}

		NrcWeightCapture wc = new NrcWeightCapture();
		wc.setCaseId(caseId);
		wc.setWeightKg(req.getWeightKg());
		wc.setDiet(req.getDiet());
		wc.setComment(req.getComment());
		wc.setCaptureDate(parseDate(req.getCaptureDate() != null ? req.getCaptureDate() : LocalDate.now().toString()));
		wc.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		wc.setCreatedBy(createdBy);
		weightCaptureRepository.save(wc);

		a.setLastMeasuredWeight(req.getWeightKg());
		a.setLastMeasuredDate(wc.getCaptureDate());
		a.setFormStatus(nextWeightCaptureStatus(a.getFormStatus()));
		a.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		a.setLastUpdatedBy(createdBy);
		admissionRepository.save(a);

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Weight capture recorded successfully");
		return response;
	}

	private BigDecimal nextWeightCaptureStatus(BigDecimal current) {
		if (current == null || current.compareTo(new BigDecimal("3")) < 0) {
			return new BigDecimal("3.0");
		}
		return current.add(new BigDecimal("0.1")).setScale(1, RoundingMode.HALF_UP);
	}

	/** Admin-only - deletes the weight-capture history first (FK), then the admission row itself. */
	public AbstractResponse deleteAdmission(int caseId) {
		if (!admissionRepository.existsById(caseId)) {
			return denied("Admission not found");
		}
		for (NrcWeightCapture wc : weightCaptureRepository.findByCaseIdOrderByCaptureDateAsc(caseId)) {
			weightCaptureRepository.delete(wc);
		}
		admissionRepository.deleteById(caseId);

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Admission deleted successfully");
		return response;
	}

	private AbstractResponse denied(String message) {
		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("1");
		response.setErrorMessage(message);
		return response;
	}

	/** "Male"/"Female"/"Others" (or already "M"/"F"/"O") -> single-letter code, matching child_gender CHAR(1). */
	private String normalizeGender(String value) {
		if (value == null || value.isEmpty()) return null;
		return value.trim().substring(0, 1).toUpperCase();
	}

	/**
	 * Inverse of normalizeGender - "M"/"F"/"O" -> the full word the Edit
	 * form's <select> options are keyed by. Package-visible (not just this
	 * class's own DTOs) so FollowupEventService#toListDto can reuse it for
	 * the Follow-up list's PatientNamePipe gender param too, instead of a
	 * second copy of the same switch.
	 */
	static String expandGender(String code) {
		if (code == null || code.isEmpty()) return null;
		switch (code.toUpperCase()) {
			case "M": return "Male";
			case "F": return "Female";
			case "O": return "Others";
			default: return code;
		}
	}

	private Integer sumAgeInMonths(Integer years, Integer months) {
		int y = years != null ? years : 0;
		int m = months != null ? months : 0;
		return y * 12 + m;
	}

	private java.util.Date parseDate(String value) {
		if (value == null || value.isEmpty()) return null;
		try {
			return DATE_FMT.parse(value);
		} catch (Exception e) {
			return null;
		}
	}

	private Timestamp parseTimestamp(String value) {
		java.util.Date d = parseDate(value);
		return d != null ? new Timestamp(d.getTime()) : null;
	}

	private String formatDate(java.util.Date value) {
		return value != null ? DATE_FMT.format(value) : null;
	}

	private String formatTimestamp(Timestamp value) {
		return value != null ? DATE_FMT.format(value) : null;
	}
}
