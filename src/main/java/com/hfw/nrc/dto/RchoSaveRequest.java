package com.hfw.nrc.dto;

/** Admin nav bar "RCHO" page - Add/Edit RCHO form body. rchoId is 0/null for a new RCHO. */
public class RchoSaveRequest {

	private Integer rchoId;
	private int districtId;
	private String rchoName;

	public Integer getRchoId() { return rchoId; }
	public void setRchoId(Integer rchoId) { this.rchoId = rchoId; }

	public int getDistrictId() { return districtId; }
	public void setDistrictId(int districtId) { this.districtId = districtId; }

	public String getRchoName() { return rchoName; }
	public void setRchoName(String rchoName) { this.rchoName = rchoName; }
}
