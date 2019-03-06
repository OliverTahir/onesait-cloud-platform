package com.minsait.onesait.platform.reports.service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

public interface GenerateReportService {
	
	ReportDto generate(Report entity, ReportTypeEnum pdf);
}


/*ReportDto generate(byte[] bytes, String name, ReportExtension extension, ReportTypeEnum type);

ReportDto generate(InputStream is, String name, ReportExtension extension, ReportTypeEnum type);

ReportDto generate(JasperReport jasperReport, String name, ReportExtension extension, ReportTypeEnum type);

ReportDto generate(Report entity, ReportTypeEnum pdf) throws JRException;

//
ReportDto generate(InputStream is, Map<String, Object> params, ReportTypeEnum pdf) throws JRException;

ReportDto generateJasper(InputStream is, Map<String, Object> params, ReportTypeEnum pdf) throws JRException;
*/

//ReportDto generate(File file, Map<String, Object> params, ReportTypeEnum pdf) throws JRException;