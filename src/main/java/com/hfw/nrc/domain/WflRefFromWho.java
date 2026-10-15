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
 * Consolidated from nrcadmin.entity.WflRefFromWho + nrcapp.entity.WflRefFromWho (Phase 2).
 * Audit-trail back-reference collections (@OneToMany createdBy/lastUpdatedBy
 * etc. present in both source entities) are intentionally dropped - they're
 * Hibernate navigation conveniences with no data-integrity purpose; the
 * service layer queries child tables directly instead, matching KABHI's own
 * minimal-relationship convention.
 */
@Entity
@Table(name = "wfl_ref_from_who", schema = "config")
public class WflRefFromWho {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rec_id")
	private int recId;

	private String gender;

	private String l;

	@Column(name = "last_updated_on")
	private Timestamp lastUpdatedOn;

	@Column(name = "length_cm")
	private BigDecimal lengthCm;

	private String m;

	private BigDecimal median;

	@Column(name = "minus_1_sd")
	private BigDecimal minus1Sd;

	@Column(name = "minus_2_sd")
	private BigDecimal minus2Sd;

	@Column(name = "minus_3_sd")
	private BigDecimal minus3Sd;

	@Column(name = "plus_1_sd")
	private BigDecimal plus1Sd;

	@Column(name = "plus_2_sd")
	private BigDecimal plus2Sd;

	@Column(name = "plus_3_sd")
	private BigDecimal plus3Sd;

	private String s;


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

	public String getL() {
		return l;
	}

	public void setL(String l) {
		this.l = l;
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

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
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

	public BigDecimal getPlus1Sd() {
		return plus1Sd;
	}

	public void setPlus1Sd(BigDecimal plus1Sd) {
		this.plus1Sd = plus1Sd;
	}

	public BigDecimal getPlus2Sd() {
		return plus2Sd;
	}

	public void setPlus2Sd(BigDecimal plus2Sd) {
		this.plus2Sd = plus2Sd;
	}

	public BigDecimal getPlus3Sd() {
		return plus3Sd;
	}

	public void setPlus3Sd(BigDecimal plus3Sd) {
		this.plus3Sd = plus3Sd;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}
}
