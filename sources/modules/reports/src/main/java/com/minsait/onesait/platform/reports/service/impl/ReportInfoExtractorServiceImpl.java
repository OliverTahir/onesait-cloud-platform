package com.minsait.onesait.platform.reports.service.impl;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.reports.dto.ParamterDto;
import com.minsait.onesait.platform.reports.dto.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoExtractorService;
import com.minsait.onesait.platform.reports.service.converter.FieldDtoConverter;
import com.minsait.onesait.platform.reports.service.converter.ParameterDtoConverter;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;

@Service
@Slf4j
public class ReportInfoExtractorServiceImpl implements ReportInfoExtractorService {

	@Autowired
	private ParameterDtoConverter parameterConverter;
	
	@Autowired
	private FieldDtoConverter fieldConverter;
	
	Predicate<JRParameter> filterSystemParameters = parameter -> !parameter.isSystemDefined() && parameter.isForPrompting();
	
	/*public ReportInfoDto extractMetadata(JasperDesign design) throws JRException {
		
		JasperReport report = JasperCompileManager.compileReport(design);
		
		return extractMetadata(report);
	}*/
	
	@Override
	public ReportInfoDto extractMetadata(JasperReport report) {
		
		List<ParamterDto> parameters = parameterConverter.convert(report.getParameters(), filterSystemParameters);
		return ReportInfoDto.builder()
				.parameters(parameters)
				.fields(fieldConverter.convert(report.getFields()))
				.build();
				
	}

}
