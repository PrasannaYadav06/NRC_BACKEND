package com.hfw.nrc.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfw.nrc.domain.CaseDetail;
import com.hfw.nrc.dto.CaseDetailsAdminDto;
import com.hfw.nrc.dto.DeleteResponse;
import com.hfw.nrc.repository.AdmissionComorbidConditionRepository;
import com.hfw.nrc.repository.AdmissionCriteriaRepository;
import com.hfw.nrc.repository.CaseAllocationRepository;
import com.hfw.nrc.repository.CaseDetailRepository;
import com.hfw.nrc.repository.CaseMotherDetailRepository;
import com.hfw.nrc.repository.CaseMotherGrowthRepository;
import com.hfw.nrc.repository.CaseMotherSocialFactorRepository;
import com.hfw.nrc.repository.CaseReferralRepository;
import com.hfw.nrc.repository.ChildGrowthRepository;
import com.hfw.nrc.repository.DischargeChildDetailRepository;
import com.hfw.nrc.repository.DischargeComorbidConditionRepository;
import com.hfw.nrc.repository.DischargeMotherDetailRepository;
import com.hfw.nrc.repository.FollowupRepository;
import com.hfw.nrc.repository.FollowupScheduleRepository;

/**
 * Ported from nrcadmin.service.CaseDetailsService - the last piece of
 * Group C. Admin-only "find/delete a case by SAM number" tool
 * (GET/DELETE /caseDetails/{..}); deleteAllByCaseId is a genuine hard
 * cascading delete across 13 tables, ported faithfully since it's a live,
 * callable endpoint today - not something this project can quietly soften.
 * Note the source repository's own dead code: CaseDetailsRepository had a
 * second, MySQL-syntax "deleteAllByCaseId" native query
 * (DELETE t1,t2 FROM ... JOIN ...) that is invalid T-SQL and was never
 * actually called by this service (the service method of the same name
 * loops through each child repository's own delete instead) - not ported,
 * it was unreachable in the source too.
 */
@Service
public class CaseDetailsAdminService {

	@Autowired private CaseDetailRepository caseDetailRepository;
	@Autowired private AdmissionComorbidConditionRepository admissionComorbidConditionRepository;
	@Autowired private CaseAllocationRepository caseAllocationRepository;
	@Autowired private AdmissionCriteriaRepository admissionCriteriaRepository;
	@Autowired private CaseMotherDetailRepository caseMotherDetailRepository;
	@Autowired private CaseMotherGrowthRepository caseMotherGrowthRepository;
	@Autowired private CaseMotherSocialFactorRepository caseMotherSocialFactorRepository;
	@Autowired private FollowupRepository followupRepository;
	@Autowired private FollowupScheduleRepository followupScheduleRepository;
	@Autowired private CaseReferralRepository caseReferralRepository;
	@Autowired private DischargeChildDetailRepository dischargeChildDetailRepository;
	@Autowired private DischargeComorbidConditionRepository dischargeComorbidConditionRepository;
	@Autowired private DischargeMotherDetailRepository dischargeMotherDetailRepository;
	@Autowired private ChildGrowthRepository childGrowthRepository;

	public List<CaseDetailsAdminDto> findCaseDetailsBySamNo(int samNum) {
		List<CaseDetailsAdminDto> result = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		for (CaseDetail caseInd : caseDetailRepository.findBySamNum(samNum)) {
			CaseDetailsAdminDto dto = new CaseDetailsAdminDto();
			dto.setCaseId(caseInd.getCaseId());
			dto.setSamNum(caseInd.getSamNum());
			dto.setTargetWeight(caseInd.getTargetWeight());
			dto.setAdmissionSd(caseInd.getAdmissionSd());
			dto.setDischargeSd(caseInd.getDischargeSd());

			if (caseInd.getChildDemography() != null) {
				dto.setChildId(caseInd.getChildDemography().getChildId());
				dto.setChildName(caseInd.getChildDemography().getName());
				dto.setGender(caseInd.getChildDemography().getGender());
				dto.setAddress(caseInd.getChildDemography().getAddressLine1());
				if (caseInd.getChildDemography().getDateOfBirth() != null) {
					dto.setDateOfBirth(dateFormat.format(caseInd.getChildDemography().getDateOfBirth()));
				}
			}
			if (caseInd.getHospital() != null) {
				dto.setHId(caseInd.getHospital().getHId());
				dto.setHospitalName(caseInd.getHospital().getName());
			}
			if (caseInd.getAdmissionDate() != null) {
				dto.setAdmissionDate(dateFormat.format(caseInd.getAdmissionDate()));
			}
			if (caseInd.getDischargeDate() != null) {
				dto.setDischargeDate(dateFormat.format(caseInd.getDischargeDate()));
			}
			result.add(dto);
		}
		return result;
	}

	@Transactional
	public DeleteResponse deleteAllByCaseId(int caseId) {
		if (!caseDetailRepository.existsById(caseId)) {
			return new DeleteResponse("Details Not Found");
		}

		admissionComorbidConditionRepository.deleteByCaseDetail_CaseId(caseId);
		caseAllocationRepository.deleteByCaseDetail_CaseId(caseId);
		admissionCriteriaRepository.deleteByCaseDetail_CaseId(caseId);
		caseMotherDetailRepository.deleteByCaseDetail_CaseId(caseId);
		caseMotherGrowthRepository.deleteByCaseDetail_CaseId(caseId);
		caseMotherSocialFactorRepository.deleteByCaseDetail_CaseId(caseId);
		followupRepository.deleteByCaseDetail_CaseId(caseId);
		followupScheduleRepository.deleteByCaseDetail_CaseId(caseId);
		caseReferralRepository.deleteByCaseDetail_CaseId(caseId);
		dischargeChildDetailRepository.deleteByCaseDetail_CaseId(caseId);
		dischargeComorbidConditionRepository.deleteByCaseDetail_CaseId(caseId);
		dischargeMotherDetailRepository.deleteByCaseDetail_CaseId(caseId);
		childGrowthRepository.deleteByCaseDetail_CaseId(caseId);
		caseDetailRepository.deleteById(caseId);

		return new DeleteResponse("Details Deleted Successfully");
	}
}
