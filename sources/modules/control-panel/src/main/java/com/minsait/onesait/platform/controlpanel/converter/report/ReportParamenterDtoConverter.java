package com.minsait.onesait.platform.controlpanel.converter.report;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.minsait.onesait.platform.config.model.ReportParameter;
import com.minsait.onesait.platform.config.model.ReportParameterType;
import com.minsait.onesait.platform.reports.converter.base.AbstractBaseConverter;
import com.minsait.onesait.platform.reports.model.ParameterDto;

@Component
public class ReportParamenterDtoConverter extends AbstractBaseConverter<ReportParameter, ParameterDto<?>> {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	@Override
	public ParameterDto<?> convert(ReportParameter entity) {
		
		ReportParameterType type = entity.getType();
		
		return ParameterDto.builder()
				.id(entity.getId())
				.name(entity.getName())
				.description(entity.getDescription())
				.type(type.getJavaType())
				.value(valueToString(entity.getValue()))
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
