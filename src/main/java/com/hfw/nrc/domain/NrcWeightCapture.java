package com.hfw.nrc.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Daily weight-capture history (decision #2): NrcAdmission always carries the
 * latest weight/date in place (lastMeasuredWeight/lastMeasuredDate); this
 * table is the full event log feeding the In-Patient Care view popup's
 * "1st capture / 2nd capture / ..." list. See
 * db/migration/V6__create_nrc_weight_capture.sql.
 */
@Entity
@Table(name = "nrc_weight_capture", schema = "nr")
public class NrcWeightCapture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "capture_id")
	private int captureId;

	@Column(name = "case_id")
	private int caseId;

	@Column(name = "weight_kg")
	private BigDecimal weightKg;

	@Column(name = "diet")
	private String diet;

	@Column(name = "comment")
	private String comment;

	@Temporal(TemporalType.DATE)
	@Column(name = "capture_date")
	private Date captureDate;

	@Column(name = "legacy_rec_id")
	private Integer legacyRecId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "created_by")
	private Integer createdBy;

	public int getCaptureId() { return captureId; }
	public void setCaptureId(int captureId) { this.captureId = captureId; }

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

	public String getDiet() { return diet; }
	public void setDiet(String diet) { this.diet = diet; }

	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }

	public Date getCaptureDate() { return captureDate; }
	public void setCaptureDate(Date captureDate) { this.captureDate = captureDate; }

	public Integer getLegacyRecId() { return legacyRecId; }
	public void setLegacyRecId(Integer legacyRecId) { this.legacyRecId = legacyRecId; }

	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }

	public Integer getCreatedBy() { return createdBy; }
	public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
}
