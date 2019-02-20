package com.minsait.onesait.platform.controlpanel.converter.report;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.minsait.onesait.platform.config.model.ReportParameter;
import com.minsait.onesait.platform.config.model.ReportParameterType;
import com.minsait.onesait.platform.reports.converter.AbstractBaseConverter;
import com.minsait.onesait.platform.reports.model.ParameterDto;

@Component
public class ReportParamenterConverter extends AbstractBaseConverter<ParameterDto<?>, ReportParameter> {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	@Override
	public ReportParameter convert(ParameterDto<?> input) {
		
		ReportParameterType type = ReportParameterType.fromJavaType(input.getType());
		
		return ReportParameter.builder()
				.name(input.getName())
				.description(input.getDescription())
				.type(type)
				.value(valueToString(input.getValue()))
				.build();
	}
	
	private String valueToString(Object value) {
		String result = null;
		
		if (value instanceof Date) {
			result = dateFormat.format((Date) value);
		} else {
			result = String.valueOf(value);
		}
		
		return result;
	}

	
	
}
