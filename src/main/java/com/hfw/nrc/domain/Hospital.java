package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.Hospital + nrcapp.entity.Hospital (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "hospital", schema = "nr")
public class Hospital {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "h_id")
	private int hId;

	private String address;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "h_code")
	private String hCode;

	@Column(name = "land_line_number")
	private String landLineNumber;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "name")
	private String name;

	@Column(name = "name_short")
	private String nameShort;

	@Column(name = "pincode")
	private Integer pincode;

	@Column(name = "district_id")
	private Integer districtId;

	/** DISTRICT or TALUKA - backfilled in Stage 1 from the hospital-credentials Excel (see V-series migration for that stage). */
	@Column(name = "hospital_level")
	private String hospitalLevel;

	/** Item 3 (V22/V23) - nullable; only backfilled for TALUKA-level hospitals whose name matched the Excel source, see V23's javadoc. */
	@Column(name = "taluka_id")
	private Integer talukaId;

	/** NRC Hospital Registration redesign (V26) - nullable, mutually exclusive with chcId, only set for TALUKA-level hospitals registered under a specific PHC. */
	@Column(name = "phc_id")
	private Integer phcId;

	/** Same as phcId, for CHC. */
	@Column(name = "chc_id")
	private Integer chcId;

	/** Karnataka Facilities page's Medical College card (V36) - whether this DISTRICT-level NRC is sited at a government medical college. Explicit flag (not name-pattern-matched at query time) so it stays correct even if a hospital's name/short name changes later. */
	@Column(name = "is_medical_college")
	private boolean isMedicalCollege;

	public boolean isMedicalCollege() {
		return isMedicalCollege;
	}

	public void setMedicalCollege(boolean isMedicalCollege) {
		this.isMedicalCollege = isMedicalCollege;
	}

	public Integer getPhcId() {
		return phcId;
	}

	public void setPhcId(Integer phcId) {
		this.phcId = phcId;
	}

	public Integer getChcId() {
		return chcId;
	}

	public void setChcId(Integer chcId) {
		this.chcId = chcId;
	}

	public String getHospitalLevel() {
		return hospitalLevel;
	}

	public void setHospitalLevel(String hospitalLevel) {
		this.hospitalLevel = hospitalLevel;
	}

	public int getHId() {
		return hId;
	}

	public void setHId(int hId) {
		this.hId = hId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getHCode() {
		return hCode;
	}

	public void setHCode(String hCode) {
		this.hCode = hCode;
	}

	public String getLandLineNumber() {
		return landLineNumber;
	}

	public void setLandLineNumber(String landLineNumber) {
		this.landLineNumber = landLineNumber;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameShort() {
		return nameShort;
	}

	public void setNameShort(String nameShort) {
		this.nameShort = nameShort;
	}

	public Integer getPincode() {
		return pincode;
	}

	public void setPincode(Integer pincode) {
		this.pincode = pincode;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public Integer getTalukaId() {
		return talukaId;
	}

	public void setTalukaId(Integer talukaId) {
		this.talukaId = talukaId;
	}
}
