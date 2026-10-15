package com.hfw.nrc.dto;

/** Ported from nrcapp.model.DischargeComorbidModel. */
public class DischargeComorbidModel {

	private int comorbidId;
	private String othersValue;
	private Boolean isChecked;

	public int getComorbidId() { return comorbidId; }
	public void setComorbidId(int comorbidId) { this.comorbidId = comorbidId; }

	public String getOthersValue() { return othersValue; }
	public void setOthersValue(String othersValue) { this.othersValue = othersValue; }

	public Boolean getIsChecked() { return isChecked; }
	public void setIsChecked(Boolean isChecked) { this.isChecked = isChecked; }
}
