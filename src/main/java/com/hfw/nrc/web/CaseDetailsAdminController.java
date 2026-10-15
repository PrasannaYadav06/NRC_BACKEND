package com.hfw.nrc.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.CaseDetailsAdminDto;
import com.hfw.nrc.dto.DeleteResponse;
import com.hfw.nrc.service.CaseDetailsAdminService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcadmin.controller.CaseDetailsController - the last piece
 * of Group C. Admin-only, same reasoning as HospitalAdminController: the
 * source had no other role to restrict against, NRC_BACKEND does now.
 * Response shapes are raw (List / DeleteResponse), not AbstractResponse -
 * matches the source's own contract, since the merged frontend's
 * deletesam.component.ts already expects a plain array/object.
 */
@RestController
public class CaseDetailsAdminController {

	@Autowired
	private CaseDetailsAdminService caseDetailsAdminService;

	@GetMapping("/caseDetails/{samNum}")
	public ResponseEntity<?> findDetails(@PathVariable("samNum") int samNum) {
		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
		}
		List<CaseDetailsAdminDto> caseDetailsList = caseDetailsAdminService.findCaseDetailsBySamNo(samNum);
		return ResponseEntity.status(HttpStatus.OK).body(caseDetailsList);
	}

	@DeleteMapping("/caseDetails/{caseId}")
	public ResponseEntity<DeleteResponse> deleteDetails(@PathVariable("caseId") int caseId) {
		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new DeleteResponse("Admin access required"));
		}
		DeleteResponse response = caseDetailsAdminService.deleteAllByCaseId(caseId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
