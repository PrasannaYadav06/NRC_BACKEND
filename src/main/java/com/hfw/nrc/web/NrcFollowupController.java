package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.FollowupDetailDto;
import com.hfw.nrc.dto.FollowupPageResponse;
import com.hfw.nrc.dto.FollowupSaveRequest;
import com.hfw.nrc.service.FollowupEventService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Stage 5 - the single Follow-up module, replacing followup/followup-search/
 * followup-history/followup-due's old spread. Discharge-only-once model:
 * a patient shows up here (Record Followup/View) only once actually
 * discharged from In-Patient Care - see FollowupEventService#listFollowups.
 */
@RestController
public class NrcFollowupController {

	@Autowired
	private FollowupEventService followupService;
	@Autowired
	private com.hfw.nrc.util.RchoScopeResolver rchoScopeResolver;

	@PostMapping("/followup")
	public ResponseEntity<AbstractResponse> save(@RequestBody FollowupSaveRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		return ResponseEntity.ok(followupService.recordFollowup(req, user.getHospitalId(), user.isAdmin(), (int) user.getUserId()));
	}

	/** Follow-up Records list - "Search Follow-up Patient" + Reporting Month/Year/District/Taluka filter. */
	@GetMapping("/followup/list")
	public ResponseEntity<FollowupPageResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false) Integer talukaId,
			@RequestParam(required = false) Integer filterHospitalId,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		Integer hId = SecurityUtils.resolveHospitalId(null);
		Integer effectiveDistrictId = rchoScopeResolver.resolveDistrictId(districtId, user);
		Integer resolvedFilterHospitalId = rchoScopeResolver.resolveFilterHospitalId(filterHospitalId, effectiveDistrictId, user);
		if (resolvedFilterHospitalId != null) {
			hId = resolvedFilterHospitalId;
		}
		return ResponseEntity.ok(followupService.listFollowups(hId, search, month, year, status, effectiveDistrictId, talukaId, sort, page, size));
	}

	/** "1st followup / 2nd followup / ... up to latest" View popup. */
	@GetMapping("/followup/case/{caseId}")
	public ResponseEntity<FollowupDetailDto> detail(@PathVariable int caseId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		FollowupDetailDto dto = followupService.getDetail(caseId, user.getHospitalId(), user.isAdmin());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}
}
