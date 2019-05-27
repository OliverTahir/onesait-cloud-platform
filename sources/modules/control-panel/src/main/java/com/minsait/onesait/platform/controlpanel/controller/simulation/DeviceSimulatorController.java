/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.DeviceSimulation;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.DataSchemaValidationException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.simulation.DeviceSimulationService;
import com.minsait.onesait.platform.controlpanel.services.simulation.SimulationService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
@RequestMapping("devicesimulation")
public class DeviceSimulatorController {

	@Autowired
	private DeviceSimulationService deviceSimulationService;
	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private SimulationService simulationService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	private static final String SIMULATORS_STR = "simulators";

	@GetMapping("list")
	public String List(Model model) {

		List<DeviceSimulation> simulations = new ArrayList<DeviceSimulation>();
		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			simulations = deviceSimulationService.getAllSimulations();
		else
			simulations = deviceSimulationService.getSimulationsForUser(utils.getUserId());

		model.addAttribute("simulations", simulations);
		return "simulator/list";
	}

	@GetMapping("create")
	public String createForm(Model model) {
		final List<String> clients = deviceSimulationService.getClientsForUser(utils.getUserId()).stream()
				.filter(c -> clientPlatformService.getOntologiesByClientPlatform(c).size() > 0)
				.collect(Collectors.toList());
		final List<String> simulators = deviceSimulationService.getSimulatorTypes();
		model.addAttribute("platformClients", clients);
		model.addAttribute(SIMULATORS_STR, simulators);
		model.addAttribute("simulation", new DeviceSimulation());
		return "simulator/create";
	}

	@GetMapping("update/{id}")
	public String updateForm(Model model, @PathVariable("id") String id) {
		final List<String> clients = deviceSimulationService.getClientsForUser(utils.getUserId());
		final List<String> simulators = deviceSimulationService.getSimulatorTypes();
		final DeviceSimulation simulation = deviceSimulationService.getSimulationById(id);
		model.addAttribute("platformClient", simulation.getClientPlatform());
		model.addAttribute("ontology", simulation.getOntology());
		model.addAttribute("token", simulation.getToken());
		model.addAttribute("platformClients", clients);
		model.addAttribute(SIMULATORS_STR, simulators);
		model.addAttribute("simulation", simulation);
		model.addAttribute("ontologies", deviceSimulationService
				.getClientOntologiesIdentification(simulation.getClientPlatform().getIdentification()));
		model.addAttribute("tokens", deviceSimulationService
				.getClientTokensIdentification(simulation.getClientPlatform().getIdentification()));
		return "simulator/create";
	}

	@PostMapping("create")
	public String create(Model model, @RequestParam String identification, @RequestParam String jsonMap,
			@RequestParam String ontology, @RequestParam String clientPlatform, @RequestParam String token,
			@RequestParam int interval, @RequestParam String jsonInstances, @RequestParam String instancesMode)
			throws JsonProcessingException, IOException {

		simulationService.createSimulation(identification, interval, utils.getUserId(),

				simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology, jsonMap,
						jsonInstances, instancesMode));

		return "redirect:/devicesimulation/list";
	}

	@PostMapping("ontologiesandtokens")
	public String getOntologiesAndTokens(Model model, @RequestParam String clientPlatformId) {

		model.addAttribute("ontologies", deviceSimulationService.getClientOntologiesIdentification(clientPlatformId));
		model.addAttribute("tokens", deviceSimulationService.getClientTokensIdentification(clientPlatformId));

		return "simulator/create :: ontologiesAndTokens";
	}

	@PostMapping("ontologyfields")
	public String getOntologyfields(Model model, @RequestParam String ontologyIdentification)
			throws JsonProcessingException, IOException {

		model.addAttribute("fields", ontologyService.getOntologyFields(ontologyIdentification, utils.getUserId()));
		model.addAttribute(SIMULATORS_STR, deviceSimulationService.getSimulatorTypes());
		return "simulator/create :: ontologyFields";
	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		final DeviceSimulation simulation = deviceSimulationService.getSimulationById(id);
		List<DeviceSimulation> simulations = new ArrayList<DeviceSimulation>();
		if (simulation != null) {
			if (simulation.isActive())
				simulationService.unscheduleSimulation(simulation);
			else
				simulationService.scheduleSimulation(simulation);
		}
		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			simulations = deviceSimulationService.getAllSimulations();
		else
			simulations = deviceSimulationService.getSimulationsForUser(utils.getUserId());
		model.addAttribute("simulations", simulations);
		return "simulator/list :: simulations";

	}

	@PutMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id, @RequestParam String identification,
			@RequestParam String jsonMap, @RequestParam String ontology, @RequestParam String clientPlatform,
			@RequestParam String token, @RequestParam int interval, @RequestParam String jsonInstances,
			@RequestParam String instancesMode, RedirectAttributes redirect)
			throws JsonProcessingException, IOException {

		final DeviceSimulation simulation = deviceSimulationService.getSimulationById(id);
		if (simulation != null) {
			if (!simulation.isActive()) {
				simulationService.updateSimulation(identification, interval,

						simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology,
								jsonMap, jsonInstances, instancesMode),

						simulation);
				return "redirect:/devicesimulation/list";
			} else {
				utils.addRedirectMessage("simulation.update.isactive", redirect);
				return "redirect:/devicesimulation/update/" + id;
			}

		} else {
			utils.addRedirectMessage("simulation.update.error", redirect);
			return "redirect:/devicesimulation/update/" + id;
		}

	}

	@DeleteMapping("{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		final DeviceSimulation simulation = deviceSimulationService.getSimulationById(id);
		if (simulation != null) {
			try {
				entityDeletionService.deleteDeviceSimulation(simulation);
			} catch (final Exception e) {
				utils.addRedirectException(e, redirect);
				return "error";
			}
			return "ok";
		} else {
			utils.addRedirectMessage("simulation.exists.false", redirect);
			return "error";
		}
	}

	@PostMapping("checkjson")
	public @ResponseBody String checkJson(Model model, @RequestParam("json") String Json,
			@RequestParam("ontology") String ontology) {
		final ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(Json);
			if (node.isArray())
				node.forEach(n -> {
					ontologyDataService.checkOntologySchemaCompliance(n,
							ontologyService.getOntologyByIdentification(ontology, utils.getUserId()));
				});
			else {

				ontologyDataService.checkOntologySchemaCompliance(node,
						ontologyService.getOntologyByIdentification(ontology, utils.getUserId()));
			}
		} catch (final IOException e) {
			return "Invalid json";
		} catch (final DataSchemaValidationException e) {
			return e.getMessage();
		}
		return "ok";

	}
}
