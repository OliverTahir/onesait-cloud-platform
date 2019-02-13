package com.minsait.onesait.platform.reports.service.converter;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.reports.dto.ParamterDto;

import net.sf.jasperreports.engine.JRParameter;

@Component
public class ParameterDtoConverter extends AbstractBaseConverter<JRParameter, ParamterDto> {

	public ParamterDto convert(JRParameter paramter) {
		return new ParamterDto.Builder()
				.name(paramter.getName())
				.description(paramter.getDescription())
				.type(paramter.getValueClass())
				.build();
	}
}
