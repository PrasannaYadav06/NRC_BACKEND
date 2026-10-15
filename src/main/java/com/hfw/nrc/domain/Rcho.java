package com.hfw.nrc.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * One row per district (see db/migration/V26). An RCHO's hospital scope is
 * derived by joining nr.hospital.district_id = nr.rcho.district_id - not a
 * separate hospital-link column, since every hospital already carries its
 * own district_id.
 */
@Entity
@Table(name = "rcho", schema = "nr")
public class Rcho {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rcho_id")
	private int rchoId;

	@Column(name = "district_id")
	private int districtId;

	@Column(name = "u_id")
	private int uId;

	@Column(name = "rcho_name")
	private String rchoName;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	public int getRchoId() { return rchoId; }
	public void setRchoId(int rchoId) { this.rchoId = rchoId; }

	public int getDistrictId() { return districtId; }
	public void setDistrictId(int districtId) { this.districtId = districtId; }

	public int getUId() { return uId; }
	public void setUId(int uId) { this.uId = uId; }

	public String getRchoName() { return rchoName; }
	public void setRchoName(String rchoName) { this.rchoName = rchoName; }

	public Timestamp getCreatedOn() { return createdOn; }
	public void setCreatedOn(Timestamp createdOn) { this.createdOn = createdOn; }

	public Timestamp getLastUpdatedOn() { return lastUpdatedOn; }
	public void setLastUpdatedOn(Timestamp lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }
}
