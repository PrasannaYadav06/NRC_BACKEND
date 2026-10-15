package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.ComorbidConditionValue + nrcapp.entity.ComorbidConditionValue (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "comorbid_condition_values", schema = "config")
public class ComorbidConditionValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comorbid_id")
	private int comorbidId;

	@Column(name = "comorbid_condition")
	private String comorbidCondition;

	@Column(name = "created_on")
	private Timestamp createdOn;

	private String description;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;


	public int getComorbidId() {
		return comorbidId;
	}

	public void setComorbidId(int comorbidId) {
		this.comorbidId = comorbidId;
	}

	public String getComorbidCondition() {
		return comorbidCondition;
	}

	public void setComorbidCondition(String comorbidCondition) {
		this.comorbidCondition = comorbidCondition;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}
}
