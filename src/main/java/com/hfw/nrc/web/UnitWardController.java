package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.service.GeneralConfigService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's /getallunits/{loginUserId}
 * and /getallwards/{loginUserId} (Group B - hospital-scoped via a user id).
 * The source app trusted the path variable outright; here it goes through
 * SecurityUtils.resolveUserId first so a HOSPITAL_USER can only ever query
 * their own hospital's units/wards - ADMIN can still query for anyone.
 */
@RestController
public class UnitWardController {

	@Autowired
	private GeneralConfigService generalConfigService;

	@GetMapping("/getallunits/{loginUserId}")
	public ResponseEntity<AbstractResponse> getAllUnits(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllUnits(userId));
	}

	@GetMapping("/getallwards/{loginUserId}")
	public ResponseEntity<AbstractResponse> getAllWards(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(generalConfigService.getAllWards(userId));
	}
}
