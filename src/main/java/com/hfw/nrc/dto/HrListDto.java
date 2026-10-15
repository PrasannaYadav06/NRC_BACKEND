package com.hfw.nrc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Stage 6 - one row of the "Search HR Records" table: per-hospital-per-month staffing snapshot, rolled up to totals (View popup shows the full per-designation breakdown). */
public class HrListDto {

	private int hrRecId;
	private Integer hId;
	private String hospitalName;
	private int month;
	private int year;
	private Integer numberOfBeds;
	private int totalSanctioned;
	private int totalWorking;
	private int totalVacant;

	@JsonProperty("hrRecId")
	public int getHrRecId() { return hrRecId; }
	@JsonProperty("hrRecId")
	public void setHrRecId(int hrRecId) { this.hrRecId = hrRecId; }
	@JsonProperty("hId")
	public Integer getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(Integer hId) { this.hId = hId; }
	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
	public int getMonth() { return month; }
	public void setMonth(int month) { this.month = month; }
	public int getYear() { return year; }
	public void setYear(int year) { this.year = year; }
	public Integer getNumberOfBeds() { return numberOfBeds; }
	public void setNumberOfBeds(Integer numberOfBeds) { this.numberOfBeds = numberOfBeds; }
	public int getTotalSanctioned() { return totalSanctioned; }
	public void setTotalSanctioned(int totalSanctioned) { this.totalSanctioned = totalSanctioned; }
	public int getTotalWorking() { return totalWorking; }
	public void setTotalWorking(int totalWorking) { this.totalWorking = totalWorking; }
	public int getTotalVacant() { return totalVacant; }
	public void setTotalVacant(int totalVacant) { this.totalVacant = totalVacant; }
}
