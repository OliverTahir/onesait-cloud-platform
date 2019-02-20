package com.minsait.onesait.platform.reports.converter;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.reports.model.ParameterDto;

import net.sf.jasperreports.engine.JRParameter;

@Component
public class ParameterDtoConverter extends AbstractBaseConverter<JRParameter, ParameterDto<?>> {

	public ParameterDto<?> convert(JRParameter paramter) {
		
		return ParameterDto.builder()
				.name(paramter.getName())
				.description(paramter.getDescription())
				.type(paramter.getValueClass().getName())
				.build();
	}


}
