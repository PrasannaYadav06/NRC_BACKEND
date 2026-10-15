package com.hfw.nrc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Master data for the NRC Hospital Registration form's PHC dropdown - see db/migration/V25. Read-only reference data, same convention as District/Taluka (no @GeneratedValue). */
@Entity
@Table(name = "phc", schema = "config")
public class Phc {

	@Id
	@Column(name = "phc_id")
	private Integer phcId;

	@Column(name = "phc_name")
	private String phcName;

	@Column(name = "taluka_id")
	private Integer talukaId;

	@Column(name = "district_id")
	private Integer districtId;

	public Integer getPhcId() { return phcId; }
	public void setPhcId(Integer phcId) { this.phcId = phcId; }

	public String getPhcName() { return phcName; }
	public void setPhcName(String phcName) { this.phcName = phcName; }

	public Integer getTalukaId() { return talukaId; }
	public void setTalukaId(Integer talukaId) { this.talukaId = talukaId; }

	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }
}
