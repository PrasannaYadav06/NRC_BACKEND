package com.hfw.nrc.dto;

/** Item 13 - one row of the Search Patient page's result picker (before the caller opens the full A4 history view). */
public class PatientLookupDto {

	private int caseId;
	private Long uhId;
	private Integer samNum;
	private String childName;
	private String childGender;
	private String motherName;
	private String hospitalName;
	private String formStatus;
	private String statusLabel;

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public Integer getSamNum() { return samNum; }
	public void setSamNum(Integer samNum) { this.samNum = samNum; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public String getChildGender() { return childGender; }
	public void setChildGender(String childGender) { this.childGender = childGender; }
	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }
	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
	public String getFormStatus() { return formStatus; }
	public void setFormStatus(String formStatus) { this.formStatus = formStatus; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}
