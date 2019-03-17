/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ParameterMapConverter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportTypeEnum;
import com.minsait.onesait.platform.controlpanel.services.report.ReportInfoService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

@Slf4j
@RequestMapping("/reports")
@Controller
public class ReportController {

	@Autowired
	private UserService userService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportConverter reportConverter;

	@Autowired
	private ParameterMapConverter parameterMapConverter;

	@Autowired
	private ReportInfoService reportInfoService;

	@Autowired
	private AppWebUtils utils;

	@GetMapping(value = "/list/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ReportDto>> listData() {

		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		return new ResponseEntity<>(reports.stream().map(r -> reportConverter.convert(r)).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	@GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
	public String list(Model model) {
		model.addAttribute("owners",
				userService.getAllActiveUsers().stream()
						.filter(user -> !Type.ROLE_ADMINISTRATOR.toString().equals(user.getRole().getId())
								&& !Type.ROLE_SYS_ADMIN.toString().equals(user.getRole().getId()))
						.map(User::getUserId).collect(Collectors.toList()));
		return "reports/list";
	}

	@GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView create(Model model) {

		final ReportDto report = ReportDto.builder().isPublic(Boolean.FALSE).build();

		return new ModelAndView("reports/create", "report", report);
	}

	@GetMapping(value = "/edit/{id}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView edit(@PathVariable("id") String id) {

		log.debug("INI. Redirect to Report edit. Find report id: {}", id);

		final Report entity = reportService.findById(id);

		final ReportDto report = reportConverter.convert(entity);

		return new ModelAndView("reports/create", "report", report);
	}

	@PostMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
	public String save(@Valid @ModelAttribute("report") ReportDto report) {
		log.debug("INI. Report save");

		final Report entity = reportConverter.convert(report);

		reportService.saveOrUpdate(entity);

		return "redirect:/reports/list";
	}

	@PutMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
	public String update(@Valid @ModelAttribute("report") ReportDto report) {

		log.debug("INI. Report update");

		final Report target = reportService.findById(report.getId());

		final Report entity = reportConverter.merge(target, report);

		reportService.saveOrUpdate(entity);

		return "redirect:/reports/list";
	}

	@PostMapping(value = "/download/report/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	public ResponseEntity<?> downloadReport(@PathVariable("id") String id, @RequestBody String json)
			throws JRException, IOException {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		final String decoded = URLDecoder.decode(json, "UTF-8");
		final ObjectMapper mapper = new ObjectMapper();
		final List<ReportParameter> parameters = mapper.readValue(decoded.substring(0, decoded.length() - 1),
				new TypeReference<List<ReportParameter>>() {
				});

		final Map<String, Object> map = parameters == null ? new HashMap<>()
				: parameterMapConverter.convert(parameters);

		final byte[] content = reportInfoService.generate(entity, ReportTypeEnum.PDF, map);

		return generateAttachmentResponse(content, ReportTypeEnum.PDF.contentType(),
				entity.getName() + "." + ReportTypeEnum.PDF.extension());

	}

	@GetMapping(value = "/download/report-design/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	public ResponseEntity<?> downloadTemplate(@PathVariable("id") String id) throws IOException, JRException {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return generateAttachmentResponse(entity.getFile(), ReportTypeEnum.JRXML.contentType(),
				entity.getName() + "." + entity.getName() + "." + ReportTypeEnum.JRXML.extension());

	}

	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
		log.debug("INI. Retrieve data");

		reportService.disable(id);

		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}

	@PostMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReportInfoDto> reportInfo(@RequestParam("file") MultipartFile multipartFile)
			throws IOException {

		final ReportInfoDto reportInfoDto = reportInfoService.extract(multipartFile.getInputStream(),
				ReportExtension.valueOf(FilenameUtils.getExtension(multipartFile.getOriginalFilename()).toUpperCase()));

		return new ResponseEntity<ReportInfoDto>(reportInfoDto, HttpStatus.OK);
	}

	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ReportParameter>> parameters(@PathVariable("id") String id) throws IOException {

		final Report report = reportService.findById(id);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		final ReportInfoDto reportInfoDto = reportInfoService.extract(new ByteArrayInputStream(report.getFile()),
				report.getExtension());

		return new ResponseEntity<>(reportInfoDto.getParameters(), HttpStatus.OK);
	}

	private ResponseEntity<?> generateAttachmentResponse(byte[] byteArray, String contentType, String fileName) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(byteArray.length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(byteArray);

	}
}
