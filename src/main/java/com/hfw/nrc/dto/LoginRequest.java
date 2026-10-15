package com.hfw.nrc.dto;

public class LoginRequest {

	private String username;
	private String password;
	private String captchaId;
	private String captchaAnswer;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCaptchaId() { return captchaId; }
	public void setCaptchaId(String captchaId) { this.captchaId = captchaId; }

	public String getCaptchaAnswer() { return captchaAnswer; }
	public void setCaptchaAnswer(String captchaAnswer) { this.captchaAnswer = captchaAnswer; }
}
