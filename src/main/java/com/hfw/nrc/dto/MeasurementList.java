package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.MeasurementList - request body for /deletechildgrowth. */
public class MeasurementList {

	private List<Long> measurementList = new ArrayList<>();

	public List<Long> getMeasurementList() { return measurementList; }
	public void setMeasurementList(List<Long> measurementList) { this.measurementList = measurementList; }
}
