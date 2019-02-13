package com.minsait.onesait.platform.reports.service;

import java.io.IOException;
import java.io.InputStream;

import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

public interface GenerateReportService {
	
	ReportDataDto generate(byte[] bytes, String name, ReportTypeEnum type) throws JRException, IOException;
	
	ReportDataDto generate(InputStream is, String name, ReportTypeEnum type) throws JRException, IOException;

	ReportDataDto generate(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException;
}
