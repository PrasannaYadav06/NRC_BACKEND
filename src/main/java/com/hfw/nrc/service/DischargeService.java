package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.CaseMotherGrowth;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.DischargeChildDetail;
import com.hfw.nrc.domain.DischargeComorbidCondition;
import com.hfw.nrc.domain.DischargeMotherDetail;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.CaseMotherGrowthModel;
import com.hfw.nrc.dto.DischargeChildModel;
import com.hfw.nrc.dto.DischargeComorbidModel;
import com.hfw.nrc.dto.DischargeMotherModel;
import com.hfw.nrc.dto.DischargeRequestModel;
import com.hfw.nrc.dto.ViewCaseRequestModel;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.CaseMotherGrowthRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.ComorbidConditionValueRepository;
import com.hfw.nrc.repository.DischargeChildDetailRepository;
import com.hfw.nrc.repository.DischargeComorbidConditionRepository;
import com.hfw.nrc.repository.DischargeMotherDetailRepository;
import com.hfw.nrc.repository.MedicalRiskRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.OutcomeRepository;
import com.hfw.nrc.util.DateFormatUtil;

/**
 * Ported from nrcapp.service.NRCOperationServiceImpl's discharge flow
 * (dischargeCase/updateDischarge/viewDischarge + the shared
 * setTargetWeightAchieved helper). Same no-cascade adaptation as
 * CaseCreationService: child records are saved explicitly via their own
 * repositories rather than through a dropped @OneToMany collection.
 *
 * Follow-up-schedule generation (nrcFollowupService.generateFollowupSched /
 * updateFollowups in the source) is currently a logged no-op - see
 * NRCFollowupService javadoc; full port is Group E work. Discharge itself
 * (vitals, outcome, comorbid/risk records, target-weight flag) is fully
 * functional without it.
 */
@Service
public class DischargeService {

	private static final BigDecimal AVG_GAIN_THRESHOLD = new BigDecimal(5);

	@Autowired private CaseDetailRepository caseDetailRepository;
	@Autowired private ChildGrowthRepository childGrowthRepository;
	@Autowired private DischargeChildDetailRepository dischargeChildDetailRepository;
	@Autowired private DischargeComorbidConditionRepository dischargeComorbidConditionRepository;
	@Autowired private DischargeMotherDetailRepository dischargeMotherDetailRepository;
	@Autowired private CaseMotherGrowthRepository caseMotherGrowthRepository;
	@Autowired private OutcomeRepository outcomeRepository;
	@Autowired private ComorbidConditionValueRepository comorbidConditionValueRepository;
	@Autowired private MedicalRiskRepository medicalRiskRepository;
	@Autowired private NrcUserRepository nrcUserRepository;
	@Autowired private NRCFollowupService nrcFollowupService;

	private static NrcUser userRef(int userId) {
		NrcUser user = new NrcUser();
		user.setuId(userId);
		return user;
	}

	@Transactional
	public AbstractResponse dischargeCase(DischargeRequestModel caseReqModel) {
		AbstractResponse response = new AbstractResponse();

		if (caseReqModel.getDischargeChildDetails().get(0).getAvgWeightGainKg() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid avg weight gain value");
			return response;
		}

		CaseDetail caseDetailEntity = caseDetailRepository.findByCaseIdAndHospitalId(caseReqModel.getCaseId(), caseReqModel.getHospitalId());
		if (caseDetailEntity == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case details not found");
			return response;
		}

		if (!caseDetailRepository.checkDischargedRaw(caseDetailEntity.getCaseId(), caseDetailEntity.getHospital().getHId()).isEmpty()) {
			response.setErrorCode("1");
			response.setErrorMessage("This case with case id: " + caseDetailEntity.getCaseId() + " was already discharged");
			return response;
		}

		NrcUser user = userRef(caseReqModel.getLoginUserId());
		caseDetailEntity.setLastUpdatedBy(user);
		caseDetailEntity.setLastUpdatedOn(new Timestamp(new Date().getTime()));

		try {
			if (caseReqModel.getDischargeDate() != null) {
				caseDetailEntity.setDischargeDate(DateFormatUtil.getTimestampFromDashedDateString(caseReqModel.getDischargeDate()));
			}
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid discharge date format");
			return response;
		}
		caseDetailEntity.setDischargeSd(caseReqModel.getDischargeSd());

		List<ChildGrowth> childGrowthList = new ArrayList<>();

		ChildGrowth minWeightEntity = new ChildGrowth();
		minWeightEntity.setRecordedAtAdmission(false);
		minWeightEntity.setRecordedAtDischarge(true);
		minWeightEntity.setHeightCm(null);
		minWeightEntity.setWeightKg(caseReqModel.getMinWeight());
		minWeightEntity.setCreatedBy(user);
		minWeightEntity.setCreatedOn(new Timestamp(new Date().getTime()));
		minWeightEntity.setCaseDetail(caseDetailEntity);
		try {
			if (caseReqModel.getMinWeightDate() != null) {
				minWeightEntity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getMinWeightDate()));
			}
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid min weight date format");
			return response;
		}
		childGrowthList.add(minWeightEntity);

		ChildGrowth dischargeWeightEntity = new ChildGrowth();
		dischargeWeightEntity.setRecordedAtAdmission(false);
		dischargeWeightEntity.setRecordedAtDischarge(true);
		dischargeWeightEntity.setHeightCm(caseReqModel.getDischargeHeight());
		dischargeWeightEntity.setWeightKg(caseReqModel.getDischargeWeight());
		dischargeWeightEntity.setCreatedBy(user);
		dischargeWeightEntity.setCreatedOn(new Timestamp(new Date().getTime()));
		dischargeWeightEntity.setCaseDetail(caseDetailEntity);
		try {
			if (caseReqModel.getDischargeDate() != null) {
				dischargeWeightEntity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getDischargeDate()));
			}
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid discharge date format");
			return response;
		}
		childGrowthList.add(dischargeWeightEntity);
		childGrowthRepository.saveAll(childGrowthList);

		Integer[] outcomeIdHolder = new Integer[1];
		List<DischargeChildDetail> dischargeChildList = new ArrayList<>();
		for (DischargeChildModel disChildInd : caseReqModel.getDischargeChildDetails()) {
			DischargeChildDetail entity = new DischargeChildDetail();
			entity.setAvgWeightGainKg(disChildInd.getAvgWeightGainKg());
			entity.setClinicalImpression(disChildInd.getClinicalImpression());
			entity.setDietaryAdvice(disChildInd.getDietaryAdvice());
			entity.setMedicalAdvice(disChildInd.getMedicalAdvice());
			entity.setTreatmentGiven(disChildInd.getTreatmentGiven());
			if (disChildInd.getOutcomeId() != null) {
				outcomeRepository.findById(disChildInd.getOutcomeId()).ifPresent(o -> {
					entity.setOutcomeId(o.getOutcomeId());
					outcomeIdHolder[0] = o.getOutcomeId();
				});
			}
			entity.setCreatedBy(user);
			entity.setCreatedOn(new Timestamp(new Date().getTime()));
			entity.setCaseDetail(caseDetailEntity);
			dischargeChildList.add(entity);
		}
		dischargeChildDetailRepository.saveAll(dischargeChildList);

		if (caseReqModel.getDischargeComorbidConditions() != null) {
			List<DischargeComorbidCondition> list = new ArrayList<>();
			for (DischargeComorbidModel ind : caseReqModel.getDischargeComorbidConditions()) {
				DischargeComorbidCondition entity = new DischargeComorbidCondition();
				entity.setCreatedBy(user);
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				comorbidConditionValueRepository.findById(ind.getComorbidId()).ifPresent(c -> entity.setComorbidId(c.getComorbidId()));
				entity.setOthersValue(ind.getOthersValue());
				list.add(entity);
			}
			dischargeComorbidConditionRepository.saveAll(list);
		}

		if (caseReqModel.getDischargeMotherDetails() != null) {
			List<DischargeMotherDetail> list = new ArrayList<>();
			for (DischargeMotherModel ind : caseReqModel.getDischargeMotherDetails()) {
				if (ind.getMedicalRiskId() == null) {
					continue;
				}
				DischargeMotherDetail entity = new DischargeMotherDetail();
				entity.setCreatedBy(user);
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				medicalRiskRepository.findById(ind.getMedicalRiskId()).ifPresent(r -> entity.setMedicalRiskId(r.getMedicalRiskId()));
				list.add(entity);
			}
			dischargeMotherDetailRepository.saveAll(list);
		}

		if (caseReqModel.getDischargeMotherGrowths() != null) {
			List<CaseMotherGrowth> list = new ArrayList<>();
			for (CaseMotherGrowthModel ind : caseReqModel.getDischargeMotherGrowths()) {
				if (ind.isAllFieldsNull()) {
					continue;
				}
				CaseMotherGrowth entity = new CaseMotherGrowth();
				entity.setCreatedBy(user);
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				entity.setHeightCm(ind.getHeightCm());
				entity.setWeightKg(ind.getWeightKg());
				entity.setMuacCm(ind.getMuacCm());
				entity.setBmi(ind.getBmi());
				entity.setAdvice(ind.getAdvice());
				try {
					entity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getDischargeDate()));
				} catch (ParseException e) {
					response.setErrorCode("1");
					response.setErrorMessage("Invalid date format in mother msmt date");
				}
				entity.setRecordedAtDischarge(true);
				list.add(entity);
			}
			caseMotherGrowthRepository.saveAll(list);
		}

		nrcFollowupService.generateFollowupSched(caseReqModel.getCaseId(), caseReqModel.getLoginUserId(), outcomeIdHolder[0]);

		caseDetailRepository.save(caseDetailEntity);
		setTargetWeightAchieved(caseDetailEntity, caseReqModel.getLoginUserId());
		caseDetailRepository.save(caseDetailEntity);

		response.setErrorCode("0");
		response.setResponseObject(null);
		response.setSuccessMessage("Discharged case record with case id : " + caseReqModel.getCaseId());
		return response;
	}

	@Transactional
	public AbstractResponse updateDischarge(DischargeRequestModel caseReqModel) {
		AbstractResponse response = new AbstractResponse();

		if (caseReqModel.getDischargeChildDetails().get(0).getAvgWeightGainKg() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid avg weight gain value");
			return response;
		}

		CaseDetail caseDetailEntity = caseDetailRepository.findByCaseIdAndHospitalId(caseReqModel.getCaseId(), caseReqModel.getHospitalId());
		if (caseDetailEntity == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case details not found");
			return response;
		}

		NrcUser user = userRef(caseReqModel.getLoginUserId());
		caseDetailEntity.setLastUpdatedBy(user);
		caseDetailEntity.setLastUpdatedOn(new Timestamp(new Date().getTime()));

		try {
			if (caseReqModel.getDischargeDate() != null) {
				caseDetailEntity.setDischargeDate(DateFormatUtil.getTimestampFromDashedDateString(caseReqModel.getDischargeDate()));
			}
		} catch (ParseException e) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid discharge date format");
			return response;
		}
		caseDetailEntity.setDischargeSd(caseReqModel.getDischargeSd());

		List<ChildGrowth> childGrowthList = childGrowthRepository.findDischargeGrowthsOrdered(caseReqModel.getCaseId());
		// Ported as-is: source only guards size()>0, not size()>1, before
		// indexing get(1) - a latent IndexOutOfBoundsException in the source
		// if exactly one discharge-flagged growth row exists.
		if (!childGrowthList.isEmpty()) {
			ChildGrowth first = childGrowthList.get(0);
			first.setHeightCm(null);
			first.setWeightKg(caseReqModel.getMinWeight());
			first.setLastUpdatedBy(user);
			first.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			first.setCaseDetail(caseDetailEntity);
			try {
				if (caseReqModel.getMinWeightDate() != null) {
					first.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getMinWeightDate()));
				}
			} catch (ParseException e) {
				response.setErrorCode("1");
				response.setErrorMessage("Invalid min weight date format");
				return response;
			}

			ChildGrowth second = childGrowthList.get(1);
			second.setHeightCm(caseReqModel.getDischargeHeight());
			second.setWeightKg(caseReqModel.getDischargeWeight());
			second.setLastUpdatedBy(user);
			second.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			second.setCaseDetail(caseDetailEntity);
			try {
				if (caseReqModel.getDischargeDate() != null) {
					second.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getDischargeDate()));
				}
			} catch (ParseException e) {
				response.setErrorCode("1");
				response.setErrorMessage("Invalid discharge date format");
				return response;
			}
			childGrowthRepository.saveAll(childGrowthList);
		}

		Integer[] outcomeIdHolder = new Integer[1];
		List<DischargeChildDetail> dischargeChildList = new ArrayList<>();
		for (DischargeChildModel disChildInd : caseReqModel.getDischargeChildDetails()) {
			DischargeChildDetail entity = dischargeChildDetailRepository.findFirstByCaseDetail_CaseId(caseReqModel.getCaseId());
			if (entity != null) {
				entity.setAvgWeightGainKg(disChildInd.getAvgWeightGainKg());
				entity.setClinicalImpression(disChildInd.getClinicalImpression());
				entity.setDietaryAdvice(disChildInd.getDietaryAdvice());
				entity.setMedicalAdvice(disChildInd.getMedicalAdvice());
				entity.setTreatmentGiven(disChildInd.getTreatmentGiven());
				if (disChildInd.getOutcomeId() != null) {
					outcomeRepository.findById(disChildInd.getOutcomeId()).ifPresent(o -> {
						entity.setOutcomeId(o.getOutcomeId());
						outcomeIdHolder[0] = o.getOutcomeId();
					});
				}
				entity.setLastUpdatedBy(user);
				entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				dischargeChildList.add(entity);
			}
			// Source unconditionally adds disChildEntity here even when null
			// (a real NPE-on-save risk); skipped defensively rather than
			// reproduced, since there's no legitimate case for a null save.
		}
		dischargeChildDetailRepository.saveAll(dischargeChildList);

		List<DischargeComorbidCondition> previous = dischargeComorbidConditionRepository.findByCaseDetail_CaseId(caseReqModel.getCaseId());
		if (!previous.isEmpty()) {
			dischargeComorbidConditionRepository.deleteByCaseDetail_CaseId(caseReqModel.getCaseId());
		}
		if (caseReqModel.getDischargeComorbidConditions() != null) {
			List<DischargeComorbidCondition> list = new ArrayList<>();
			for (DischargeComorbidModel ind : caseReqModel.getDischargeComorbidConditions()) {
				DischargeComorbidCondition entity = new DischargeComorbidCondition();
				entity.setCreatedBy(user);
				entity.setCreatedOn(new Timestamp(new Date().getTime()));
				entity.setCaseDetail(caseDetailEntity);
				comorbidConditionValueRepository.findById(ind.getComorbidId()).ifPresent(c -> entity.setComorbidId(c.getComorbidId()));
				entity.setOthersValue(ind.getOthersValue());
				list.add(entity);
			}
			dischargeComorbidConditionRepository.saveAll(list);
		}

		if (caseReqModel.getDischargeMotherDetails() != null) {
			List<DischargeMotherDetail> list = new ArrayList<>();
			for (DischargeMotherModel ind : caseReqModel.getDischargeMotherDetails()) {
				DischargeMotherDetail entity = dischargeMotherDetailRepository.findFirstByCaseDetail_CaseId(caseReqModel.getCaseId());
				if (entity == null) {
					entity = new DischargeMotherDetail();
					entity.setCreatedBy(user);
					entity.setCreatedOn(new Timestamp(new Date().getTime()));
					entity.setCaseDetail(caseDetailEntity);
				} else {
					entity.setLastUpdatedBy(user);
					entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
					entity.setCaseDetail(caseDetailEntity);
				}
				if (ind.getMedicalRiskId() != null) {
					DischargeMotherDetail finalEntity = entity;
					medicalRiskRepository.findById(ind.getMedicalRiskId()).ifPresent(r -> finalEntity.setMedicalRiskId(r.getMedicalRiskId()));
				} else {
					entity.setMedicalRiskId(null);
				}
				list.add(entity);
			}
			dischargeMotherDetailRepository.saveAll(list);
		}

		if (caseReqModel.getDischargeMotherGrowths() != null) {
			List<CaseMotherGrowth> list = new ArrayList<>();
			for (CaseMotherGrowthModel ind : caseReqModel.getDischargeMotherGrowths()) {
				if (ind.isAllFieldsNull()) {
					continue;
				}
				CaseMotherGrowth entity = caseMotherGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtDischargeTrue(caseReqModel.getCaseId());
				if (entity == null) {
					entity = new CaseMotherGrowth();
					entity.setCreatedBy(user);
					entity.setCreatedOn(new Timestamp(new Date().getTime()));
					entity.setCaseDetail(caseDetailEntity);
				} else {
					entity.setLastUpdatedBy(user);
					entity.setLastUpdatedOn(new Timestamp(new Date().getTime()));
					entity.setCaseDetail(caseDetailEntity);
				}
				entity.setHeightCm(ind.getHeightCm());
				entity.setWeightKg(ind.getWeightKg());
				entity.setMuacCm(ind.getMuacCm());
				entity.setBmi(ind.getBmi());
				entity.setAdvice(ind.getAdvice());
				try {
					entity.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(caseReqModel.getDischargeDate()));
				} catch (ParseException e) {
					response.setErrorCode("1");
					response.setErrorMessage("Invalid date format in mother msmt date");
				}
				entity.setRecordedAtDischarge(true);
				list.add(entity);
			}
			caseMotherGrowthRepository.saveAll(list);
		}

		nrcFollowupService.updateFollowups(caseReqModel.getCaseId(), caseReqModel.getLoginUserId(), outcomeIdHolder[0]);

		caseDetailRepository.save(caseDetailEntity);
		setTargetWeightAchieved(caseDetailEntity, caseReqModel.getLoginUserId());
		caseDetailRepository.save(caseDetailEntity);

		response.setErrorCode("0");
		response.setSuccessMessage("Updated discharged case record with case id : " + caseReqModel.getCaseId());
		return response;
	}

	public AbstractResponse viewDischarge(ViewCaseRequestModel viewCaseReqModel) {
		AbstractResponse response = new AbstractResponse();

		NrcUser user = nrcUserRepository.findById(viewCaseReqModel.getLoginUserId()).orElse(null);
		if (user == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid user id provided.");
			return response;
		}
		Integer hospitalId = user.getHospitalId();

		if (viewCaseReqModel.getCaseId() == null) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("Please enter a valid case id");
			return response;
		}

		CaseDetail caseDetailEntity = caseDetailRepository.findByCaseIdAndHospitalId(viewCaseReqModel.getCaseId(), hospitalId);
		if (caseDetailEntity == null) {
			response.setErrorCode("1");
			response.setResponseObject(null);
			response.setErrorMessage("The given case id does not exist");
			return response;
		}
		if (caseDetailEntity.getDischargeDate() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("This case is not yet discharged");
			return response;
		}

		DischargeRequestModel dischDetailsModel = buildDischargeRequestModel(caseDetailEntity);

		response.setErrorCode("0");
		response.setResponseObject(dischDetailsModel);
		response.setSuccessMessage("Discharge details for case id : " + viewCaseReqModel.getCaseId());
		return response;
	}

	/** Ported from ModelTransformationUtil#transferCaseDetailEntityToDischargeRequestModel, adapted to query by caseId instead of navigating dropped collections. */
	private DischargeRequestModel buildDischargeRequestModel(CaseDetail caseDetailEntity) {
		DischargeRequestModel model = new DischargeRequestModel();

		if (caseDetailEntity.getDischargeDate() != null) {
			model.setDischargeDate(DateFormatUtil.getDateInddMMyyyyhhMMssStringFromTimestamp(caseDetailEntity.getDischargeDate()));
		}
		if (caseDetailEntity.getDischargeSd() != null) {
			model.setDischargeSd(caseDetailEntity.getDischargeSd());
		}
		model.setCaseId(caseDetailEntity.getCaseId());
		model.setLoginUserId(caseDetailEntity.getCreatedBy().getuId());
		model.setHospitalId(caseDetailEntity.getHospital().getHId());

		for (ChildGrowth cg : childGrowthRepository.findDischargeGrowthsOrdered(caseDetailEntity.getCaseId())) {
			if (Boolean.TRUE.equals(cg.getRecordedAtDischarge()) && cg.getHeightCm() == null) {
				model.setMinWeight(cg.getWeightKg());
				if (cg.getMeasurementDate() != null) {
					model.setMinWeightDate(DateFormatUtil.getDateInyyyyMMddString(cg.getMeasurementDate()));
				}
			} else if (Boolean.TRUE.equals(cg.getRecordedAtDischarge())) {
				model.setDischargeHeight(cg.getHeightCm());
				model.setDischargeWeight(cg.getWeightKg());
			}
		}

		List<DischargeChildModel> dischargeChildDetails = new ArrayList<>();
		DischargeChildDetail disChild = dischargeChildDetailRepository.findFirstByCaseDetail_CaseId(caseDetailEntity.getCaseId());
		if (disChild != null) {
			DischargeChildModel m = new DischargeChildModel();
			m.setClinicalImpression(disChild.getClinicalImpression());
			m.setAvgWeightGainKg(disChild.getAvgWeightGainKg());
			m.setDietaryAdvice(disChild.getDietaryAdvice());
			m.setMedicalAdvice(disChild.getMedicalAdvice());
			m.setTreatmentGiven(disChild.getTreatmentGiven());
			m.setOutcomeId(disChild.getOutcomeId());
			dischargeChildDetails.add(m);
		}
		model.setDischargeChildDetails(dischargeChildDetails);

		List<DischargeMotherModel> dischargeMothers = new ArrayList<>();
		DischargeMotherDetail disMother = dischargeMotherDetailRepository.findFirstByCaseDetail_CaseId(caseDetailEntity.getCaseId());
		if (disMother != null) {
			DischargeMotherModel m = new DischargeMotherModel();
			m.setMedicalRiskId(disMother.getMedicalRiskId());
			dischargeMothers.add(m);
		} else {
			dischargeMothers.add(new DischargeMotherModel());
		}
		model.setDischargeMotherDetails(dischargeMothers);

		// Ported verbatim from the source's odd filter
		// (recId != caseId, effectively always true) - see DischargeService
		// class javadoc / migration notes for why this isn't tightened to
		// "recordedAtDischarge only", matching production behavior exactly.
		List<CaseMotherGrowthModel> dischargeMotherGrowths = new ArrayList<>();
		List<CaseMotherGrowth> allMotherGrowths = caseMotherGrowthRepository.findByCaseDetail_CaseId(caseDetailEntity.getCaseId());
		if (!allMotherGrowths.isEmpty()) {
			for (CaseMotherGrowth cmg : allMotherGrowths) {
				if (cmg.getRecId() != caseDetailEntity.getCaseId()) {
					CaseMotherGrowthModel m = new CaseMotherGrowthModel();
					m.setWeightKg(cmg.getWeightKg());
					m.setHeightCm(cmg.getHeightCm());
					m.setMuacCm(cmg.getMuacCm());
					m.setBmi(cmg.getBmi());
					m.setAdvice(cmg.getAdvice());
					dischargeMotherGrowths.add(m);
				}
			}
		} else {
			dischargeMotherGrowths.add(new CaseMotherGrowthModel());
		}
		model.setDischargeMotherGrowths(dischargeMotherGrowths);

		List<DischargeComorbidModel> dischargeComorbidConditions = new ArrayList<>();
		for (DischargeComorbidCondition dc : dischargeComorbidConditionRepository.findByCaseDetail_CaseId(caseDetailEntity.getCaseId())) {
			DischargeComorbidModel m = new DischargeComorbidModel();
			m.setComorbidId(dc.getComorbidId());
			m.setOthersValue(dc.getOthersValue());
			m.setIsChecked(true);
			dischargeComorbidConditions.add(m);
		}
		for (var cv : comorbidConditionValueRepository.findNotYetSelectedForDischarge(caseDetailEntity.getCaseId())) {
			DischargeComorbidModel m = new DischargeComorbidModel();
			m.setComorbidId(cv.getComorbidId());
			m.setIsChecked(false);
			dischargeComorbidConditions.add(m);
		}
		model.setDischargeComorbidConditions(dischargeComorbidConditions);

		return model;
	}

	/**
	 * Ported from NRCOperationServiceImpl#setPastTargetWeightAchieved. One of
	 * the source's own "ONE TIME APIS" (its comment, not ours) - a batch
	 * fixup that re-runs the target-weight-achieved calculation across every
	 * already-discharged case in the caller's hospital. Kept here (not
	 * dropped as one-time/dead) because /setTargetAchieved/{userId} is still
	 * a live, callable endpoint in the source, not commented out.
	 */
	@Transactional
	public AbstractResponse setPastTargetWeightAchieved(Integer uId) {
		AbstractResponse response = new AbstractResponse();

		NrcUser user = nrcUserRepository.findById(uId).orElse(null);
		if (user == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Invalid user id provided.");
			return response;
		}

		List<CaseDetail> caseList = caseDetailRepository.findAllDischargedForHospital(user.getHospitalId());
		for (CaseDetail caseInd : caseList) {
			setTargetWeightAchieved(caseInd, uId);
			caseDetailRepository.save(caseInd);
		}

		response.setErrorCode("0");
		response.setSuccessMessage("Setting past target weight acheived flag success");
		return response;
	}

	/** Ported from NRCOperationServiceImpl#setTargetWeightAchieved. Mutates caseRec in place; caller persists. */
	void setTargetWeightAchieved(CaseDetail caseRec, Integer userId) {
		boolean percentAchieved = caseDetailRepository.findTargetStatus(caseRec.getCaseId()) != null;

		boolean averageAchieved = false;
		List<Object[]> growthRows = childGrowthRepository.findLastFourMeasurements(caseRec.getCaseId());
		if (growthRows.size() == 4) {
			BigDecimal totalGain = BigDecimal.ZERO;
			for (int i = 0; i < 3; i++) {
				BigDecimal wt1 = (BigDecimal) growthRows.get(i)[1];
				BigDecimal wt2 = (BigDecimal) growthRows.get(i + 1)[1];
				totalGain = totalGain.add(findWeightGain(wt1, wt2));
			}
			BigDecimal avgGain = totalGain.divide(new BigDecimal(3), 2, RoundingMode.HALF_EVEN);
			averageAchieved = avgGain.compareTo(AVG_GAIN_THRESHOLD) >= 0;
		}

		caseRec.setTargetWeightAchieved(percentAchieved || averageAchieved ? true : null);
	}

	private static BigDecimal findWeightGain(BigDecimal wt1, BigDecimal wt2) {
		double val = ((wt1.doubleValue() - wt2.doubleValue()) / wt2.doubleValue()) * 1000;
		return new BigDecimal(val).setScale(2, RoundingMode.HALF_EVEN);
	}
}
