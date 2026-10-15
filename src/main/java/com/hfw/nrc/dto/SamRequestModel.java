package com.hfw.nrc.dto;

/** Ported from nrcapp.model.SamRequestModel. */
public class SamRequestModel {

	private Integer loginUserId;
	private Integer samNumber;

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }
}
