package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.FollowupIndList;
import com.hfw.nrc.dto.FollowupModel;
import com.hfw.nrc.dto.FollowupRequestModel;
import com.hfw.nrc.service.NRCFollowupService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's Group E endpoints
 * (follow-up scheduling). userId in request bodies goes through
 * SecurityUtils first. /deletefollowups has no userId field to override
 * (matches the source, which doesn't scope that delete by hospital either).
 */
@RestController
public class FollowupController {

	@Autowired
	private NRCFollowupService nrcFollowupService;

	@PostMapping("/savefollowup")
	public ResponseEntity<AbstractResponse> saveFollowup(@RequestBody FollowupRequestModel followupRequestModel) {
		followupRequestModel.setUserId(SecurityUtils.resolveUserId(followupRequestModel.getUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(nrcFollowupService.saveFollowup(followupRequestModel));
	}

	@GetMapping("/getallfollowups/{caseId}")
	public ResponseEntity<AbstractResponse> getAllFollowups(@PathVariable("caseId") Integer caseId) {
		return ResponseEntity.status(HttpStatus.OK).body(nrcFollowupService.getAllFollowups(caseId));
	}

	@GetMapping("/getfollowupdue/{loginUserId}")
	public ResponseEntity<AbstractResponse> getAllFollowupsDue(@PathVariable("loginUserId") Integer loginUserId) {
		Integer userId = SecurityUtils.resolveUserId(loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(nrcFollowupService.getAllFollowupsDue(userId));
	}

	@PostMapping("/deletefollowups")
	public ResponseEntity<AbstractResponse> deleteFollowups(@RequestBody FollowupIndList follupList) {
		return ResponseEntity.status(HttpStatus.OK).body(nrcFollowupService.deleteFollowupList(follupList));
	}

	@PostMapping("/getfollowupduebydate")
	public ResponseEntity<AbstractResponse> getAllFollowupsDueByDate(@RequestBody FollowupModel model) {
		model.setUserId(SecurityUtils.resolveUserId(model.getUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(nrcFollowupService.getAllFollowupsDuebyDate(model));
	}
}
