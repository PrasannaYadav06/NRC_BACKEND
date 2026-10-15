package com.hfw.nrc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * New geographic master data (item 3, V22 migration) - not present in either
 * source app. Read-only reference data (never inserted via JPA, same as
 * District), so no @GeneratedValue even though the underlying column is
 * IDENTITY - matches District's own convention.
 */
@Entity
@Table(name = "taluka", schema = "config")
public class Taluka {

	@Id
	@Column(name = "taluka_id")
	private Integer talukaId;

	@Column(name = "taluka_name")
	private String talukaName;

	@Column(name = "district_id")
	private Integer districtId;

	public Integer getTalukaId() {
		return talukaId;
	}

	public void setTalukaId(Integer talukaId) {
		this.talukaId = talukaId;
	}

	public String getTalukaName() {
		return talukaName;
	}

	public void setTalukaName(String talukaName) {
		this.talukaName = talukaName;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}
}
