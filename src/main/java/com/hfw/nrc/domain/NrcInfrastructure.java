package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Infrastructure Module scaffold (V35) - one row per hospital. `details` is
 * deliberately a single free-text field rather than a fixed set of columns:
 * the Department hasn't shared its reporting format/guidelines yet (per the
 * user's own ask), so this is a generic placeholder meant to be extended
 * with real structured fields once that format arrives, not a final shape.
 */
@Entity
@Table(name = "nrc_infrastructure", schema = "nr")
public class NrcInfrastructure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "infra_id")
	private int infraId;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "details")
	private String details;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "last_updated_by")
	private Integer lastUpdatedBy;

	public int getInfraId() {
		return infraId;
	}

	public void setInfraId(int infraId) {
		this.infraId = infraId;
	}

	public int getHId() {
		return hId;
	}

	public void setHId(int hId) {
		this.hId = hId;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public Integer getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Integer lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}
}
