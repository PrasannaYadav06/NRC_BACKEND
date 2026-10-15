package com.hfw.nrc.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.FollowupConfig + nrcapp.entity.FollowupConfig (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "followup_config", schema = "config")
public class FollowupConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "followup_id")
	private int followupId;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "days_after_discharge")
	private int daysAfterDischarge;

	private String description;

	@Column(name = "followup_name")
	private String followupName;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;


	public int getFollowupId() {
		return followupId;
	}

	public void setFollowupId(int followupId) {
		this.followupId = followupId;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public int getDaysAfterDischarge() {
		return daysAfterDischarge;
	}

	public void setDaysAfterDischarge(int daysAfterDischarge) {
		this.daysAfterDischarge = daysAfterDischarge;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFollowupName() {
		return followupName;
	}

	public void setFollowupName(String followupName) {
		this.followupName = followupName;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}
}
