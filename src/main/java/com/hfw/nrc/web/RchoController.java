package com.hfw.nrc.web;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.RchoSaveRequest;
import com.hfw.nrc.service.RchoService;
import com.hfw.nrc.util.SecurityUtils;

/** Admin nav bar "RCHO" page - list/search/paginate, Add/Edit. Admin-only, same gating convention as HospitalAdminController. */
@RestController
public class RchoController {

	@Autowired
	private RchoService rchoService;

	private boolean notAdmin() {
		return SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin();
	}

	private ResponseEntity<?> errorResponse(Exception e) {
		e.printStackTrace();
		HttpStatus status = (e instanceof NoSuchElementException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Request failed"));
	}

	/** Self-scoped - lets an RCHO discover their own district (JWT has only their u_id, not district) to lock the In-Patient Care/Follow-up filter. */
	@GetMapping("/rcho/mine")
	public ResponseEntity<?> getMine() {
		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isRcho()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(rchoService.getMine(SecurityUtils.currentUser()));
	}

	/** RCHO's HR page - the linked-hospital table/search source (see RchoService#getMyHospitals). */
	@GetMapping("/rcho/my-hospitals")
	public ResponseEntity<?> getMyHospitals() {
		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isRcho()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(rchoService.getMyHospitals(SecurityUtils.currentUser()));
	}

	@GetMapping("/rcho/list")
	public ResponseEntity<?> list(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(rchoService.list(search, page, size));
	}

	@GetMapping("/rcho/{id}")
	public ResponseEntity<?> getById(@PathVariable int id) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		Object dto = rchoService.getById(id);
		return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	/** NRC Hospital Registration form's read-only RCHO field, auto-fetched on District change. */
	@GetMapping("/getrchobydistrict/{districtId}")
	public ResponseEntity<?> getByDistrict(@PathVariable int districtId) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		Object dto = rchoService.getByDistrict(districtId);
		return ResponseEntity.ok(dto); // null body is a valid "not created yet" answer, not an error
	}

	@PostMapping("/rcho/save")
	public ResponseEntity<?> create(@RequestBody RchoSaveRequest req) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			return ResponseEntity.ok(rchoService.create(req));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@PostMapping("/rcho/{id}/update")
	public ResponseEntity<?> update(@PathVariable int id, @RequestBody RchoSaveRequest req) {
		if (notAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		try {
			rchoService.update(id, req.getRchoName());
			return ResponseEntity.ok(Map.of("message", "RCHO updated successfully"));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}
}
