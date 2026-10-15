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
import com.hfw.nrc.dto.SDRequestModel;
import com.hfw.nrc.dto.SamRequestModel;
import com.hfw.nrc.service.NRCOperationService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's patient-list/count/SAM
 * cluster (part of Group C - case management). Every {loginUserId} path
 * variable and SamRequestModel#loginUserId body field now goes through
 * SecurityUtils.resolveUserId first, same fix as UnitWardController.
 */
@RestController
public class PatientListController {

	@Autowired
	private NRCOperationService nrcOperationService;

	@GetMapping("/getallinpatients/{loginUserId}")
	public ResponseEntity<AbstractResponse> getAllInPatients(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.getAllInPatients(userId));
	}

	@GetMapping("/getcount/{loginUserId}")
	public ResponseEntity<AbstractResponse> getCount(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.getCount(userId));
	}

	@GetMapping("/getcurrinpatients/{loginUserId}/{currFiscal}")
	public ResponseEntity<AbstractResponse> getCurrPatients(@PathVariable("loginUserId") Integer loginUserId,
			@PathVariable("currFiscal") Integer currFiscal) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.getCurrPatients(userId, currFiscal));
	}

	@GetMapping("/getdischargedpatients/{loginUserId}/{currFiscal}")
	public ResponseEntity<AbstractResponse> getDischargedPatients(@PathVariable("loginUserId") Integer loginUserId,
			@PathVariable("currFiscal") Integer currFiscal) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.getDischargedPatients(userId, currFiscal));
	}

	@GetMapping("/getmaximumsam/{loginUserId}")
	public ResponseEntity<AbstractResponse> getMaxSam(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.getMaxSamInHospital(userId));
	}

	@PostMapping("/checksamnumber")
	public ResponseEntity<AbstractResponse> checkSamNum(@RequestBody SamRequestModel samRequestModel) {
		samRequestModel.setLoginUserId(SecurityUtils.resolveUserId(samRequestModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.checkSamNum(samRequestModel));
	}

	@PostMapping("/findsdvalue")
	public ResponseEntity<AbstractResponse> getSDValue(@RequestBody SDRequestModel sdReqModel) {
		return ResponseEntity.status(HttpStatus.OK).body(nrcOperationService.calculateSDValue(sdReqModel));
	}
}
