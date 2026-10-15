package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.FollowupResponseModel. */
public class FollowupResponseModel {

	private Integer caseId;
	private String followupDate;
	private BigDecimal heightCm;
	private BigDecimal weightKg;
	private BigDecimal standardDev;
	private String remarks;
	private Integer followupId;

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public String getFollowupDate() { return followupDate; }
	public void setFollowupDate(String followupDate) { this.followupDate = followupDate; }

	public BigDecimal getHeightCm() { return heightCm; }
	public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

	public BigDecimal getStandardDev() { return standardDev; }
	public void setStandardDev(BigDecimal standardDev) { this.standardDev = standardDev; }

	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = remarks; }

	public Integer getFollowupId() { return followupId; }
	public void setFollowupId(Integer followupId) { this.followupId = followupId; }
}
