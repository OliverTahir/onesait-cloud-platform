package com.minsait.onesait.platform.config.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.minsait.onesait.platform.config.model.ReportExtension;

@Converter(autoApply = true)
public class ReportExtensionConverter implements AttributeConverter<ReportExtension, String> {

	@Override
	public String convertToDatabaseColumn(ReportExtension attribute) {
		
		return attribute.valueOf();
	}

	@Override
	public ReportExtension convertToEntityAttribute(String extension) {
		return ReportExtension.instance(extension);
	}

}
