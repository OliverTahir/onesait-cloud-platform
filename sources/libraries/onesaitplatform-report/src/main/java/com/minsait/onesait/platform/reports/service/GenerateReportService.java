package com.minsait.onesait.platform.reports.service;

import java.io.IOException;
import java.io.InputStream;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

public interface GenerateReportService {
	
	ReportDto generate(byte[] bytes, String name, ReportTypeEnum type) throws JRException, IOException;
	
	ReportDto generate(InputStream is, String name, ReportTypeEnum type) throws JRException, IOException;

	ReportDto generate(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException;

	ReportDto generate(Report entity, ReportTypeEnum pdf) throws JRException;
	
}
