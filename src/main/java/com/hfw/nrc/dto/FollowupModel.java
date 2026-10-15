package com.hfw.nrc.dto;

/** Ported from nrcapp.model.FollowupModel - request body for /getfollowupduebydate. */
public class FollowupModel {

	private Integer userId;
	private String date;

	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }

	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
}
