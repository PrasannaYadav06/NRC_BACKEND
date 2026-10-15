package com.hfw.nrc.dto;

/** Ported from nrcapp.model.SearchRequestModel. */
public class SearchRequestModel {

	private Long uhId;
	private Long rchId;
	private Integer samNumber;
	private String childName;
	private String motherName;
	private String mobileNumber;
	private Integer loginUserId;
	private Boolean inTrackChild;

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }

	public Boolean getInTrackChild() { return inTrackChild; }
	public void setInTrackChild(Boolean inTrackChild) { this.inTrackChild = inTrackChild; }
}
