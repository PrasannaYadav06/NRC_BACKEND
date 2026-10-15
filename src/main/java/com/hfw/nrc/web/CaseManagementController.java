package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.CaseDetailsRequestModel;
import com.hfw.nrc.dto.ChildDetailRequestModel;
import com.hfw.nrc.dto.DischargeRequestModel;
import com.hfw.nrc.dto.ExistingDetailRequestModel;
import com.hfw.nrc.dto.SearchRequestModel;
import com.hfw.nrc.dto.ViewCaseRequestModel;
import com.hfw.nrc.service.CaseCreationService;
import com.hfw.nrc.service.CaseQueryService;
import com.hfw.nrc.service.DischargeService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's case-management
 * endpoints (Group C: creation, discharge, view, search). Every
 * hospitalId/loginUserId in a request body now goes through SecurityUtils
 * first - same fix as PatientListController/UnitWardController - a
 * non-admin cannot act under a different hospital's id or attribute an
 * action to a different user.
 */
@RestController
public class CaseManagementController {

	@Autowired
	private CaseCreationService caseCreationService;
	@Autowired
	private DischargeService dischargeService;
	@Autowired
	private CaseQueryService caseQueryService;

	@PostMapping("/createcase")
	public ResponseEntity<AbstractResponse> createCase(@RequestBody CaseDetailsRequestModel caseDetailsModel) {
		caseDetailsModel.setHospitalId(SecurityUtils.resolveHospitalId(caseDetailsModel.getHospitalId()));
		caseDetailsModel.setLoginUserId(SecurityUtils.resolveUserId(caseDetailsModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseCreationService.createCase(caseDetailsModel));
	}

	/** dischargeCase in the source - the "discharge" flow, despite the URL name. */
	@PostMapping("/updatecase")
	public ResponseEntity<AbstractResponse> updateCase(@RequestBody DischargeRequestModel caseReqModel) {
		caseReqModel.setHospitalId(SecurityUtils.resolveHospitalId(caseReqModel.getHospitalId()));
		caseReqModel.setLoginUserId(SecurityUtils.resolveUserId(caseReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(dischargeService.dischargeCase(caseReqModel));
	}

	@PostMapping("/updatedischarge")
	public ResponseEntity<AbstractResponse> updateDischarge(@RequestBody DischargeRequestModel caseReqModel) {
		caseReqModel.setHospitalId(SecurityUtils.resolveHospitalId(caseReqModel.getHospitalId()));
		caseReqModel.setLoginUserId(SecurityUtils.resolveUserId(caseReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(dischargeService.updateDischarge(caseReqModel));
	}

	@PostMapping("/viewdischarge")
	public ResponseEntity<AbstractResponse> viewDischarge(@RequestBody ViewCaseRequestModel viewCaseReqModel) {
		viewCaseReqModel.setLoginUserId(SecurityUtils.resolveUserId(viewCaseReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(dischargeService.viewDischarge(viewCaseReqModel));
	}

	@PostMapping("/searchdetails")
	public ResponseEntity<AbstractResponse> searchDetails(@RequestBody SearchRequestModel searchReqModel) {
		searchReqModel.setLoginUserId(SecurityUtils.resolveUserId(searchReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseQueryService.searchDetails(searchReqModel));
	}

	@PostMapping("/getcasedetails")
	public ResponseEntity<AbstractResponse> getCaseDetails(@RequestBody ExistingDetailRequestModel edrModel) {
		edrModel.setHospitalId(SecurityUtils.resolveHospitalId(edrModel.getHospitalId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseQueryService.getCaseDetails(edrModel));
	}

	@PostMapping("/getchilddetails")
	public ResponseEntity<AbstractResponse> getChildDetails(@RequestBody ChildDetailRequestModel cdrModel) {
		cdrModel.setHospitalId(SecurityUtils.resolveHospitalId(cdrModel.getHospitalId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseQueryService.getChildDetails(cdrModel));
	}

	/** Standalone child-record update (also used internally by createcase's re-admission path). */
	@PostMapping("/updatechild")
	public ResponseEntity<AbstractResponse> updateChild(@RequestBody CaseDetailsRequestModel caseDetailsModel) {
		caseDetailsModel.setHospitalId(SecurityUtils.resolveHospitalId(caseDetailsModel.getHospitalId()));
		caseDetailsModel.setLoginUserId(SecurityUtils.resolveUserId(caseDetailsModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseCreationService.updateChild(caseDetailsModel));
	}

	@PostMapping("/updateadmission")
	public ResponseEntity<AbstractResponse> updateAdmission(@RequestBody CaseDetailsRequestModel caseDetailsModel) {
		caseDetailsModel.setHospitalId(SecurityUtils.resolveHospitalId(caseDetailsModel.getHospitalId()));
		caseDetailsModel.setLoginUserId(SecurityUtils.resolveUserId(caseDetailsModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseCreationService.updateAdmission(caseDetailsModel));
	}

	@PostMapping("/viewadmission")
	public ResponseEntity<AbstractResponse> viewAdmission(@RequestBody ViewCaseRequestModel viewCaseReqModel) {
		viewCaseReqModel.setLoginUserId(SecurityUtils.resolveUserId(viewCaseReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(caseCreationService.viewAdmission(viewCaseReqModel));
	}

	/**
	 * Ported from NRCRestController's setTargetAchieved (one of the source's
	 * own "ONE TIME APIS" - still a live, uncommented endpoint though, not
	 * dropped). Batch-recalculates the target-weight-achieved flag across
	 * every already-discharged case in the caller's hospital.
	 */
	@GetMapping("/setTargetAchieved/{userId}")
	public ResponseEntity<AbstractResponse> setTargetAchieved(@PathVariable("userId") Integer userId) {
		Integer resolvedUserId = SecurityUtils.resolveUserId(userId);
		return ResponseEntity.status(HttpStatus.OK).body(dischargeService.setPastTargetWeightAchieved(resolvedUserId));
	}
}
