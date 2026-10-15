package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stage 4 - create/update payload for the single consolidated Discharge
 * module. Mirrors the shape discharge.component.ts#onSubmitDischarge
 * already builds (see that file's own comment - it was rebuilt field-by-
 * field against the old DischargeRequestModel earlier this project) so the
 * frontend needed no payload-shape rewrite, only a new endpoint target.
 */
public class DischargeSaveRequest {

	private Integer loginUserId;
	private Integer hospitalId;
	private Integer caseId;
	private String dischargeDate;
	private BigDecimal dischargeWeight;
	private BigDecimal dischargeHeight;
	private BigDecimal dischargeSd;
	private String minWeightDate;
	private BigDecimal minWeight;

	private List<DischargeChildItem> dischargeChildDetails;
	private List<ComorbidItem> dischargeComorbidConditions;
	private List<MotherRiskItem> dischargeMotherDetails;
	private List<MotherGrowthItem> dischargeMotherGrowths;

	public static class DischargeChildItem {
		private BigDecimal avgWeightGainKg;
		private String clinicalImpression;
		private String treatmentGiven;
		private String medicalAdvice;
		private String dietaryAdvice;
		private Integer outcomeId;

		public BigDecimal getAvgWeightGainKg() { return avgWeightGainKg; }
		public void setAvgWeightGainKg(BigDecimal avgWeightGainKg) { this.avgWeightGainKg = avgWeightGainKg; }
		public String getClinicalImpression() { return clinicalImpression; }
		public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }
		public String getTreatmentGiven() { return treatmentGiven; }
		public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }
		public String getMedicalAdvice() { return medicalAdvice; }
		public void setMedicalAdvice(String medicalAdvice) { this.medicalAdvice = medicalAdvice; }
		public String getDietaryAdvice() { return dietaryAdvice; }
		public void setDietaryAdvice(String dietaryAdvice) { this.dietaryAdvice = dietaryAdvice; }
		public Integer getOutcomeId() { return outcomeId; }
		public void setOutcomeId(Integer outcomeId) { this.outcomeId = outcomeId; }
	}

	public static class ComorbidItem {
		private Integer comorbidId;
		private String othersValue;
		public Integer getComorbidId() { return comorbidId; }
		public void setComorbidId(Integer comorbidId) { this.comorbidId = comorbidId; }
		public String getOthersValue() { return othersValue; }
		public void setOthersValue(String othersValue) { this.othersValue = othersValue; }
	}

	public static class MotherRiskItem {
		private Integer medicalRiskId;
		public Integer getMedicalRiskId() { return medicalRiskId; }
		public void setMedicalRiskId(Integer medicalRiskId) { this.medicalRiskId = medicalRiskId; }
	}

	public static class MotherGrowthItem {
		private BigDecimal weightKg;
		private BigDecimal heightCm;
		private BigDecimal muacCm;
		public BigDecimal getWeightKg() { return weightKg; }
		public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
		public BigDecimal getHeightCm() { return heightCm; }
		public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
		public BigDecimal getMuacCm() { return muacCm; }
		public void setMuacCm(BigDecimal muacCm) { this.muacCm = muacCm; }
	}

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }
	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }
	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
	public BigDecimal getDischargeWeight() { return dischargeWeight; }
	public void setDischargeWeight(BigDecimal dischargeWeight) { this.dischargeWeight = dischargeWeight; }
	public BigDecimal getDischargeHeight() { return dischargeHeight; }
	public void setDischargeHeight(BigDecimal dischargeHeight) { this.dischargeHeight = dischargeHeight; }
	public BigDecimal getDischargeSd() { return dischargeSd; }
	public void setDischargeSd(BigDecimal dischargeSd) { this.dischargeSd = dischargeSd; }
	public String getMinWeightDate() { return minWeightDate; }
	public void setMinWeightDate(String minWeightDate) { this.minWeightDate = minWeightDate; }
	public BigDecimal getMinWeight() { return minWeight; }
	public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }
	public List<DischargeChildItem> getDischargeChildDetails() { return dischargeChildDetails; }
	public void setDischargeChildDetails(List<DischargeChildItem> dischargeChildDetails) { this.dischargeChildDetails = dischargeChildDetails; }
	public List<ComorbidItem> getDischargeComorbidConditions() { return dischargeComorbidConditions; }
	public void setDischargeComorbidConditions(List<ComorbidItem> dischargeComorbidConditions) { this.dischargeComorbidConditions = dischargeComorbidConditions; }
	public List<MotherRiskItem> getDischargeMotherDetails() { return dischargeMotherDetails; }
	public void setDischargeMotherDetails(List<MotherRiskItem> dischargeMotherDetails) { this.dischargeMotherDetails = dischargeMotherDetails; }
	public List<MotherGrowthItem> getDischargeMotherGrowths() { return dischargeMotherGrowths; }
	public void setDischargeMotherGrowths(List<MotherGrowthItem> dischargeMotherGrowths) { this.dischargeMotherGrowths = dischargeMotherGrowths; }
}
