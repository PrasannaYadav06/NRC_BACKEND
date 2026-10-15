package com.hfw.nrc.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Stage 5 - one row of the Follow-up Records table, exact column set per spec. */
public class FollowupListDto {

	private int caseId;
	private Integer hId;
	private Long uhId;
	private Integer samNo;
	private String childName;
	private String childGender;
	private String contactNo;
	private String dischargeDate;
	private String lastFollowupDate;
	/**
	 * The 4 scheduled follow-up checkpoints (see FollowupEventService
	 * #generateSchedule), each flagged done/pending so the frontend can color
	 * the "Next Follow-up No's"/"Next Follow-up Dates" columns green/red -
	 * "done" means a VISIT has actually been recorded for that ordinal
	 * checkpoint (followupVisitCount &gt;= this item's seq), not just that
	 * the scheduled date has passed.
	 */
	private List<ScheduleItemDto> followupSchedule;
	private String formStatus;
	private String statusLabel;
	/** Count of "VISIT" rows in nr.nrc_followup for this case so far - drives the blinking green "N-Followup Done" badge next to statusLabel once at least one visit is recorded. */
	private Integer followupVisitCount;

	@JsonProperty("caseId")
	public int getCaseId() { return caseId; }
	@JsonProperty("caseId")
	public void setCaseId(int caseId) { this.caseId = caseId; }
	@JsonProperty("hId")
	public Integer getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(Integer hId) { this.hId = hId; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public Integer getSamNo() { return samNo; }
	public void setSamNo(Integer samNo) { this.samNo = samNo; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public String getChildGender() { return childGender; }
	public void setChildGender(String childGender) { this.childGender = childGender; }
	public String getContactNo() { return contactNo; }
	public void setContactNo(String contactNo) { this.contactNo = contactNo; }
	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
	public String getLastFollowupDate() { return lastFollowupDate; }
	public void setLastFollowupDate(String lastFollowupDate) { this.lastFollowupDate = lastFollowupDate; }
	public List<ScheduleItemDto> getFollowupSchedule() { return followupSchedule; }
	public void setFollowupSchedule(List<ScheduleItemDto> followupSchedule) { this.followupSchedule = followupSchedule; }
	public String getFormStatus() { return formStatus; }
	public void setFormStatus(String formStatus) { this.formStatus = formStatus; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
	public Integer getFollowupVisitCount() { return followupVisitCount; }
	public void setFollowupVisitCount(Integer followupVisitCount) { this.followupVisitCount = followupVisitCount; }

	public static class ScheduleItemDto {
		private int seq;
		private String date;
		private boolean completed;

		public int getSeq() { return seq; }
		public void setSeq(int seq) { this.seq = seq; }
		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
		public boolean isCompleted() { return completed; }
		public void setCompleted(boolean completed) { this.completed = completed; }
	}
}
