package com.hfw.nrc.dto;

import java.util.List;

/** RCHO Data Table row + view/edit prefill. */
public class RchoRecordDto {

	private int rchoId;
	private int districtId;
	private String districtName;
	private String rchoName;
	private String username;
	private List<String> hospitalNames;
	private int hospitalCount;

	public int getRchoId() { return rchoId; }
	public void setRchoId(int rchoId) { this.rchoId = rchoId; }

	public int getDistrictId() { return districtId; }
	public void setDistrictId(int districtId) { this.districtId = districtId; }

	public String getDistrictName() { return districtName; }
	public void setDistrictName(String districtName) { this.districtName = districtName; }

	public String getRchoName() { return rchoName; }
	public void setRchoName(String rchoName) { this.rchoName = rchoName; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public List<String> getHospitalNames() { return hospitalNames; }
	public void setHospitalNames(List<String> hospitalNames) { this.hospitalNames = hospitalNames; }

	public int getHospitalCount() { return hospitalCount; }
	public void setHospitalCount(int hospitalCount) { this.hospitalCount = hospitalCount; }
}
