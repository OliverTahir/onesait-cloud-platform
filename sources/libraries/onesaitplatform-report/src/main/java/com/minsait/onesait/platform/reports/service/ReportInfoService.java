package com.minsait.onesait.platform.reports.service;

import java.io.InputStream;

import com.minsait.onesait.platform.config.model.ReportExtension;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;

public interface ReportInfoService {

	ReportInfoDto extract(InputStream is, ReportExtension reportExtension);
}
