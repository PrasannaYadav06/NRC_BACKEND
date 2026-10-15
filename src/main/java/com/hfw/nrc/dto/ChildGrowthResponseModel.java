package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.ChildGrowthResponseModel. */
public class ChildGrowthResponseModel {

	private Integer measurementId;
	private BigDecimal heightCm;
	private BigDecimal weightCm;
	private String measurementDate;
	private BigDecimal weightGain;
	private String diet;
	private String comments;
	private Integer measurementType;
	private Integer ageInMonths;

	public Integer getMeasurementId() { return measurementId; }
	public void setMeasurementId(Integer measurementId) { this.measurementId = measurementId; }

	public BigDecimal getHeightCm() { return heightCm; }
	public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

	public BigDecimal getWeightCm() { return weightCm; }
	public void setWeightCm(BigDecimal weightCm) { this.weightCm = weightCm; }

	public String getMeasurementDate() { return measurementDate; }
	public void setMeasurementDate(String measurementDate) { this.measurementDate = measurementDate; }

	public BigDecimal getWeightGain() { return weightGain; }
	public void setWeightGain(BigDecimal weightGain) { this.weightGain = weightGain; }

	public String getDiet() { return diet; }
	public void setDiet(String diet) { this.diet = diet; }

	public String getComments() { return comments; }
	public void setComments(String comments) { this.comments = comments; }

	public Integer getMeasurementType() { return measurementType; }
	public void setMeasurementType(Integer measurementType) { this.measurementType = measurementType; }

	public Integer getAgeInMonths() { return ageInMonths; }
	public void setAgeInMonths(Integer ageInMonths) { this.ageInMonths = ageInMonths; }
}
