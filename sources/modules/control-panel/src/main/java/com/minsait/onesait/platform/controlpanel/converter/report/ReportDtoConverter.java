package com.minsait.onesait.platform.controlpanel.converter.report;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.converter.reports.AbstractBaseConverter;

@Component
public class ReportDtoConverter extends AbstractBaseConverter<Report, ReportDto> {

	@Override
	public ReportDto convert(Report report) {
		return ReportDto.builder()
				.id(report.getId())
				.name(report.getName())
				.description(report.getDescription())
				.owner(report.getUser().getUserId())
				.created(report.getCreatedAt())
				.isPublic(report.getIsPublic())
				.build();
	}

}