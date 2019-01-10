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
package com.minsait.onesait.platform.controlpanel.controller.flow;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowDomain.State;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineService;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineServiceFactory;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/flows")
@Slf4j
public class FlowDomainController {

	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;

	@Value("${onesaitplatform.flowengine.services.baseurl:http://localhost:20100/flowengine/admin}")
	private String baseUrl;

	@Value("${onesaitplatform.flowengine.services.proxyurl:http://localhost:5050/}")
	private String proxyUrl;

	@Value("${onesaitplatform.controlpanel.avoidsslverification:false}")
	private boolean avoidSSLVerification;

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	private FlowEngineService flowEngineService;

	private static final String DOMAINS_STR = "domains";
	private static final String ERROR_403 = "error/403";
	private static final String FLOW_ENGINE_ACTIVE_STR = "flowEngineActive";
	private static final String MESSAGE_STR = "message";
	private static final String MESSAGE_ALERT_TYPE_STR = "messageAlertType";

	@PostConstruct
	public void init() {
		flowEngineService = FlowEngineServiceFactory.getFlowEngineService(baseUrl, restRequestTimeout,
				avoidSSLVerification);
	}

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model) {

		final List<FlowEngineDomainStatus> domainStatusList = getUserDomains(userService.getUser(utils.getUserId()),
				model);
		model.addAttribute(DOMAINS_STR, domainStatusList);
		model.addAttribute("userRole", utils.getRole());
		return "flows/list";
	}

	@PostMapping(value = "/create")
	public String create(@Valid FlowDomain domain, BindingResult bindingResult, RedirectAttributes redirect) {
		if (domain.getIdentification() == null || domain.getIdentification().isEmpty()) {
			log.debug("Domain identifier is missing");
			utils.addRedirectMessage("domain.create.error", redirect);
			return "redirect:/flows/create";
		}
		try {
			domainService.createFlowDomain(domain.getIdentification(), userService.getUser(utils.getUserId()));
		} catch (final Exception e) {
			log.debug("Cannot create flow domain.");
			utils.addRedirectMessage("domain.create.error", redirect);
			return "redirect:/flows/create";
		}
		return "redirect:/flows/list";
	}

	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {
		final FlowDomain domain = new FlowDomain();
		model.addAttribute("domain", domain);
		return "flows/create";

	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(id);
			if (!domainService.hasUserManageAccess(domain.getId(), utils.getUserId())) {
				log.debug("Cannot delete flow domain.");
				utils.addRedirectMessage("domain.delete.error", redirect);
				return "redirect:/flows/list";
			}
			domainService.deleteFlowdomain(id);
		} catch (Exception e) {
			log.debug("Cannot delete flow domain.");
			utils.addRedirectMessage("domain.delete.error", redirect);
			return "redirect:/flows/list";
		}
		return "redirect:/flows/list";
	}

	@PostMapping(value = "/startstop/{id}")
	public ResponseEntity<?> startStop(@PathVariable("id") String id) {
		try {
			final FlowDomain domain = domainService.getFlowDomainById(id);
			if (!domainService.hasUserManageAccess(domain.getId(), utils.getUserId()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			final FlowEngineDomain engineDom = FlowEngineDomain.builder().domain(domain.getIdentification())
					.port(domain.getPort()).home(domain.getHome()).servicePort(domain.getServicePort()).build();
			if (State.STOP.name().equals(domain.getState())) {
				flowEngineService.startFlowEngineDomain(engineDom);
				domain.setState(State.START.name());
			} else if (State.START.name().equals(domain.getState())) {
				flowEngineService.stopFlowEngineDomain(domain.getIdentification());
				domain.setState(State.STOP.name());
			}
			domainService.updateDomain(domain);

		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/start")
	public String startDomain(Model model, @Valid @RequestBody FlowEngineDomainStatus domainStatus,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(domainStatus.getDomain());
			final ResponseEntity<?> re = startStop(domain.getId());
			if (!re.getStatusCode().equals(HttpStatus.OK))
				if (re.getStatusCode().equals(HttpStatus.FORBIDDEN))
					return ERROR_403;
				else
					throw new Exception();
			domainStatus.setState(State.START.name());
			model.addAttribute(FLOW_ENGINE_ACTIVE_STR, true);
		} catch (final Exception e) {
			log.error("Unable to start domain = {}.", domainStatus.getDomain());
			model.addAttribute(MESSAGE_STR, utils.getMessage("domain.error.notstarted", "Unable to stop domain"));
			model.addAttribute(MESSAGE_ALERT_TYPE_STR, "ERROR");
			model.addAttribute(FLOW_ENGINE_ACTIVE_STR, false);
		}
		final List<FlowEngineDomainStatus> domainStatusList = getUserDomains(userService.getUser(utils.getUserId()),
				model);
		model.addAttribute(DOMAINS_STR, domainStatusList);
		return "flows/list :: domain";

	}

	@PostMapping(value = "/stop", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String stopDomain(Model model, @Valid @RequestBody FlowEngineDomainStatus domainStatus,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(domainStatus.getDomain());
			final ResponseEntity<?> re = startStop(domain.getId());
			if (!re.getStatusCode().equals(HttpStatus.OK))
				if (re.getStatusCode().equals(HttpStatus.FORBIDDEN))
					return ERROR_403;
				else
					throw new Exception();
			// Clean status not executing
			domainStatus.setState(State.STOP.name());
			domainStatus.setCpu("--");
			domainStatus.setMemory("--");
		} catch (final Exception e) {
			log.error("Unable to start domain = {}.", domainStatus.getDomain());
			model.addAttribute(MESSAGE_STR, utils.getMessage("domain.error.notstopped", "Unable to stop domain"));
			model.addAttribute(MESSAGE_ALERT_TYPE_STR, "ERROR");
		}
		final List<FlowEngineDomainStatus> domainStatusList = getUserDomains(userService.getUser(utils.getUserId()),
				model);
		model.addAttribute(DOMAINS_STR, domainStatusList);
		return "flows/list :: domain";

	}

	@GetMapping(value = "/show/{domainId}", produces = "text/html")
	public String showNodeRedPanelForm(Model model, @PathVariable(value = "domainId") String domainId) {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainId);
		if (domainService.hasUserViewAccess(domain.getId(), utils.getUserId())) {
			final String password = domain.getUser().getPassword();
			final String auth = domain.getUser() + ":" + password;
			final String authBase64 = Base64.getEncoder().encodeToString(auth.getBytes());
			/*
			 * model.addAttribute("proxy", "http://localhost:5050/" + domainId +
			 * "/?usuario=" + utils.getUserId() + "&password=" + password);
			 */
			model.addAttribute("proxy", proxyUrl + domainId + "/?authentication=" + authBase64);
			return "flows/show";
		} else
			return ERROR_403;

	}

	@GetMapping(value = "/check/{domainId}")
	public @ResponseBody boolean checkAvailableDomainIdentifier(@PathVariable(value = "domainId") String domainId) {
		return !domainService.domainExists(domainId);
	}

	private List<FlowEngineDomainStatus> getUserDomains(User user, Model model) {
		List<FlowEngineDomainStatus> domainStatusList = null;
		final List<FlowDomain> domainList = domainService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		if (domainList != null && !domainList.isEmpty()) {

			final List<String> domainListIds = new ArrayList<>();
			for (final FlowDomain domain : domainList) {
				domainListIds.add(domain.getIdentification());
			}
			// Get status info from FlowEngineAdmin
			try {
				domainStatusList = flowEngineService.getFlowEngineDomainStatus(domainListIds);
				model.addAttribute(FLOW_ENGINE_ACTIVE_STR, true);
			} catch (final Exception e) {
				// Flow Engine is either unavailable or not synchronized
				log.error("Unable to retrieve Flow Domain info. Cause = {}, Message = {}", e.getCause(),
						e.getMessage());
				model.addAttribute(FLOW_ENGINE_ACTIVE_STR, false);
				model.addAttribute(MESSAGE_STR,
						utils.getMessage("domain.flow.Engine.notstarted", "Flow Engine is temporarily unreachable."));
				model.addAttribute(MESSAGE_ALERT_TYPE_STR, "WARNING");
			}

			if (domainStatusList != null && !domainStatusList.isEmpty()) {
				// change memory to MB
				for (final FlowEngineDomainStatus domainStatus : domainStatusList) {
					if (!domainStatus.getMemory().isEmpty()) {
						final Double mem = new Double(domainStatus.getMemory()) / (1024d * 1024d);
						domainStatus.setMemory(String.format("%.2f", mem));
					}
					if (!domainStatus.getCpu().isEmpty()) {
						final Double cpu = new Double(domainStatus.getCpu());
						domainStatus.setCpu(String.format("%.2f", cpu));
					}
				}
			} else {
				// Create default
				domainStatusList = new ArrayList<>();
				for (final FlowDomain domain : domainList) {
					final FlowEngineDomainStatus domainStatus = FlowEngineDomainStatus.builder()
							.domain(domain.getIdentification()).home(domain.getHome()).port(domain.getPort())
							.servicePort(domain.getServicePort()).cpu("--").memory("--")
							.user(domain.getUser().getUserId()).state(domain.getState()).runtimeState("--").build();
					domainStatusList.add(domainStatus);
				}
			}
		}
		return domainStatusList;
	}
}
