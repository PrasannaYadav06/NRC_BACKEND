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
import com.hfw.nrc.dto.HrDetailDto;
import com.hfw.nrc.dto.HrPageResponse;
import com.hfw.nrc.dto.HrSaveRequest;
import com.hfw.nrc.service.NrcHrService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Stage 6 - the single HR module: a district/taluka user's own self-service
 * "Human Resources" form (GET /hr/latest + POST /hr, one upserted row per
 * hospital/month), and "Search HR Records" (GET /hr/list + GET /hr/{id})
 * for browsing every hospital's monthly submissions - admin sees all,
 * a district/taluka user is scoped to their own hospital.
 */
@RestController
public class NrcHrController {

	@Autowired
	private NrcHrService hrService;

	@GetMapping("/hr/latest")
	public ResponseEntity<HrDetailDto> latest() {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		HrDetailDto dto = hrService.getLatestForHospital(user.getHospitalId());
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/hr")
	public ResponseEntity<AbstractResponse> save(@RequestBody HrSaveRequest req) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		return ResponseEntity.ok(hrService.saveOrUpdate(req, user.getHospitalId(), (int) user.getUserId()));
	}

	@GetMapping("/hr/list")
	public ResponseEntity<HrPageResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		Integer hId = SecurityUtils.resolveHospitalId(null);
		return ResponseEntity.ok(hrService.listHr(hId, search, month, year, page, size));
	}

	@GetMapping("/hr/{hrRecId}")
	public ResponseEntity<HrDetailDto> detail(@PathVariable int hrRecId) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		HrDetailDto dto = hrService.getDetail(hrRecId, user.getHospitalId(), user.isAdmin());
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(dto);
	}
}
