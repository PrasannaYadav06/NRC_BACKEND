package com.hfw.nrc.dto;

/**
 * Item 13 - the Search Patient page's single-A4-sheet full-history view:
 * admission (which already carries its own weight-capture timeline, see
 * AdmissionDetailDto#weightCaptures) + the latest discharge event (null if
 * never discharged) + every follow-up visit (null visits if none recorded
 * yet). Composed from the three existing view-popup DTOs rather than
 * reinventing their field sets - see NrcAdmissionService#getFullHistory.
 */
public class PatientFullHistoryDto {

	private AdmissionDetailDto admission;
	private DischargeDetailDto discharge;
	private FollowupDetailDto followup;
	/** Drives the page's blinking status banner - "Active" / "Discharged" / "Under Follow-up". */
	private String statusLabel;

	public AdmissionDetailDto getAdmission() { return admission; }
	public void setAdmission(AdmissionDetailDto admission) { this.admission = admission; }
	public DischargeDetailDto getDischarge() { return discharge; }
	public void setDischarge(DischargeDetailDto discharge) { this.discharge = discharge; }
	public FollowupDetailDto getFollowup() { return followup; }
	public void setFollowup(FollowupDetailDto followup) { this.followup = followup; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}
