package com.hfw.nrc.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Stage 3 of the KABHI-style restructuring - one row per In-Patient Care
 * admission, consolidating what used to be 11 separate tables (case_details,
 * child_demography, mother_demogrphy, case_allocation, case_referral,
 * case_mother_details, admission-time slices of case_mother_growth/
 * child_growth, and the flattened multi-select tables admission_criteria/
 * admission_comorbid_condition/case_mother_social_factors). See
 * db/migration/V4__create_nrc_admission.sql for the full column-by-column
 * provenance and db/migration/V5__populate_nrc_admission.sql for the backfill
 * that populated this from the (untouched, still-live) old tables.
 *
 * Deliberately flat (no @ManyToOne relations) - matches KABHI's own
 * minimal-relationship convention (see CaseDetail's javadoc) and this entity
 * already carries ~90 columns; adding lazy-loaded joins on top would only
 * add N+1 risk for no benefit, since every field the UI needs is already
 * denormalized onto this one row.
 *
 * Discharge-specific data (discharge date, discharge measurements) is
 * intentionally NOT on this entity - it lives on NrcDischarge (Stage 4),
 * FK'd back to caseId. formStatus reaching 4.x is what marks "discharged"
 * here.
 */
@Entity
@Table(name = "nrc_admission", schema = "nr")
public class NrcAdmission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "case_id")
	private int caseId;

	@Column(name = "h_id")
	private Integer hId;

	@Column(name = "ip_num")
	private Long ipNum;

	@Column(name = "uh_id")
	private Long uhId;

	@Column(name = "sam_num")
	private Integer samNum;

	@Column(name = "fiscal_year")
	private String fiscalYear;

	@Column(name = "admission_type_id")
	private Integer admissionTypeId;

	@Column(name = "admission_date")
	private Timestamp admissionDate;

	@Column(name = "target_weight")
	private BigDecimal targetWeight;

	@Column(name = "admission_sd")
	private BigDecimal admissionSd;

	@Column(name = "addmited_by_doctor")
	private Integer addmitedByDoctor;

	@Column(name = "target_weight_achieved")
	private Boolean targetWeightAchieved;

	// ===== Child demography =====

	@Column(name = "child_rch_id")
	private Long childRchId;

	@Column(name = "child_name")
	private String childName;

	@Temporal(TemporalType.DATE)
	@Column(name = "child_date_of_birth")
	private Date childDateOfBirth;

	@Column(name = "child_age_in_months")
	private Integer childAgeInMonths;

	@Column(name = "child_gender")
	private String childGender;

	@Column(name = "child_address_line1")
	private String childAddressLine1;

	@Column(name = "child_address_line2")
	private String childAddressLine2;

	@Column(name = "child_address_line3")
	private String childAddressLine3;

	@Column(name = "child_mobile_number")
	private String childMobileNumber;

	@Column(name = "child_alternate_mobile_number")
	private String childAlternateMobileNumber;

	@Column(name = "child_category")
	private String childCategory;

	@Column(name = "child_religion_id")
	private Integer childReligionId;

	@Column(name = "child_caste_id")
	private Integer childCasteId;

	@Column(name = "child_pincode")
	private Integer childPincode;

	@Column(name = "child_district_id")
	private Integer childDistrictId;

	@Column(name = "child_has_guardian")
	private Boolean childHasGuardian;

	@Column(name = "child_guardian_name")
	private String childGuardianName;

	// ===== Mother demography =====

	@Column(name = "mother_rch_id")
	private Long motherRchId;

	@Column(name = "mother_name")
	private String motherName;

	@Temporal(TemporalType.DATE)
	@Column(name = "mother_date_of_birth")
	private Date motherDateOfBirth;

	@Column(name = "mother_age")
	private Integer motherAge;

	@Column(name = "mother_education_qualification_id")
	private Integer motherEducationQualificationId;

	@Column(name = "mother_family_planning")
	private Boolean motherFamilyPlanning;

	// ===== Allocation =====

	@Column(name = "unit_id")
	private Integer unitId;

	@Column(name = "ward_id")
	private Integer wardId;

	// ===== Referral =====

	@Column(name = "referred_from_id")
	private Integer referredFromId;

	@Column(name = "referred_by")
	private String referredBy;

	@Column(name = "referred_by_m_numb")
	private String referredByMNumb;

	// ===== Mother clinical details =====

	@Column(name = "risk_category_id")
	private Integer riskCategoryId;

	@Column(name = "no_of_children")
	private Integer noOfChildren;

	// Replaces the admission form's Gravida/Para/No. of children fields with
	// a single free-text history field. gravida_id/mother_para/no_of_children
	// columns are kept as-is (non-destructive) - just no longer populated by
	// the current form.
	@Column(name = "obstetric_history", length = 1000)
	private String obstetricHistory;

	@Column(name = "medical_problem")
	private Boolean medicalProblem;

	@Column(name = "medical_problem_details")
	private String medicalProblemDetails;

	@Column(name = "drugs_prescribed")
	private Boolean drugsPrescribed;

	@Column(name = "drugs_prescribed_details")
	private String drugsPrescribedDetails;

	@Column(name = "present_dietary_choice")
	private String presentDietaryChoice;

	@Column(name = "no_of_meals_per_day")
	private Integer noOfMealsPerDay;

	@Column(name = "pregnant_at_present")
	private Boolean pregnantAtPresent;

	@Column(name = "number_of_pregnancy_months")
	private BigDecimal numberOfPregnancyMonths;

	@Column(name = "registered_for_anc")
	private Boolean registeredForAnc;

	@Column(name = "breast_feeding_at_present")
	private Boolean breastFeedingAtPresent;

	@Temporal(TemporalType.DATE)
	@Column(name = "last_menstruation_date")
	private Date lastMenstruationDate;

	// ===== Mother growth, admission-time slice =====

	@Column(name = "mother_weight_kg_admission")
	private BigDecimal motherWeightKgAdmission;

	@Column(name = "mother_height_cm_admission")
	private BigDecimal motherHeightCmAdmission;

	@Column(name = "mother_bmi_admission")
	private BigDecimal motherBmiAdmission;

	@Column(name = "mother_muac_cm_admission")
	private BigDecimal motherMuacCmAdmission;

	@Column(name = "mother_hemoglobin_admission")
	private BigDecimal motherHemoglobinAdmission;

	@Column(name = "mother_neck_circumference_cm_admission")
	private BigDecimal motherNeckCircumferenceCmAdmission;

	@Column(name = "mother_gravida")
	private Integer motherGravida;

	@Column(name = "mother_para")
	private Integer motherPara;

	@Column(name = "mother_blood_sugar_fasting_admission")
	private BigDecimal motherBloodSugarFastingAdmission;

	@Column(name = "mother_blood_sugar_random_admission")
	private BigDecimal motherBloodSugarRandomAdmission;

	@Column(name = "mother_bp_sys_admission")
	private BigDecimal motherBpSysAdmission;

	@Column(name = "mother_bp_dia_admission")
	private BigDecimal motherBpDiaAdmission;

	@Column(name = "mother_growth_advice_admission")
	private String motherGrowthAdviceAdmission;

	// ===== Child growth, admission-time slice =====

	@Column(name = "admission_weight_kg")
	private BigDecimal admissionWeightKg;

	@Column(name = "admission_height_cm")
	private BigDecimal admissionHeightCm;

	// ===== Admission criteria, flattened (config.admission_criteria_values) =====

	@Column(name = "ac_wfl_wfh_lt3z")
	private Boolean acWflWfhLt3z;

	@Column(name = "ac_wfa_lt3z")
	private Boolean acWfaLt3z;

	@Column(name = "ac_muac_lt115")
	private Boolean acMuacLt115;

	@Column(name = "ac_severe_wasting")
	private Boolean acSevereWasting;

	@Column(name = "ac_bl_edema")
	private Boolean acBlEdema;

	@Column(name = "ac_others")
	private Boolean acOthers;

	@Column(name = "ac_others_value")
	private String acOthersValue;

	@Column(name = "ac_muac_115_125")
	private Boolean acMuac115125;

	@Column(name = "ac_wfl_wfh_lt2z")
	private Boolean acWflWfhLt2z;

	@Column(name = "ac_wfl_wfh_lt1z")
	private Boolean acWflWfhLt1z;

	// ===== Comorbid conditions, flattened (config.comorbid_condition_values) =====

	@Column(name = "cc_pneumonia")
	private Boolean ccPneumonia;

	@Column(name = "cc_gastroenteritis")
	private Boolean ccGastroenteritis;

	@Column(name = "cc_sepsis")
	private Boolean ccSepsis;

	@Column(name = "cc_chd")
	private Boolean ccChd;

	@Column(name = "cc_cp")
	private Boolean ccCp;

	@Column(name = "cc_renal_disease")
	private Boolean ccRenalDisease;

	@Column(name = "cc_meningitis")
	private Boolean ccMeningitis;

	@Column(name = "cc_ics")
	private Boolean ccIcs;

	@Column(name = "cc_iem")
	private Boolean ccIem;

	@Column(name = "cc_tuberculosis")
	private Boolean ccTuberculosis;

	@Column(name = "cc_others")
	private Boolean ccOthers;

	@Column(name = "cc_others_value")
	private String ccOthersValue;

	@Column(name = "cc_covid19")
	private Boolean ccCovid19;

	// ===== Social factors, flattened (config.social_factors) =====

	@Column(name = "sf_poverty")
	private Boolean sfPoverty;

	@Column(name = "sf_ignorance")
	private Boolean sfIgnorance;

	@Column(name = "sf_illiteracy")
	private Boolean sfIlliteracy;

	@Column(name = "sf_migrants")
	private Boolean sfMigrants;

	@Column(name = "sf_working_mother")
	private Boolean sfWorkingMother;

	@Column(name = "sf_others")
	private Boolean sfOthers;

	@Column(name = "sf_others_value")
	private String sfOthersValue;

	// ===== Denormalized "latest" summary columns (decision #2) =====

	@Column(name = "last_measured_weight")
	private BigDecimal lastMeasuredWeight;

	@Temporal(TemporalType.DATE)
	@Column(name = "last_measured_date")
	private Date lastMeasuredDate;

	@Column(name = "last_discharge_date")
	private Timestamp lastDischargeDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "last_followup_date")
	private Date lastFollowupDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "next_followup_date")
	private Date nextFollowupDate;

	// form_status state machine: 0=new, 1=first update, 3.x=weight-capture
	// sequence, 4.x=discharge, 5.x=followup.
	@Column(name = "form_status")
	private BigDecimal formStatus;

	// ===== Legacy linkage =====

	@Column(name = "legacy_case_id")
	private Integer legacyCaseId;

	@Column(name = "legacy_child_id")
	private Integer legacyChildId;

	@Column(name = "legacy_mother_id")
	private Integer legacyMotherId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "last_updated_by")
	private Integer lastUpdatedBy;

	// ===== Getters / setters =====

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	public Integer getHId() { return hId; }
	public void setHId(Integer hId) { this.hId = hId; }

	public Long getIpNum() { return ipNum; }
	public void setIpNum(Long ipNum) { this.ipNum = ipNum; }

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public Integer getSamNum() { return samNum; }
	public void setSamNum(Integer samNum) { this.samNum = samNum; }

	public String getFiscalYear() { return fiscalYear; }
	public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

	public Integer getAdmissionTypeId() { return admissionTypeId; }
	public void setAdmissionTypeId(Integer admissionTypeId) { this.admissionTypeId = admissionTypeId; }

	public Timestamp getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(Timestamp admissionDate) { this.admissionDate = admissionDate; }

	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

	public BigDecimal getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(BigDecimal admissionSd) { this.admissionSd = admissionSd; }

	public Integer getAddmitedByDoctor() { return addmitedByDoctor; }
	public void setAddmitedByDoctor(Integer addmitedByDoctor) { this.addmitedByDoctor = addmitedByDoctor; }

	public Boolean getTargetWeightAchieved() { return targetWeightAchieved; }
	public void setTargetWeightAchieved(Boolean targetWeightAchieved) { this.targetWeightAchieved = targetWeightAchieved; }

	public Long getChildRchId() { return childRchId; }
	public void setChildRchId(Long childRchId) { this.childRchId = childRchId; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public Date getChildDateOfBirth() { return childDateOfBirth; }
	public void setChildDateOfBirth(Date childDateOfBirth) { this.childDateOfBirth = childDateOfBirth; }

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

	public Boolean getChildHasGuardian() { return childHasGuardian; }
	public void setChildHasGuardian(Boolean childHasGuardian) { this.childHasGuardian = childHasGuardian; }

	public String getChildGuardianName() { return childGuardianName; }
	public void setChildGuardianName(String childGuardianName) { this.childGuardianName = childGuardianName; }

	public Long getMotherRchId() { return motherRchId; }
	public void setMotherRchId(Long motherRchId) { this.motherRchId = motherRchId; }

	public String getMotherName() { return motherName; }
	public void setMotherName(String motherName) { this.motherName = motherName; }

	public Date getMotherDateOfBirth() { return motherDateOfBirth; }
	public void setMotherDateOfBirth(Date motherDateOfBirth) { this.motherDateOfBirth = motherDateOfBirth; }

	public Integer getMotherAge() { return motherAge; }
	public void setMotherAge(Integer motherAge) { this.motherAge = motherAge; }

	public Integer getMotherEducationQualificationId() { return motherEducationQualificationId; }
	public void setMotherEducationQualificationId(Integer motherEducationQualificationId) { this.motherEducationQualificationId = motherEducationQualificationId; }

	public Boolean getMotherFamilyPlanning() { return motherFamilyPlanning; }
	public void setMotherFamilyPlanning(Boolean motherFamilyPlanning) { this.motherFamilyPlanning = motherFamilyPlanning; }

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

	public Date getLastMenstruationDate() { return lastMenstruationDate; }
	public void setLastMenstruationDate(Date lastMenstruationDate) { this.lastMenstruationDate = lastMenstruationDate; }

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

	public String getMotherGrowthAdviceAdmission() { return motherGrowthAdviceAdmission; }
	public void setMotherGrowthAdviceAdmission(String motherGrowthAdviceAdmission) { this.motherGrowthAdviceAdmission = motherGrowthAdviceAdmission; }

	public BigDecimal getAdmissionWeightKg() { return admissionWeightKg; }
	public void setAdmissionWeightKg(BigDecimal admissionWeightKg) { this.admissionWeightKg = admissionWeightKg; }

	public BigDecimal getAdmissionHeightCm() { return admissionHeightCm; }
	public void setAdmissionHeightCm(BigDecimal admissionHeightCm) { this.admissionHeightCm = admissionHeightCm; }

	public Boolean getAcWflWfhLt3z() { return acWflWfhLt3z; }
	public void setAcWflWfhLt3z(Boolean acWflWfhLt3z) { this.acWflWfhLt3z = acWflWfhLt3z; }

	public Boolean getAcWfaLt3z() { return acWfaLt3z; }
	public void setAcWfaLt3z(Boolean acWfaLt3z) { this.acWfaLt3z = acWfaLt3z; }

	public Boolean getAcMuacLt115() { return acMuacLt115; }
	public void setAcMuacLt115(Boolean acMuacLt115) { this.acMuacLt115 = acMuacLt115; }

	public Boolean getAcSevereWasting() { return acSevereWasting; }
	public void setAcSevereWasting(Boolean acSevereWasting) { this.acSevereWasting = acSevereWasting; }

	public Boolean getAcBlEdema() { return acBlEdema; }
	public void setAcBlEdema(Boolean acBlEdema) { this.acBlEdema = acBlEdema; }

	public Boolean getAcOthers() { return acOthers; }
	public void setAcOthers(Boolean acOthers) { this.acOthers = acOthers; }

	public String getAcOthersValue() { return acOthersValue; }
	public void setAcOthersValue(String acOthersValue) { this.acOthersValue = acOthersValue; }

	public Boolean getAcMuac115125() { return acMuac115125; }
	public void setAcMuac115125(Boolean acMuac115125) { this.acMuac115125 = acMuac115125; }

	public Boolean getAcWflWfhLt2z() { return acWflWfhLt2z; }
	public void setAcWflWfhLt2z(Boolean acWflWfhLt2z) { this.acWflWfhLt2z = acWflWfhLt2z; }

	public Boolean getAcWflWfhLt1z() { return acWflWfhLt1z; }
	public void setAcWflWfhLt1z(Boolean acWflWfhLt1z) { this.acWflWfhLt1z = acWflWfhLt1z; }

	public Boolean getCcPneumonia() { return ccPneumonia; }
	public void setCcPneumonia(Boolean ccPneumonia) { this.ccPneumonia = ccPneumonia; }

	public Boolean getCcGastroenteritis() { return ccGastroenteritis; }
	public void setCcGastroenteritis(Boolean ccGastroenteritis) { this.ccGastroenteritis = ccGastroenteritis; }

	public Boolean getCcSepsis() { return ccSepsis; }
	public void setCcSepsis(Boolean ccSepsis) { this.ccSepsis = ccSepsis; }

	public Boolean getCcChd() { return ccChd; }
	public void setCcChd(Boolean ccChd) { this.ccChd = ccChd; }

	public Boolean getCcCp() { return ccCp; }
	public void setCcCp(Boolean ccCp) { this.ccCp = ccCp; }

	public Boolean getCcRenalDisease() { return ccRenalDisease; }
	public void setCcRenalDisease(Boolean ccRenalDisease) { this.ccRenalDisease = ccRenalDisease; }

	public Boolean getCcMeningitis() { return ccMeningitis; }
	public void setCcMeningitis(Boolean ccMeningitis) { this.ccMeningitis = ccMeningitis; }

	public Boolean getCcIcs() { return ccIcs; }
	public void setCcIcs(Boolean ccIcs) { this.ccIcs = ccIcs; }

	public Boolean getCcIem() { return ccIem; }
	public void setCcIem(Boolean ccIem) { this.ccIem = ccIem; }

	public Boolean getCcTuberculosis() { return ccTuberculosis; }
	public void setCcTuberculosis(Boolean ccTuberculosis) { this.ccTuberculosis = ccTuberculosis; }

	public Boolean getCcOthers() { return ccOthers; }
	public void setCcOthers(Boolean ccOthers) { this.ccOthers = ccOthers; }

	public String getCcOthersValue() { return ccOthersValue; }
	public void setCcOthersValue(String ccOthersValue) { this.ccOthersValue = ccOthersValue; }

	public Boolean getCcCovid19() { return ccCovid19; }
	public void setCcCovid19(Boolean ccCovid19) { this.ccCovid19 = ccCovid19; }

	public Boolean getSfPoverty() { return sfPoverty; }
	public void setSfPoverty(Boolean sfPoverty) { this.sfPoverty = sfPoverty; }

	public Boolean getSfIgnorance() { return sfIgnorance; }
	public void setSfIgnorance(Boolean sfIgnorance) { this.sfIgnorance = sfIgnorance; }

	public Boolean getSfIlliteracy() { return sfIlliteracy; }
	public void setSfIlliteracy(Boolean sfIlliteracy) { this.sfIlliteracy = sfIlliteracy; }

	public Boolean getSfMigrants() { return sfMigrants; }
	public void setSfMigrants(Boolean sfMigrants) { this.sfMigrants = sfMigrants; }

	public Boolean getSfWorkingMother() { return sfWorkingMother; }
	public void setSfWorkingMother(Boolean sfWorkingMother) { this.sfWorkingMother = sfWorkingMother; }

	public Boolean getSfOthers() { return sfOthers; }
	public void setSfOthers(Boolean sfOthers) { this.sfOthers = sfOthers; }

	public String getSfOthersValue() { return sfOthersValue; }
	public void setSfOthersValue(String sfOthersValue) { this.sfOthersValue = sfOthersValue; }

	public BigDecimal getLastMeasuredWeight() { return lastMeasuredWeight; }
	public void setLastMeasuredWeight(BigDecimal lastMeasuredWeight) { this.lastMeasuredWeight = lastMeasuredWeight; }

	public Date getLastMeasuredDate() { return lastMeasuredDate; }
	public void setLastMeasuredDate(Date lastMeasuredDate) { this.lastMeasuredDate = lastMeasuredDate; }

	public Timestamp getLastDischargeDate() { return lastDischargeDate; }
	public void setLastDischargeDate(Timestamp lastDischargeDate) { this.lastDischargeDate = lastDischargeDate; }

	public Date getLastFollowupDate() { return lastFollowupDate; }
	public void setLastFollowupDate(Date lastFollowupDate) { this.lastFollowupDate = lastFollowupDate; }

	public Date getNextFollowupDate() { return nextFollowupDate; }
	public void setNextFollowupDate(Date nextFollowupDate) { this.nextFollowupDate = nextFollowupDate; }

	public BigDecimal getFormStatus() { return formStatus; }
	public void setFormStatus(BigDecimal formStatus) { this.formStatus = formStatus; }

	public Integer getLegacyCaseId() { return legacyCaseId; }
	public void setLegacyCaseId(Integer legacyCaseId) { this.legacyCaseId = legacyCaseId; }

	public Integer getLegacyChildId() { return legacyChildId; }
	public void setLegacyChildId(Integer legacyChildId) { this.legacyChildId = legacyChildId; }

	public Integer getLegacyMotherId() { return legacyMotherId; }
	public void setLegacyMotherId(Integer legacyMotherId) { this.legacyMotherId = legacyMotherId; }

	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }

	public Integer getCreatedBy() { return createdBy; }
	public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

	public Timestamp getLastUpdatedOn() { return lastUpdatedOn; }
	public void setLastUpdatedOn(Timestamp lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }

	public Integer getLastUpdatedBy() { return lastUpdatedBy; }
	public void setLastUpdatedBy(Integer lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
