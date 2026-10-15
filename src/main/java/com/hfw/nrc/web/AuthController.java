package com.hfw.nrc.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.ChangePasswordRequestModel;
import com.hfw.nrc.dto.LoginRequest;
import com.hfw.nrc.dto.LoginResponse;
import com.hfw.nrc.service.AuthService;
import com.hfw.nrc.util.SecurityUtils;

/**
 * Replaces nrcadmin's AdminController#loginUser/logoutSession and nrcapp's
 * NRCRestController#loginNRCUser - one JWT-issuing endpoint for both roles.
 * There is no server-side token invalidation on /logout (nothing to
 * invalidate with stateless JWTs - the client discards the token; same as
 * KABHI_BACKEND, which also has no server-side logout) - /logout here exists
 * purely to record the audit event and last_logout_on timestamp.
 */
@RestController
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		return authService.login(request.getUsername(), request.getPassword(),
				request.getCaptchaId(), request.getCaptchaAnswer(), clientIp(httpRequest));
	}

	@PostMapping("/logout")
	public AbstractResponse logout(HttpServletRequest httpRequest) {
		AbstractResponse response = new AbstractResponse();
		var user = SecurityUtils.currentUser();
		authService.logout(user != null ? (int) user.getUserId() : null,
				user != null ? user.getUsername() : null, clientIp(httpRequest));
		response.setErrorCode("0");
		response.setSuccessMessage("Logged out successfully");
		return response;
	}

	@PostMapping("/changepassword")
	public AbstractResponse changePassword(@RequestBody ChangePasswordRequestModel request) {
		return authService.changePassword(request);
	}

	/** X-Forwarded-For first (reverse-proxy deployments), falling back to the raw socket address. */
	private static String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isEmpty()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
