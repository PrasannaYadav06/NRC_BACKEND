package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.NrcAdmission;
import com.hfw.nrc.domain.NrcDischarge;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.DischargeDetailDto;
import com.hfw.nrc.dto.DischargeSaveRequest;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcDischargeRepository;

/**
 * Stage 4 - the single service behind the consolidated Discharge module
 * (replaces the old DischargeService writing to case_details + 3 split
 * tables + child_growth/case_mother_growth discharge-slice rows). Every
 * submission - first discharge or a later edit - inserts a NEW event row
 * (nr.nrc_discharge is a history table, see its own javadoc), and bumps
 * nr.nrc_admission.formStatus 4 -> 4.1 -> 4.2 ... per the spec's state
 * machine, mirroring NrcAdmissionService's weight-capture 3/3.1/3.2 pattern.
 */
@Service
public class NrcDischargeService {

	@Autowired private NrcDischargeRepository dischargeRepository;
	@Autowired private NrcAdmissionRepository admissionRepository;
	@Autowired private FollowupEventService followupEventService;

	private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

	public AbstractResponse saveDischarge(DischargeSaveRequest req, Integer hospitalId, boolean isAdmin, int userId) {
		Optional<NrcAdmission> found = admissionRepository.findById(req.getCaseId());
		if (!found.isPresent()) {
			return denied("Admission not found");
		}
		NrcAdmission a = found.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) {
			return denied("Not authorized for this admission");
		}

		NrcDischarge d = new NrcDischarge();
		d.setCaseId(a.getCaseId());
		d.setDischargeDate(parseTimestamp(req.getDischargeDate()));
		d.setDischargeSd(req.getDischargeSd());
		d.setDischargeWeightKg(req.getDischargeWeight());
		d.setDischargeHeightCm(req.getDischargeHeight());
		d.setMinWeightDate(parseDate(req.getMinWeightDate()));
		d.setMinWeightKg(req.getMinWeight());

		if (req.getDischargeChildDetails() != null && !req.getDischargeChildDetails().isEmpty()) {
			DischargeSaveRequest.DischargeChildItem c = req.getDischargeChildDetails().get(0);
			d.setAvgWeightGainKg(c.getAvgWeightGainKg());
			d.setClinicalImpression(c.getClinicalImpression());
			d.setTreatmentGiven(c.getTreatmentGiven());
			d.setMedicalAdvice(c.getMedicalAdvice());
			d.setDietaryAdvice(c.getDietaryAdvice());
			d.setOutcomeId(c.getOutcomeId());
		}
		if (req.getDischargeMotherDetails() != null && !req.getDischargeMotherDetails().isEmpty()) {
			d.setMedicalRiskId(req.getDischargeMotherDetails().get(0).getMedicalRiskId());
		}
		if (req.getDischargeMotherGrowths() != null && !req.getDischargeMotherGrowths().isEmpty()) {
			DischargeSaveRequest.MotherGrowthItem mg = req.getDischargeMotherGrowths().get(0);
			d.setMotherWeightKgDischarge(mg.getWeightKg());
			d.setMotherHeightCmDischarge(mg.getHeightCm());
			d.setMotherMuacCmDischarge(mg.getMuacCm());
		}

		resetComorbid(d);
		if (req.getDischargeComorbidConditions() != null) {
			for (DischargeSaveRequest.ComorbidItem c : req.getDischargeComorbidConditions()) {
				applyComorbid(d, c.getComorbidId(), c.getOthersValue());
			}
		}

		BigDecimal nextStatus = nextDischargeStatus(a.getFormStatus());
		d.setFormStatusAtEvent(nextStatus);
		d.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		d.setCreatedBy(userId);
		dischargeRepository.save(d);

		a.setFormStatus(nextStatus);
		a.setLastDischargeDate(d.getDischargeDate());
		a.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
		a.setLastUpdatedBy(userId);
		admissionRepository.save(a);

		if (d.getDischargeDate() != null) {
			followupEventService.generateSchedule(a.getCaseId(), d.getDischargeDate().toLocalDateTime().toLocalDate(), userId);
		}

		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("0");
		response.setSuccessMessage("Discharge submitted successfully");
		return response;
	}

	private BigDecimal nextDischargeStatus(BigDecimal current) {
		if (current == null || current.compareTo(new BigDecimal("4")) < 0) {
			return new BigDecimal("4.0");
		}
		return current.add(new BigDecimal("0.1")).setScale(1, RoundingMode.HALF_UP);
	}

	private void resetComorbid(NrcDischarge d) {
		d.setCcPneumonia(false); d.setCcGastroenteritis(false); d.setCcSepsis(false); d.setCcChd(false);
		d.setCcCp(false); d.setCcRenalDisease(false); d.setCcMeningitis(false); d.setCcIcs(false);
		d.setCcIem(false); d.setCcTuberculosis(false); d.setCcOthers(false); d.setCcOthersValue(null); d.setCcCovid19(false);
	}

	private void applyComorbid(NrcDischarge d, Integer comorbidId, String othersValue) {
		if (comorbidId == null) return;
		switch (comorbidId) {
			case 1: d.setCcPneumonia(true); break;
			case 2: d.setCcGastroenteritis(true); break;
			case 3: d.setCcSepsis(true); break;
			case 4: d.setCcChd(true); break;
			case 5: d.setCcCp(true); break;
			case 6: d.setCcRenalDisease(true); break;
			case 7: d.setCcMeningitis(true); break;
			case 8: d.setCcIcs(true); break;
			case 9: d.setCcIem(true); break;
			case 10: d.setCcTuberculosis(true); break;
			case 11: d.setCcOthers(true); d.setCcOthersValue(othersValue); break;
			case 12: d.setCcCovid19(true); break;
			default: break;
		}
	}

	/** "Already discharged" interstitial + Edit-form prefill - latest event for the case. */
	public DischargeDetailDto getLatestForCase(int caseId, Integer hospitalId, boolean isAdmin) {
		Optional<NrcAdmission> adm = admissionRepository.findById(caseId);
		if (!adm.isPresent()) return null;
		NrcAdmission a = adm.get();
		if (!isAdmin && (a.getHId() == null || !a.getHId().equals(hospitalId))) return null;

		Optional<NrcDischarge> latest = dischargeRepository.findFirstByCaseIdOrderByDischargeRecIdDesc(caseId);
		if (!latest.isPresent()) return null;
		return toDetailDto(latest.get(), a);
	}

	private DischargeDetailDto toDetailDto(NrcDischarge d, NrcAdmission a) {
		DischargeDetailDto dto = new DischargeDetailDto();
		dto.setDischargeRecId(d.getDischargeRecId());
		dto.setCaseId(d.getCaseId());
		dto.setChildName(a.getChildName());
		dto.setUhId(a.getUhId());
		dto.setIpNum(a.getIpNum());
		dto.setSamNum(a.getSamNum());
		dto.setContactNo(a.getChildMobileNumber());

		dto.setDischargeDate(formatTimestamp(d.getDischargeDate()));
		dto.setDischargeSd(d.getDischargeSd());
		dto.setDischargeWeight(d.getDischargeWeightKg());
		dto.setDischargeHeight(d.getDischargeHeightCm());
		dto.setMinWeightDate(formatDate(d.getMinWeightDate()));
		dto.setMinWeight(d.getMinWeightKg());

		dto.setAvgWeightGainKg(d.getAvgWeightGainKg());
		dto.setClinicalImpression(d.getClinicalImpression());
		dto.setOutcomeId(d.getOutcomeId());
		dto.setTreatmentGiven(d.getTreatmentGiven());
		dto.setMedicalAdvice(d.getMedicalAdvice());
		dto.setDietaryAdvice(d.getDietaryAdvice());

		dto.setMedicalRiskId(d.getMedicalRiskId());
		dto.setMotherWeightDischarge(d.getMotherWeightKgDischarge());
		dto.setMotherHeightDischarge(d.getMotherHeightCmDischarge());
		dto.setMotherMuacDischarge(d.getMotherMuacCmDischarge());

		List<Integer> comorbidIds = new ArrayList<>();
		if (Boolean.TRUE.equals(d.getCcPneumonia())) comorbidIds.add(1);
		if (Boolean.TRUE.equals(d.getCcGastroenteritis())) comorbidIds.add(2);
		if (Boolean.TRUE.equals(d.getCcSepsis())) comorbidIds.add(3);
		if (Boolean.TRUE.equals(d.getCcChd())) comorbidIds.add(4);
		if (Boolean.TRUE.equals(d.getCcCp())) comorbidIds.add(5);
		if (Boolean.TRUE.equals(d.getCcRenalDisease())) comorbidIds.add(6);
		if (Boolean.TRUE.equals(d.getCcMeningitis())) comorbidIds.add(7);
		if (Boolean.TRUE.equals(d.getCcIcs())) comorbidIds.add(8);
		if (Boolean.TRUE.equals(d.getCcIem())) comorbidIds.add(9);
		if (Boolean.TRUE.equals(d.getCcTuberculosis())) comorbidIds.add(10);
		if (Boolean.TRUE.equals(d.getCcOthers())) comorbidIds.add(11);
		if (Boolean.TRUE.equals(d.getCcCovid19())) comorbidIds.add(12);
		dto.setSelectedComorbidIds(comorbidIds);
		dto.setComorbidOthersValue(d.getCcOthersValue());

		return dto;
	}

	private AbstractResponse denied(String message) {
		AbstractResponse response = new AbstractResponse();
		response.setErrorCode("1");
		response.setErrorMessage(message);
		return response;
	}

	private java.util.Date parseDate(String value) {
		if (value == null || value.isEmpty()) return null;
		try { return DATE_FMT.parse(value); } catch (Exception e) { return null; }
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
