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
 * Consolidated from nrcadmin.entity.CaseDetail + nrcapp.entity.CaseDetail (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "case_details", schema = "nr")
public class CaseDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "case_id")
	private int caseId;

	@Column(name = "admission_date")
	private Timestamp admissionDate;

	@Column(name = "admission_type_id")
	private int admissionTypeId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "discharge_date")
	private Timestamp dischargeDate;

	@Column(name = "fiscal_year")
	private String fiscalYear;

	@Column(name = "ip_num")
	private Long ipNum;

	@Column(name = "uh_id")
	private Long uhId;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "sam_num")
	private int samNum;

	@Column(name = "target_weight")
	private BigDecimal targetWeight;

	@Column(name = "admission_sd")
	private BigDecimal admissionSd;

	@Column(name = "discharge_sd")
	private BigDecimal dischargeSd;

	@Column(name = "target_weight_achieved")
	private Boolean targetWeightAchieved;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_id")
	private ChildDemography childDemography;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "addmited_by_doctor")
	private Doctor addmittedByDoctor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discharged_by_doctor")
	private Doctor dischargedByDoctor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "h_id")
	private Hospital hospital;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mother_id")
	private MotherDemogrphy motherDemogrphy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getCaseId() {
		return caseId;
	}

	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	public Timestamp getAdmissionDate() {
		return admissionDate;
	}

	public void setAdmissionDate(Timestamp admissionDate) {
		this.admissionDate = admissionDate;
	}

	public int getAdmissionTypeId() {
		return admissionTypeId;
	}

	public void setAdmissionTypeId(int admissionTypeId) {
		this.admissionTypeId = admissionTypeId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getDischargeDate() {
		return dischargeDate;
	}

	public void setDischargeDate(Timestamp dischargeDate) {
		this.dischargeDate = dischargeDate;
	}

	public String getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(String fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

	public Long getIpNum() {
		return ipNum;
	}

	public void setIpNum(Long ipNum) {
		this.ipNum = ipNum;
	}

	public Long getUhId() {
		return uhId;
	}

	public void setUhId(Long uhId) {
		this.uhId = uhId;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public int getSamNum() {
		return samNum;
	}

	public void setSamNum(int samNum) {
		this.samNum = samNum;
	}

	public BigDecimal getTargetWeight() {
		return targetWeight;
	}

	public void setTargetWeight(BigDecimal targetWeight) {
		this.targetWeight = targetWeight;
	}

	public BigDecimal getAdmissionSd() {
		return admissionSd;
	}

	public void setAdmissionSd(BigDecimal admissionSd) {
		this.admissionSd = admissionSd;
	}

	public BigDecimal getDischargeSd() {
		return dischargeSd;
	}

	public void setDischargeSd(BigDecimal dischargeSd) {
		this.dischargeSd = dischargeSd;
	}

	public Boolean getTargetWeightAchieved() {
		return targetWeightAchieved;
	}

	public void setTargetWeightAchieved(Boolean targetWeightAchieved) {
		this.targetWeightAchieved = targetWeightAchieved;
	}

	public ChildDemography getChildDemography() {
		return childDemography;
	}

	public void setChildDemography(ChildDemography childDemography) {
		this.childDemography = childDemography;
	}

	public Doctor getAddmittedByDoctor() {
		return addmittedByDoctor;
	}

	public void setAddmittedByDoctor(Doctor addmittedByDoctor) {
		this.addmittedByDoctor = addmittedByDoctor;
	}

	public Doctor getDischargedByDoctor() {
		return dischargedByDoctor;
	}

	public void setDischargedByDoctor(Doctor dischargedByDoctor) {
		this.dischargedByDoctor = dischargedByDoctor;
	}

	public Hospital getHospital() {
		return hospital;
	}

	public void setHospital(Hospital hospital) {
		this.hospital = hospital;
	}

	public MotherDemogrphy getMotherDemogrphy() {
		return motherDemogrphy;
	}

	public void setMotherDemogrphy(MotherDemogrphy motherDemogrphy) {
		this.motherDemogrphy = motherDemogrphy;
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
