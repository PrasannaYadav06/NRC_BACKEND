package com.hfw.nrc.dto;

/** Ported from nrcapp.model.ResourceRequestModel - request body for /getresourceslist. */
public class ResourceRequestModel {

	private Integer month;
	private Integer year;
	private Integer loginUserId;

	public Integer getMonth() { return month; }
	public void setMonth(Integer month) { this.month = month; }

	public Integer getYear() { return year; }
	public void setYear(Integer year) { this.year = year; }

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }
}
