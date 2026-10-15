package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stage 6 - one row per (hospital, month, year) HR submission, replacing
 * nr.hospital_details' narrow EAV shape (one row per hr_type_id) with
 * KABHI's flattened-columns convention. See db/migration/V15 for the full
 * rationale, including the MSW-category history gap this closes.
 */
@Entity
@Table(name = "nrc_hr", schema = "nr")
public class NrcHr {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hr_rec_id")
	private int hrRecId;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "month")
	private int month;

	@Column(name = "year")
	private int year;

	@Column(name = "number_of_beds")
	private Integer numberOfBeds;

	@Column(name = "officer_sanctioned") private Integer officerSanctioned;
	@Column(name = "officer_working") private Integer officerWorking;
	@Column(name = "officer_vacant") private Integer officerVacant;
	@Column(name = "officer_training") private Integer officerTraining;

	@Column(name = "nurse_sanctioned") private Integer nurseSanctioned;
	@Column(name = "nurse_working") private Integer nurseWorking;
	@Column(name = "nurse_vacant") private Integer nurseVacant;
	@Column(name = "nurse_training") private Integer nurseTraining;

	@Column(name = "dietician_sanctioned") private Integer dieticianSanctioned;
	@Column(name = "dietician_working") private Integer dieticianWorking;
	@Column(name = "dietician_vacant") private Integer dieticianVacant;
	@Column(name = "dietician_training") private Integer dieticianTraining;

	@Column(name = "cook_sanctioned") private Integer cookSanctioned;
	@Column(name = "cook_working") private Integer cookWorking;
	@Column(name = "cook_vacant") private Integer cookVacant;
	@Column(name = "cook_training") private Integer cookTraining;

	@Column(name = "tender_sanctioned") private Integer tenderSanctioned;
	@Column(name = "tender_working") private Integer tenderWorking;
	@Column(name = "tender_vacant") private Integer tenderVacant;
	@Column(name = "tender_training") private Integer tenderTraining;

	@Column(name = "msw_sanctioned") private Integer mswSanctioned;
	@Column(name = "msw_working") private Integer mswWorking;
	@Column(name = "msw_vacant") private Integer mswVacant;
	@Column(name = "msw_training") private Integer mswTraining;

	@Column(name = "legacy_details_ids")
	private String legacyDetailsIds;

	@Column(name = "created_on")
	private Timestamp createdOn;
	@Column(name = "created_by")
	private Integer createdBy;
	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;
	@Column(name = "last_updated_by")
	private Integer lastUpdatedBy;

	public int getHrRecId() { return hrRecId; }
	public void setHrRecId(int hrRecId) { this.hrRecId = hrRecId; }
	public int getHId() { return hId; }
	public void setHId(int hId) { this.hId = hId; }
	public int getMonth() { return month; }
	public void setMonth(int month) { this.month = month; }
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	public Integer getNumberOfBeds() { return numberOfBeds; }
	public void setNumberOfBeds(Integer numberOfBeds) { this.numberOfBeds = numberOfBeds; }

	public Integer getOfficerSanctioned() { return officerSanctioned; }
	public void setOfficerSanctioned(Integer v) { this.officerSanctioned = v; }
	public Integer getOfficerWorking() { return officerWorking; }
	public void setOfficerWorking(Integer v) { this.officerWorking = v; }
	public Integer getOfficerVacant() { return officerVacant; }
	public void setOfficerVacant(Integer v) { this.officerVacant = v; }
	public Integer getOfficerTraining() { return officerTraining; }
	public void setOfficerTraining(Integer v) { this.officerTraining = v; }

	public Integer getNurseSanctioned() { return nurseSanctioned; }
	public void setNurseSanctioned(Integer v) { this.nurseSanctioned = v; }
	public Integer getNurseWorking() { return nurseWorking; }
	public void setNurseWorking(Integer v) { this.nurseWorking = v; }
	public Integer getNurseVacant() { return nurseVacant; }
	public void setNurseVacant(Integer v) { this.nurseVacant = v; }
	public Integer getNurseTraining() { return nurseTraining; }
	public void setNurseTraining(Integer v) { this.nurseTraining = v; }

	public Integer getDieticianSanctioned() { return dieticianSanctioned; }
	public void setDieticianSanctioned(Integer v) { this.dieticianSanctioned = v; }
	public Integer getDieticianWorking() { return dieticianWorking; }
	public void setDieticianWorking(Integer v) { this.dieticianWorking = v; }
	public Integer getDieticianVacant() { return dieticianVacant; }
	public void setDieticianVacant(Integer v) { this.dieticianVacant = v; }
	public Integer getDieticianTraining() { return dieticianTraining; }
	public void setDieticianTraining(Integer v) { this.dieticianTraining = v; }

	public Integer getCookSanctioned() { return cookSanctioned; }
	public void setCookSanctioned(Integer v) { this.cookSanctioned = v; }
	public Integer getCookWorking() { return cookWorking; }
	public void setCookWorking(Integer v) { this.cookWorking = v; }
	public Integer getCookVacant() { return cookVacant; }
	public void setCookVacant(Integer v) { this.cookVacant = v; }
	public Integer getCookTraining() { return cookTraining; }
	public void setCookTraining(Integer v) { this.cookTraining = v; }

	public Integer getTenderSanctioned() { return tenderSanctioned; }
	public void setTenderSanctioned(Integer v) { this.tenderSanctioned = v; }
	public Integer getTenderWorking() { return tenderWorking; }
	public void setTenderWorking(Integer v) { this.tenderWorking = v; }
	public Integer getTenderVacant() { return tenderVacant; }
	public void setTenderVacant(Integer v) { this.tenderVacant = v; }
	public Integer getTenderTraining() { return tenderTraining; }
	public void setTenderTraining(Integer v) { this.tenderTraining = v; }

	public Integer getMswSanctioned() { return mswSanctioned; }
	public void setMswSanctioned(Integer v) { this.mswSanctioned = v; }
	public Integer getMswWorking() { return mswWorking; }
	public void setMswWorking(Integer v) { this.mswWorking = v; }
	public Integer getMswVacant() { return mswVacant; }
	public void setMswVacant(Integer v) { this.mswVacant = v; }
	public Integer getMswTraining() { return mswTraining; }
	public void setMswTraining(Integer v) { this.mswTraining = v; }

	public String getLegacyDetailsIds() { return legacyDetailsIds; }
	public void setLegacyDetailsIds(String legacyDetailsIds) { this.legacyDetailsIds = legacyDetailsIds; }

	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }
	public Integer getCreatedBy() { return createdBy; }
	public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
	public Timestamp getLastUpdatedOn() { return lastUpdatedOn; }
	public void setLastUpdatedOn(Timestamp lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }
	public Integer getLastUpdatedBy() { return lastUpdatedBy; }
	public void setLastUpdatedBy(Integer lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
