package com.hfw.nrc.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.util.CaptchaService;

/** Backs the login page's image captcha - see CaptchaService for the full rationale. */
@RestController
public class CaptchaController {

	@Autowired
	private CaptchaService captchaService;

	@GetMapping("/getCaptcha")
	public Map<String, String> getCaptcha() {
		CaptchaService.Captcha captcha = captchaService.generate();
		Map<String, String> response = new HashMap<>();
		response.put("captchaId", captcha.captchaId);
		response.put("image", captcha.imageDataUrl);
		if (captcha.debugAnswer != null) {
			response.put("debugAnswer", captcha.debugAnswer);
		}
		return response;
	}
}
