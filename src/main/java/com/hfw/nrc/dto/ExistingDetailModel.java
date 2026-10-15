package com.hfw.nrc.dto;

/** Ported from nrcapp.model.ExistingDetailModel - the actual response payload for /getcasedetails. */
public class ExistingDetailModel implements AbstractModel {

	private Long uhId;

	// child details
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private Integer pincode;
	private Integer districtId;
	private Integer childAgeinYears;
	private Integer childAgeinMonths;
	private Integer casteId;
	private String category;
	private String childDob;
	private String gender;
	private String mobileNumber;
	private String childName;
	private Long rchId;
	private Integer religionId;

	// mother details
	private Integer motherAge;
	private String motherDob;
	private String motherName;

	// case details
	private Integer admissionTypeId;
	private Integer samNumber;

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public String getAddressLine1() { return addressLine1; }
	public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

	public String getAddressLine2() { return addressLine2; }
	public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

	public String getAddressLine3() { return addressLine3; }
	public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }

	public Integer getPincode() { return pincode; }
	public void setPincode(Integer pincode) { this.pincode = pincode; }

	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }

	public Integer getChildAgeinYears() { return childAgeinYears; }
	public void setChildAgeinYears(Integer childAgeinYears) { this.childAgeinYears = childAgeinYears; }

	public Integer getChildAgeinMonths() { return childAgeinMonths; }
	public void setChildAgeinMonths(Integer childAgeinMonths) { this.childAgeinMonths = childAgeinMonths; }

	public Integer getCasteId() { return casteId; }
	public void setCasteId(Integer casteId) { this.casteId = casteId; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getChildDob() { return childDob; }
	public void setChildDob(String childDob) { this.childDob = childDob; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }

	public Integer getReligionId() { return religionId; }
	public void setReligionId(Integer religionId) { this.religionId = religionId; }

	public Integer getMotherAge() { return motherAge; }
	public void setMotherAge(Integer motherAge) { this.motherAge = motherAge; }

	public String getMotherDob() { return motherDob; }
	public void setMotherDob(String motherDob) { this.motherDob = motherDob; }

	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }

	public Integer getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(Integer admissionTypeId) { this.admissionTypeId = admissionTypeId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }
}
