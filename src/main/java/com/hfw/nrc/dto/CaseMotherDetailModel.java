package com.hfw.nrc.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Ported from nrcapp.model.CaseMotherDetailModel. */
public class CaseMotherDetailModel {

	private Integer breastFeedingAtPresent;
	private Integer drugsPrescribed;
	private String drugsPrescribedDetails;
	private String lastMenstruationDate;
	private Integer medicalProblem;
	private String medicalProblemDetails;
	private Integer noOfChildren;
	private Integer noOfMealsPerDay;
	private Integer pregnantAtPresent;
	private BigDecimal numberOfPregnancyMonths;
	private String presentDietaryChoice;
	private Integer registeredForAnc;
	private Integer riskCategoryId;

	public Integer getBreastFeedingAtPresent() { return breastFeedingAtPresent; }
	public void setBreastFeedingAtPresent(Integer breastFeedingAtPresent) { this.breastFeedingAtPresent = breastFeedingAtPresent; }

	public Integer getDrugsPrescribed() { return drugsPrescribed; }
	public void setDrugsPrescribed(Integer drugsPrescribed) { this.drugsPrescribed = drugsPrescribed; }

	public String getDrugsPrescribedDetails() { return drugsPrescribedDetails; }
	public void setDrugsPrescribedDetails(String drugsPrescribedDetails) { this.drugsPrescribedDetails = drugsPrescribedDetails; }

	public String getLastMenstruationDate() { return lastMenstruationDate; }
	public void setLastMenstruationDate(String lastMenstruationDate) { this.lastMenstruationDate = lastMenstruationDate; }

	public Integer getMedicalProblem() { return medicalProblem; }
	public void setMedicalProblem(Integer medicalProblem) { this.medicalProblem = medicalProblem; }

	public String getMedicalProblemDetails() { return medicalProblemDetails; }
	public void setMedicalProblemDetails(String medicalProblemDetails) { this.medicalProblemDetails = medicalProblemDetails; }

	public Integer getNoOfChildren() { return noOfChildren; }
	public void setNoOfChildren(Integer noOfChildren) { this.noOfChildren = noOfChildren; }

	public Integer getNoOfMealsPerDay() { return noOfMealsPerDay; }
	public void setNoOfMealsPerDay(Integer noOfMealsPerDay) { this.noOfMealsPerDay = noOfMealsPerDay; }

	public Integer getPregnantAtPresent() { return pregnantAtPresent; }
	public void setPregnantAtPresent(Integer pregnantAtPresent) { this.pregnantAtPresent = pregnantAtPresent; }

	public BigDecimal getNumberOfPregnancyMonths() { return numberOfPregnancyMonths; }
	public void setNumberOfPregnancyMonths(BigDecimal numberOfPregnancyMonths) { this.numberOfPregnancyMonths = numberOfPregnancyMonths; }

	public String getPresentDietaryChoice() { return presentDietaryChoice; }
	public void setPresentDietaryChoice(String presentDietaryChoice) { this.presentDietaryChoice = presentDietaryChoice; }

	public Integer getRegisteredForAnc() { return registeredForAnc; }
	public void setRegisteredForAnc(Integer registeredForAnc) { this.registeredForAnc = registeredForAnc; }

	public Integer getRiskCategoryId() { return riskCategoryId; }
	public void setRiskCategoryId(Integer riskCategoryId) { this.riskCategoryId = riskCategoryId; }

	/**
	 * Ported from the source's reflection-based checkNull(): true when every
	 * field is null (the sub-record was left entirely empty by the client and
	 * should be skipped rather than saved as a blank row).
	 */
	@JsonIgnore
	public boolean isAllFieldsNull() {
		return breastFeedingAtPresent == null && drugsPrescribed == null && drugsPrescribedDetails == null
				&& lastMenstruationDate == null && medicalProblem == null && medicalProblemDetails == null
				&& noOfChildren == null && noOfMealsPerDay == null && pregnantAtPresent == null
				&& numberOfPregnancyMonths == null && presentDietaryChoice == null && registeredForAnc == null
				&& riskCategoryId == null;
	}
}
