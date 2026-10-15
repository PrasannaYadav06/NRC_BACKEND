package com.hfw.nrc.dto;

/** Ported from nrcadmin.common.DeleteResponse - response body for DELETE /caseDetails/{caseId}. */
public class DeleteResponse {

	private String message;

	public DeleteResponse() {
	}

	public DeleteResponse(String message) {
		this.message = message;
	}

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
}
