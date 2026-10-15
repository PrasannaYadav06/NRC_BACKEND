package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.FollowupRequestModel - request body for /savefollowup. */
public class FollowupRequestModel {

	private Integer caseId;
	private Integer userId;
	private String doneBy;
	private String followupDate;
	private Integer placeId;
	private Integer statusId;
	private BigDecimal heightCm;
	private BigDecimal weightKg;
	private BigDecimal standardDev;
	private String remarks;

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }

	public String getDoneBy() { return doneBy; }
	public void setDoneBy(String doneBy) { this.doneBy = doneBy; }

	public String getFollowupDate() { return followupDate; }
	public void setFollowupDate(String followupDate) { this.followupDate = followupDate; }

	public Integer getPlaceId() { return placeId; }
	public void setPlaceId(Integer placeId) { this.placeId = placeId; }

	public Integer getStatusId() { return statusId; }
	public void setStatusId(Integer statusId) { this.statusId = statusId; }

	public BigDecimal getHeightCm() { return heightCm; }
	public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

	public BigDecimal getStandardDev() { return standardDev; }
	public void setStandardDev(BigDecimal standardDev) { this.standardDev = standardDev; }

	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
}
