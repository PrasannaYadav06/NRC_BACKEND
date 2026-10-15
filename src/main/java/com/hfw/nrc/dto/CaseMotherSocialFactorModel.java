package com.hfw.nrc.dto;

/** Ported from nrcapp.model.CaseMotherSocialFactorModel. */
public class CaseMotherSocialFactorModel {

	private Integer socialFactorsId;
	private String othersValue;
	private Boolean isChecked;

	public Integer getSocialFactorsId() { return socialFactorsId; }
	public void setSocialFactorsId(Integer socialFactorsId) { this.socialFactorsId = socialFactorsId; }

	public String getOthersValue() { return othersValue; }
	public void setOthersValue(String othersValue) { this.othersValue = othersValue; }

	public Boolean getIsChecked() { return isChecked; }
	public void setIsChecked(Boolean isChecked) { this.isChecked = isChecked; }
}
