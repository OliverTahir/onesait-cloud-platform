package com.minsait.onesait.platform.reports.service.converter;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.reports.dto.FieldDto;

import net.sf.jasperreports.engine.JRField;

@Component
public class FieldDtoConverter extends AbstractBaseConverter<JRField, FieldDto> {

	public FieldDto convert(JRField field) {
		return new FieldDto.Builder()
				.name(field.getName())
				.type(field.getValueClass())
				.build();
	}
}
