package com.minsait.onesait.platform.reports.converter;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.reports.model.FieldDto;

import net.sf.jasperreports.engine.JRField;

@Component
public class FieldDtoConverter extends AbstractBaseConverter<JRField, FieldDto<?>> {

	public FieldDto<?> convert(JRField field) {
		return FieldDto.builder()
				.name(field.getName())
				.description(field.getDescription())
				.type(field.getValueClass())
				.build();
	}
}
