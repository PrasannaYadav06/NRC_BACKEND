package com.hfw.nrc.report;

/** One fiscal-year month's row in the Summary Report (NrcReportService#generateSummaryReport). */
public class SummaryReportRowDto {

	private final String monthLabel;
	private final int admissions;
	private final int discharges;
	private final int deaths;

	public SummaryReportRowDto(String monthLabel, int admissions, int discharges, int deaths) {
		this.monthLabel = monthLabel;
		this.admissions = admissions;
		this.discharges = discharges;
		this.deaths = deaths;
	}

	public String getMonthLabel() { return monthLabel; }
	public int getAdmissions() { return admissions; }
	public int getDischarges() { return discharges; }
	public int getDeaths() { return deaths; }
}
