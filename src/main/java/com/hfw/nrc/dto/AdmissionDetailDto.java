package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stage 3 - full single-admission shape, backing both the View popup
 * (ordered sections per spec: Registration Details, Child Demography,
 * Admission criteria, Mother Details/Demography, Weight capture data) and
 * the Edit form's prefill. Flat (mirrors NrcAdmission's columns directly)
 * rather than nested like AdmissionSaveRequest - simpler for the view popup
 * to render field-by-field, and the edit form only needs to unpack it back
 * into its 4 FormGroups plus reconstruct the criteria/comorbid/social
 * checkbox arrays from the selected*Ids lists below.
 */
public class AdmissionDetailDto {

	private int caseId;
	private Integer hId;
	private String hospitalName;
	private Long ipNum;
	private Long uhId;
	private Integer samNum;
	private Integer admissionTypeId;
	private String admissionDate;
	private BigDecimal targetWeight;
	private BigDecimal admissionSd;

	private String childName;
	private String childDateOfBirth;
	private Integer childAgeInMonths;
	private String childGender;
	private String childAddressLine1;
	private String childAddressLine2;
	private String childAddressLine3;
	private String childMobileNumber;
	private String childAlternateMobileNumber;
	private String childCategory;
	private Integer childReligionId;
	private Integer childCasteId;
	private Integer childPincode;
	private Integer childDistrictId;
	private Long childRchId;
	private Boolean childHasGuardian;
	private String childGuardianName;

	private String motherName;
	private String motherDateOfBirth;
	private Integer motherAge;
	private Integer motherEducationQualificationId;
	private Boolean motherFamilyPlanning;
	private Long motherRchId;

	private Integer unitId;
	private Integer wardId;
	private Integer referredFromId;
	private String referredBy;
	private String referredByMNumb;

	private Integer riskCategoryId;
	private Integer noOfChildren;
	private String obstetricHistory;
	private Boolean medicalProblem;
	private String medicalProblemDetails;
	private Boolean drugsPrescribed;
	private String drugsPrescribedDetails;
	private String presentDietaryChoice;
	private Integer noOfMealsPerDay;
	private Boolean pregnantAtPresent;
	private BigDecimal numberOfPregnancyMonths;
	private Boolean registeredForAnc;
	private Boolean breastFeedingAtPresent;
	private String lastMenstruationDate;

	private BigDecimal motherWeightKgAdmission;
	private BigDecimal motherHeightCmAdmission;
	private BigDecimal motherBmiAdmission;
	private BigDecimal motherMuacCmAdmission;
	private BigDecimal motherHemoglobinAdmission;
	private BigDecimal motherNeckCircumferenceCmAdmission;
	private Integer motherGravida;
	private Integer motherPara;
	private BigDecimal motherBloodSugarFastingAdmission;
	private BigDecimal motherBloodSugarRandomAdmission;
	private BigDecimal motherBpSysAdmission;
	private BigDecimal motherBpDiaAdmission;

	private BigDecimal admissionWeightKg;
	private BigDecimal admissionHeightCm;

	// Reconstructed from the ac_*/cc_*/sf_* boolean columns - lets the Edit
	// form re-check the right checkboxes without the frontend needing to know
	// the flattened column layout.
	private List<Integer> selectedCriteriaIds;
	private String criteriaOthersValue;
	private List<Integer> selectedComorbidIds;
	private String comorbidOthersValue;
	private List<Integer> selectedSocialFactorIds;
	private String socialFactorOthersValue;

	private BigDecimal lastMeasuredWeight;
	private String lastMeasuredDate;
	private String formStatus;

	// Weight-capture history (decision #2) - "1st capture / 2nd capture / ..."
	private List<WeightCaptureDto> weightCaptures;

	public static class WeightCaptureDto {
		private BigDecimal weightKg;
		private String diet;
		private String comment;
		private String captureDate;
		public BigDecimal getWeightKg() { return weightKg; }
		public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
		public String getDiet() { return diet; }
		public void setDiet(String diet) { this.diet = diet; }
		public String getComment() { return comment; }
		public void setComment(String comment) { this.comment = comment; }
		public String getCaptureDate() { return captureDate; }
		public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }
	}

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	@JsonProperty("hId")
	public Integer getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(Integer hId) { this.hId = hId; }
	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }
	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }
	public Integer getSamNum() { return samNum; }
	public void setSamNum(Integer samNum) { this.samNum = samNum; }
	public Integer getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(Integer admissionTypeId) { this.admissionTypeId = admissionTypeId; }
	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }
	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }
	public BigDecimal getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(BigDecimal admissionSd) { this.admissionSd = admissionSd; }
	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }
	public String getChildDateOfBirth() { return childDateOfBirth; }
	public void setChildDateOfBirth(String childDateOfBirth) { this.childDateOfBirth = childDateOfBirth; }
	public Integer getChildAgeInMonths() { return childAgeInMonths; }
	public void setChildAgeInMonths(Integer childAgeInMonths) { this.childAgeInMonths = childAgeInMonths; }
	public String getChildGender() { return childGender; }
	public void setChildGender(String childGender) { this.childGender = childGender; }
	public String getChildAddressLine1() { return childAddressLine1; }
	public void setChildAddressLine1(String childAddressLine1) { this.childAddressLine1 = childAddressLine1; }
	public String getChildAddressLine2() { return childAddressLine2; }
	public void setChildAddressLine2(String childAddressLine2) { this.childAddressLine2 = childAddressLine2; }
	public String getChildAddressLine3() { return childAddressLine3; }
	public void setChildAddressLine3(String childAddressLine3) { this.childAddressLine3 = childAddressLine3; }
	public String getChildMobileNumber() { return childMobileNumber; }
	public void setChildMobileNumber(String childMobileNumber) { this.childMobileNumber = childMobileNumber; }
	public String getChildAlternateMobileNumber() { return childAlternateMobileNumber; }
	public void setChildAlternateMobileNumber(String childAlternateMobileNumber) { this.childAlternateMobileNumber = childAlternateMobileNumber; }
	public String getChildCategory() { return childCategory; }
	public void setChildCategory(String childCategory) { this.childCategory = childCategory; }
	public Integer getChildReligionId() { return childReligionId; }
	public void setChildReligionId(Integer childReligionId) { this.childReligionId = childReligionId; }
	public Integer getChildCasteId() { return childCasteId; }
	public void setChildCasteId(Integer childCasteId) { this.childCasteId = childCasteId; }
	public Integer getChildPincode() { return childPincode; }
	public void setChildPincode(Integer childPincode) { this.childPincode = childPincode; }
	public Integer getChildDistrictId() { return childDistrictId; }
	public void setChildDistrictId(Integer childDistrictId) { this.childDistrictId = childDistrictId; }
	public Long getChildRchId() { return childRchId; }
	public void setChildRchId(Long childRchId) { this.childRchId = childRchId; }
	public Boolean getChildHasGuardian() { return childHasGuardian; }
	public void setChildHasGuardian(Boolean childHasGuardian) { this.childHasGuardian = childHasGuardian; }
	public String getChildGuardianName() { return childGuardianName; }
	public void setChildGuardianName(String childGuardianName) { this.childGuardianName = childGuardianName; }
	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }
	public String getMotherDateOfBirth() { return motherDateOfBirth; }
	public void setMotherDateOfBirth(String motherDateOfBirth) { this.motherDateOfBirth = motherDateOfBirth; }
	public Integer getMotherAge() { return motherAge; }
	public void setMotherAge(Integer motherAge) { this.motherAge = motherAge; }
	public Integer getMotherEducationQualificationId() { return motherEducationQualificationId; }
	public void setMotherEducationQualificationId(Integer motherEducationQualificationId) { this.motherEducationQualificationId = motherEducationQualificationId; }
	public Boolean getMotherFamilyPlanning() { return motherFamilyPlanning; }
	public void setMotherFamilyPlanning(Boolean motherFamilyPlanning) { this.motherFamilyPlanning = motherFamilyPlanning; }
	public Long getMotherRchId() { return motherRchId; }
	public void setMotherRchId(Long motherRchId) { this.motherRchId = motherRchId; }
	public Integer getUnitId() { return unitId; }
	public void setUnitId(Integer unitId) { this.unitId = unitId; }
	public Integer getWardId() { return wardId; }
	public void setWardId(Integer wardId) { this.wardId = wardId; }
	public Integer getReferredFromId() { return referredFromId; }
	public void setReferredFromId(Integer referredFromId) { this.referredFromId = referredFromId; }
	public String getReferredBy() { return referredBy; }
	public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
	public String getReferredByMNumb() { return referredByMNumb; }
	public void setReferredByMNumb(String referredByMNumb) { this.referredByMNumb = referredByMNumb; }
	public Integer getRiskCategoryId() { return riskCategoryId; }
	public void setRiskCategoryId(Integer riskCategoryId) { this.riskCategoryId = riskCategoryId; }
	public Integer getNoOfChildren() { return noOfChildren; }
	public void setNoOfChildren(Integer noOfChildren) { this.noOfChildren = noOfChildren; }
	public String getObstetricHistory() { return obstetricHistory; }
	public void setObstetricHistory(String obstetricHistory) { this.obstetricHistory = obstetricHistory; }
	public Boolean getMedicalProblem() { return medicalProblem; }
	public void setMedicalProblem(Boolean medicalProblem) { this.medicalProblem = medicalProblem; }
	public String getMedicalProblemDetails() { return medicalProblemDetails; }
	public void setMedicalProblemDetails(String medicalProblemDetails) { this.medicalProblemDetails = medicalProblemDetails; }
	public Boolean getDrugsPrescribed() { return drugsPrescribed; }
	public void setDrugsPrescribed(Boolean drugsPrescribed) { this.drugsPrescribed = drugsPrescribed; }
	public String getDrugsPrescribedDetails() { return drugsPrescribedDetails; }
	public void setDrugsPrescribedDetails(String drugsPrescribedDetails) { this.drugsPrescribedDetails = drugsPrescribedDetails; }
	public String getPresentDietaryChoice() { return presentDietaryChoice; }
	public void setPresentDietaryChoice(String presentDietaryChoice) { this.presentDietaryChoice = presentDietaryChoice; }
	public Integer getNoOfMealsPerDay() { return noOfMealsPerDay; }
	public void setNoOfMealsPerDay(Integer noOfMealsPerDay) { this.noOfMealsPerDay = noOfMealsPerDay; }
	public Boolean getPregnantAtPresent() { return pregnantAtPresent; }
	public void setPregnantAtPresent(Boolean pregnantAtPresent) { this.pregnantAtPresent = pregnantAtPresent; }
	public BigDecimal getNumberOfPregnancyMonths() { return numberOfPregnancyMonths; }
	public void setNumberOfPregnancyMonths(BigDecimal numberOfPregnancyMonths) { this.numberOfPregnancyMonths = numberOfPregnancyMonths; }
	public Boolean getRegisteredForAnc() { return registeredForAnc; }
	public void setRegisteredForAnc(Boolean registeredForAnc) { this.registeredForAnc = registeredForAnc; }
	public Boolean getBreastFeedingAtPresent() { return breastFeedingAtPresent; }
	public void setBreastFeedingAtPresent(Boolean breastFeedingAtPresent) { this.breastFeedingAtPresent = breastFeedingAtPresent; }
	public String getLastMenstruationDate() { return lastMenstruationDate; }
	public void setLastMenstruationDate(String lastMenstruationDate) { this.lastMenstruationDate = lastMenstruationDate; }
	public BigDecimal getMotherWeightKgAdmission() { return motherWeightKgAdmission; }
	public void setMotherWeightKgAdmission(BigDecimal motherWeightKgAdmission) { this.motherWeightKgAdmission = motherWeightKgAdmission; }
	public BigDecimal getMotherHeightCmAdmission() { return motherHeightCmAdmission; }
	public void setMotherHeightCmAdmission(BigDecimal motherHeightCmAdmission) { this.motherHeightCmAdmission = motherHeightCmAdmission; }
	public BigDecimal getMotherBmiAdmission() { return motherBmiAdmission; }
	public void setMotherBmiAdmission(BigDecimal motherBmiAdmission) { this.motherBmiAdmission = motherBmiAdmission; }
	public BigDecimal getMotherMuacCmAdmission() { return motherMuacCmAdmission; }
	public void setMotherMuacCmAdmission(BigDecimal motherMuacCmAdmission) { this.motherMuacCmAdmission = motherMuacCmAdmission; }
	public BigDecimal getMotherHemoglobinAdmission() { return motherHemoglobinAdmission; }
	public void setMotherHemoglobinAdmission(BigDecimal motherHemoglobinAdmission) { this.motherHemoglobinAdmission = motherHemoglobinAdmission; }
	public BigDecimal getMotherNeckCircumferenceCmAdmission() { return motherNeckCircumferenceCmAdmission; }
	public void setMotherNeckCircumferenceCmAdmission(BigDecimal motherNeckCircumferenceCmAdmission) { this.motherNeckCircumferenceCmAdmission = motherNeckCircumferenceCmAdmission; }
	public Integer getMotherGravida() { return motherGravida; }
	public void setMotherGravida(Integer motherGravida) { this.motherGravida = motherGravida; }
	public Integer getMotherPara() { return motherPara; }
	public void setMotherPara(Integer motherPara) { this.motherPara = motherPara; }
	public BigDecimal getMotherBloodSugarFastingAdmission() { return motherBloodSugarFastingAdmission; }
	public void setMotherBloodSugarFastingAdmission(BigDecimal motherBloodSugarFastingAdmission) { this.motherBloodSugarFastingAdmission = motherBloodSugarFastingAdmission; }
	public BigDecimal getMotherBloodSugarRandomAdmission() { return motherBloodSugarRandomAdmission; }
	public void setMotherBloodSugarRandomAdmission(BigDecimal motherBloodSugarRandomAdmission) { this.motherBloodSugarRandomAdmission = motherBloodSugarRandomAdmission; }
	public BigDecimal getMotherBpSysAdmission() { return motherBpSysAdmission; }
	public void setMotherBpSysAdmission(BigDecimal motherBpSysAdmission) { this.motherBpSysAdmission = motherBpSysAdmission; }
	public BigDecimal getMotherBpDiaAdmission() { return motherBpDiaAdmission; }
	public void setMotherBpDiaAdmission(BigDecimal motherBpDiaAdmission) { this.motherBpDiaAdmission = motherBpDiaAdmission; }
	public BigDecimal getAdmissionWeightKg() { return admissionWeightKg; }
	public void setAdmissionWeightKg(BigDecimal admissionWeightKg) { this.admissionWeightKg = admissionWeightKg; }
	public BigDecimal getAdmissionHeightCm() { return admissionHeightCm; }
	public void setAdmissionHeightCm(BigDecimal admissionHeightCm) { this.admissionHeightCm = admissionHeightCm; }
	public List<Integer> getSelectedCriteriaIds() { return selectedCriteriaIds; }
	public void setSelectedCriteriaIds(List<Integer> selectedCriteriaIds) { this.selectedCriteriaIds = selectedCriteriaIds; }
	public String getCriteriaOthersValue() { return criteriaOthersValue; }
	public void setCriteriaOthersValue(String criteriaOthersValue) { this.criteriaOthersValue = criteriaOthersValue; }
	public List<Integer> getSelectedComorbidIds() { return selectedComorbidIds; }
	public void setSelectedComorbidIds(List<Integer> selectedComorbidIds) { this.selectedComorbidIds = selectedComorbidIds; }
	public String getComorbidOthersValue() { return comorbidOthersValue; }
	public void setComorbidOthersValue(String comorbidOthersValue) { this.comorbidOthersValue = comorbidOthersValue; }
	public List<Integer> getSelectedSocialFactorIds() { return selectedSocialFactorIds; }
	public void setSelectedSocialFactorIds(List<Integer> selectedSocialFactorIds) { this.selectedSocialFactorIds = selectedSocialFactorIds; }
	public String getSocialFactorOthersValue() { return socialFactorOthersValue; }
	public void setSocialFactorOthersValue(String socialFactorOthersValue) { this.socialFactorOthersValue = socialFactorOthersValue; }
	public BigDecimal getLastMeasuredWeight() { return lastMeasuredWeight; }
	public void setLastMeasuredWeight(BigDecimal lastMeasuredWeight) { this.lastMeasuredWeight = lastMeasuredWeight; }
	public String getLastMeasuredDate() { return lastMeasuredDate; }
	public void setLastMeasuredDate(String lastMeasuredDate) { this.lastMeasuredDate = lastMeasuredDate; }
	public String getFormStatus() { return formStatus; }
	public void setFormStatus(String formStatus) { this.formStatus = formStatus; }
	public List<WeightCaptureDto> getWeightCaptures() { return weightCaptures; }
	public void setWeightCaptures(List<WeightCaptureDto> weightCaptures) { this.weightCaptures = weightCaptures; }
}
