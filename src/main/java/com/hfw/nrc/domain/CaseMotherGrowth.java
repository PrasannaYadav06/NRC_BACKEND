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
 * Consolidated from nrcadmin.entity.CaseMotherGrowth + nrcapp.entity.CaseMotherGrowth (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "case_mother_growth", schema = "nr")
public class CaseMotherGrowth {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rec_id")
	private int recId;

	@Column(name = "blood_pressure_dia")
	private BigDecimal bloodPressureDia;

	@Column(name = "blood_pressure_sys")
	private BigDecimal bloodPressureSys;

	@Column(name = "blood_sugar_fasting")
	private BigDecimal bloodSugarFasting;

	@Column(name = "blood_sugar_random")
	private BigDecimal bloodSugarRandom;

	private BigDecimal bmi;

	@Column(name = "created_on")
	private Timestamp createdOn;

	private Integer gravida;

	@Column(name = "height_cm")
	private BigDecimal heightCm;

	private BigDecimal hemoglobin;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Temporal(TemporalType.DATE)
	@Column(name = "measurement_date")
	private Date measurementDate;

	@Column(name = "muac_cm")
	private BigDecimal muacCm;

	@Column(name = "neck_circumference_cm")
	private BigDecimal neckCircumferenceCm;

	private Integer para;

	@Column(name = "advice")
	private String advice;

	@Column(name = "recorded_at_admission")
	private Boolean recordedAtAdmission;

	@Column(name = "recorded_at_discharge")
	private Boolean recordedAtDischarge;

	@Column(name = "weight_kg")
	private BigDecimal weightKg;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id")
	private CaseDetail caseDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getRecId() {
		return recId;
	}

	public void setRecId(int recId) {
		this.recId = recId;
	}

	public BigDecimal getBloodPressureDia() {
		return bloodPressureDia;
	}

	public void setBloodPressureDia(BigDecimal bloodPressureDia) {
		this.bloodPressureDia = bloodPressureDia;
	}

	public BigDecimal getBloodPressureSys() {
		return bloodPressureSys;
	}

	public void setBloodPressureSys(BigDecimal bloodPressureSys) {
		this.bloodPressureSys = bloodPressureSys;
	}

	public BigDecimal getBloodSugarFasting() {
		return bloodSugarFasting;
	}

	public void setBloodSugarFasting(BigDecimal bloodSugarFasting) {
		this.bloodSugarFasting = bloodSugarFasting;
	}

	public BigDecimal getBloodSugarRandom() {
		return bloodSugarRandom;
	}

	public void setBloodSugarRandom(BigDecimal bloodSugarRandom) {
		this.bloodSugarRandom = bloodSugarRandom;
	}

	public BigDecimal getBmi() {
		return bmi;
	}

	public void setBmi(BigDecimal bmi) {
		this.bmi = bmi;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Integer getGravida() {
		return gravida;
	}

	public void setGravida(Integer gravida) {
		this.gravida = gravida;
	}

	public BigDecimal getHeightCm() {
		return heightCm;
	}

	public void setHeightCm(BigDecimal heightCm) {
		this.heightCm = heightCm;
	}

	public BigDecimal getHemoglobin() {
		return hemoglobin;
	}

	public void setHemoglobin(BigDecimal hemoglobin) {
		this.hemoglobin = hemoglobin;
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

	public BigDecimal getMuacCm() {
		return muacCm;
	}

	public void setMuacCm(BigDecimal muacCm) {
		this.muacCm = muacCm;
	}

	public BigDecimal getNeckCircumferenceCm() {
		return neckCircumferenceCm;
	}

	public void setNeckCircumferenceCm(BigDecimal neckCircumferenceCm) {
		this.neckCircumferenceCm = neckCircumferenceCm;
	}

	public Integer getPara() {
		return para;
	}

	public void setPara(Integer para) {
		this.para = para;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
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
