package com.minsait.onesait.platform.reports.service;

import com.minsait.onesait.platform.reports.dto.ReportInfoDto;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

public interface ReportInfoExtractorService {

	//ReportInfoDto extractMetadata(JasperDesign design)
	
	ReportInfoDto extractMetadata(JasperReport report);
}
