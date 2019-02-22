package com.minsait.onesait.platform.reports.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ReportDto implements Serializable {

	private static final long serialVersionUID = -1806266282549826766L;

	private String name;
	
	private String extension;
	
	private String contentType;
	
	private byte[] content;
	
	private ReportInfoDto reportInfo;
	
	public String getFullName() {
		return name + "." + extension;
	}
	
}
