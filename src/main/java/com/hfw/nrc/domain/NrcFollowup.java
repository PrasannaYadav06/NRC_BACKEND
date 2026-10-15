package com.hfw.nrc.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stage 5 - one row per follow-up EVENT (either a completed VISIT or a
 * scheduled due-date SCHEDULE entry), FK'd to NrcAdmission.caseId.
 * Consolidates nr.followup + nr.followup_schedule - see
 * db/migration/V11__create_nrc_followup.sql for the full rationale
 * (why these aren't merged row-by-row).
 */
@Entity
@Table(name = "nrc_followup", schema = "nr")
public class NrcFollowup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "followup_rec_id")
	private int followupRecId;

	@Column(name = "case_id")
	private int caseId;

	@Column(name = "record_type")
	private String recordType; // "VISIT" or "SCHEDULE"

	@Column(name = "followup_seq")
	private Integer followupSeq;

	@Column(name = "followup_date")
	private LocalDate followupDate;

	@Column(name = "place_id")
	private Integer placeId;

	@Column(name = "status_id")
	private Integer statusId;

	@Column(name = "followup_sd")
	private BigDecimal followupSd;

	@Column(name = "done_by")
	private String doneBy;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "weight_kg")
	private BigDecimal weightKg;

	@Column(name = "height_cm")
	private BigDecimal heightCm;

	@Column(name = "form_status_at_event")
	private BigDecimal formStatusAtEvent;

	@Column(name = "legacy_followup_id")
	private Integer legacyFollowupId;

	@Column(name = "legacy_sched_id")
	private Integer legacySchedId;

	@Column(name = "legacy_case_id")
	private Integer legacyCaseId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "last_updated_by")
	private Integer lastUpdatedBy;

	public int getFollowupRecId() { return followupRecId; }
	public void setFollowupRecId(int followupRecId) { this.followupRecId = followupRecId; }
	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }
	public String getRecordType() { return recordType; }
	public void setRecordType(String recordType) { this.recordType = recordType; }
	public Integer getFollowupSeq() { return followupSeq; }
	public void setFollowupSeq(Integer followupSeq) { this.followupSeq = followupSeq; }
	public LocalDate getFollowupDate() { return followupDate; }
	public void setFollowupDate(LocalDate followupDate) { this.followupDate = followupDate; }
	public Integer getPlaceId() { return placeId; }
	public void setPlaceId(Integer placeId) { this.placeId = placeId; }
	public Integer getStatusId() { return statusId; }
	public void setStatusId(Integer statusId) { this.statusId = statusId; }
	public BigDecimal getFollowupSd() { return followupSd; }
	public void setFollowupSd(BigDecimal followupSd) { this.followupSd = followupSd; }
	public String getDoneBy() { return doneBy; }
	public void setDoneBy(String doneBy) { this.doneBy = doneBy; }
	public String getRemarks() { return remarks; }
	public void setRemarks(String remarks) { this.remarks = remarks; }
	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }
	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
	public BigDecimal getHeightCm() { return heightCm; }
	public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
	public BigDecimal getFormStatusAtEvent() { return formStatusAtEvent; }
	public void setFormStatusAtEvent(BigDecimal formStatusAtEvent) { this.formStatusAtEvent = formStatusAtEvent; }
	public Integer getLegacyFollowupId() { return legacyFollowupId; }
	public void setLegacyFollowupId(Integer legacyFollowupId) { this.legacyFollowupId = legacyFollowupId; }
	public Integer getLegacySchedId() { return legacySchedId; }
	public void setLegacySchedId(Integer legacySchedId) { this.legacySchedId = legacySchedId; }
	public Integer getLegacyCaseId() { return legacyCaseId; }
	public void setLegacyCaseId(Integer legacyCaseId) { this.legacyCaseId = legacyCaseId; }
	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }
	public Integer getCreatedBy() { return createdBy; }
	public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
	public Timestamp getLastUpdatedOn() { return lastUpdatedOn; }
	public void setLastUpdatedOn(Timestamp lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }
	public Integer getLastUpdatedBy() { return lastUpdatedBy; }
	public void setLastUpdatedBy(Integer lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
