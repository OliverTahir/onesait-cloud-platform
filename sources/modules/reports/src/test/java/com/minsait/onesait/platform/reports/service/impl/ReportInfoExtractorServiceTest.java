package com.minsait.onesait.platform.reports.service.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.reports.dto.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoExtractorService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = { "com.minsait.onesait.platform.reports" })
public class ReportInfoExtractorServiceTest {

	@Autowired
	ReportInfoExtractorService service;
	
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void test1() throws JRException, IOException {
		
		Resource resource = new ClassPathResource("extractMetadata/Coffee_Landscape.jasper");
		
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(resource.getInputStream());
		
		ReportInfoDto reportInfoDto = service.extractMetadata(jasperReport);
		
		System.out.println(reportInfoDto);
	}
	
	@Test
	public void test2() throws JRException, IOException {
		
		Resource resource = new ClassPathResource("extractMetadata/Coffee_Landscape.jrxml");
		
		JasperDesign design = JRXmlLoader.load(resource.getInputStream());
		
		JasperReport report = JasperCompileManager.compileReport(design);
		
		ReportInfoDto reportInfoDto = service.extractMetadata(report);
		
		System.out.println(reportInfoDto);
	}
}
