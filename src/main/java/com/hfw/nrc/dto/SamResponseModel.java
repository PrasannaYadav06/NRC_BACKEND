package com.hfw.nrc.dto;

/** Ported from nrcapp.model.SamResponseModel. */
public class SamResponseModel implements AbstractModel {

	private Integer samNumber;
	private Boolean samExists;

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public Boolean getSamExists() { return samExists; }
	public void setSamExists(Boolean samExists) { this.samExists = samExists; }
}
