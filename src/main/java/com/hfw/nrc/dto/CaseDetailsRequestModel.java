package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ported from nrcapp.model.CaseDetailsRequestModel - the request body for
 * /createcase, /updateadmission, and (mother/child sub-updates) /updatecase.
 * Commented-out-in-source fields (dischargeDate, fiscalYear, admittedByDoctor,
 * dischargedByDoctor, and the entire Sneha-Angan block) are dropped, not
 * ported - dead in the source too.
 */
public class CaseDetailsRequestModel implements AbstractModel {

	// Mother details
	private int motherAge;
	private String motherDob;
	private Integer educationQualificationId;
	private Integer familyPlanning;
	private String motherName;
	private int loginUserId;

	// Child details
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private Integer districtId;
	private int pinCode;
	private int childAgeinYears;
	private int childAgeinMonths;
	private Integer casteId;
	private String category;
	private String childDob;
	private String gender;
	private String mobileNumber;
	private String alternateNumber;
	private String childName;
	private Long rchId;
	private Integer religionId;

	// For updating child and mother
	private Integer childId;
	private Integer motherId;

	// Case details
	private String admissionDate;
	private int admissionTypeId;
	private Long ipNum;
	private Long uhId;
	private BigDecimal targetWeight;
	private Integer admissionSd;
	private Integer hospitalId;
	private Integer caseId;
	private Integer samNumber;
	private Boolean dischargeStatus;

	// Sub-record lists
	private List<AdmissionComorbidModel> comorbidConditions;
	private List<AdmissionCriteriaModel> admissionCriterias;
	private List<CaseAllocationModel> caseAllocations;
	private List<CaseMotherDetailModel> caseMotherDetails;
	private List<CaseMotherGrowthModel> caseMotherGrowths;
	private List<CaseMotherSocialFactorModel> caseMotherSocialFactors;
	private List<CaseReferralModel> caseReferrals;
	private List<NRCChildGrowthModel> childGrowths;

	public int getMotherAge() { return motherAge; }
	public void setMotherAge(int motherAge) { this.motherAge = motherAge; }

	public String getMotherDob() { return motherDob; }
	public void setMotherDob(String motherDob) { this.motherDob = motherDob; }

	public Integer getEducationQualificationId() { return educationQualificationId; }
	public void setEducationQualificationId(Integer educationQualificationId) { this.educationQualificationId = educationQualificationId; }

	public Integer getFamilyPlanning() { return familyPlanning; }
	public void setFamilyPlanning(Integer familyPlanning) { this.familyPlanning = familyPlanning; }

	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }

	public int getLoginUserId() { return loginUserId; }
	public void setLoginUserId(int loginUserId) { this.loginUserId = loginUserId; }

	public String getAddressLine1() { return addressLine1; }
	public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

	public String getAddressLine2() { return addressLine2; }
	public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

	public String getAddressLine3() { return addressLine3; }
	public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }

	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }

	public int getPinCode() { return pinCode; }
	public void setPinCode(int pinCode) { this.pinCode = pinCode; }

	public int getChildAgeinYears() { return childAgeinYears; }
	public void setChildAgeinYears(int childAgeinYears) { this.childAgeinYears = childAgeinYears; }

	public int getChildAgeinMonths() { return childAgeinMonths; }
	public void setChildAgeinMonths(int childAgeinMonths) { this.childAgeinMonths = childAgeinMonths; }

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

	public String getAlternateNumber() { return alternateNumber; }
	public void setAlternateNumber(String alternateNumber) { this.alternateNumber = alternateNumber; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }

	public Integer getReligionId() { return religionId; }
	public void setReligionId(Integer religionId) { this.religionId = religionId; }

	public Integer getChildId() { return childId; }
	public void setChildId(Integer childId) { this.childId = childId; }

	public Integer getMotherId() { return motherId; }
	public void setMotherId(Integer motherId) { this.motherId = motherId; }

	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

	public int getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(int admissionTypeId) { this.admissionTypeId = admissionTypeId; }

	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

	public Integer getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(Integer admissionSd) { this.admissionSd = admissionSd; }

	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public Boolean getDischargeStatus() { return dischargeStatus; }
	public void setDischargeStatus(Boolean dischargeStatus) { this.dischargeStatus = dischargeStatus; }

	public List<AdmissionComorbidModel> getComorbidConditions() { return comorbidConditions; }
	public void setComorbidConditions(List<AdmissionComorbidModel> comorbidConditions) { this.comorbidConditions = comorbidConditions; }

	public List<AdmissionCriteriaModel> getAdmissionCriterias() { return admissionCriterias; }
	public void setAdmissionCriterias(List<AdmissionCriteriaModel> admissionCriterias) { this.admissionCriterias = admissionCriterias; }

	public List<CaseAllocationModel> getCaseAllocations() { return caseAllocations; }
	public void setCaseAllocations(List<CaseAllocationModel> caseAllocations) { this.caseAllocations = caseAllocations; }

	public List<CaseMotherDetailModel> getCaseMotherDetails() { return caseMotherDetails; }
	public void setCaseMotherDetails(List<CaseMotherDetailModel> caseMotherDetails) { this.caseMotherDetails = caseMotherDetails; }

	public List<CaseMotherGrowthModel> getCaseMotherGrowths() { return caseMotherGrowths; }
	public void setCaseMotherGrowths(List<CaseMotherGrowthModel> caseMotherGrowths) { this.caseMotherGrowths = caseMotherGrowths; }

	public List<CaseMotherSocialFactorModel> getCaseMotherSocialFactors() { return caseMotherSocialFactors; }
	public void setCaseMotherSocialFactors(List<CaseMotherSocialFactorModel> caseMotherSocialFactors) { this.caseMotherSocialFactors = caseMotherSocialFactors; }

	public List<CaseReferralModel> getCaseReferrals() { return caseReferrals; }
	public void setCaseReferrals(List<CaseReferralModel> caseReferrals) { this.caseReferrals = caseReferrals; }

	public List<NRCChildGrowthModel> getChildGrowths() { return childGrowths; }
	public void setChildGrowths(List<NRCChildGrowthModel> childGrowths) { this.childGrowths = childGrowths; }
}
