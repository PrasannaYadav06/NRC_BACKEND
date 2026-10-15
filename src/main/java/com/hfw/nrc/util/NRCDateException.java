package com.hfw.nrc.util;

/** Ported verbatim from nrcapp.exception.NRCDateException (checked exception). */
public class NRCDateException extends Exception {

	private static final long serialVersionUID = 1L;

	private String exceptionMessage;

	public NRCDateException(String msg) {
		this.exceptionMessage = msg;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
}
