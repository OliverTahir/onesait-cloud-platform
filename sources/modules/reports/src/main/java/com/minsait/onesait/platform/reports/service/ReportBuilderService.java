package com.minsait.onesait.platform.reports.service;

import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

public interface ReportBuilderService {

	ReportDataDto generateReport(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException;
}
