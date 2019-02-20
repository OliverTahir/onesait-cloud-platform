package com.minsait.onesait.platform.reports.service;

import java.io.InputStream;

import com.minsait.onesait.platform.reports.model.ReportInfoDto;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

public interface ReportInfoService {

	ReportInfoDto extract(InputStream is) throws JRException;
	
	ReportInfoDto extract(JasperReport report);
}
