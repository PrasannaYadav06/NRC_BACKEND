package com.hfw.nrc.dto;

/** Ported from nrcapp.model.ChildDetailRequestModel. */
public class ChildDetailRequestModel {

	private Integer hospitalId;
	private Integer caseId;

	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }
}
