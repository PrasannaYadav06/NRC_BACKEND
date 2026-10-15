package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.CaseDetailModel. */
public class CaseDetailModel {

	private Long ipNum;
	private String patientName;
	private BigDecimal targetWeight;
	private String admissionDate;
	private String dischargeDate;
	private Integer samNumber;
	private Integer caseId;
	private Boolean dischargeStatus;
	private String lastMeasuredDate;
	private BigDecimal lastMeasuredWeight;
	private BigDecimal dischargeWeight;

	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }

	public String getPatientName() { return patientName; }
	public void setPatientName(String patientName) { this.patientName = patientName; }

	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Boolean getDischargeStatus() { return dischargeStatus; }
	public void setDischargeStatus(Boolean dischargeStatus) { this.dischargeStatus = dischargeStatus; }

	public String getLastMeasuredDate() { return lastMeasuredDate; }
	public void setLastMeasuredDate(String lastMeasuredDate) { this.lastMeasuredDate = lastMeasuredDate; }

	public BigDecimal getLastMeasuredWeight() { return lastMeasuredWeight; }
	public void setLastMeasuredWeight(BigDecimal lastMeasuredWeight) { this.lastMeasuredWeight = lastMeasuredWeight; }

	public BigDecimal getDischargeWeight() { return dischargeWeight; }
	public void setDischargeWeight(BigDecimal dischargeWeight) { this.dischargeWeight = dischargeWeight; }
}
