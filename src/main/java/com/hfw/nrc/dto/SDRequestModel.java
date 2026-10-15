package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.SDRequestModel. Not an AbstractModel - request only. */
public class SDRequestModel {

	private BigDecimal weight;
	private BigDecimal height;
	private String gender;

	public BigDecimal getWeight() { return weight; }
	public void setWeight(BigDecimal weight) { this.weight = weight; }

	public BigDecimal getHeight() { return height; }
	public void setHeight(BigDecimal height) { this.height = height; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
}
