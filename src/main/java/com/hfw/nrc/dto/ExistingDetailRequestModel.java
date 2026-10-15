package com.hfw.nrc.dto;

/** Ported from nrcapp.model.ExistingDetailRequestModel. */
public class ExistingDetailRequestModel {

	private Integer caseId;
	private Integer hospitalId;

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
}
