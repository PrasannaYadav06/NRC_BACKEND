package com.hfw.nrc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Master data for the NRC Hospital Registration form's CHC dropdown - see db/migration/V25. Read-only reference data, same convention as District/Taluka (no @GeneratedValue). */
@Entity
@Table(name = "chc", schema = "config")
public class Chc {

	@Id
	@Column(name = "chc_id")
	private Integer chcId;

	@Column(name = "chc_name")
	private String chcName;

	@Column(name = "taluka_id")
	private Integer talukaId;

	@Column(name = "district_id")
	private Integer districtId;

	public Integer getChcId() { return chcId; }
	public void setChcId(Integer chcId) { this.chcId = chcId; }

	public String getChcName() { return chcName; }
	public void setChcName(String chcName) { this.chcName = chcName; }

	public Integer getTalukaId() { return talukaId; }
	public void setTalukaId(Integer talukaId) { this.talukaId = talukaId; }

	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }
}
