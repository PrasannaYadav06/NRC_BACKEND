package com.hfw.nrc.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Consolidated from nrcadmin.entity.ChildGrowth + nrcapp.entity.ChildGrowth (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "child_growth", schema = "nr")
public class ChildGrowth {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rec_id")
	private int recId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "height_cm")
	private BigDecimal heightCm;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Temporal(TemporalType.DATE)
	@Column(name = "measurement_date")
	private Date measurementDate;

	@Column(name = "recorded_at_admission")
	private Boolean recordedAtAdmission;

	@Column(name = "recorded_at_discharge")
	private Boolean recordedAtDischarge;

	@Column(name = "weight_kg")
	private BigDecimal weightKg;

	@Column(name = "diet")
	private String diet;

	@Column(name = "daily_weight_comment")
	private String comments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id")
	private CaseDetail caseDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "followup_id")
	private Followup followup;


	public int getRecId() {
		return recId;
	}

	public void setRecId(int recId) {
		this.recId = recId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public BigDecimal getHeightCm() {
		return heightCm;
	}

	public void setHeightCm(BigDecimal heightCm) {
		this.heightCm = heightCm;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public Date getMeasurementDate() {
		return measurementDate;
	}

	public void setMeasurementDate(Date measurementDate) {
		this.measurementDate = measurementDate;
	}

	public Boolean getRecordedAtAdmission() {
		return recordedAtAdmission;
	}

	public void setRecordedAtAdmission(Boolean recordedAtAdmission) {
		this.recordedAtAdmission = recordedAtAdmission;
	}

	public Boolean getRecordedAtDischarge() {
		return recordedAtDischarge;
	}

	public void setRecordedAtDischarge(Boolean recordedAtDischarge) {
		this.recordedAtDischarge = recordedAtDischarge;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(BigDecimal weightKg) {
		this.weightKg = weightKg;
	}

	public String getDiet() {
		return diet;
	}

	public void setDiet(String diet) {
		this.diet = diet;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public CaseDetail getCaseDetail() {
		return caseDetail;
	}

	public void setCaseDetail(CaseDetail caseDetail) {
		this.caseDetail = caseDetail;
	}

	public NrcUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(NrcUser createdBy) {
		this.createdBy = createdBy;
	}

	public NrcUser getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(NrcUser lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public Followup getFollowup() {
		return followup;
	}

	public void setFollowup(Followup followup) {
		this.followup = followup;
	}
}
