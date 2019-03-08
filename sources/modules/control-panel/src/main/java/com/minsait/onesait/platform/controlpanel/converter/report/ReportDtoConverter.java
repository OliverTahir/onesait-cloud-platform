package com.minsait.onesait.platform.controlpanel.converter.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.reports.converter.base.AbstractBaseConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportDtoConverter extends AbstractBaseConverter<Report, ReportDto> {

	@Autowired
	private ReportParamenterDtoConverter parameterDtoConverter; 
	
	@Override
	public ReportDto convert(Report report) {
		log.debug("INI. Convert entity Report: {}", report);
		
		ReportDto reportDto = ReportDto.builder()
				.id(report.getId())
				.name(report.getName())
				.description(report.getDescription())
				.owner(report.getUser().getUserId())
				.created(report.getCreatedAt())
				.isPublic(report.getIsPublic())
				.parameters(parameterDtoConverter.convert(report.getParameters()))
				.build();
		
		log.debug("END. Converted ReportDto: {}", reportDto);
		
		return reportDto;
	}
}
