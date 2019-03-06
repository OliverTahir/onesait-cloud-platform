package com.minsait.onesait.platform.config.model;


public enum ReportExtension {
	JRXML, JASPER;
	
	public String valueOf() {
		return toString().toLowerCase();
	}
	
	public static ReportExtension instance(String extension) {
		ReportExtension[] values = ReportExtension.values();
		for (ReportExtension value : values) {
			if (value.valueOf().equalsIgnoreCase(extension) ) {
				return value;
			}
		}
		
		// TODO: Exception ????
		
		return null;
	}
}
