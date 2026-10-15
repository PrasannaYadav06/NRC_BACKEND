package com.hfw.nrc.dto;

/** Echoes generated RCHO login credentials back to the admin right after creation (password is never retrievable again after this response). */
public class RchoCredentialsDto {

	private int rchoId;
	private String rchoName;
	private String username;
	private String password;

	public int getRchoId() { return rchoId; }
	public void setRchoId(int rchoId) { this.rchoId = rchoId; }

	public String getRchoName() { return rchoName; }
	public void setRchoName(String rchoName) { this.rchoName = rchoName; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}
