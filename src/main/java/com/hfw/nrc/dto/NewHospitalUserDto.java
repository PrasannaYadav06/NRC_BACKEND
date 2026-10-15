package com.hfw.nrc.dto;

/**
 * Ported from nrcadmin.common.UserDto - the "user" sub-object of the
 * /hospital/save request body (the login account created for the new
 * hospital). Only userId/password, matching the source exactly - the
 * frontend also sends a stray "uid":0 the source never bound either.
 */
public class NewHospitalUserDto {

	private String userId;
	private String password;

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}
