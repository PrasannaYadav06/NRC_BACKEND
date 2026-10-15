package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Stage 4 - single discharge event, backing the View popup / interstitial / Edit prefill. */
public class DischargeDetailDto {

	private int dischargeRecId;
	private int caseId;
	private String childName;
	private Long uhId;
	private Long ipNum;
	private Integer samNum;
	private String contactNo;

	private String dischargeDate;
	private BigDecimal dischargeSd;
	private BigDecimal dischargeWeight;
	private BigDecimal dischargeHeight;
	private String minWeightDate;
	private BigDecimal minWeight;

	private BigDecimal avgWeightGainKg;
	private String clinicalImpression;
	private Integer outcomeId;
	private String treatmentGiven;
	private String medicalAdvice;
	private String dietaryAdvice;

	private Integer medicalRiskId;
	private BigDecimal motherWeightDischarge;
	private BigDecimal motherHeightDischarge;
	private BigDecimal motherMuacDischarge;

	private List<Integer> selectedComorbidIds;
	private String comorbidOthersValue;

	public int getDischargeRecId() { return dischargeRecId; }
	public void setDischargeRecId(int dischargeRecId) { this.dischargeRecId = dischargeRecId; }
	@JsonProperty("caseId")
	public int getCaseId() { return caseId; }
	@JsonProperty("caseId")
	public void setCaseId(int caseId) { this.caseId = caseId; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }
	public Integer getSamNum() { return samNum; }
	public void setSamNum(Integer samNum) { this.samNum = samNum; }
	public String getContactNo() { return contactNo; }
	public void setContactNo(String contactNo) { this.contactNo = contactNo; }
	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
	public BigDecimal getDischargeSd() { return dischargeSd; }
	public void setDischargeSd(BigDecimal dischargeSd) { this.dischargeSd = dischargeSd; }
	public BigDecimal getDischargeWeight() { return dischargeWeight; }
	public void setDischargeWeight(BigDecimal dischargeWeight) { this.dischargeWeight = dischargeWeight; }
	public BigDecimal getDischargeHeight() { return dischargeHeight; }
	public void setDischargeHeight(BigDecimal dischargeHeight) { this.dischargeHeight = dischargeHeight; }
	public String getMinWeightDate() { return minWeightDate; }
	public void setMinWeightDate(String minWeightDate) { this.minWeightDate = minWeightDate; }
	public BigDecimal getMinWeight() { return minWeight; }
	public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }
	public BigDecimal getAvgWeightGainKg() { return avgWeightGainKg; }
	public void setAvgWeightGainKg(BigDecimal avgWeightGainKg) { this.avgWeightGainKg = avgWeightGainKg; }
	public String getClinicalImpression() { return clinicalImpression; }
	public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }
	public Integer getOutcomeId() { return outcomeId; }
	public void setOutcomeId(Integer outcomeId) { this.outcomeId = outcomeId; }
	public String getTreatmentGiven() { return treatmentGiven; }
	public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }
	public String getMedicalAdvice() { return medicalAdvice; }
	public void setMedicalAdvice(String medicalAdvice) { this.medicalAdvice = medicalAdvice; }
	public String getDietaryAdvice() { return dietaryAdvice; }
	public void setDietaryAdvice(String dietaryAdvice) { this.dietaryAdvice = dietaryAdvice; }
	public Integer getMedicalRiskId() { return medicalRiskId; }
	public void setMedicalRiskId(Integer medicalRiskId) { this.medicalRiskId = medicalRiskId; }
	public BigDecimal getMotherWeightDischarge() { return motherWeightDischarge; }
	public void setMotherWeightDischarge(BigDecimal motherWeightDischarge) { this.motherWeightDischarge = motherWeightDischarge; }
	public BigDecimal getMotherHeightDischarge() { return motherHeightDischarge; }
	public void setMotherHeightDischarge(BigDecimal motherHeightDischarge) { this.motherHeightDischarge = motherHeightDischarge; }
	public BigDecimal getMotherMuacDischarge() { return motherMuacDischarge; }
	public void setMotherMuacDischarge(BigDecimal motherMuacDischarge) { this.motherMuacDischarge = motherMuacDischarge; }
	public List<Integer> getSelectedComorbidIds() { return selectedComorbidIds; }
	public void setSelectedComorbidIds(List<Integer> selectedComorbidIds) { this.selectedComorbidIds = selectedComorbidIds; }
	public String getComorbidOthersValue() { return comorbidOthersValue; }
	public void setComorbidOthersValue(String comorbidOthersValue) { this.comorbidOthersValue = comorbidOthersValue; }
}
