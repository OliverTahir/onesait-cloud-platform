package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.converter.report.ReportDtoConverter;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/reports")
@RestController
@Slf4j
public class RestReportController {

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportDtoConverter reportDtoConverter;
	
	@Autowired
	AppWebUtils utils;
	
	@GetMapping(value = "/list/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ReportDto> list() {
		log.debug("INI. Retrieve data");
		
		List<Report> reports = null;
		
		if (utils.isAdministrator()) {
			log.debug("User admin: {} => Retrieve all reports", utils.getUserId());
			reports = reportService.findAllActiveReports();
		} else {
			log.debug("User NOT admin: {} => Retrieve yours reports", utils.getUserId());
			reports = reportService.findAllActiveReportsByUserId(utils.getUserId());
		}
		
		return reportDtoConverter.convert(reports);
	}
	
	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> delete(@PathVariable("id") Long id) {
		log.debug("INI. Retrieve data");
		
		reportService.disable(id);
		
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}
}
