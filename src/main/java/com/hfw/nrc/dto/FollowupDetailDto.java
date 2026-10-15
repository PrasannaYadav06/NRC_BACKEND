package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

/** Stage 5 - "1st followup / 2nd followup / ... up to latest" View popup, per spec. */
public class FollowupDetailDto {

	private String childName;
	private Long uhId;
	private String samNo;
	private String rchId;
	private String gender;
	private List<VisitDto> visits;

	public static class VisitDto {
		private int seq;
		private String followupDate;
		private String placeId;
		private String statusId;
		private BigDecimal weightKg;
		private BigDecimal heightCm;
		private BigDecimal followupSd;
		private String doneBy;
		private String remarks;

		public int getSeq() { return seq; }
		public void setSeq(int seq) { this.seq = seq; }
		public String getFollowupDate() { return followupDate; }
		public void setFollowupDate(String followupDate) { this.followupDate = followupDate; }
		public String getPlaceId() { return placeId; }
		public void setPlaceId(String placeId) { this.placeId = placeId; }
		public String getStatusId() { return statusId; }
		public void setStatusId(String statusId) { this.statusId = statusId; }
		public BigDecimal getWeightKg() { return weightKg; }
		public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
		public BigDecimal getHeightCm() { return heightCm; }
		public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
		public BigDecimal getFollowupSd() { return followupSd; }
		public void setFollowupSd(BigDecimal followupSd) { this.followupSd = followupSd; }
		public String getDoneBy() { return doneBy; }
		public void setDoneBy(String doneBy) { this.doneBy = doneBy; }
		public String getRemarks() { return remarks; }
		public void setRemarks(String remarks) { this.remarks = remarks; }
	}

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public String getSamNo() { return samNo; }
	public void setSamNo(String samNo) { this.samNo = samNo; }
	public String getRchId() { return rchId; }
	public void setRchId(String rchId) { this.rchId = rchId; }
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
	public List<VisitDto> getVisits() { return visits; }
	public void setVisits(List<VisitDto> visits) { this.visits = visits; }
}
