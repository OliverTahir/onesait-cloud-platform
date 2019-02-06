package com.minsait.onesait.platform.reports.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {
		"com.minsait.onesait.platform.reports"
})
public class ReportBuilderServiceTest {

	@Autowired
	ReportBuilderService service;
	
	@Test
	public void test() throws JRException, IOException {
		Resource resource = new ClassPathResource("report/test.jasper");
		
		ReportDataDto reportData = service.generateReport(resource.getInputStream(), "test", ReportTypeEnum.PDF);
		
		System.out.println(reportData);
	}
	
}
