package com.minsait.onesait.platform.config.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.minsait.onesait.platform.config.model.ReportParameterType;

@Converter(autoApply = true)
public class ReportParameterTypeConverter implements AttributeConverter<ReportParameterType, String> {

	@Override
	public String convertToDatabaseColumn(ReportParameterType attribute) {
		
		return attribute.getDbType();
	}

	@Override
	public ReportParameterType convertToEntityAttribute(String dbType) {
		return ReportParameterType.fromDatabaseType(dbType);
	}

}
