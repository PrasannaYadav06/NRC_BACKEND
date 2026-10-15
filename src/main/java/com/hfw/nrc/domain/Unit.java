package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.Unit + nrcapp.entity.Unit (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "unit", schema = "config")
public class Unit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "unit_id")
	private int unitId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	private String description;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	private String unit;


	public int getUnitId() {
		return unitId;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
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

	public int getHId() {
		return hId;
	}

	public void setHId(int hId) {
		this.hId = hId;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
