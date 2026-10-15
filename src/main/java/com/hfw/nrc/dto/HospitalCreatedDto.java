package com.hfw.nrc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST /hospital/save and GET /hospital/preview-identity response payload -
 * the server-generated code/name/username/password, and (only when a new
 * one was created for this registration) the district's RCHO credentials.
 *
 * @JsonProperty on hCode/hId is required, not optional polish - same
 * Jackson bean-naming gotcha as HospitalAdminDto/HospitalRecordDto:
 * getHCode()/getHId() strip to "HCode"/"HId" (both leading letters
 * uppercase), which Jackson's default mangler serializes as "hcode"/"hid"
 * (fully lowercased) instead of the frontend's expected "hCode"/"hId" -
 * confirmed live, the Registration form's confirmation dialog showed
 * "Hospital Code: undefined" without this.
 */
public class HospitalCreatedDto implements AbstractModel {

	private int hId;
	private String hCode;
	private String name;
	private String username;
	private String password;
	private boolean rchoCreated;
	private RchoCredentialsDto rcho;

	@JsonProperty("hId")
	public int getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(int hId) { this.hId = hId; }

	@JsonProperty("hCode")
	public String getHCode() { return hCode; }
	@JsonProperty("hCode")
	public void setHCode(String hCode) { this.hCode = hCode; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public boolean isRchoCreated() { return rchoCreated; }
	public void setRchoCreated(boolean rchoCreated) { this.rchoCreated = rchoCreated; }

	public RchoCredentialsDto getRcho() { return rcho; }
	public void setRcho(RchoCredentialsDto rcho) { this.rcho = rcho; }
}
