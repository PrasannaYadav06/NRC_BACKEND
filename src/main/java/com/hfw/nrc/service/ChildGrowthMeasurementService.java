package com.hfw.nrc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.domain.ChildGrowth;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ChildGrowthList;
import com.hfw.nrc.dto.ChildGrowthResponseModel;
import com.hfw.nrc.dto.ChildMeasurementRequestModel;
import com.hfw.nrc.dto.MeasurementList;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.util.DateFormatUtil;

/**
 * Ported from nrcapp.service.ChildGrowthServiceImpl's three active methods
 * (saveGrowthInfo/deleteGrowthList/getGrowthList - the rest of that class is
 * dead Sneha-Angan-integration code, per the original endpoint audit).
 * Response shape/messages preserved exactly (frontend contract).
 */
@Service
public class ChildGrowthMeasurementService {

	@Autowired
	private CaseDetailRepository caseDetailRepository;
	@Autowired
	private ChildGrowthRepository childGrowthRepository;

	@Transactional
	public AbstractResponse saveGrowthInfo(ChildMeasurementRequestModel cmReq) {
		AbstractResponse response = new AbstractResponse();

		if (cmReq.getMeasurementDate() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Please enter a valid measurement date");
			return response;
		}
		if (cmReq.getDiet() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Please enter a valid diet data");
			return response;
		}
		if (cmReq.getWeightKg() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Please enter valid weight ");
			return response;
		}

		List<ChildGrowth> measurements = childGrowthRepository
				.findByCaseDetail_CaseIdAndRecordedAtAdmissionFalseAndRecordedAtDischargeFalseOrderByMeasurementDateDesc(cmReq.getCaseId());
		for (ChildGrowth msmtInd : measurements) {
			try {
				if (msmtInd.getMeasurementDate().compareTo(DateFormatUtil.getDateFromyyyyMMddDashedString(cmReq.getMeasurementDate())) == 0) {
					response.setErrorCode("2");
					response.setErrorMessage("Measurement for this date already exists");
					return response;
				}
			} catch (ParseException ignored) {
			}
		}

		// Source has a bug here worth noting: it calls response.setErrorCode(...)
		// twice for these two cases (overwriting "1" with the message text as
		// the error code, never actually calling setErrorMessage) - fixed here
		// since it's a pure typo with no business-logic implication, not a
		// query/calculation bug like the ones deliberately preserved elsewhere.
		if (cmReq.getCaseId() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case details not provided. Please provide case id");
			return response;
		}

		CaseDetail detailRec = caseDetailRepository.findByCaseIdAndHospitalId(cmReq.getCaseId(), cmReq.getHospitalId());
		if (detailRec == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case details not found for given case id");
			return response;
		}

		try {
			if (detailRec.getAdmissionDate().compareTo(DateFormatUtil.getDateFromyyyyMMddDashedString(cmReq.getMeasurementDate())) > 0) {
				response.setErrorCode("2");
				response.setErrorMessage("This date is before admission date.Please enter valid date.");
				return response;
			}
		} catch (ParseException ignored) {
		}

		ChildGrowth growthInfo = new ChildGrowth();
		ChildGrowth admissionGrowth = childGrowthRepository.findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(cmReq.getCaseId());
		if (admissionGrowth != null) {
			growthInfo.setHeightCm(admissionGrowth.getHeightCm());
		}
		updateTargetWeight(cmReq, detailRec, false);

		NrcUser user = new NrcUser();
		user.setuId(cmReq.getUserId());
		growthInfo.setCreatedBy(user);
		growthInfo.setCreatedOn(new Timestamp(new Date().getTime()));
		growthInfo.setCaseDetail(detailRec);
		if (cmReq.getDiet() != null && !cmReq.getDiet().isEmpty()) growthInfo.setDiet(cmReq.getDiet());
		if (cmReq.getComments() != null && !cmReq.getComments().isEmpty()) growthInfo.setComments(cmReq.getComments());
		if (cmReq.getWeightKg() != null) growthInfo.setWeightKg(cmReq.getWeightKg());
		if (cmReq.getMeasurementDate() != null && !cmReq.getMeasurementDate().isEmpty()) {
			try {
				growthInfo.setMeasurementDate(DateFormatUtil.getDateFromyyyyMMddDashedString(cmReq.getMeasurementDate()));
			} catch (ParseException ignored) {
			}
		}
		growthInfo.setRecordedAtAdmission(false);
		growthInfo.setRecordedAtDischarge(false);

		ChildGrowth saved = childGrowthRepository.save(growthInfo);

		response.setErrorCode("0");
		response.setSuccessMessage("Saved record with id: " + saved.getRecId());
		return response;
	}

	@Transactional
	public AbstractResponse deleteGrowthList(MeasurementList msmtList) {
		AbstractResponse res = new AbstractResponse();

		if (msmtList.getMeasurementList() == null || msmtList.getMeasurementList().isEmpty()) {
			res.setErrorCode("1");
			res.setErrorMessage("No measurements provided to delete");
			return res;
		}

		// Ported as-is: the source resolves the case by id alone here, with no
		// hospital-ownership check (unlike every other lookup in this class) -
		// low severity since the growth-record ids themselves aren't guessable
		// client input in the normal flow, but noted for parity, not silently
		// tightened.
		Integer caseId = childGrowthRepository.findCaseIdByRecId(msmtList.getMeasurementList().get(0).intValue());
		CaseDetail detailRec = caseId != null ? caseDetailRepository.findById(caseId).orElse(null) : null;

		List<Integer> ids = new ArrayList<>();
		for (Long id : msmtList.getMeasurementList()) {
			ids.add(id.intValue());
		}
		int noOfRecordDeleted = childGrowthRepository.deleteDailyCareByIdIn(ids);
		updateTargetWeight(null, detailRec, true);

		res.setErrorCode("0");
		res.setSuccessMessage("Deleted " + noOfRecordDeleted + " no(s) of growth measurements");
		return res;
	}

	@Transactional
	public AbstractResponse getGrowthList(ChildMeasurementRequestModel cmReq) {
		AbstractResponse response = new AbstractResponse();

		if (cmReq.getCaseId() == null) {
			response.setErrorCode("1");
			response.setErrorMessage("Case id not provided. Provide valid case id");
			return response;
		}

		CaseDetail detailRec = caseDetailRepository.findByCaseIdAndHospitalId(cmReq.getCaseId(), cmReq.getHospitalId());
		if (detailRec == null) {
			response.setErrorCode("1");
			response.setErrorMessage("No measurement data found for given case id");
			return response;
		}

		List<ChildGrowth> growthList = childGrowthRepository.findByCaseDetail_CaseIdOrderByMeasurementDateDesc(cmReq.getCaseId());
		if (growthList.isEmpty()) {
			response.setErrorCode("1");
			response.setErrorMessage("No measurement data found for given case id");
			return response;
		}

		Collections.sort(growthList, (g1, g2) -> g2.getMeasurementDate().compareTo(g1.getMeasurementDate()));
		ChildGrowthList resultList = new ChildGrowthList();
		resultList.setSamNumber(detailRec.getSamNum());
		resultList.setChildName(detailRec.getChildDemography().getName());

		int n = growthList.size();
		for (int i = 0; i < n - 1; i++) {
			ChildGrowthResponseModel resultTemp = new ChildGrowthResponseModel();
			BigDecimal weightGain = findWeightGain(growthList.get(i).getWeightKg(), growthList.get(i + 1).getWeightKg());
			resultTemp.setWeightGain(weightGain);
			if (growthList.get(i).getMeasurementDate().equals(detailRec.getAdmissionDate())) {
				continue;
			}
			ChildGrowth ge = growthList.get(i);
			resultTemp.setMeasurementId(ge.getRecId());
			if (ge.getHeightCm() != null) resultTemp.setHeightCm(ge.getHeightCm());
			if (ge.getWeightKg() != null) resultTemp.setWeightCm(ge.getWeightKg());
			if (ge.getMeasurementDate() != null) resultTemp.setMeasurementDate(DateFormatUtil.getDateInddMMyyyyString(ge.getMeasurementDate()));
			if (ge.getDiet() != null && !ge.getDiet().isEmpty()) resultTemp.setDiet(ge.getDiet());
			if (ge.getComments() != null && !ge.getComments().isEmpty()) resultTemp.setComments(ge.getComments());
			resultList.getGrowthList().add(resultTemp);
		}

		response.setErrorCode("0");
		response.setSuccessMessage("Found growth measurements for given case id :" + cmReq.getCaseId());
		response.setResponseObject(resultList);
		return response;
	}

	/** Ported from ChildGrowthServiceImpl#updateTargetWeight. */
	private void updateTargetWeight(ChildMeasurementRequestModel cmReqModel, CaseDetail detailRec, boolean deleteFlag) {
		if (detailRec == null) {
			return;
		}
		BigDecimal minWeight = BigDecimal.valueOf(Double.MAX_VALUE);
		List<ChildGrowth> growthList = childGrowthRepository.findByCaseDetail_CaseIdOrderByMeasurementDateDesc(detailRec.getCaseId());
		for (ChildGrowth growth : growthList) {
			if (growth.getWeightKg() != null && growth.getWeightKg().compareTo(minWeight) <= 0) {
				minWeight = growth.getWeightKg();
			}
		}

		NrcUser user = new NrcUser();
		if (!deleteFlag) {
			user.setuId(cmReqModel.getUserId());
			if (cmReqModel.getWeightKg().compareTo(minWeight) < 0) {
				minWeight = cmReqModel.getWeightKg();
				detailRec.setTargetWeight(minWeight.multiply(new BigDecimal("1.15")));
				detailRec.setLastUpdatedBy(user);
				detailRec.setLastUpdatedOn(new Timestamp(new Date().getTime()));
			}
		} else {
			user.setuId(detailRec.getCreatedBy().getuId());
			detailRec.setTargetWeight(minWeight.multiply(new BigDecimal("1.15")));
			detailRec.setLastUpdatedBy(user);
			detailRec.setLastUpdatedOn(new Timestamp(new Date().getTime()));
		}

		caseDetailRepository.save(detailRec);
	}

	private static BigDecimal findWeightGain(BigDecimal wt1, BigDecimal wt2) {
		double val = ((wt1.doubleValue() - wt2.doubleValue()) / wt2.doubleValue()) * 1000;
		return new BigDecimal(val).setScale(2, RoundingMode.HALF_EVEN);
	}
}
