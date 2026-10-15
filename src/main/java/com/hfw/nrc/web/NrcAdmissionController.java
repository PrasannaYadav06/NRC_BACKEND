package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.AdmissionDetailDto;
import com.hfw.nrc.dto.AdmissionPageResponse;
import com.hfw.nrc.dto.AdmissionSaveRequest;
import com.hfw.nrc.dto.PatientFullHistoryDto;
import com.hfw.nrc.dto.PatientLookupPageResponse;
import com.hfw.nrc.dto.WeightCaptureRequest;
import com.hfw.nrc.service.NrcAdmissionService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.RchoScopeResolver;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Stage 3 - the single In-Patient Care admission module, replacing the old
 * create/edit/update/view/readmission spread. All endpoints hospital-scoped
 * via SecurityUtils.resolveHospitalId (admin: any hospital; district/taluka
 * user: forced to their own, server-enforced regardless of what the client
 * sends) - same pattern as HospitalAdminController (Stage 2).
 */
@RestController
public class NrcAdmissionController {

	@Autowired
	private NrcAdmissionService admissionService;
	@Autowired
	private RchoScopeResolver rchoScopeResolver;

	@PostMapping("/admission")
	public ResponseEntity<AbstractResponse> create(@RequestBody AdmissionSaveRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		Integer hospitalId = SecurityUtils.resolveHospitalId(req.getHospitalId());
		if (SecurityUtils.isDenied(hospitalId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(admissionService.createAdmission(req, hospitalId, (int) user.getUserId()));
	}

	@PutMapping("/admission/{caseId}")
	public ResponseEntity<AbstractResponse> update(@PathVariable int caseId, @RequestBody AdmissionSaveRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		return ResponseEntity.ok(admissionService.updateAdmission(caseId, req, user.getHospitalId(), user.isAdmin(), (int) user.getUserId()));
	}

	@GetMapping("/admission/{caseId}")
	public ResponseEntity<AdmissionDetailDto> get(@PathVariable int caseId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		AdmissionDetailDto dto = admissionService.getDetail(caseId, user.getHospitalId(), user.isAdmin());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}

	/** In-Patient Care Records list - "Search Patient" + Reporting Month/Year/Form-Status filter. */
	@GetMapping("/admission/list")
	public ResponseEntity<AdmissionPageResponse> list(
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
		Integer hId = SecurityUtils.resolveHospitalId(null); // null request => admin sees all, user forced to own
		Integer effectiveDistrictId = rchoScopeResolver.resolveDistrictId(districtId, user);
		Integer resolvedFilterHospitalId = rchoScopeResolver.resolveFilterHospitalId(filterHospitalId, effectiveDistrictId, user);
		if (resolvedFilterHospitalId != null) {
			hId = resolvedFilterHospitalId;
		}
		return ResponseEntity.ok(admissionService.listAdmissions(hId, search, month, year, status, effectiveDistrictId, talukaId, sort, page, size));
	}

	/** Item 13 - Search Patient page's search bar. */
	@GetMapping("/admission/search-patient")
	public ResponseEntity<PatientLookupPageResponse> searchPatient(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		Integer hId = SecurityUtils.resolveHospitalId(null);
		return ResponseEntity.ok(admissionService.searchPatients(hId, search, page, size));
	}

	/** Item 13 - the Search Patient page's single-A4-sheet full history (admission + weight-capture timeline + discharge + follow-up). */
	@GetMapping("/admission/{caseId}/full-history")
	public ResponseEntity<PatientFullHistoryDto> fullHistory(@PathVariable int caseId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		PatientFullHistoryDto dto = admissionService.getFullHistory(caseId, user.getHospitalId(), user.isAdmin());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}

	/** "Daily Weight Capture" popup submit. */
	@PostMapping("/admission/{caseId}/weight-capture")
	public ResponseEntity<AbstractResponse> recordWeightCapture(@PathVariable int caseId, @RequestBody WeightCaptureRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		return ResponseEntity.ok(admissionService.recordWeightCapture(caseId, req, user.getHospitalId(), user.isAdmin(), (int) user.getUserId()));
	}

	/** Admin-only delete, with confirmation handled client-side per spec. */
	@DeleteMapping("/admission/{caseId}")
	public ResponseEntity<AbstractResponse> delete(@PathVariable int caseId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null || !user.isAdmin()) {
			AbstractResponse denied = new AbstractResponse();
			denied.setErrorCode("1");
			denied.setErrorMessage("Admin access required");
			return ResponseEntity.status(HttpStatus.OK).body(denied);
		}
		return ResponseEntity.ok(admissionService.deleteAdmission(caseId));
	}
}
