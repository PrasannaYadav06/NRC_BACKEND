package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.Date;

/** Ported from nrcapp.model.LastFourMsmtResponseModel. */
public class LastFourMsmtResponseModel implements AbstractModel {

	private Integer caseId;
	private BigDecimal weightKg;
	private Date measurementDate;

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public BigDecimal getWeightKg() { return weightKg; }
	public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

	public Date getMeasurementDate() { return measurementDate; }
	public void setMeasurementDate(Date measurementDate) { this.measurementDate = measurementDate; }
}
