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
 * Consolidated from nrcadmin.entity.HrTypeConfig + nrcapp.entity.HrTypeConfig (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "hr_type_config", schema = "config")
public class HrTypeConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hr_type_id")
	private int hrTypeId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	private String description;

	@Column(name = "hr_type")
	private String hrType;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hr_type_value_id")
	private HrTypeValueConfig hrTypeValueConfig;


	public int getHrTypeId() {
		return hrTypeId;
	}

	public void setHrTypeId(int hrTypeId) {
		this.hrTypeId = hrTypeId;
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

	public String getHrType() {
		return hrType;
	}

	public void setHrType(String hrType) {
		this.hrType = hrType;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public HrTypeValueConfig getHrTypeValueConfig() {
		return hrTypeValueConfig;
	}

	public void setHrTypeValueConfig(HrTypeValueConfig hrTypeValueConfig) {
		this.hrTypeValueConfig = hrTypeValueConfig;
	}
}
