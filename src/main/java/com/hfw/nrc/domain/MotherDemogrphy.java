package com.hfw.nrc.domain;

import java.util.Date;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Consolidated from nrcadmin.entity.MotherDemogrphy + nrcapp.entity.MotherDemogrphy (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "mother_demogrphy", schema = "nr")
public class MotherDemogrphy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mother_id")
	private int motherId;

	private int age;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Temporal(TemporalType.DATE)
	@Column(name = "date_of_birth")
	private Date dateOfBirth;

	@Column(name = "education_qualification_id")
	private Integer educationQualificationId;

	@Column(name = "family_planning")
	private Boolean familyPlanning;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	private String name;

	@Column(name = "rch_id")
	private Long rchId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getMotherId() {
		return motherId;
	}

	public void setMotherId(int motherId) {
		this.motherId = motherId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Integer getEducationQualificationId() {
		return educationQualificationId;
	}

	public void setEducationQualificationId(Integer educationQualificationId) {
		this.educationQualificationId = educationQualificationId;
	}

	public Boolean getFamilyPlanning() {
		return familyPlanning;
	}

	public void setFamilyPlanning(Boolean familyPlanning) {
		this.familyPlanning = familyPlanning;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getRchId() {
		return rchId;
	}

	public void setRchId(Long rchId) {
		this.rchId = rchId;
	}

	public NrcUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(NrcUser createdBy) {
		this.createdBy = createdBy;
	}

	public NrcUser getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(NrcUser lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}
}
