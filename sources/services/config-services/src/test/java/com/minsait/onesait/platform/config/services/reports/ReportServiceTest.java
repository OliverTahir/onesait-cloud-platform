package com.minsait.onesait.platform.config.services.reports;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportType;
import com.minsait.onesait.platform.config.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest //(classes = { ReportTestConfig.class })
@Transactional
public class ReportServiceTest {

	@Autowired
	ReportService service;
	
	@Test
	@Commit
	public void test() {
		
		ReportType reportType = new ReportType();
		reportType.setCode("PDF");
		reportType.setDescription("Report PDF");
		
		User user = new User();
		user.setUserId("developer");
		
		//final User user = userRepository.findByUserId("developer");
		
		Report report = new Report();
		report.setName("TestReport");
		report.setIsPublic(Boolean.TRUE);
		report.setActive(Boolean.TRUE);
		report.setReportType(reportType);
		report.setUser(user);
		
		service.saveOrUpdate(report);
	}
	
}
