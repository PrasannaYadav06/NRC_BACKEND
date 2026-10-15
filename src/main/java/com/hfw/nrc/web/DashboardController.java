package com.hfw.nrc.web;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.service.DashboardService;
import com.hfw.nrc.util.NrcUserPrincipal;
import com.hfw.nrc.util.SecurityUtils;

/** Backs the NRC Data Dashboard page - see DashboardService for the response shape rationale. */
@RestController
public class DashboardController {

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/dashboardSummary")
	public ResponseEntity<?> getDashboardSummary(
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year) {
		NrcUserPrincipal user = SecurityUtils.currentUser();
		if (user == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
		}

		LocalDate today = LocalDate.now();
		int effectiveMonth = month != null ? month : today.getMonthValue();
		int effectiveYear = year != null ? year : today.getYear();

		Integer hospitalId = user.isAdmin() ? null : user.getHospitalId();
		Map<String, Object> data = dashboardService.getDashboardSummary(user.isAdmin(), hospitalId, effectiveMonth, effectiveYear);
		return ResponseEntity.ok(data);
	}
}
