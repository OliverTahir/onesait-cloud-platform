package com.minsait.onesait.platform.reports.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportExtension;
import com.minsait.onesait.platform.config.model.ReportParameter;
import com.minsait.onesait.platform.config.model.ReportParameterType;
import com.minsait.onesait.platform.config.model.ReportType;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class GenerateReportServiceTest {
	
	@Autowired
	GenerateReportService generateReportService;
	
	@Before
	public void setUp() {
		//-Djava.net.useSystemProxies=true
		System.setProperty("java.net.useSystemProxies", "true");
	}
	
	@Test
	public void test_jrxml() throws JRException, IOException {
		
		Report report = mockJrxmlReport();
		
		ReportDto reportData = generateReportService.generate(report, ReportTypeEnum.PDF);
		
		System.out.println(reportData);
	}

	private Report mockJrxmlReport() throws IOException {
		String pathname = "C:\\Users\\aponcep\\JaspersoftWorkspace\\MyReports\\todo.jrxml";	
		File file = new File(pathname);
		FileInputStream is = new FileInputStream(file);
		byte[] bytes = IOUtils.toByteArray(is);
		
		ReportType reportType = mockReportType();
		List<ReportParameter> reportParameters = mockReportParameters();
		
		Report report = new Report();
		report.setName("test_jrxml_" + System.currentTimeMillis());
		report.setDescription("Description");
		report.setFile(bytes);
		report.setExtension(ReportExtension.JRXML);
		report.setReportType(reportType);
		report.setParameters(reportParameters);
		
		return report;
	}
	
	private List<ReportParameter> mockReportParameters() {
		List<ReportParameter> reportParameters = new ArrayList<ReportParameter>();
		ReportParameter title = ReportParameter.builder()
				.name("title")
				.description("Titulo")
				.value("Tutulo")
				.type(ReportParameterType.STRING)
				.build();
		
		ReportParameter dateIni = ReportParameter.builder()
				.name("dateIni")
				.description("Fecha de inicio")
				.value("01/01/2019")
				.type(ReportParameterType.DATE)
				.build();
		
		ReportParameter dateEnd = ReportParameter.builder()
				.name("dateEnd")
				.description("Fecha de fin")
				.value("01/01/2021")
				.type(ReportParameterType.DATE)
				.build();
		
		ReportParameter customDouble = ReportParameter.builder()
				.name("cDouble")
				.description("Double example")
				.value("80.85")
				.type(ReportParameterType.DOUBLE)
				.build();
		
		reportParameters.add(title);
		reportParameters.add(dateIni);
		reportParameters.add(dateEnd);
		reportParameters.add(customDouble);		
		
		return reportParameters;
	}
	
	private ReportType mockReportType() {
		ReportType reportType = new ReportType();
		reportType.setId(3L);
		return reportType;
	}
}