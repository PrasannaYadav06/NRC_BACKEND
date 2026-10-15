package com.hfw.nrc.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Consolidated from nrcadmin.entity.WflRefFromNrc + nrcapp.entity.WflRefFromNrc (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "wfl_ref_from_nrc", schema = "config")
public class WflRefFromNrc {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rec_id")
	private int recId;

	private String gender;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "length_cm")
	private BigDecimal lengthCm;

	private BigDecimal median;

	@Column(name = "minus_1_sd")
	private BigDecimal minus1Sd;

	@Column(name = "minus_2_sd")
	private BigDecimal minus2Sd;

	@Column(name = "minus_3_sd")
	private BigDecimal minus3Sd;

	@Column(name = "minus_4_sd")
	private BigDecimal minus4Sd;


	public int getRecId() {
		return recId;
	}

	public void setRecId(int recId) {
		this.recId = recId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public BigDecimal getLengthCm() {
		return lengthCm;
	}

	public void setLengthCm(BigDecimal lengthCm) {
		this.lengthCm = lengthCm;
	}

	public BigDecimal getMedian() {
		return median;
	}

	public void setMedian(BigDecimal median) {
		this.median = median;
	}

	public BigDecimal getMinus1Sd() {
		return minus1Sd;
	}

	public void setMinus1Sd(BigDecimal minus1Sd) {
		this.minus1Sd = minus1Sd;
	}

	public BigDecimal getMinus2Sd() {
		return minus2Sd;
	}

	public void setMinus2Sd(BigDecimal minus2Sd) {
		this.minus2Sd = minus2Sd;
	}

	public BigDecimal getMinus3Sd() {
		return minus3Sd;
	}

	public void setMinus3Sd(BigDecimal minus3Sd) {
		this.minus3Sd = minus3Sd;
	}

	public BigDecimal getMinus4Sd() {
		return minus4Sd;
	}

	public void setMinus4Sd(BigDecimal minus4Sd) {
		this.minus4Sd = minus4Sd;
	}
}
