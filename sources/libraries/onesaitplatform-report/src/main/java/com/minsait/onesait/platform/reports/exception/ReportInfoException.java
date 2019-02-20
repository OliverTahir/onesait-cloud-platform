package com.minsait.onesait.platform.reports.exception;

public class ReportInfoException extends RuntimeException {
	
	private static final long serialVersionUID = 4436746717017182058L;

	public ReportInfoException() {
		super();
	}

	public ReportInfoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReportInfoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportInfoException(String message) {
		super(message);
	}

	public ReportInfoException(Throwable cause) {
		super(cause);
	}
}
