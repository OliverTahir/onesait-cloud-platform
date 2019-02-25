package com.minsait.onesait.platform.reports.service.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.reports.converter.FieldDtoConverter;
import com.minsait.onesait.platform.reports.converter.ParameterDtoConverter;
import com.minsait.onesait.platform.reports.model.FieldDto;
import com.minsait.onesait.platform.reports.model.ParameterDto;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

	@Autowired
	private ParameterDtoConverter parameterConverter;
	
	@Autowired
	private FieldDtoConverter fieldConverter;
	
	
	private Predicate<JRParameter> filterSystemParameters = 
			parameter -> !parameter.isSystemDefined() && parameter.isForPrompting();
	
	private Predicate<JRParameter> filterDatasourceParameter = 
			parameter -> "net.sf.jasperreports.json.source".equals(parameter.getName()) && !parameter.isForPrompting();
	
	
	@Override
	public ReportInfoDto extract(InputStream is) throws JRException {
		
		JasperReport report = JasperCompileManager.compileReport(is); 
		
		return extract(report);
	}
	
	@Override
	public ReportInfoDto extract(JasperReport report) {
		
		String[] propertyNames = report.getPropertyNames();
		
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



/*JRDataset mainDataset = report.getMainDataset();
if (mainDataset != null) {
	JRQuery query = mainDataset.getQuery();
	System.out.println(query.getText()); 
}


JRDataset[] datasets = report.getDatasets();
if (datasets !=  null) {
	for (JRDataset dataset : datasets) {
		
		JRQuery query = dataset.getQuery();
		System.out.println(query.getText()); 
		
		JRParameter[] dsParameters = dataset.getParameters();
		if (dsParameters != null) {
			for (JRParameter dsParameter : dsParameters) {
				System.out.println(dsParameter);
			}
		}
	}
}*/