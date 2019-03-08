package com.minsait.onesait.platform.controlpanel.converter.report;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportExtension;
import com.minsait.onesait.platform.config.model.ReportType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.reports.converter.base.BaseConverter;
import com.minsait.onesait.platform.reports.exception.UploadFileException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportConverter implements BaseConverter<ReportDto, Report> {

	@Autowired
	private AppWebUtils appWebUtils;
	
	@Autowired
	private ReportParamenterConverter paramenterConverter;
	
	// TODO: REF-003
	@Override
	public Report convert(ReportDto report) {
		log.debug("INI. Convert entity Report: {}  -->  ReportDto");
		
		if (report.getFile().isEmpty()) {
			log.error("Report template musbe not empty");
			throw new UploadFileException("Report template musbe not empty");
		}
		
		Report entity = new Report();
		
		entity.setName(report.getName());
		entity.setDescription(report.getDescription());
		entity.setIsPublic(report.getIsPublic());
		entity.setFile(getReportBytes(report.getFile()));
		entity.setExtension(getReportExtension(report.getFile()));
		entity.setParameters(paramenterConverter.convert(report.getParameters()));
		
		// Inner
		entity.setActive(Boolean.TRUE);
		entity.setUser(findUser());
		entity.setReportType(findReportType());
		
		return entity;
	}
	
	public Report merge (Report target, ReportDto source) {
		Report entity = target;
		
		entity.setName(source.getName());
		entity.setDescription(source.getDescription());
		entity.setIsPublic(source.getIsPublic());
		if (!source.getFile().isEmpty()) {
			entity.setFile(getReportBytes(source.getFile()));
			entity.setExtension(getReportExtension(source.getFile()));
		}
		entity.setParameters(paramenterConverter.merge(target.getParameters(), source.getParameters()));
		
		return entity;
	}
	
	// -- Inner methods -- //
	private byte[] getReportBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new UploadFileException();
		}
	}

	private ReportExtension getReportExtension (MultipartFile file){
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		return ReportExtension.instance(extension);
	}
	
	private User findUser() {
		User user = new User();
		user.setUserId(appWebUtils.getUserId());
		return user;
	}
	
	// TODO: REF-DES-005
	private ReportType findReportType() {
		ReportType reportType = new ReportType();
		reportType.setId(3L);
		return reportType;
	}
}
