package com.minsait.onesait.platform.reports.service.impl;

import java.io.ByteArrayOutputStream;

import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.service.ReportBuilderService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;

public class ReportBuilderServiceImpl implements ReportBuilderService {

	@Override
	public ReportDataDto generateReport(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		// TODO: Sin paramtros !!
		JasperFillManager.fillReportToStream(jasperReport, outputStream, null);
		
		return ReportDataDto.builder() //
				.name(name) //
				.contentType(type.valueOf()) //
				.content(outputStream.toByteArray()) //
				.build();
				
	}
}
