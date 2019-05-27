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
package com.minsait.onesait.platform.controlpanel.controller.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.client.dto.DeviceDTO;
import com.minsait.onesait.platform.config.services.client.dto.GenerateTokensResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationRequest;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenSelectedRequest;
import com.minsait.onesait.platform.config.services.client.dto.TokensRequest;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/devices")
@Slf4j
public class ClientPlatformController {

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;
	@Autowired
	private OPResourceService resourceService;

	private static final String LOG_ONTOLOGY_PREFIX = "LOG_";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ACCESS_LEVEL_STR = "accessLevel";
	private static final String DEVICE_STR = "device";
	private static final String REDIRECT_DEV_CREATE = "redirect:/devices/create";
	private static final String REDIRECT_DEV_LIST = "redirect:/devices/list";
	private static final String ERROR_403 = "/error/403";
	private static final String TOKEN_STR = "token";
	private static final String ACTIVE_STR = "active";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification,
			@RequestParam(required = false) String[] ontologies) {

		if (identification != null) {
			if (identification.equals(""))
				identification = null;
		}

		if (ontologies != null) {
			if (ontologies.length == 0)
				ontologies = null;
		}
		pupulateClientList(model,
				clientPlatformService.getAllClientPlatformByCriteria(utils.getUserId(), identification, ontologies));

		return "devices/list";

	}

	private void pupulateClientList(Model model, List<ClientPlatform> clients) {

		final List<DeviceDTO> devicesDTO = new ArrayList<DeviceDTO>();

		if (clients != null && clients.size() > 0) {
			for (final ClientPlatform client : clients) {
				final DeviceDTO deviceDTO = new DeviceDTO();
				deviceDTO.setUser(client.getUser().getUserId());
				deviceDTO.setDateCreated(client.getCreatedAt());
				deviceDTO.setDescription(client.getDescription());
				deviceDTO.setId(client.getId());
				deviceDTO.setIdentification(client.getIdentification());
				if (client.getClientPlatformOntologies() != null && client.getClientPlatformOntologies().size() > 0) {
					final List<String> list = new ArrayList<String>();
					for (final ClientPlatformOntology cpo : client.getClientPlatformOntologies()) {
						list.add(cpo.getOntology().getIdentification());
					}
					deviceDTO.setOntologies(StringUtils.arrayToDelimitedString(list.toArray(), ", "));
				}
				devicesDTO.add(deviceDTO);
			}
		}

		model.addAttribute("devices", devicesDTO);
		model.addAttribute(ONTOLOGIES_STR,
				ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(), null, null));
		model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final ClientPlatform device = clientPlatformService.getById(id);
			if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
				return new ResponseEntity<>(utils.getMessage("device.delete.error.forbidden", "forbidden"),
						HttpStatus.FORBIDDEN);
			}
			if (resourceService.isResourceSharedInAnyProject(device))
				return new ResponseEntity<>(
						"This Device Definition is shared within a Project, revoke access from project prior to deleting",
						HttpStatus.PRECONDITION_FAILED);
			try {
				manageDBPersistenceServiceFacade.removeTable4Ontology(LOG_ONTOLOGY_PREFIX + device.getIdentification());
			} catch (final Exception e) {
			}
			entityDeletionService.deleteClient(id);
		} catch (final Exception e) {
			return new ResponseEntity<>(utils.getMessage("device.delete.error", "error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("/controlpanel/devices/list", HttpStatus.OK);
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();

		createInitalTokenToJson(deviceDTO);
		model.addAttribute(DEVICE_STR, deviceDTO);
		model.addAttribute(ONTOLOGIES_STR,
				ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(), null, null));
		model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
		return "devices/create";
	}

	@PostMapping(value = { "/create" })
	public String createDevice(Model model, @Valid DeviceCreateDTO device, BindingResult bindingResult,
			RedirectAttributes redirect) {

		try {
			final String userId = utils.getUserId();
			final ClientPlatform ndevice = clientPlatformService.createClientPlatform(device, userId, false);
			final Ontology onto = clientPlatformService.createDeviceLogOntology(ndevice.getIdentification());
			ontologyBusinessService.createOntology(onto, userId, null);
			clientPlatformService.createOntologyRelation(onto, ndevice);

		} catch (final ClientPlatformServiceException e) {
			log.debug("Cannot create clientPlatform");
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final JSONException e) {
			log.debug("Cannot create clientPlatform");
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final OntologyBusinessServiceException e) {
			log.debug("Error creating ontology");
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final Exception e) {
			log.debug("Error creating device");
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		}
		return REDIRECT_DEV_LIST;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		final ClientPlatform device = clientPlatformService.getById(id);

		if (device != null) {
			if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
				return ERROR_403;
			}
			final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();
			deviceDTO.setId(device.getId());
			deviceDTO.setDescription(device.getDescription());
			deviceDTO.setIdentification(device.getIdentification());
			deviceDTO.setMetadata(device.getMetadata());
			deviceDTO.setUserId(device.getUser().getUserId());
			mapOntologiesToJson(model, device, deviceDTO);
			mapTokensToJson(model, device, deviceDTO);
			model.addAttribute(DEVICE_STR, deviceDTO);
			model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
			return "devices/create";
		} else {
			return REDIRECT_DEV_LIST;
		}
	}

	private void mapOntologiesToJson(Model model, ClientPlatform device, DeviceCreateDTO deviceDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());

		for (final ClientPlatformOntology cpo : device.getClientPlatformOntologies()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("id", cpo.getOntology().getIdentification());
			on.put("access", cpo.getAccess());

			for (final Iterator<Ontology> iterator = ontologies.iterator(); iterator.hasNext();) {
				final Ontology ontology = iterator.next();
				if (ontology.getIdentification().equals(cpo.getOntology().getIdentification())) {
					iterator.remove();
					break;
				}
			}
			arrayNode.add(on);
		}

		try {
			deviceDTO.setClientPlatformOntologies(mapper.writer().writeValueAsString(arrayNode));
			model.addAttribute(ONTOLOGIES_STR, ontologies);
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void mapTokensToJson(Model model, ClientPlatform device, DeviceCreateDTO deviceDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		for (final Token token : device.getTokens()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("id", token.getId());
			on.put(TOKEN_STR, token.getToken());
			on.put(ACTIVE_STR, token.isActive());
			arrayNode.add(on);
		}

		try {
			deviceDTO.setTokens(mapper.writer().writeValueAsString(arrayNode));
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void createInitalTokenToJson(DeviceCreateDTO deviceDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();

		final ObjectNode on = mapper.createObjectNode();
		on.put("id", "");
		on.put(TOKEN_STR, UUID.randomUUID().toString().replaceAll("-", ""));
		on.put(ACTIVE_STR, true);
		arrayNode.add(on);

		try {
			deviceDTO.setTokens(mapper.writer().writeValueAsString(arrayNode));
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDevice(Model model, @PathVariable("id") String id, @Valid DeviceCreateDTO uDevice,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some device properties missing");
			utils.addRedirectMessage("device.validation.error", redirect);
			return "redirect:/devices/update/" + id;
		}

		if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
			return ERROR_403;
		}
		try {
			clientPlatformService.updateDevice(uDevice, utils.getUserId());
		} catch (final ClientPlatformServiceException e) {
			log.debug("Cannot update device");
			utils.addRedirectMessage("device.update.error", redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final JSONException e) {
			log.debug("Cannot update device");
			utils.addRedirectMessage("device.update.error", redirect);
			return REDIRECT_DEV_CREATE;
		}

		return REDIRECT_DEV_LIST;
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final ClientPlatform device = clientPlatformService.getById(id);

		if (device != null) {
			if (!clientPlatformService.hasUserViewAccess(id, utils.getUserId())) {
				return ERROR_403;
			}
			final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();
			deviceDTO.setId(device.getId());
			deviceDTO.setDescription(device.getDescription());
			deviceDTO.setIdentification(device.getIdentification());
			deviceDTO.setMetadata(device.getMetadata());
			deviceDTO.setUserId(device.getUser().getUserId());
			mapOntologiesToJson(model, device, deviceDTO);
			mapTokensToJson(model, device, deviceDTO);
			model.addAttribute(DEVICE_STR, deviceDTO);
			model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
			return "devices/show";
		} else {
			return REDIRECT_DEV_LIST;
		}

	}

	@PostMapping(value = "/desactivateToken")
	public @ResponseBody TokenActivationResponse desactivateToken(@RequestBody TokenActivationRequest request) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setRequestedActive(request.isActive());
		response.setToken(request.getToken());
		try {
			final Token token = tokenService.getTokenByID(request.getToken());
			tokenService.deactivateToken(token, request.isActive());
			response.setOk(true);
		} catch (final Exception e) {
			response.setOk(false);
		}
		return response;
	}

	@PostMapping(value = "/deleteToken")
	public @ResponseBody TokenActivationResponse deleteToken(@RequestBody TokenSelectedRequest request) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setToken(request.getToken());
		try {
			final Token token = tokenService.getTokenByID(request.getToken());
			if (!clientPlatformService.hasUserManageAccess(token.getClientPlatform().getId(), utils.getUserId())) {
				response.setOk(false);
			} else {
				entityDeletionService.deleteToken(token.getId());
				response.setOk(true);
			}
		} catch (final Exception e) {
			response.setOk(false);
		}
		return response;
	}

	@PostMapping(value = "/generateToken")
	public @ResponseBody GenerateTokensResponse generateTokens(@RequestBody TokensRequest request) {

		final GenerateTokensResponse response = new GenerateTokensResponse();
		if (request == null || request.getDeviceIdentification() == null
				|| request.getDeviceIdentification().equals("")) {
			response.setOk(false);
		}
		if (tokenService.generateTokenForClient(
				clientPlatformService.getByIdentification(request.getDeviceIdentification())) != null) {
			response.setOk(true);
		} else {
			response.setOk(false);
		}
		return response;
	}

	@PostMapping(value = "/loadDeviceTokens")
	public @ResponseBody String loadDeviceTokens(@RequestBody TokensRequest request) {

		final ClientPlatform clientPlatform = clientPlatformService
				.getByIdentification(request.getDeviceIdentification());
		if (!clientPlatformService.hasUserManageAccess(clientPlatform.getId(), utils.getUserId())) {
			return ERROR_403;
		} else {
			final List<Token> tokens = tokenService.getTokens(clientPlatform);
			if (tokens != null && tokens.size() > 0) {
				final ObjectMapper mapper = new ObjectMapper();
				final ArrayNode arrayNode = mapper.createArrayNode();

				for (final Token token : tokens) {
					final ObjectNode on = mapper.createObjectNode();
					on.put("id", token.getId());
					on.put(TOKEN_STR, token.getToken());
					on.put(ACTIVE_STR, token.isActive());
					arrayNode.add(on);
				}

				try {
					return mapper.writer().writeValueAsString(arrayNode);
				} catch (final JsonProcessingException e) {
					return ERROR_403;
				}
			}
			return "[]";
		}

	}

}
