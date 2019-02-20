package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.converter.report.ReportDtoConverter;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.reports.exception.ReportInfoException;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.ReportInfoService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

@Slf4j
@RequestMapping("/reports")
@RestController
public class RestReportController {

	// -- Service -- //
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportInfoService reportInfoService;
	
	@Autowired
	private AppWebUtils utils;
	
	// -- Converter -- //
	@Autowired
	private ReportDtoConverter reportDtoConverter;
	
	@GetMapping(value = "/list/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ReportDto> list() {
		log.debug("INI. Retrieve data");
		
		List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports() : 
				reportService.findAllActiveReportsByUserId(utils.getUserId());
		
		return reportDtoConverter.convert(reports);
	}
	
	/**
	 * <p>
	 * 
	 * see {link ReportExceptionTranslatorAspect}
	 * see {link ReportInfoExceptionAdvisor}
	 * 
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 * @throws JRException
	 */
	@PostMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReportInfoDto> reportInfo(@RequestParam("file") MultipartFile multipartFile) throws IOException, JRException {
		
		InputStream is = multipartFile.getInputStream();
		
		ReportInfoDto reportInfoDto = reportInfoService.extract(is);
		
		return new ResponseEntity<ReportInfoDto>(reportInfoDto, HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> delete(@PathVariable("id") Long id) {
		log.debug("INI. Retrieve data");
		
		reportService.disable(id);
		
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}
}
