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
 * Stage 4 of the KABHI-style restructuring - one row per discharge EVENT
 * (not one row per case, unlike NrcAdmission - live data already contains
 * cases with more than one discharge submission, see
 * db/migration/V7__create_nrc_discharge.sql), FK'd to NrcAdmission.caseId.
 * Consolidates discharge_child_details + discharge_comorbid_condition +
 * discharge_mother_details + case_details' discharge_date/discharge_sd/
 * discharged_by_doctor + the discharge-time slice of child_growth/
 * case_mother_growth. See V7-V10 migrations for the full provenance.
 */
@Entity
@Table(name = "nrc_discharge", schema = "nr")
public class NrcDischarge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "discharge_rec_id")
	private int dischargeRecId;

	@Column(name = "case_id")
	private int caseId;

	@Column(name = "discharge_date")
	private Timestamp dischargeDate;

	@Column(name = "discharge_sd")
	private BigDecimal dischargeSd;

	@Column(name = "discharged_by_doctor")
	private Integer dischargedByDoctor;

	@Column(name = "avg_weight_gain_kg")
	private BigDecimal avgWeightGainKg;

	@Column(name = "clinical_impression")
	private String clinicalImpression;

	@Column(name = "outcome_id")
	private Integer outcomeId;

	@Column(name = "treatment_given")
	private String treatmentGiven;

	@Column(name = "medical_advice")
	private String medicalAdvice;

	@Column(name = "dietary_advice")
	private String dietaryAdvice;

	@Column(name = "medical_risk_id")
	private Integer medicalRiskId;

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

	@Column(name = "form_status_at_event")
	private BigDecimal formStatusAtEvent;

	@Column(name = "discharge_weight_kg")
	private BigDecimal dischargeWeightKg;

	@Column(name = "discharge_height_cm")
	private BigDecimal dischargeHeightCm;

	@Column(name = "min_weight_kg")
	private BigDecimal minWeightKg;

	@Temporal(TemporalType.DATE)
	@Column(name = "min_weight_date")
	private Date minWeightDate;

	@Column(name = "mother_weight_kg_discharge")
	private BigDecimal motherWeightKgDischarge;

	@Column(name = "mother_height_cm_discharge")
	private BigDecimal motherHeightCmDischarge;

	@Column(name = "mother_muac_cm_discharge")
	private BigDecimal motherMuacCmDischarge;

	@Column(name = "legacy_case_id")
	private Integer legacyCaseId;

	@Column(name = "legacy_discharge_child_id")
	private Integer legacyDischargeChildId;

	@Column(name = "legacy_discharge_mother_id")
	private Integer legacyDischargeMotherId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "last_updated_by")
	private Integer lastUpdatedBy;

	public int getDischargeRecId() { return dischargeRecId; }
	public void setDischargeRecId(int dischargeRecId) { this.dischargeRecId = dischargeRecId; }

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	public Timestamp getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(Timestamp dischargeDate) { this.dischargeDate = dischargeDate; }

	public BigDecimal getDischargeSd() { return dischargeSd; }
	public void setDischargeSd(BigDecimal dischargeSd) { this.dischargeSd = dischargeSd; }

	public Integer getDischargedByDoctor() { return dischargedByDoctor; }
	public void setDischargedByDoctor(Integer dischargedByDoctor) { this.dischargedByDoctor = dischargedByDoctor; }

	public BigDecimal getAvgWeightGainKg() { return avgWeightGainKg; }
	public void setAvgWeightGainKg(BigDecimal avgWeightGainKg) { this.avgWeightGainKg = avgWeightGainKg; }

	public String getClinicalImpression() { return clinicalImpression; }
	public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }

	public Integer getOutcomeId() { return outcomeId; }
	public void setOutcomeId(Integer outcomeId) { this.outcomeId = outcomeId; }

	public String getTreatmentGiven() { return treatmentGiven; }
	public void setTreatmentGiven(String treatmentGiven) { this.treatmentGiven = treatmentGiven; }

	public String getMedicalAdvice() { return medicalAdvice; }
	public void setMedicalAdvice(String medicalAdvice) { this.medicalAdvice = medicalAdvice; }

	public String getDietaryAdvice() { return dietaryAdvice; }
	public void setDietaryAdvice(String dietaryAdvice) { this.dietaryAdvice = dietaryAdvice; }

	public Integer getMedicalRiskId() { return medicalRiskId; }
	public void setMedicalRiskId(Integer medicalRiskId) { this.medicalRiskId = medicalRiskId; }

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

	public BigDecimal getFormStatusAtEvent() { return formStatusAtEvent; }
	public void setFormStatusAtEvent(BigDecimal formStatusAtEvent) { this.formStatusAtEvent = formStatusAtEvent; }

	public BigDecimal getDischargeWeightKg() { return dischargeWeightKg; }
	public void setDischargeWeightKg(BigDecimal dischargeWeightKg) { this.dischargeWeightKg = dischargeWeightKg; }

	public BigDecimal getDischargeHeightCm() { return dischargeHeightCm; }
	public void setDischargeHeightCm(BigDecimal dischargeHeightCm) { this.dischargeHeightCm = dischargeHeightCm; }

	public BigDecimal getMinWeightKg() { return minWeightKg; }
	public void setMinWeightKg(BigDecimal minWeightKg) { this.minWeightKg = minWeightKg; }

	public Date getMinWeightDate() { return minWeightDate; }
	public void setMinWeightDate(Date minWeightDate) { this.minWeightDate = minWeightDate; }

	public BigDecimal getMotherWeightKgDischarge() { return motherWeightKgDischarge; }
	public void setMotherWeightKgDischarge(BigDecimal motherWeightKgDischarge) { this.motherWeightKgDischarge = motherWeightKgDischarge; }

	public BigDecimal getMotherHeightCmDischarge() { return motherHeightCmDischarge; }
	public void setMotherHeightCmDischarge(BigDecimal motherHeightCmDischarge) { this.motherHeightCmDischarge = motherHeightCmDischarge; }

	public BigDecimal getMotherMuacCmDischarge() { return motherMuacCmDischarge; }
	public void setMotherMuacCmDischarge(BigDecimal motherMuacCmDischarge) { this.motherMuacCmDischarge = motherMuacCmDischarge; }

	public Integer getLegacyCaseId() { return legacyCaseId; }
	public void setLegacyCaseId(Integer legacyCaseId) { this.legacyCaseId = legacyCaseId; }

	public Integer getLegacyDischargeChildId() { return legacyDischargeChildId; }
	public void setLegacyDischargeChildId(Integer legacyDischargeChildId) { this.legacyDischargeChildId = legacyDischargeChildId; }

	public Integer getLegacyDischargeMotherId() { return legacyDischargeMotherId; }
	public void setLegacyDischargeMotherId(Integer legacyDischargeMotherId) { this.legacyDischargeMotherId = legacyDischargeMotherId; }

	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }

	public Integer getCreatedBy() { return createdBy; }
	public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

	public Timestamp getLastUpdatedOn() { return lastUpdatedOn; }
	public void setLastUpdatedOn(Timestamp lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }

	public Integer getLastUpdatedBy() { return lastUpdatedBy; }
	public void setLastUpdatedBy(Integer lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
