package com.minsait.onesait.platform.reports.service.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ReportExtension;
import com.minsait.onesait.platform.reports.converter.FieldDtoConverter;
import com.minsait.onesait.platform.reports.converter.ParameterDtoConverter;
import com.minsait.onesait.platform.reports.exception.GenerateReportException;
import com.minsait.onesait.platform.reports.exception.ReportInfoException;
import com.minsait.onesait.platform.reports.model.FieldDto;
import com.minsait.onesait.platform.reports.model.ParameterDto;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

	// -- Converter -- //
	@Autowired
	private ParameterDtoConverter parameterConverter;
	
	@Autowired
	private FieldDtoConverter fieldConverter;
	
	// -- Filter -- //
	private Predicate<JRParameter> filterSystemParameters = 
			parameter -> !parameter.isSystemDefined() && parameter.isForPrompting();
	
	private Predicate<JRParameter> filterDatasourceParameter = 
			parameter -> "net.sf.jasperreports.json.source".equals(parameter.getName()) && !parameter.isForPrompting();
	
	
	@Override
	public ReportInfoDto extract(InputStream is, ReportExtension reportExtension) {
		ReportInfoDto reportInfo = null;
		
		switch (reportExtension) {
			case JRXML:
				reportInfo = extractFromJrxml(is, reportExtension);
				break;
				
			case JASPER:
				reportInfo = extractFromJasper(is, reportExtension);
				break;
	
			default:
				throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}
		
		return reportInfo;	
	}
	
	private ReportInfoDto extractFromJrxml(InputStream is, ReportExtension reportExtension) {
		try {
			
			JasperReport report = JasperCompileManager.compileReport(is);
			
			return extractFromReport(report);
			
		} catch (JRException e) {
			throw new ReportInfoException(e);
		} 
	}
	
	private ReportInfoDto extractFromJasper(InputStream is, ReportExtension reportExtension) {
		try {
			
			JasperReport report = (JasperReport) JRLoader.loadObject(is);
			
			return extractFromReport(report);
			
		} catch (JRException e) {
			throw new ReportInfoException(e);
		} 
	}
	
	private ReportInfoDto extractFromReport(JasperReport report) {
		log.debug("INI. Extract data from report: {}", report.getName());
		
		List<ParameterDto<?>> parameters = parameterConverter.convert(report.getParameters(), filterSystemParameters);
		
		List<FieldDto<?>> fields = fieldConverter.convert(report.getFields());
		
		String dataSource = Arrays.stream(report.getParameters())
			.filter(filterDatasourceParameter)
			.map(parameter ->  {
				return parameter.getDefaultValueExpression() != null  ? parameter.getDefaultValueExpression().getText() : "";
			})
			.findFirst().orElse("");
		
		return ReportInfoDto.builder()
				.parameters(parameters)
				.fields(fields)
				.dataSource(dataSource)
				.build();
	}
}