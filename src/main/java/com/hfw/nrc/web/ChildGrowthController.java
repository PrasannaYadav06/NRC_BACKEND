package com.hfw.nrc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ChildMeasurementRequestModel;
import com.hfw.nrc.dto.MeasurementList;
import com.hfw.nrc.service.ChildGrowthMeasurementService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Ported from nrcapp.controller.NRCRestController's Group D endpoints
 * (daily-care child growth measurements). hospitalId/userId in the request
 * body go through SecurityUtils first - same fix applied everywhere else.
 * /deletechildgrowth has no hospitalId/userId field to override (the source
 * doesn't scope that lookup by hospital either - see
 * ChildGrowthMeasurementService#deleteGrowthList javadoc).
 */
@RestController
public class ChildGrowthController {

	@Autowired
	private ChildGrowthMeasurementService childGrowthMeasurementService;

	@PostMapping("/savechildgrowth")
	public ResponseEntity<AbstractResponse> saveChildGrowths(@RequestBody ChildMeasurementRequestModel cmReqModel) {
		cmReqModel.setHospitalId(SecurityUtils.resolveHospitalId(cmReqModel.getHospitalId()));
		cmReqModel.setUserId(SecurityUtils.resolveUserId(cmReqModel.getUserId()));
		return ResponseEntity.status(HttpStatus.OK).body(childGrowthMeasurementService.saveGrowthInfo(cmReqModel));
	}

	@PostMapping("/getchildgrowthlist")
	public ResponseEntity<AbstractResponse> getChildGrowths(@RequestBody ChildMeasurementRequestModel cmReqModel) {
		cmReqModel.setHospitalId(SecurityUtils.resolveHospitalId(cmReqModel.getHospitalId()));
		return ResponseEntity.status(HttpStatus.OK).body(childGrowthMeasurementService.getGrowthList(cmReqModel));
	}

	@PostMapping("/deletechildgrowth")
	public ResponseEntity<AbstractResponse> deleteChildGrowths(@RequestBody MeasurementList msmtList) {
		return ResponseEntity.status(HttpStatus.OK).body(childGrowthMeasurementService.deleteGrowthList(msmtList));
	}
}
