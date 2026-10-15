package com.hfw.nrc.dto;

/** Ported from nrcapp.model.AdmissionCriteriaModel. */
public class AdmissionCriteriaModel {

	private int criteriaId;
	private String othersValue;
	private Boolean isChecked;

	public int getCriteriaId() { return criteriaId; }
	public void setCriteriaId(int criteriaId) { this.criteriaId = criteriaId; }

	public String getOthersValue() { return othersValue; }
	public void setOthersValue(String othersValue) { this.othersValue = othersValue; }

	public Boolean getIsChecked() { return isChecked; }
	public void setIsChecked(Boolean isChecked) { this.isChecked = isChecked; }
}
