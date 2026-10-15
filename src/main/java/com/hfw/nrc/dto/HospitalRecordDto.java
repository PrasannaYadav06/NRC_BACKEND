package com.hfw.nrc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hospital Records list row / single-hospital view-or-edit payload (Stage 2
 * of the restructuring - "Add New NRC Hospital" + "Hospital Records" pages).
 * One shape serves all three uses (list row, view popup, edit-form prefill)
 * since they need essentially the same fields.
 */
public class HospitalRecordDto {

	private int hId;
	private String hCode;
	private String name;
	private String nameShort;
	private Integer districtId;
	private String districtName;
	private Integer talukaId;
	private String talukaName;
	private String phcName;
	private String chcName;
	private String hospitalLevel;
	private Integer pincode;
	private String mobileNumber;
	private String emailId;
	private String landLineNumber;
	private Integer numberOfBeds;
	private String username;

	// Jackson's default bean-naming mangler fully lowercases a leading run of
	// capitals followed by a lowercase letter (HId -> "hid", not "hId") -
	// same class of bug as HospitalAdminDto/UpdateHospitalRequestModel,
	// forced to the frontend's actual camelCase convention explicitly.
	@JsonProperty("hId")
	public int getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(int hId) { this.hId = hId; }

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

	public String getDistrictName() { return districtName; }
	public void setDistrictName(String districtName) { this.districtName = districtName; }

	public Integer getTalukaId() { return talukaId; }
	public void setTalukaId(Integer talukaId) { this.talukaId = talukaId; }

	public String getTalukaName() { return talukaName; }
	public void setTalukaName(String talukaName) { this.talukaName = talukaName; }

	public String getPhcName() { return phcName; }
	public void setPhcName(String phcName) { this.phcName = phcName; }

	public String getChcName() { return chcName; }
	public void setChcName(String chcName) { this.chcName = chcName; }

	public String getHospitalLevel() { return hospitalLevel; }
	public void setHospitalLevel(String hospitalLevel) { this.hospitalLevel = hospitalLevel; }

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

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
}
