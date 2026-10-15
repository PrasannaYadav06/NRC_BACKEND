package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.HospitalDetail;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.CaseDetailList;
import com.hfw.nrc.dto.CaseDetailModel;
import com.hfw.nrc.dto.CountModel;
import com.hfw.nrc.dto.LastFourMsmtResponseModel;
import com.hfw.nrc.dto.SDRequestModel;
import com.hfw.nrc.dto.SDResponseModel;
import com.hfw.nrc.dto.SamRequestModel;
import com.hfw.nrc.dto.SamResponseModel;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.HospitalDetailRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.util.DateFormatUtil;
import com.hfw.nrc.util.ReportDateUtil;

/**
 * Ported (first slice - the patient-list/count/SAM cluster) from
 * nrcapp.service.NRCOperationServiceImpl, which in the source is one 2,685-
 * line class covering case create/update/discharge/search too - those are
 * ported separately as they're built out. Response shape/messages preserved
 * exactly (frontend contract, see README "Response contract").
 */
@Service
public class NRCOperationService {

	private static final int BEDS_HR_TYPE_ID = 25;
	private static final int ZERO_INPATIENTS = 0;
	private static final BigDecimal AVG_GAIN_THRESHOLD = new BigDecimal(5);

	@Autowired
	private NrcUserRepository nrcUserRepository;
	@Autowired
	private CaseDetailRepository caseDetailRepository;
	@Autowired
	private ChildGrowthRepository childGrowthRepository;
	@Autowired
	private HospitalDetailRepository hospitalDetailRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private static AbstractResponse invalidUserId() {
		AbstractResponse res = new AbstractResponse();
		res.setErrorCode("1");
		res.setErrorMessage("Invalid user ID");
		return res;
	}

	/** Ported from ModelTransformationUtil#transferCaseDetailEntityToCaseDetailModel. */
	private static void transferCaseDetailEntityToCaseDetailModel(CaseDetail entity, CaseDetailModel model) {
		if (entity.getIpNum() != null) {
			model.setIpNum(entity.getIpNum());
		}
		model.setPatientName(entity.getChildDemography().getName());
		if (entity.getTargetWeight() != null) {
			model.setTargetWeight(entity.getTargetWeight());
		}
		model.setSamNumber(entity.getSamNum());
		if (entity.getAdmissionDate() != null) {
			model.setAdmissionDate(DateFormatUtil.getDateInddMMyyyyhhMMssStringFromTimestamp(entity.getAdmissionDate()));
		}
		if (entity.getDischargeDate() != null) {
			model.setDischargeDate(DateFormatUtil.getDateInddMMyyyyhhMMssStringFromTimestamp(entity.getDischargeDate()));
		}
		model.setCaseId(entity.getCaseId());
	}

	/** Ported from NRCOperationServiceImpl#findWeightGain. */
	private static BigDecimal findWeightGain(BigDecimal wt1, BigDecimal wt2) {
		double val = ((wt1.doubleValue() - wt2.doubleValue()) / wt2.doubleValue()) * 1000;
		return new BigDecimal(val).setScale(2, RoundingMode.HALF_EVEN);
	}

	public AbstractResponse getAllInPatients(Integer userId) {
		AbstractResponse res = new AbstractResponse();
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			return invalidUserId();
		}
		Integer hospitalId = user.getHospitalId();
		List<CaseDetail> caseEntityList = caseDetailRepository.findInpatientsForHospital(hospitalId);
		if (caseEntityList.isEmpty()) {
			res.setErrorCode("1");
			res.setErrorMessage("No admitted cases found");
			return res;
		}
		CaseDetailList caseList = new CaseDetailList();
		for (CaseDetail caseInd : caseEntityList) {
			CaseDetailModel caseTemp = new CaseDetailModel();
			transferCaseDetailEntityToCaseDetailModel(caseInd, caseTemp);

			List<Object[]> growthRows = childGrowthRepository.findLastFourMeasurements(caseInd.getCaseId());
			if (!growthRows.isEmpty()) {
				Object[] first = growthRows.get(0);
				BigDecimal firstWeight = (BigDecimal) first[1];
				if (firstWeight != null) {
					caseTemp.setLastMeasuredWeight(firstWeight);
				}
				if (first[2] != null) {
					caseTemp.setLastMeasuredDate(DateFormatUtil.getDateInddMMyyyyString((java.util.Date) first[2]));
				}
				if (growthRows.size() == 4) {
					BigDecimal totalGain = BigDecimal.ZERO;
					for (int i = 0; i < 3; i++) {
						BigDecimal wt1 = (BigDecimal) growthRows.get(i)[1];
						BigDecimal wt2 = (BigDecimal) growthRows.get(i + 1)[1];
						totalGain = totalGain.add(findWeightGain(wt1, wt2));
					}
					BigDecimal avgGain = totalGain.divide(new BigDecimal(3), 2, RoundingMode.HALF_EVEN);
					caseTemp.setDischargeStatus(avgGain.compareTo(AVG_GAIN_THRESHOLD) > 0);
				}
			} else {
				caseTemp.setDischargeStatus(null);
			}
			caseList.getCaseDetailList().add(caseTemp);
		}
		caseList.getCaseDetailList().sort((c1, c2) -> c1.getPatientName().compareToIgnoreCase(c2.getPatientName()));

		res.setErrorCode("0");
		res.setSuccessMessage("Found inpatient list for the hospital: " + hospitalId);
		res.setResponseObject(caseList);
		return res;
	}

	public AbstractResponse getCount(Integer userId) {
		AbstractResponse res = new AbstractResponse();
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			return invalidUserId();
		}
		Integer hospitalId = user.getHospitalId();
		List<CaseDetail> caseEntityList = caseDetailRepository.findInpatientsForHospital(hospitalId);
		List<HospitalDetail> hospitalDetails = hospitalDetailRepository.findLatestForHospital(hospitalId);

		CountModel countModel = new CountModel();
		if (!hospitalDetails.isEmpty()) {
			for (HospitalDetail detail : hospitalDetails) {
				if (detail.getHrTypeId() == BEDS_HR_TYPE_ID) {
					countModel.setNumberOfBeds(detail.getValue());
				}
			}
			countModel.setInpatientCount(!caseEntityList.isEmpty() ? caseEntityList.size() : ZERO_INPATIENTS);
		}

		res.setErrorCode("0");
		res.setSuccessMessage("Found counts for the hospital id: " + hospitalId);
		res.setResponseObject(countModel);
		return res;
	}

	private AbstractResponse patientsInRange(Integer userId, Integer currFiscal, boolean discharged) {
		AbstractResponse res = new AbstractResponse();
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			return invalidUserId();
		}
		Integer hospitalId = user.getHospitalId();

		if (currFiscal == null || (currFiscal != 0 && currFiscal != 1)) {
			res.setErrorCode("1");
			res.setErrorMessage("Invalid fiscal flag");
			return res;
		}
		List<String> range = ReportDateUtil.reportingMonthRange(currFiscal == 1);
		String startDate = range.get(0);
		String endDate = range.get(1);

		List<CaseDetail> caseEntityList = discharged
				? caseDetailRepository.findDischargedForHospitalBetween(hospitalId, startDate, endDate)
				: caseDetailRepository.findAdmittedForHospitalBetween(hospitalId, startDate, endDate);

		if (caseEntityList.isEmpty()) {
			res.setErrorCode("1");
			res.setErrorMessage(discharged ? "No discharged cases found" : "No admitted cases found");
			return res;
		}

		CaseDetailList caseList = new CaseDetailList();
		for (CaseDetail caseInd : caseEntityList) {
			CaseDetailModel caseTemp = new CaseDetailModel();
			transferCaseDetailEntityToCaseDetailModel(caseInd, caseTemp);
			if (discharged) {
				for (ChildGrowth cg : childGrowthRepository
						.findByCaseDetail_CaseIdAndRecordedAtDischargeTrueAndHeightCmIsNotNull(caseInd.getCaseId())) {
					caseTemp.setDischargeWeight(cg.getWeightKg());
				}
			}
			caseList.getCaseDetailList().add(caseTemp);
		}
		caseList.getCaseDetailList().sort((c1, c2) -> c1.getPatientName().compareToIgnoreCase(c2.getPatientName()));

		String period = currFiscal == 1 ? "current" : "previous";
		res.setErrorCode("0");
		res.setSuccessMessage((discharged ? "Found discharged patient list in " : "Found admission list in ") + period
				+ " fiscal for the hospital : " + hospitalId);
		res.setResponseObject(caseList);
		return res;
	}

	public AbstractResponse getCurrPatients(Integer userId, Integer currFiscal) {
		return patientsInRange(userId, currFiscal, false);
	}

	public AbstractResponse getDischargedPatients(Integer userId, Integer currFiscal) {
		return patientsInRange(userId, currFiscal, true);
	}

	public AbstractResponse getMaxSamInHospital(Integer userId) {
		AbstractResponse res = new AbstractResponse();
		NrcUser user = nrcUserRepository.findById(userId).orElse(null);
		if (user == null) {
			return invalidUserId();
		}
		Integer hospitalId = user.getHospitalId();
		Integer samNum = caseDetailRepository.findMaxSamInHospital(hospitalId);

		SamResponseModel result = new SamResponseModel();
		result.setSamNumber(samNum != null ? samNum : 0);

		res.setErrorCode("0");
		res.setSuccessMessage("Maximum sam for the hospital: " + hospitalId);
		res.setResponseObject(result);
		return res;
	}

	public AbstractResponse checkSamNum(SamRequestModel samRequestModel) {
		AbstractResponse res = new AbstractResponse();
		NrcUser user = nrcUserRepository.findById(samRequestModel.getLoginUserId()).orElse(null);
		if (user == null) {
			res.setErrorCode("1");
			res.setErrorMessage("Invalid user id provided.");
			return res;
		}
		Integer hospitalId = user.getHospitalId();

		SamResponseModel result = new SamResponseModel();
		result.setSamNumber(samRequestModel.getSamNumber());
		Integer status = caseDetailRepository.countSamExists(samRequestModel.getSamNumber(), hospitalId);

		boolean exists = status != null && status == 1;
		result.setSamExists(exists);
		res.setErrorCode("0");
		res.setSuccessMessage(exists
				? "This sam number already exists for the hosptial: " + hospitalId
				: "This is a valid sam number for the hosptial: " + hospitalId);
		res.setResponseObject(result);
		return res;
	}

	/**
	 * Ported from NRCOperationServiceImpl#findSDValue, which itself delegates
	 * to a call to the SQL Server stored procedure nr.sd_calculation(height,
	 * weight, gender) rather than querying the WflRefFromNrc/WflRefFromWho
	 * tables directly (the direct-query version is commented out in the
	 * source) - confirmed the procedure exists in nr schema before porting.
	 */
	public AbstractResponse calculateSDValue(SDRequestModel sdReqModel) {
		AbstractResponse res = new AbstractResponse();

		BigDecimal oneHundredTwenty = new BigDecimal(120);
		if (sdReqModel.getHeight().compareTo(oneHundredTwenty) > 0) {
			res.setErrorCode("1");
			res.setErrorMessage("Height entered should be below 120.0");
			return res;
		}

		String sdValue = callSdCalculation(sdReqModel);

		if (sdValue != null) {
			SDResponseModel resModel = new SDResponseModel();
			resModel.setSdValue(sdValue);
			res.setErrorCode("0");
			res.setSuccessMessage("Returned SD value");
			res.setResponseObject(resModel);
		} else {
			res.setErrorCode("1");
			res.setErrorMessage("No SD value returned");
		}
		return res;
	}

	/**
	 * Ported from NRCOperationServiceImpl#findSDValue - used internally by the
	 * case-creation flow (CaseCreationService#addChildGrowth) when computing
	 * admissionSd. Unlike calculateSDValue (the /findsdvalue endpoint), this
	 * has no ">120" upper-bound rejection - the source's findSDValue didn't
	 * have one either, only the height-below-45 clamp.
	 */
	public String findSDValueForCreate(SDRequestModel sdReqModel) {
		return callSdCalculation(sdReqModel);
	}

	private String callSdCalculation(SDRequestModel sdReqModel) {
		BigDecimal fourtyFive = new BigDecimal(45);
		BigDecimal height = sdReqModel.getHeight().compareTo(fourtyFive) < 0 ? fourtyFive : sdReqModel.getHeight();
		height = height.setScale(0, RoundingMode.HALF_UP);

		@SuppressWarnings("unchecked")
		List<Object> rows = entityManager
				.createNativeQuery("exec nr.sd_calculation ?1, ?2, ?3")
				.setParameter(1, height)
				.setParameter(2, sdReqModel.getWeight())
				.setParameter(3, sdReqModel.getGender())
				.getResultList();

		return !rows.isEmpty() && rows.get(0) != null ? String.valueOf(rows.get(0)) : null;
	}
}
