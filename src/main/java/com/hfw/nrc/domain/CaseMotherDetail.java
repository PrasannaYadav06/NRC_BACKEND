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
 * Consolidated from nrcadmin.entity.CaseMotherDetail + nrcapp.entity.CaseMotherDetail (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "case_mother_details", schema = "nr")
public class CaseMotherDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "case_m_details_id")
	private int caseMDetailsId;

	@Column(name = "breast_feeding_at_present")
	private Boolean breastFeedingAtPresent;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "drugs_prescribed")
	private Boolean drugsPrescribed;

	@Column(name = "drugs_prescribed_details")
	private String drugsPrescribedDetails;

	@Temporal(TemporalType.DATE)
	@Column(name = "last_menstruation_date")
	private Date lastMenstruationDate;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "medical_problem")
	private Boolean medicalProblem;

	@Column(name = "medical_problem_details")
	private String medicalProblemDetails;

	@Column(name = "no_of_children")
	private Integer noOfChildren;

	@Column(name = "no_of_meals_per_day")
	private Integer noOfMealsPerDay;

	@Column(name = "number_of_pregnancy_months")
	private BigDecimal numberOfPregnancyMonths;

	@Column(name = "pregnant_at_present")
	private Boolean pregnantAtPresent;

	@Column(name = "present_dietary_choice")
	private String presentDietaryChoice;

	@Column(name = "registered_for_anc")
	private Boolean registeredForAnc;

	@Column(name = "risk_category_id")
	private Integer riskCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id")
	private CaseDetail caseDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getCaseMDetailsId() {
		return caseMDetailsId;
	}

	public void setCaseMDetailsId(int caseMDetailsId) {
		this.caseMDetailsId = caseMDetailsId;
	}

	public Boolean getBreastFeedingAtPresent() {
		return breastFeedingAtPresent;
	}

	public void setBreastFeedingAtPresent(Boolean breastFeedingAtPresent) {
		this.breastFeedingAtPresent = breastFeedingAtPresent;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Boolean getDrugsPrescribed() {
		return drugsPrescribed;
	}

	public void setDrugsPrescribed(Boolean drugsPrescribed) {
		this.drugsPrescribed = drugsPrescribed;
	}

	public String getDrugsPrescribedDetails() {
		return drugsPrescribedDetails;
	}

	public void setDrugsPrescribedDetails(String drugsPrescribedDetails) {
		this.drugsPrescribedDetails = drugsPrescribedDetails;
	}

	public Date getLastMenstruationDate() {
		return lastMenstruationDate;
	}

	public void setLastMenstruationDate(Date lastMenstruationDate) {
		this.lastMenstruationDate = lastMenstruationDate;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public Boolean getMedicalProblem() {
		return medicalProblem;
	}

	public void setMedicalProblem(Boolean medicalProblem) {
		this.medicalProblem = medicalProblem;
	}

	public String getMedicalProblemDetails() {
		return medicalProblemDetails;
	}

	public void setMedicalProblemDetails(String medicalProblemDetails) {
		this.medicalProblemDetails = medicalProblemDetails;
	}

	public Integer getNoOfChildren() {
		return noOfChildren;
	}

	public void setNoOfChildren(Integer noOfChildren) {
		this.noOfChildren = noOfChildren;
	}

	public Integer getNoOfMealsPerDay() {
		return noOfMealsPerDay;
	}

	public void setNoOfMealsPerDay(Integer noOfMealsPerDay) {
		this.noOfMealsPerDay = noOfMealsPerDay;
	}

	public BigDecimal getNumberOfPregnancyMonths() {
		return numberOfPregnancyMonths;
	}

	public void setNumberOfPregnancyMonths(BigDecimal numberOfPregnancyMonths) {
		this.numberOfPregnancyMonths = numberOfPregnancyMonths;
	}

	public Boolean getPregnantAtPresent() {
		return pregnantAtPresent;
	}

	public void setPregnantAtPresent(Boolean pregnantAtPresent) {
		this.pregnantAtPresent = pregnantAtPresent;
	}

	public String getPresentDietaryChoice() {
		return presentDietaryChoice;
	}

	public void setPresentDietaryChoice(String presentDietaryChoice) {
		this.presentDietaryChoice = presentDietaryChoice;
	}

	public Boolean getRegisteredForAnc() {
		return registeredForAnc;
	}

	public void setRegisteredForAnc(Boolean registeredForAnc) {
		this.registeredForAnc = registeredForAnc;
	}

	public Integer getRiskCategoryId() {
		return riskCategoryId;
	}

	public void setRiskCategoryId(Integer riskCategoryId) {
		this.riskCategoryId = riskCategoryId;
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
