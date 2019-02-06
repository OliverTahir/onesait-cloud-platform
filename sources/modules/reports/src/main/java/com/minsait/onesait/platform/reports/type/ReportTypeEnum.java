package com.minsait.onesait.platform.reports.type;

public enum ReportTypeEnum {

	PDF("application/pdf");
	
	private ReportTypeEnum(String code) {
		this.code = code;
	}
	
	private String code;
	
	public String valueOf() {
		return code;
	}
}
