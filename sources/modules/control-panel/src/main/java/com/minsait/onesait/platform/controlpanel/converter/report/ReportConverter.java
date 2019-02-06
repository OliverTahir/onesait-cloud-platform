package com.minsait.onesait.platform.controlpanel.converter.report;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.converter.reports.BaseConverter;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Component
public class ReportConverter implements BaseConverter<ReportDto, Report> {

	@Autowired
	private AppWebUtils appWebUtils;
	
	// TODO: create
	@Override
	public Report convert(ReportDto report) {
		Report entity = new Report();
		
		// Form
		entity.setName(report.getName());
		entity.setDescription(report.getDescription());
		entity.setIsPublic(report.getIsPublic());
		
		try {
			entity.setFile(report.getFile().getBytes());
		} catch (IOException e) {
			throw new RuntimeException("ERROR AL SUBIR EL FICHERO !!!!!!!");
		}
		
		// Inner
		entity.setActive(Boolean.TRUE);
		entity.setUser(findUser());
		entity.setReportType(findReportType());
		
		return entity;
	}
	
	private User findUser() {
		User user = new User();
		user.setUserId(appWebUtils.getUserId());
		return user;
	}
	
	private ReportType findReportType() {
		ReportType reportType = new ReportType();
		reportType.setId(3L);
		return reportType;
	}
}
