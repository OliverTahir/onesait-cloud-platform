package com.minsait.onesait.platform.reports.model;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ReportInfoDto implements Serializable {

	private static final long serialVersionUID = -6785564223596371607L;
	
	private List<ParameterDto<?>> parameters;
	
	private List<FieldDto<?>> fields;
	
	private String dataSource;
}
