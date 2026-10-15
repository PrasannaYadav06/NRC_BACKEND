package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.service.GeneralConfigService;

/**
 * Ported from nrcapp.controller.NRCRestController's reference/lookup-data
 * endpoints (Group A - no path variables, same response for every caller).
 * Always returns HTTP 200; callers read the body's errorCode field to tell
 * success ("0") from failure ("1") - same contract the frontend already
 * expects, not a REST-status convention change.
 *
 * nrcadmin's GET /district/list returned the same data as /getalldistricts
 * below (both just list all districts) - only one is kept.
 */
@RestController
public class ReferenceLookupController {

	@Autowired
	private GeneralConfigService generalConfigService;

	@GetMapping("/getallreligion")
	public ResponseEntity<AbstractResponse> getAllReligion() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllReligion());
	}

	@GetMapping("/getallcaste")
	public ResponseEntity<AbstractResponse> getAllCaste() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllCaste());
	}

	@GetMapping("/getallcategory")
	public ResponseEntity<AbstractResponse> getAllCategory() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllCategory());
	}

	@GetMapping("/getallgender")
	public ResponseEntity<AbstractResponse> getAllGender() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllGender());
	}

	@GetMapping("/getalladmissiontypes")
	public ResponseEntity<AbstractResponse> getAllAdmissionTypes() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllAdmissionTypes());
	}

	@GetMapping("/getalladmissioncriteria")
	public ResponseEntity<AbstractResponse> getAllAdmissionCriteria() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllAdmissionCriteria());
	}

	@GetMapping("/getallcomorbidvalues")
	public ResponseEntity<AbstractResponse> getAllComorbidValues() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllComorbidValues());
	}

	@GetMapping("/getallreferredfrom")
	public ResponseEntity<AbstractResponse> getAllReferredFrom() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllReferredFrom());
	}

	@GetMapping("/getallsocialfactors")
	public ResponseEntity<AbstractResponse> getAllSocialFactors() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllSocialFactors());
	}

	@GetMapping("/getallqualifications")
	public ResponseEntity<AbstractResponse> getAllQualifications() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllQualifications());
	}

	@GetMapping("/getallmedicalrisks")
	public ResponseEntity<AbstractResponse> getAllMedicalRisks() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllMedicalRisks());
	}

	@GetMapping("/getalloutcomes")
	public ResponseEntity<AbstractResponse> getAllOutcomes() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllOutcomes());
	}

	@GetMapping("/getallriskcategory")
	public ResponseEntity<AbstractResponse> getAllRiskCategory() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllRiskCategory());
	}

	@GetMapping("/getallplacesforfollowup")
	public ResponseEntity<AbstractResponse> getAllPlacesForFollowup() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllPlacesForFollowup());
	}

	/** Path preserves the original typo ("malnutrtion") - the frontend calls this exact URL. */
	@GetMapping("/getallmalnutrtionstatus")
	public ResponseEntity<AbstractResponse> getAllMalnutritionStatus() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllMalStatus());
	}

	@GetMapping("/getalldistricts")
	public ResponseEntity<AbstractResponse> getAllDistricts() {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllDistricts());
	}

	/**
	 * Item 3 - cascading Taluka dropdown. includeHospitals=true (In-Patient
	 * Care/Follow-up's filter) also mixes in PHC/CHC-linked hospitals as
	 * their own selectable entries (see GeneralConfigService#getAllTaluks);
	 * defaults to false so the Registration form's own Taluka picker is
	 * unaffected.
	 */
	@GetMapping("/getalltaluks")
	public ResponseEntity<AbstractResponse> getAllTaluks(
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false, defaultValue = "false") boolean includeHospitals) {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllTaluks(districtId, includeHospitals));
	}

	/** NRC Hospital Registration form's PHC dropdown, cascading off Taluka. */
	@GetMapping("/getallphcs")
	public ResponseEntity<AbstractResponse> getAllPhcs(@RequestParam(required = false) Integer talukaId) {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllPhcs(talukaId));
	}

	/** NRC Hospital Registration form's CHC dropdown, cascading off Taluka. */
	@GetMapping("/getallchcs")
	public ResponseEntity<AbstractResponse> getAllChcs(@RequestParam(required = false) Integer talukaId) {
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllChcs(talukaId));
	}
}
