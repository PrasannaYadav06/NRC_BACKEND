package com.hfw.nrc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Edit-existing-hospital request (Stage 2). Deliberately not the same DTO
 * as AddHospitalRequestModel (create) - password is optional here (blank =
 * leave unchanged) and username is never changed once an account exists
 * (it's the login identifier), whereas create requires both.
 */
public class UpdateHospitalRequestModel {

	private String hCode;
	private String name;
	private String nameShort;
	private Integer districtId;
	private Integer pincode;
	private String mobileNumber;
	private String emailId;
	private String landLineNumber;
	private Integer numberOfBeds;
	private String newPassword; // optional - blank/null leaves the password unchanged

	// Same getHCode()/setHCode() -> "HCode" Jackson-mangling pitfall as
	// HospitalAdminDto (see its javadoc) - caught live here too (h_code came
	// back null on the first test) before this shipped further.
	@JsonProperty("hCode")
	public String getHCode() { return hCode; }
	@JsonProperty("hCode")
	public void setHCode(String hCode) { this.hCode = hCode; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getNameShort() { return nameShort; }
	public void setNameShort(String nameShort) { this.nameShort = nameShort; }

	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }

	public Integer getPincode() { return pincode; }
	public void setPincode(Integer pincode) { this.pincode = pincode; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getEmailId() { return emailId; }
	public void setEmailId(String emailId) { this.emailId = emailId; }

	public String getLandLineNumber() { return landLineNumber; }
	public void setLandLineNumber(String landLineNumber) { this.landLineNumber = landLineNumber; }

	public Integer getNumberOfBeds() { return numberOfBeds; }
	public void setNumberOfBeds(Integer numberOfBeds) { this.numberOfBeds = numberOfBeds; }

	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
