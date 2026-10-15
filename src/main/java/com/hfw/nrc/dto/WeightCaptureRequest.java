package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Stage 3 - "Daily Weight Capture" popup submit: Date(today only)/Diet/Weight/Comment. */
public class WeightCaptureRequest {

	private String captureDate;
	private BigDecimal weightKg;
	private String diet;
	private String comment;

	public String getCaptureDate() { return captureDate; }
	public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }
	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
	public String getDiet() { return diet; }
	public void setDiet(String diet) { this.diet = diet; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
}
