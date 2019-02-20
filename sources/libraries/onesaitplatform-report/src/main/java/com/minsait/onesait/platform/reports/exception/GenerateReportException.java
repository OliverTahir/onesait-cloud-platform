package com.minsait.onesait.platform.reports.exception;

public class GenerateReportException extends RuntimeException {
	
	private static final long serialVersionUID = -4235158988362006733L;

	public GenerateReportException() {
		super();
	}

	public GenerateReportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GenerateReportException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenerateReportException(String message) {
		super(message);
	}

	public GenerateReportException(Throwable cause) {
		super(cause);
	}
}
