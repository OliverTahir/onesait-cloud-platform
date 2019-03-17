package com.minsait.onesait.platform.reports.type;

public enum ReportTypeEnum {

	PDF("application/pdf", "pdf");
	
	private ReportTypeEnum(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}
	
	private String contentType;
	private String extension;
	
	public String contentType() {
		return contentType;
	}
	
	public String extension() {
		return extension;
	}
}
