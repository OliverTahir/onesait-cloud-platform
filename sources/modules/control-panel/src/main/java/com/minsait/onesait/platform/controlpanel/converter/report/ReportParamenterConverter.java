package com.minsait.onesait.platform.controlpanel.converter.report;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.minsait.onesait.platform.config.model.ReportParameter;
import com.minsait.onesait.platform.config.model.ReportParameterType;
import com.minsait.onesait.platform.reports.converter.base.AbstractBaseConverter;
import com.minsait.onesait.platform.reports.model.ParameterDto;

@Component
public class ReportParamenterConverter extends AbstractBaseConverter<ParameterDto<?>, ReportParameter> {

	// FIXME
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	// Group value by id
	private Collector<ParameterDto<?>, ?, Map<Long, String>> groupingById = Collectors.groupingBy(ParameterDto::getId, Collectors.mapping(ParameterDto::getValue, Collectors.joining()));
	
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
	
	public List<ReportParameter> merge(List<ReportParameter> targetParameters, List<ParameterDto<?>> sourceParameters) {
		
		if (targetParameters != null) {
			Map<Long, String> updatePairIdValue = sourceParameters.stream()
					.collect(groupingById);
			
			for (ReportParameter parameter : targetParameters) {
				parameter.setValue(updatePairIdValue.get(parameter.getId()));
			}
		}
		
		return targetParameters;
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
