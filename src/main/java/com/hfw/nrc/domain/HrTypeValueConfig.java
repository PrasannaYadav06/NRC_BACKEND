package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.HrTypeValueConfig + nrcapp.entity.HrTypeValueConfig (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "hr_type_value_config", schema = "config")
public class HrTypeValueConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hr_type_value_id")
	private int hrTypeValueId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	private String description;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "value_type")
	private String valueType;


	public int getHrTypeValueId() {
		return hrTypeValueId;
	}

	public void setHrTypeValueId(int hrTypeValueId) {
		this.hrTypeValueId = hrTypeValueId;
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

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
}
