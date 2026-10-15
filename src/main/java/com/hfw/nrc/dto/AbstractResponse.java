package com.hfw.nrc.dto;

/**
 * The response envelope every nrcapp endpoint returns - ported verbatim
 * (field names/shape unchanged) from nrcapp.model.AbstractResponse, since
 * the frontend already parses JSON in this exact shape.
 */
public class AbstractResponse {

	private String errorCode;
	private String errorMessage;
	private String successMessage;
	private String sessionId;
	private String systemTime;

	private AbstractModel responseObject;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSystemTime() {
		return systemTime;
	}

	public void setSystemTime(String systemTime) {
		this.systemTime = systemTime;
	}

	public AbstractModel getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(AbstractModel responseObject) {
		this.responseObject = responseObject;
	}
}
