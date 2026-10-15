package com.hfw.nrc.dto;

import java.util.List;

/**
 * Stage 3 - create/update payload for the single consolidated In-Patient
 * Care admission module. Deliberately mirrors the nested shape the existing
 * frontend admission form already builds in onSubmitAdmission() (see
 * admission.component.ts) - the old shape targeted the now-superseded
 * /createcase endpoint (which fanned this out across 8 legacy tables); this
 * is the same shape landing on ONE table (nr.nrc_admission) instead. Kept
 * nested rather than flattened so the frontend's already-correct
 * payload-building logic (checkbox arrays, SD calc, etc.) needed no rewrite
 * beyond pointing at the new endpoint.
 */
public class AdmissionSaveRequest {

	private Integer loginUserId;
	private Integer hospitalId;
	private Integer samNumber;
	private Long uhId;
	private Long ipNum;
	private Integer admissionTypeId;

	private String childName;
	private String gender;
	private String category;
	private String childDob;
	private Integer childAgeinYears;
	private Integer childAgeinMonths;
	private Integer religionId;
	private Integer casteId;
	private String mobileNumber;
	private String alternateNumber;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private Integer pinCode;
	private Integer districtId;
	private Long rchId;
	private Boolean hasGuardian;
	private String guardianName;

	private String admissionDate;
	private java.math.BigDecimal targetWeight;
	private Integer admissionSd;

	private String motherName;
	private String motherDob;
	private Integer motherAge;
	private Integer educationQualificationId;
	private Boolean familyPlanning;
	private Long motherRchId;

	private List<CaseAllocationItem> caseAllocations;
	private List<CaseReferralItem> caseReferrals;
	private List<ChildGrowthItem> childGrowths;
	private List<CaseMotherDetailItem> caseMotherDetails;
	private List<CaseMotherGrowthItem> caseMotherGrowths;
	private List<ComorbidItem> comorbidConditions;
	private List<CriteriaItem> admissionCriterias;
	private List<SocialFactorItem> caseMotherSocialFactors;

	public static class CaseAllocationItem {
		private Integer unitId;
		private Integer wardId;
		public Integer getUnitId() { return unitId; }
		public void setUnitId(Integer unitId) { this.unitId = unitId; }
		public Integer getWardId() { return wardId; }
		public void setWardId(Integer wardId) { this.wardId = wardId; }
	}

	public static class CaseReferralItem {
		private Integer referredFromId;
		private String referredBy;
		private String referredByNumber;
		public Integer getReferredFromId() { return referredFromId; }
		public void setReferredFromId(Integer referredFromId) { this.referredFromId = referredFromId; }
		public String getReferredBy() { return referredBy; }
		public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
		public String getReferredByNumber() { return referredByNumber; }
		public void setReferredByNumber(String referredByNumber) { this.referredByNumber = referredByNumber; }
	}

	public static class ChildGrowthItem {
		private String measurementDate;
		private java.math.BigDecimal heightCm;
		private java.math.BigDecimal weightKg;
		public String getMeasurementDate() { return measurementDate; }
		public void setMeasurementDate(String measurementDate) { this.measurementDate = measurementDate; }
		public java.math.BigDecimal getHeightCm() { return heightCm; }
		public void setHeightCm(java.math.BigDecimal heightCm) { this.heightCm = heightCm; }
		public java.math.BigDecimal getWeightKg() { return weightKg; }
		public void setWeightKg(java.math.BigDecimal weightKg) { this.weightKg = weightKg; }
	}

	public static class CaseMotherDetailItem {
		private Boolean breastFeedingAtPresent;
		private Boolean drugsPrescribed;
		private String drugsPrescribedDetails;
		private String lastMenstruationDate;
		private Boolean medicalProblem;
		private String medicalProblemDetails;
		private Integer noOfChildren;
		private String obstetricHistory;
		private Integer noOfMealsPerDay;
		private Boolean pregnantAtPresent;
		private java.math.BigDecimal numberOfPregnancyMonths;
		private String presentDietaryChoice;
		private Boolean registeredForAnc;
		private Integer riskCategoryId;

		public Boolean getBreastFeedingAtPresent() { return breastFeedingAtPresent; }
		public void setBreastFeedingAtPresent(Boolean breastFeedingAtPresent) { this.breastFeedingAtPresent = breastFeedingAtPresent; }
		public Boolean getDrugsPrescribed() { return drugsPrescribed; }
		public void setDrugsPrescribed(Boolean drugsPrescribed) { this.drugsPrescribed = drugsPrescribed; }
		public String getDrugsPrescribedDetails() { return drugsPrescribedDetails; }
		public void setDrugsPrescribedDetails(String drugsPrescribedDetails) { this.drugsPrescribedDetails = drugsPrescribedDetails; }
		public String getLastMenstruationDate() { return lastMenstruationDate; }
		public void setLastMenstruationDate(String lastMenstruationDate) { this.lastMenstruationDate = lastMenstruationDate; }
		public Boolean getMedicalProblem() { return medicalProblem; }
		public void setMedicalProblem(Boolean medicalProblem) { this.medicalProblem = medicalProblem; }
		public String getMedicalProblemDetails() { return medicalProblemDetails; }
		public void setMedicalProblemDetails(String medicalProblemDetails) { this.medicalProblemDetails = medicalProblemDetails; }
		public Integer getNoOfChildren() { return noOfChildren; }
		public void setNoOfChildren(Integer noOfChildren) { this.noOfChildren = noOfChildren; }
		public String getObstetricHistory() { return obstetricHistory; }
		public void setObstetricHistory(String obstetricHistory) { this.obstetricHistory = obstetricHistory; }
		public Integer getNoOfMealsPerDay() { return noOfMealsPerDay; }
		public void setNoOfMealsPerDay(Integer noOfMealsPerDay) { this.noOfMealsPerDay = noOfMealsPerDay; }
		public Boolean getPregnantAtPresent() { return pregnantAtPresent; }
		public void setPregnantAtPresent(Boolean pregnantAtPresent) { this.pregnantAtPresent = pregnantAtPresent; }
		public java.math.BigDecimal getNumberOfPregnancyMonths() { return numberOfPregnancyMonths; }
		public void setNumberOfPregnancyMonths(java.math.BigDecimal numberOfPregnancyMonths) { this.numberOfPregnancyMonths = numberOfPregnancyMonths; }
		public String getPresentDietaryChoice() { return presentDietaryChoice; }
		public void setPresentDietaryChoice(String presentDietaryChoice) { this.presentDietaryChoice = presentDietaryChoice; }
		public Boolean getRegisteredForAnc() { return registeredForAnc; }
		public void setRegisteredForAnc(Boolean registeredForAnc) { this.registeredForAnc = registeredForAnc; }
		public Integer getRiskCategoryId() { return riskCategoryId; }
		public void setRiskCategoryId(Integer riskCategoryId) { this.riskCategoryId = riskCategoryId; }
	}

	public static class CaseMotherGrowthItem {
		private java.math.BigDecimal bloodPressureDia;
		private java.math.BigDecimal bloodPressureSys;
		private java.math.BigDecimal bloodSugarFasting;
		private java.math.BigDecimal bloodSugarRandom;
		private java.math.BigDecimal bmi;
		private Integer gravida;
		private java.math.BigDecimal heightCm;
		private java.math.BigDecimal hemoglobin;
		private java.math.BigDecimal muacCm;
		private java.math.BigDecimal neckCircumferenceCm;
		private Integer para;
		private java.math.BigDecimal weightKg;
		private String advice;

		public java.math.BigDecimal getBloodPressureDia() { return bloodPressureDia; }
		public void setBloodPressureDia(java.math.BigDecimal bloodPressureDia) { this.bloodPressureDia = bloodPressureDia; }
		public java.math.BigDecimal getBloodPressureSys() { return bloodPressureSys; }
		public void setBloodPressureSys(java.math.BigDecimal bloodPressureSys) { this.bloodPressureSys = bloodPressureSys; }
		public java.math.BigDecimal getBloodSugarFasting() { return bloodSugarFasting; }
		public void setBloodSugarFasting(java.math.BigDecimal bloodSugarFasting) { this.bloodSugarFasting = bloodSugarFasting; }
		public java.math.BigDecimal getBloodSugarRandom() { return bloodSugarRandom; }
		public void setBloodSugarRandom(java.math.BigDecimal bloodSugarRandom) { this.bloodSugarRandom = bloodSugarRandom; }
		public java.math.BigDecimal getBmi() { return bmi; }
		public void setBmi(java.math.BigDecimal bmi) { this.bmi = bmi; }
		public Integer getGravida() { return gravida; }
		public void setGravida(Integer gravida) { this.gravida = gravida; }
		public java.math.BigDecimal getHeightCm() { return heightCm; }
		public void setHeightCm(java.math.BigDecimal heightCm) { this.heightCm = heightCm; }
		public java.math.BigDecimal getHemoglobin() { return hemoglobin; }
		public void setHemoglobin(java.math.BigDecimal hemoglobin) { this.hemoglobin = hemoglobin; }
		public java.math.BigDecimal getMuacCm() { return muacCm; }
		public void setMuacCm(java.math.BigDecimal muacCm) { this.muacCm = muacCm; }
		public java.math.BigDecimal getNeckCircumferenceCm() { return neckCircumferenceCm; }
		public void setNeckCircumferenceCm(java.math.BigDecimal neckCircumferenceCm) { this.neckCircumferenceCm = neckCircumferenceCm; }
		public Integer getPara() { return para; }
		public void setPara(Integer para) { this.para = para; }
		public java.math.BigDecimal getWeightKg() { return weightKg; }
		public void setWeightKg(java.math.BigDecimal weightKg) { this.weightKg = weightKg; }
		public String getAdvice() { return advice; }
		public void setAdvice(String advice) { this.advice = advice; }
	}

	public static class ComorbidItem {
		private Integer comorbidId;
		private String othersValue;
		public Integer getComorbidId() { return comorbidId; }
		public void setComorbidId(Integer comorbidId) { this.comorbidId = comorbidId; }
		public String getOthersValue() { return othersValue; }
		public void setOthersValue(String othersValue) { this.othersValue = othersValue; }
	}

	public static class CriteriaItem {
		private Integer criteriaId;
		private String othersValue;
		public Integer getCriteriaId() { return criteriaId; }
		public void setCriteriaId(Integer criteriaId) { this.criteriaId = criteriaId; }
		public String getOthersValue() { return othersValue; }
		public void setOthersValue(String othersValue) { this.othersValue = othersValue; }
	}

	public static class SocialFactorItem {
		private Integer socialFactorsId;
		private String otherValue;
		public Integer getSocialFactorsId() { return socialFactorsId; }
		public void setSocialFactorsId(Integer socialFactorsId) { this.socialFactorsId = socialFactorsId; }
		public String getOtherValue() { return otherValue; }
		public void setOtherValue(String otherValue) { this.otherValue = otherValue; }
	}

	// ===== top-level getters/setters =====

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }
	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }
	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }
	public Integer getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(Integer admissionTypeId) { this.admissionTypeId = admissionTypeId; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public String getChildDob() { return childDob; }
	public void setChildDob(String childDob) { this.childDob = childDob; }
	public Integer getChildAgeinYears() { return childAgeinYears; }
	public void setChildAgeinYears(Integer childAgeinYears) { this.childAgeinYears = childAgeinYears; }
	public Integer getChildAgeinMonths() { return childAgeinMonths; }
	public void setChildAgeinMonths(Integer childAgeinMonths) { this.childAgeinMonths = childAgeinMonths; }
	public Integer getReligionId() { return religionId; }
	public void setReligionId(Integer religionId) { this.religionId = religionId; }
	public Integer getCasteId() { return casteId; }
	public void setCasteId(Integer casteId) { this.casteId = casteId; }
	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
	public String getAlternateNumber() { return alternateNumber; }
	public void setAlternateNumber(String alternateNumber) { this.alternateNumber = alternateNumber; }
	public String getAddressLine1() { return addressLine1; }
	public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
	public String getAddressLine2() { return addressLine2; }
	public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
	public String getAddressLine3() { return addressLine3; }
	public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }
	public Integer getPinCode() { return pinCode; }
	public void setPinCode(Integer pinCode) { this.pinCode = pinCode; }
	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }
	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }
	public Boolean getHasGuardian() { return hasGuardian; }
	public void setHasGuardian(Boolean hasGuardian) { this.hasGuardian = hasGuardian; }
	public String getGuardianName() { return guardianName; }
	public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }
	public java.math.BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(java.math.BigDecimal targetWeight) { this.targetWeight = targetWeight; }
	public Integer getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(Integer admissionSd) { this.admissionSd = admissionSd; }
	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }
	public String getMotherDob() { return motherDob; }
	public void setMotherDob(String motherDob) { this.motherDob = motherDob; }
	public Integer getMotherAge() { return motherAge; }
	public void setMotherAge(Integer motherAge) { this.motherAge = motherAge; }
	public Integer getEducationQualificationId() { return educationQualificationId; }
	public void setEducationQualificationId(Integer educationQualificationId) { this.educationQualificationId = educationQualificationId; }
	public Boolean getFamilyPlanning() { return familyPlanning; }
	public void setFamilyPlanning(Boolean familyPlanning) { this.familyPlanning = familyPlanning; }
	public Long getMotherRchId() { return motherRchId; }
	public void setMotherRchId(Long motherRchId) { this.motherRchId = motherRchId; }
	public List<CaseAllocationItem> getCaseAllocations() { return caseAllocations; }
	public void setCaseAllocations(List<CaseAllocationItem> caseAllocations) { this.caseAllocations = caseAllocations; }
	public List<CaseReferralItem> getCaseReferrals() { return caseReferrals; }
	public void setCaseReferrals(List<CaseReferralItem> caseReferrals) { this.caseReferrals = caseReferrals; }
	public List<ChildGrowthItem> getChildGrowths() { return childGrowths; }
	public void setChildGrowths(List<ChildGrowthItem> childGrowths) { this.childGrowths = childGrowths; }
	public List<CaseMotherDetailItem> getCaseMotherDetails() { return caseMotherDetails; }
	public void setCaseMotherDetails(List<CaseMotherDetailItem> caseMotherDetails) { this.caseMotherDetails = caseMotherDetails; }
	public List<CaseMotherGrowthItem> getCaseMotherGrowths() { return caseMotherGrowths; }
	public void setCaseMotherGrowths(List<CaseMotherGrowthItem> caseMotherGrowths) { this.caseMotherGrowths = caseMotherGrowths; }
	public List<ComorbidItem> getComorbidConditions() { return comorbidConditions; }
	public void setComorbidConditions(List<ComorbidItem> comorbidConditions) { this.comorbidConditions = comorbidConditions; }
	public List<CriteriaItem> getAdmissionCriterias() { return admissionCriterias; }
	public void setAdmissionCriterias(List<CriteriaItem> admissionCriterias) { this.admissionCriterias = admissionCriterias; }
	public List<SocialFactorItem> getCaseMotherSocialFactors() { return caseMotherSocialFactors; }
	public void setCaseMotherSocialFactors(List<SocialFactorItem> caseMotherSocialFactors) { this.caseMotherSocialFactors = caseMotherSocialFactors; }
}
