package com.hfw.nrc.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stage 6 (revised) - admin-editable sanctioned-post-count master, one row
 * per (hospital, designation). Ported from KABHI's KabhiHrSanctionedEntity/
 * tbl_kabhi_hr_sanctioned, but scoped per-hospital rather than globally per
 * designation - see db/migration/V17 for why. Seeded with a default of 1 by
 * NrcHrPostService on first use if a row is missing, so the app works
 * before anyone edits anything.
 */
@Entity
@Table(name = "nrc_hr_sanctioned", schema = "nr")
public class NrcHrSanctioned {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hr_sanctioned_id")
	private Integer hrSanctionedId;

	@Column(name = "h_id")
	private int hId;

	@Column(name = "designation")
	private String designation;

	@Column(name = "sanctioned_count")
	private int sanctionedCount;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "updated_date")
	private LocalDateTime updatedDate;

	public Integer getHrSanctionedId() { return hrSanctionedId; }
	public void setHrSanctionedId(Integer hrSanctionedId) { this.hrSanctionedId = hrSanctionedId; }
	public int getHId() { return hId; }
	public void setHId(int hId) { this.hId = hId; }
	public String getDesignation() { return designation; }
	public void setDesignation(String designation) { this.designation = designation; }
	public int getSanctionedCount() { return sanctionedCount; }
	public void setSanctionedCount(int sanctionedCount) { this.sanctionedCount = sanctionedCount; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	public LocalDateTime getUpdatedDate() { return updatedDate; }
	public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
