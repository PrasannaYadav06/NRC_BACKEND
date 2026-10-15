package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.AddHospitalRequestModel;
import com.hfw.nrc.dto.HospitalPageResponse;
import com.hfw.nrc.dto.HospitalRecordDto;
import com.hfw.nrc.dto.UpdateHospitalRequestModel;
import com.hfw.nrc.service.HospitalAdminService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcadmin.controller.GeneralController's /hospital/save
 * (Group H). nrcadmin had no other role than admin so this endpoint never
 * needed a role check of its own; NRC_BACKEND now has HOSPITAL_USER too,
 * so - same spirit as SecurityUtils.resolveHospitalId closing a
 * not-previously-possible gap - this is explicitly restricted to ADMIN.
 * /district/list (GeneralController's other endpoint) isn't re-added here:
 * it's the same data as nrcapp's /getalldistricts, already covered by
 * Group A's ReferenceLookupController.
 */
@RestController
public class HospitalAdminController {

	@Autowired
	private HospitalAdminService hospitalAdminService;

	@PostMapping("/hospital/save")
	public ResponseEntity<AbstractResponse> saveHospitalDetails(@RequestBody AddHospitalRequestModel commonDto) {
		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin()) {
			AbstractResponse denied = new AbstractResponse();
			denied.setErrorCode("1");
			denied.setErrorMessage("Admin access required");
			return ResponseEntity.status(HttpStatus.OK).body(denied);
		}
		return ResponseEntity.status(HttpStatus.OK).body(hospitalAdminService.saveAllHospitalDetails(commonDto));
	}

	private boolean notAdmin() {
		return SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin();
	}

	/** Registration form's live Hospital Code/Name/Username/Password preview - see HospitalAdminService#previewIdentity. */
	@GetMapping("/hospital/preview-identity")
	public ResponseEntity<?> previewIdentity(
			@RequestParam int districtId,
			@RequestParam(required = false) Integer talukaId,
			@RequestParam(required = false) Integer phcId,
			@RequestParam(required = false) Integer chcId) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			return ResponseEntity.ok(hospitalAdminService.previewIdentity(districtId, talukaId, phcId, chcId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
		}
	}

	/** Stage 2: "Hospital Records" list, paginated + searched by code/name/short-name, District/Taluka filterable. */
	@GetMapping("/hospital/list")
	public ResponseEntity<HospitalPageResponse> listHospitals(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false) Integer talukaId,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(hospitalAdminService.listHospitals(search, districtId, talukaId, sort, page, size));
	}

	/**
	 * Any authenticated user's own hospital record (never another
	 * hospital's, regardless of role) - unlike /hospital/{hId} above this is
	 * not admin-gated, since HOSPITAL_USER logins need it too (New
	 * Admission's District field, prefilled + disabled from the hospital's
	 * own district, matching KABHI's add-teleneurology.component.ts's
	 * login-derived districtName). hospitalId is resolved from the JWT
	 * principal, never a client-supplied value.
	 */
	@GetMapping("/hospital/mine")
	public ResponseEntity<HospitalRecordDto> getMyHospital() {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		HospitalRecordDto dto = hospitalAdminService.getHospitalDetail(user.getHospitalId());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}

	/** Stage 2: single hospital - backs both the View popup and the Edit form's prefill. */
	@GetMapping("/hospital/{hId}")
	public ResponseEntity<HospitalRecordDto> getHospital(@PathVariable int hId) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		HospitalRecordDto dto = hospitalAdminService.getHospitalDetail(hId);
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}

	/** Stage 2: Edit form's Update button - same row, never a new one. */
	@PutMapping("/hospital/{hId}")
	public ResponseEntity<AbstractResponse> updateHospital(@PathVariable int hId, @RequestBody UpdateHospitalRequestModel request) {
		if (notAdmin()) {
			AbstractResponse denied = new AbstractResponse();
			denied.setErrorCode("1");
			denied.setErrorMessage("Admin access required");
			return ResponseEntity.status(HttpStatus.OK).body(denied);
		}
		return ResponseEntity.ok(hospitalAdminService.updateHospital(hId, request));
	}
}
