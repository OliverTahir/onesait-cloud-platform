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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.reports.dto.ReportDto;
import com.minsait.onesait.platform.controlpanel.converter.report.ReportConverter;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/reports")
@Controller
@Slf4j
public class ReportController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportConverter reportConverter;
	
	//@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@ModelAttribute
	public List<String> roles() {
		
		Predicate<Role> excludedRoles = rol -> !Type.ROLE_ADMINISTRATOR.toString().equals(rol.getId()) 
				&& !Type.ROLE_SYS_ADMIN.toString().equals(rol.getId());
		
		return userService.getAllRoles().stream()
				.filter(excludedRoles)
				.map(rol -> rol.getId())
				.collect(Collectors.toList());
	}
	
	//@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@ModelAttribute("owners")
	public List<String> owners() {
		
		//TODO: List<User> users = userService.getAllActiveUsersWithoutRoles(Type.ROLE_ADMINISTRATOR, Type.ROLE_SYS_ADMIN);
			
		// Provisional
		List<User> users = userService.getAllActiveUsers().stream()
				.filter(user -> !Type.ROLE_ADMINISTRATOR.toString().equals(user.getRole().getId()) && !Type.ROLE_SYS_ADMIN.toString().equals(user.getRole().getId()))
				.collect(Collectors.toList());
		// --------
		
		return users.stream() //
				.map(user -> user.getUserId()) //
				.collect(Collectors.toList());
	}
	
	@GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
	public String list() {
		
		log.debug("INI. Report list.");
		
		return "reports/list";
	}
	
	@GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
	public String create(Model model) {
		
		log.debug("INI. Report create");
		
		ReportDto report = ReportDto.builder()
				.isPublic(Boolean.FALSE)
				.build();
		
		model.addAttribute("report", report);
		
		return "reports/create";
	}
	
	@PostMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
	public String save(@Valid @ModelAttribute("report") ReportDto report) {
		
		log.debug("INI. Report save");
		
		Report entity = reportConverter.convert(report);
		
		reportService.saveOrUpdate(entity);
		
		return "redirect:/reports/list";
	}
	
	@PutMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
	public String update(@Valid @ModelAttribute("report") ReportDto report) {
		
		log.debug("INI. Report save");
		
		Report entity = reportConverter.convert(report);
		
		reportService.saveOrUpdate(entity);
		
		return "redirect:/reports/list";
	}
	
	@GetMapping(value = "/edit", produces = MediaType.TEXT_HTML_VALUE)
	public String edit() {
		
		log.debug("INI. Report edit");
		
		return "reports/create";
	}
}
