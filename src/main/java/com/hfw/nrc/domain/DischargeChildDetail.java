package com.hfw.nrc.domain;

import java.math.BigDecimal;
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

/**
 * Consolidated from nrcadmin.entity.DischargeChildDetail + nrcapp.entity.DischargeChildDetail (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "discharge_child_details", schema = "nr")
public class DischargeChildDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "discharge_id")
	private int dischargeId;

	@Column(name = "avg_weight_gain_kg")
	private BigDecimal avgWeightGainKg;

	@Column(name = "clinical_impression")
	private String clinicalImpression;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "dietary_advice")
	private String dietaryAdvice;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "medical_advice")
	private String medicalAdvice;

	@Column(name = "outcome_id")
	private int outcomeId;

	@Column(name = "treatment_given")
	private String treatmentGiven;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id")
	private CaseDetail caseDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getDischargeId() {
		return dischargeId;
	}

	public void setDischargeId(int dischargeId) {
		this.dischargeId = dischargeId;
	}

	public BigDecimal getAvgWeightGainKg() {
		return avgWeightGainKg;
	}

	public void setAvgWeightGainKg(BigDecimal avgWeightGainKg) {
		this.avgWeightGainKg = avgWeightGainKg;
	}

	public String getClinicalImpression() {
		return clinicalImpression;
	}

	public void setClinicalImpression(String clinicalImpression) {
		this.clinicalImpression = clinicalImpression;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public String getDietaryAdvice() {
		return dietaryAdvice;
	}

	public void setDietaryAdvice(String dietaryAdvice) {
		this.dietaryAdvice = dietaryAdvice;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getMedicalAdvice() {
		return medicalAdvice;
	}

	public void setMedicalAdvice(String medicalAdvice) {
		this.medicalAdvice = medicalAdvice;
	}

	public int getOutcomeId() {
		return outcomeId;
	}

	public void setOutcomeId(int outcomeId) {
		this.outcomeId = outcomeId;
	}

	public String getTreatmentGiven() {
		return treatmentGiven;
	}

	public void setTreatmentGiven(String treatmentGiven) {
		this.treatmentGiven = treatmentGiven;
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
}
