package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.DischargeChildModel. */
public class DischargeChildModel {

	private BigDecimal avgWeightGainKg;
	private String clinicalImpression;
	private String dietaryAdvice;
	private String medicalAdvice;
	private Integer outcomeId;
	private String treatmentGiven;

	public BigDecimal getAvgWeightGainKg() { return avgWeightGainKg; }
	public void setAvgWeightGainKg(BigDecimal avgWeightGainKg) { this.avgWeightGainKg = avgWeightGainKg; }

	public String getClinicalImpression() { return clinicalImpression; }
	public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }

	public String getDietaryAdvice() { return dietaryAdvice; }
	public void setDietaryAdvice(String dietaryAdvice) { this.dietaryAdvice = dietaryAdvice; }

	public String getMedicalAdvice() { return medicalAdvice; }
	public void setMedicalAdvice(String medicalAdvice) { this.medicalAdvice = medicalAdvice; }

	public Integer getOutcomeId() { return outcomeId; }
	public void setOutcomeId(Integer outcomeId) { this.outcomeId = outcomeId; }

	public String getTreatmentGiven() { return treatmentGiven; }
	public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }
}
