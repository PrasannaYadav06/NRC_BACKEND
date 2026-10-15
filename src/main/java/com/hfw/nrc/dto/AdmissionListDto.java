package com.hfw.nrc.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stage 3 - one row of the In-Patient Care Records table: UHID | Patient
 * Name | Gender | Target Weight | Admission Date | Last Measured Weight |
 * Last Measured Date | Actions, per spec's exact column list.
 */
public class AdmissionListDto {

	private int caseId;
	private Integer hId;
	private String hospitalName;
	private Long uhId;
	private String childName;
	private String childGender;
	private BigDecimal targetWeight;
	private String admissionDate;
	private BigDecimal lastMeasuredWeight;
	private String lastMeasuredDate;
	private String formStatus;
	private String statusLabel; // "Admitted" / "Under Weight Capture" - derived from formStatus for the table's Status display
	/** Count of nr.nrc_weight_capture rows for this case so far - drives the blinking green "N-Weight Captured" badge next to statusLabel once capture has started. */
	private Integer weightCaptureCount;

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	// Jackson's default bean-naming mangler fully lowercases a leading run of
	// capitals followed by a lowercase letter (HId -> "hid") - same
	// class of bug as HospitalRecordDto etc, forced to the frontend's actual
	// camelCase convention explicitly.
	@JsonProperty("hId")
	public Integer getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(Integer hId) { this.hId = hId; }
	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public String getChildGender() { return childGender; }
	public void setChildGender(String childGender) { this.childGender = childGender; }
	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }
	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }
	public BigDecimal getLastMeasuredWeight() { return lastMeasuredWeight; }
	public void setLastMeasuredWeight(BigDecimal lastMeasuredWeight) { this.lastMeasuredWeight = lastMeasuredWeight; }
	public String getLastMeasuredDate() { return lastMeasuredDate; }
	public void setLastMeasuredDate(String lastMeasuredDate) { this.lastMeasuredDate = lastMeasuredDate; }
	public String getFormStatus() { return formStatus; }
	public void setFormStatus(String formStatus) { this.formStatus = formStatus; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
	public Integer getWeightCaptureCount() { return weightCaptureCount; }
	public void setWeightCaptureCount(Integer weightCaptureCount) { this.weightCaptureCount = weightCaptureCount; }
}
