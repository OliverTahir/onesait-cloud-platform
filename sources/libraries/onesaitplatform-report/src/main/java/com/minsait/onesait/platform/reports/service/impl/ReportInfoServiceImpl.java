package com.minsait.onesait.platform.reports.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.reports.dto.FieldDto;
import com.minsait.onesait.platform.reports.dto.ParamterDto;
import com.minsait.onesait.platform.reports.dto.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoService;
import com.minsait.onesait.platform.reports.service.converter.FieldDtoConverter;
import com.minsait.onesait.platform.reports.service.converter.ParameterDtoConverter;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

	@Autowired
	private ParameterDtoConverter parameterConverter;
	
	@Autowired
	private FieldDtoConverter fieldConverter;
	
	Predicate<JRParameter> filterSystemParameters = parameter -> !parameter.isSystemDefined() && parameter.isForPrompting();
	
	@Override
	public ReportInfoDto extract(InputStream is) throws JRException {
		
		JasperReport report = JasperCompileManager.compileReport(is); 
		
		//JasperReport report = (JasperReport) JRLoader.loadObject(is);
		
		return extract(report);
	}
	
	@Override
	public ReportInfoDto extract(JasperReport report) {
		
		List<ParamterDto> parameters = parameterConverter.convert(report.getParameters(), filterSystemParameters);
		
		List<FieldDto> fields = fieldConverter.convert(report.getFields());
		
		return ReportInfoDto.builder()
				.parameters(parameters)
				.fields(fields)
				.build();
				
	}

}
