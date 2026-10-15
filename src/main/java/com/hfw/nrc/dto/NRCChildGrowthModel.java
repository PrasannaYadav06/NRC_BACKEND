package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.NRCChildGrowthModel (fields used by createcase/updateadmission). */
public class NRCChildGrowthModel {

	private String measurementDate;
	private BigDecimal heightCm;
	private BigDecimal weightKg;

	public String getMeasurementDate() { return measurementDate; }
	public void setMeasurementDate(String measurementDate) { this.measurementDate = measurementDate; }

	public BigDecimal getHeightCm() { return heightCm; }
	public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
}
