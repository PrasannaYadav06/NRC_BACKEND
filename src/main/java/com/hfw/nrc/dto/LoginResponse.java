package com.hfw.nrc.dto;

/**
 * Small, consistent response DTO for the one /login endpoint - unlike
 * KABHI's raw Map<String,Object> return, which its own codebase already
 * flagged as inconsistent with having no ApiResponse envelope anywhere.
 */
public class LoginResponse {

	private String token;
	private String username;
	private String role;
	private Integer hospitalId;
	// The JWT itself already carries this as a claim, but the merged
	// Angular frontend's ~20 patient-module components read
	// currentUser.nrcUId straight out of localStorage rather than decoding
	// the token - so it's surfaced directly here too, not just in the JWT.
	private Integer userId;
	private boolean mustChangePassword;

	public LoginResponse(String token, String username, String role, Integer hospitalId, Integer userId, boolean mustChangePassword) {
		this.token = token;
		this.username = username;
		this.role = role;
		this.hospitalId = hospitalId;
		this.userId = userId;
		this.mustChangePassword = mustChangePassword;
	}

	public String getToken() {
		return token;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public Integer getHospitalId() {
		return hospitalId;
	}

	public Integer getUserId() {
		return userId;
	}

	public boolean isMustChangePassword() {
		return mustChangePassword;
	}
}
