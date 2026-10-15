package com.hfw.nrc.dto;

/** Ported from nrcapp.model.FollowupDueResponseModel. */
public class FollowupDueResponseModel {

	private Integer samNum;
	private Integer followupNum;
	private String childName;
	private String mobileNumber;
	private String dischargeDate;
	private String lastFollowupDate;
	private String nextFollowupDate;
	private Integer caseId;

	public Integer getSamNum() { return samNum; }
	public void setSamNum(Integer samNum) { this.samNum = samNum; }

	public Integer getFollowupNum() { return followupNum; }
	public void setFollowupNum(Integer followupNum) { this.followupNum = followupNum; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }

	public String getLastFollowupDate() { return lastFollowupDate; }
	public void setLastFollowupDate(String lastFollowupDate) { this.lastFollowupDate = lastFollowupDate; }

	public String getNextFollowupDate() { return nextFollowupDate; }
	public void setNextFollowupDate(String nextFollowupDate) { this.nextFollowupDate = nextFollowupDate; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }
}
