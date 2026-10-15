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
import com.hfw.nrc.dto.DischargeDetailDto;
import com.hfw.nrc.dto.DischargeSaveRequest;
import com.hfw.nrc.service.NrcDischargeService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Stage 4 - the single Discharge module, replacing discharge/update-
 * discharge/view-discharge's old spread. One POST for both first-discharge
 * and later edits (each call inserts a new nr.nrc_discharge event row, see
 * NrcDischargeService).
 */
@RestController
public class NrcDischargeController {

	@Autowired
	private NrcDischargeService dischargeService;

	@PostMapping("/discharge")
	public ResponseEntity<AbstractResponse> save(@RequestBody DischargeSaveRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		return ResponseEntity.ok(dischargeService.saveDischarge(req, user.getHospitalId(), user.isAdmin(), (int) user.getUserId()));
	}

	/** Latest discharge event for a case - backs the "already discharged" interstitial, the View popup, and the Edit form's prefill. */
	@GetMapping("/discharge/case/{caseId}")
	public ResponseEntity<DischargeDetailDto> latestForCase(@PathVariable int caseId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		DischargeDetailDto dto = dischargeService.getLatestForCase(caseId, user.getHospitalId(), user.isAdmin());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}
}
