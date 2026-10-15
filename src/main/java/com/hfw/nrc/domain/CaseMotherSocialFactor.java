package com.hfw.nrc.domain;

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

/**
 * Consolidated from nrcadmin.entity.CaseMotherSocialFactor + nrcapp.entity.CaseMotherSocialFactor (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "case_mother_social_factors", schema = "nr")
public class CaseMotherSocialFactor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rec_id")
	private int recId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "others_value")
	private String othersValue;

	@Column(name = "social_factors_id")
	private Integer socialFactorsId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id")
	private CaseDetail caseDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private NrcUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_updated_by")
	private NrcUser lastUpdatedBy;


	public int getRecId() {
		return recId;
	}

	public void setRecId(int recId) {
		this.recId = recId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getOthersValue() {
		return othersValue;
	}

	public void setOthersValue(String othersValue) {
		this.othersValue = othersValue;
	}

	public Integer getSocialFactorsId() {
		return socialFactorsId;
	}

	public void setSocialFactorsId(Integer socialFactorsId) {
		this.socialFactorsId = socialFactorsId;
	}

	public CaseDetail getCaseDetail() {
		return caseDetail;
	}

	public void setCaseDetail(CaseDetail caseDetail) {
		this.caseDetail = caseDetail;
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
