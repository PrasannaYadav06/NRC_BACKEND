package com.hfw.nrc.web;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

import com.hfw.nrc.domain.NrcHrPost;
import com.hfw.nrc.dto.NrcHrSubmitRequest;
import com.hfw.nrc.service.NrcHrPostService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Endpoints backing the HR page: the Add HR form, the hospital HR table,
 * the two admin summary tables, the approval workflow and the
 * sanctioned-count editor. Ported from KABHI_BACKEND's KabhiHRController
 * (/kabhihr/*) - same endpoint shapes/response bodies, at /hrpost/* here.
 * Submission bodies are plain JSON, not AES-wrapped - that's a KABHI-app-
 * wide convention (every "add-*" form there), not something the rest of
 * NRC_BACKEND does, so it's not replicated here.
 */
@RestController
public class NrcHrPostController {

	@Autowired
	private NrcHrPostService hrService;

	/** Maps a business-logic exception to the right HTTP status, consistently, across every endpoint below. */
	private ResponseEntity<?> errorResponse(Exception e) {
		// Matches KabhiHRController's own errorResponse (this module's direct
		// model) - was silently swallowing the real exception, showing only a
		// generic "Unable to load..." on the frontend with nothing in the
		// server console to diagnose from.
		e.printStackTrace();
		HttpStatus status;
		if (e instanceof SecurityException) {
			status = HttpStatus.FORBIDDEN;
		} else if (e instanceof NoSuchElementException) {
			status = HttpStatus.NOT_FOUND;
		} else if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
			status = HttpStatus.BAD_REQUEST;
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(status).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Request failed"));
	}

	private NrcUserPrincipal requireUser() {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			throw new SecurityException("Not authenticated.");
		}
		return user;
	}

	private NrcUserPrincipal requireAdmin() {
		NrcUserPrincipal user = requireUser();
		if (!user.isAdmin()) {
			throw new SecurityException("This action is restricted to ADMIN.");
		}
		return user;
	}

	private NrcUserPrincipal requireRcho() {
		NrcUserPrincipal user = requireUser();
		if (!user.isRcho()) {
			throw new SecurityException("This action is restricted to RCHO.");
		}
		return user;
	}

	// ===================== Form metadata =====================

	@GetMapping("/hrpost/meta")
	public ResponseEntity<?> getMeta() {
		try {
			return ResponseEntity.ok(hrService.getMeta(requireUser()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@GetMapping("/hrpost/availability")
	public ResponseEntity<?> getAvailability() {
		try {
			return ResponseEntity.ok(hrService.getAvailability(requireUser()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	// ===================== Submit =====================

	@PostMapping("/hrpost/submit")
	public ResponseEntity<?> submit(@RequestBody NrcHrSubmitRequest req) {
		try {
			NrcUserPrincipal user = requireUser();
			NrcHrPost saved = hrService.submit(req, user);
			return ResponseEntity.ok(Map.of(
					"hrPostId", saved.getHrPostId(),
					"approvalStatus", saved.getApprovalStatus(),
					"message", "Submitted successfully and sent for admin approval."));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	// ===================== Approval workflow =====================

	/** ADMIN's actionable queue (RCHO_APPROVED, ready for final sign-off) or an RCHO's own district PENDING queue - resolved by the caller's role. */
	@GetMapping("/hrpost/pending")
	public ResponseEntity<?> getPending(@RequestParam(required = false) Integer hId) {
		try {
			NrcUserPrincipal user = requireUser();
			if (!user.isAdmin() && !user.isRcho()) {
				throw new SecurityException("This action is restricted to ADMIN or RCHO.");
			}
			return ResponseEntity.ok(hrService.getPending(hId, user));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@PostMapping("/hrpost/approve/{id}")
	public ResponseEntity<?> approve(@PathVariable("id") int id) {
		try {
			return ResponseEntity.ok(hrService.approve(id, requireAdmin()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@PostMapping("/hrpost/reject/{id}")
	public ResponseEntity<?> reject(@PathVariable("id") int id, @RequestBody(required = false) Map<String, String> body) {
		try {
			String remarks = body != null ? body.get("remarks") : null;
			return ResponseEntity.ok(hrService.reject(id, remarks, requireAdmin()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	/** RCHO's own approval queue - district-scoped, each row enriched with hospitalName (see NrcHrPostService#getRchoQueue). */
	@GetMapping("/hrpost/rcho-queue")
	public ResponseEntity<?> getRchoQueue() {
		try {
			return ResponseEntity.ok(hrService.getRchoQueue(requireRcho()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	/** RCHO tier - first review, moves PENDING -> RCHO_APPROVED (then queues for admin). */
	@PostMapping("/hrpost/rcho-approve/{id}")
	public ResponseEntity<?> rchoApprove(@PathVariable("id") int id) {
		try {
			return ResponseEntity.ok(hrService.rchoApprove(id, requireRcho()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@PostMapping("/hrpost/rcho-reject/{id}")
	public ResponseEntity<?> rchoReject(@PathVariable("id") int id, @RequestBody(required = false) Map<String, String> body) {
		try {
			String remarks = body != null ? body.get("remarks") : null;
			return ResponseEntity.ok(hrService.rchoReject(id, remarks, requireRcho()));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	// ===================== Tables =====================

	@GetMapping("/hrpost/hospital-table")
	public ResponseEntity<?> getHospitalTable(@RequestParam(required = false) Integer hId) {
		try {
			NrcUserPrincipal user = requireUser();
			if (user.isRcho()) {
				if (hId == null) {
					return ResponseEntity.badRequest().body(Map.of("message", "No hospital specified."));
				}
				return ResponseEntity.ok(hrService.getHospitalTableForRcho(hId, user));
			}
			Integer resolved = SecurityUtils.resolveHospitalId(hId);
			if (SecurityUtils.isDenied(resolved) || resolved == null) {
				return ResponseEntity.badRequest().body(Map.of("message", "No hospital specified."));
			}
			return ResponseEntity.ok(hrService.getHospitalTable(resolved));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@GetMapping("/hrpost/cumulative-table")
	public ResponseEntity<?> getCumulativeTable(@RequestParam(required = false) String level) {
		try {
			requireAdmin();
			return ResponseEntity.ok(hrService.getCumulativeTable(level));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	@GetMapping("/hrpost/hospital-designation-matrix")
	public ResponseEntity<?> getHospitalDesignationMatrix(@RequestParam(required = false) String level) {
		try {
			requireAdmin();
			return ResponseEntity.ok(hrService.getHospitalDesignationMatrix(level));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	// ===================== Sanctioned counts =====================

	@PutMapping("/hrpost/sanctioned")
	public ResponseEntity<?> updateSanctioned(@RequestBody Map<String, Object> body) {
		try {
			NrcUserPrincipal user = requireAdmin();
			int hId = Integer.parseInt(String.valueOf(body.get("hId")));
			String designation = String.valueOf(body.get("designation"));
			int count = Integer.parseInt(String.valueOf(body.get("sanctionedCount")));
			return ResponseEntity.ok(hrService.updateSanctioned(hId, designation, count, user));
		} catch (Exception e) {
			return errorResponse(e);
		}
	}

	// ===================== Search / filter =====================

	@GetMapping("/hrpost/search")
	public ResponseEntity<?> search(
			@RequestParam(required = false) Integer hId,
			@RequestParam(required = false) String designation,
			@RequestParam(required = false) String personName,
			@RequestParam(required = false) String approvalStatus,
			@RequestParam(required = false) String employmentStatus) {
		try {
			NrcUserPrincipal user = requireUser();
			List<NrcHrPost> results = hrService.search(hId, designation, personName, approvalStatus, employmentStatus, user);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return errorResponse(e);
		}
	}
}
