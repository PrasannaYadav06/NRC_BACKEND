package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ResourceRequestModel;
import com.hfw.nrc.dto.ResourceResponseModel;
import com.hfw.nrc.service.ResourceService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's Group G endpoints
 * (human-resources staffing counts). Both endpoints are keyed by
 * loginUserId (not hospitalId directly) in the source, so loginUserId is
 * routed through SecurityUtils.resolveUserId - same fix pattern used
 * everywhere else.
 */
@RestController
public class ResourceController {

	@Autowired
	private ResourceService resourceService;

	@PostMapping("/saveresourceslist")
	public ResponseEntity<AbstractResponse> saveResources(@RequestBody ResourceResponseModel resReqModel) {
		resReqModel.setLoginUserId(SecurityUtils.resolveUserId(resReqModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(resourceService.saveResources(resReqModel));
	}

	@PostMapping("/getresourceslist")
	public ResponseEntity<AbstractResponse> getResources(@RequestBody ResourceRequestModel resourceRequestModel) {
		resourceRequestModel.setLoginUserId(SecurityUtils.resolveUserId(resourceRequestModel.getLoginUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(resourceService.getResources(resourceRequestModel));
	}
}
