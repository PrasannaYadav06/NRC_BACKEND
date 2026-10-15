package com.hfw.nrc.dto;

/** Ported from nrcapp.model.ViewCaseRequestModel. */
public class ViewCaseRequestModel {

	private Integer caseId;
	private Integer loginUserId;

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }
}
