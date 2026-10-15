package com.hfw.nrc.dto;

/** Ported from nrcapp.model.SearchQueryResponseModel. */
public class SearchQueryResponseModel implements AbstractModel {

	private Long uhId;
	private Long rchId;
	private Integer samNumber;
	private String childName;
	private String motherName;
	private String mobileNumber;
	private String lastAdmittedDate;
	private String lastDischargeDate;
	private String lastFollowupDate;
	private Integer admissionTypeId;
	private Integer caseId;
	private Integer hospitalId;
	private String gender;

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getLastAdmittedDate() { return lastAdmittedDate; }
	public void setLastAdmittedDate(String lastAdmittedDate) { this.lastAdmittedDate = lastAdmittedDate; }

	public String getLastDischargeDate() { return lastDischargeDate; }
	public void setLastDischargeDate(String lastDischargeDate) { this.lastDischargeDate = lastDischargeDate; }

	public String getLastFollowupDate() { return lastFollowupDate; }
	public void setLastFollowupDate(String lastFollowupDate) { this.lastFollowupDate = lastFollowupDate; }

	public Integer getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(Integer admissionTypeId) { this.admissionTypeId = admissionTypeId; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
}
