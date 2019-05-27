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
package com.minsait.onesait.platform.controlpanel.controller.gis.layer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/layers")
@Slf4j
public class LayerController {

	@Autowired
	LayerService layerService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private RouterService routerService;

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request) {

		final List<Layer> layers = layerService.findAllLayers(utils.getUserId());
		model.addAttribute("layers", layers);
		return "layers/list";
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		return "layers/create";
	}

	@GetMapping(value = "/createiot")
	public String createIoT(Model model) {
		List<Ontology> ontologies = ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(),
				null, null);
		model.addAttribute("ontologies", ontologies);
		model.addAttribute("layer", new LayerDTO());
		return "layers/createiot";
	}

	@GetMapping(value = "/createexternal")
	public String createExternal(Model model) {
		model.addAttribute("layer", new LayerDTO());
		return "layers/createexternal";
	}

	@GetMapping(value = "/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		Layer layer = layerService.findById(id, utils.getUserId());
		if (layer != null && layer.getOntology() != null) {
			LayerDTO layerDto = new LayerDTO();
			layerDto.setDescription(layer.getDescription());
			layerDto.setGeometryField(layer.getGeometryField());
			layerDto.setGeometryType(layer.getGeometryType());
			layerDto.setIdentification(layer.getIdentification());
			layerDto.setIsPublic(layer.isPublic());
			layerDto.setOntology(layer.getOntology().getIdentification());
			layerDto.setId(layer.getId());
			layerDto.setIsHeatMap(layer.isHeatMap());

			if (layer.isHeatMap()) {

				layerDto.setWeightField(layer.getWeightField());
				layerDto.setHeatMapMax(layer.getHeatMapMax().toString());
				layerDto.setHeatMapMin(layer.getHeatMapMin().toString());
				layerDto.setHeatMapRadius(layer.getHeatMapRadius().toString());
			} else {

				layer.setInnerColor(layerDto.getInnerColor());
				layer.setOuterColor(layerDto.getOuterColor());
				layer.setOuterThin(layerDto.getOuterThin());
				layer.setSize(layerDto.getSize());
				layerDto.setInfoBox(layer.getInfoBox());
			}

			model.addAttribute("layer", layerDto);
			return "layers/showiot";
		} else if (layer != null && layer.getOntology() == null) {
			LayerDTO layerDto = new LayerDTO();
			layerDto.setDescription(layer.getDescription());
			layerDto.setIdentification(layer.getIdentification());
			layerDto.setIsPublic(layer.isPublic());
			layerDto.setId(layer.getId());
			layerDto.setExternalType(layer.getExternalType());
			layerDto.setUrl(layer.getUrl());
			layerDto.setLayerTypeWms(layer.getLayerTypeWms());
			model.addAttribute("layer", layerDto);
			return "layers/showexternal";
		} else {
			utils.addRedirectMessage("ontology.notfound.error", redirect);
			return "redirect:/layers/list";
		}

	}

	@GetMapping(value = "/update/{id}")
	public String update(Model model, @PathVariable("id") String id) {
		Layer layer = layerService.findById(id, utils.getUserId());

		if (layer.getOntology() != null) {
			List<Ontology> ontologies = ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(),
					null, null);

			LayerDTO layerDto = new LayerDTO();
			layerDto.setDescription(layer.getDescription());
			layerDto.setGeometryField(layer.getGeometryField());
			layerDto.setGeometryType(layer.getGeometryType());
			layerDto.setIdentification(layer.getIdentification());
			layerDto.setIsPublic(layer.isPublic());
			layerDto.setOntology(layer.getOntology().getIdentification());
			layerDto.setId(layer.getId());
			layerDto.setIsHeatMap(layer.isHeatMap());

			if (layer.isHeatMap()) {

				layerDto.setWeightField(layer.getWeightField());
				layerDto.setHeatMapMax(layer.getHeatMapMax().toString());
				layerDto.setHeatMapMin(layer.getHeatMapMin().toString());
				layerDto.setHeatMapRadius(layer.getHeatMapRadius().toString());

			} else {

				layer.setInnerColor(layerDto.getInnerColor());
				layer.setOuterColor(layerDto.getOuterColor());
				layer.setOuterThin(layerDto.getOuterThin());
				layer.setSize(layerDto.getSize());
				layerDto.setInfoBox(layer.getInfoBox());
			}

			model.addAttribute("ontologies", ontologies);
			model.addAttribute("layer", layerDto);
			return "layers/createiot";
		} else {
			LayerDTO layerDto = new LayerDTO();
			layerDto.setDescription(layer.getDescription());
			layerDto.setIdentification(layer.getIdentification());
			layerDto.setIsPublic(layer.isPublic());
			layerDto.setId(layer.getId());
			layerDto.setExternalType(layer.getExternalType());
			layerDto.setUrl(layer.getUrl());
			layerDto.setLayerTypeWms(layer.getLayerTypeWms());
			model.addAttribute("layer", layerDto);
			return "layers/createexternal";
		}

	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return layerService.getAllIdentificationsByUser(utils.getUserId());
	}

	@PostMapping(value = "/getOntologyGeometryFields")
	public ResponseEntity<Map<String, String>> getOntologyGeometryFields(@RequestParam String ontologyIdentification)
			throws JsonProcessingException, IOException {

		try {
			return new ResponseEntity<Map<String, String>>(
					layerService.getOntologyGeometryFields(ontologyIdentification, utils.getUserId()), HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<Map<String, String>>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/getOntologyFields")
	public ResponseEntity<Map<String, String>> getOntologyFields(@RequestParam String ontologyIdentification)
			throws JsonProcessingException, IOException {

		try {
			return new ResponseEntity<Map<String, String>>(
					ontologyService.getOntologyFields(ontologyIdentification, utils.getUserId()), HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<Map<String, String>>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = { "/createiot", "/createexternal" })
	@Transactional
	public ResponseEntity<?> createLayer(org.springframework.ui.Model model, @Valid LayerDTO layerDto,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest httpServletRequest) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			response.put("status", "error");
			response.put("cause", utils.getMessage("ontology.validation.error", "validation error"));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		User user = userService.getUser(utils.getUserId());

		if (layerDto.getOntology() != null) {
			Ontology ontology = ontologyService.getOntologyByIdentification(layerDto.getOntology(), utils.getUserId());
			String infoBox = httpServletRequest.getParameter("infoBox");
			Boolean isPublic = Boolean.valueOf(httpServletRequest.getParameter("isPublic"));
			Boolean isHeatMap = Boolean.valueOf(httpServletRequest.getParameter("isHeatMap"));
			if (ontology != null) {
				Layer layer = new Layer();
				layer.setDescription(layerDto.getDescription());
				layer.setGeometryField(layerDto.getGeometryField());
				layer.setGeometryType(layerDto.getGeometryType());
				layer.setIdentification(layerDto.getIdentification());
				layer.setPublic(isPublic);
				layer.setUser(user);
				layer.setOntology(ontology);

				if (isHeatMap) {
					layer.setHeatMap(isHeatMap);
					layer.setWeightField(layerDto.getWeightField());
					layer.setHeatMapMax(Integer.valueOf(layerDto.getHeatMapMax()));
					layer.setHeatMapMin(Integer.valueOf(layerDto.getHeatMapMin()));
					layer.setHeatMapRadius(Integer.valueOf(layerDto.getHeatMapRadius()));
				} else {
					layer.setInnerColor(layerDto.getInnerColor());
					layer.setOuterColor(layerDto.getOuterColor());
					layer.setOuterThin(layerDto.getOuterThin());
					layer.setSize(layerDto.getSize());
					layer.setInfoBox(infoBox);
				}

				layerService.create(layer);

				response.put("redirect", "/controlpanel/layers/list");
				response.put("status", "ok");
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
			} else {
				log.error("Ontology {} not found for the user {} to create the layer {}", layerDto.getOntology(),
						user.getFullName(), layerDto.getIdentification());
				response.put("cause", "Ontology not found to create the layer");
				response.put("status", "error");
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.NOT_FOUND);
			}
		} else {
			Boolean isPublic = Boolean.valueOf(httpServletRequest.getParameter("isPublic"));
			Layer layer = new Layer();
			layer.setDescription(layerDto.getDescription());
			layer.setIdentification(layerDto.getIdentification());
			layer.setPublic(isPublic);
			layer.setUser(user);
			layer.setExternalType(layerDto.getExternalType());
			layer.setUrl(layerDto.getUrl());
			layer.setLayerTypeWms(layerDto.getLayerTypeWms());

			layerService.create(layer);

			response.put("redirect", "/controlpanel/layers/list");
			response.put("status", "ok");
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
		}
	}

	@PutMapping(value = "/update/{id}")
	@Transactional
	public ResponseEntity<?> updateLayer(org.springframework.ui.Model model, @Valid LayerDTO layerDto,
			@PathVariable("id") String id, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			response.put("status", "error");
			response.put("cause", utils.getMessage("ontology.validation.error", "validation error"));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		Layer layer = layerService.findById(id, utils.getUserId());
		User user = userService.getUser(utils.getUserId());

		if (layer.getOntology() != null) {

			Ontology ontology = ontologyService.getOntologyByIdentification(layerDto.getOntology(), utils.getUserId());
			String infoBox = httpServletRequest.getParameter("infoBox");
			Boolean isPublic = Boolean.valueOf(httpServletRequest.getParameter("isPublic"));
			Boolean isHeatMap = Boolean.valueOf(httpServletRequest.getParameter("isHeatMap"));
			if (ontology != null) {
				layer.setDescription(layerDto.getDescription());
				layer.setGeometryField(layerDto.getGeometryField());
				layer.setGeometryType(layerDto.getGeometryType());
				layer.setPublic(isPublic);
				layer.setOntology(ontology);
				layer.setHeatMap(isHeatMap);

				if (isHeatMap) {
					layer.setWeightField(layerDto.getWeightField());
					layer.setHeatMapMax(Integer.valueOf(layerDto.getHeatMapMax()));
					layer.setHeatMapMin(Integer.valueOf(layerDto.getHeatMapMin()));
					layer.setHeatMapRadius(Integer.valueOf(layerDto.getHeatMapRadius()));
					layer.setInnerColor(null);
					layer.setOuterColor(null);
					layer.setOuterThin(null);
					layer.setSize(null);
					layer.setInfoBox(null);
				} else {
					layer.setInnerColor(layerDto.getInnerColor());
					layer.setOuterColor(layerDto.getOuterColor());
					layer.setOuterThin(layerDto.getOuterThin());
					layer.setSize(layerDto.getSize());
					layer.setInfoBox(infoBox);
					layer.setWeightField(null);
					layer.setHeatMapMax(null);
					layer.setHeatMapMin(null);
				}

				layerService.create(layer);

				response.put("redirect", "/controlpanel/layers/list");
				response.put("status", "ok");
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
			} else {
				log.error("Ontology {} not found for the user {} to create the layer {}", layerDto.getOntology(),
						user.getFullName(), layerDto.getIdentification());
				response.put("cause", "Ontology not found to create the layer");
				response.put("status", "error");
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.NOT_FOUND);
			}
		} else {
			Boolean isPublic = Boolean.valueOf(httpServletRequest.getParameter("isPublic"));
			layer.setDescription(layerDto.getDescription());
			layer.setIdentification(layerDto.getIdentification());
			layer.setPublic(isPublic);
			layer.setUser(user);
			layer.setExternalType(layerDto.getExternalType());
			layer.setUrl(layerDto.getUrl());
			layer.setLayerTypeWms(layerDto.getLayerTypeWms());

			layerService.create(layer);

			response.put("redirect", "/controlpanel/layers/list");
			response.put("status", "ok");
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
		}

	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Layer layer = layerService.findById(id, utils.getUserId());
		if (layer != null) {
			try {

				layerService.deleteLayer(layer, utils.getUserId());

			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("ontology.delete.error", e.getMessage(), redirect);
				log.error("Error deleting layer. ", e);
				return "redirect:/layers/update/" + id;
			}
			return "redirect:/layers/list";
		} else {
			return "redirect:/layers/list";
		}
	}

	@GetMapping("/isLayerInUse/{layer}")
	public @ResponseBody Boolean isLayerInUse(@PathVariable("layer") String layer) {
		return this.layerService.isLayerInUse(layer);
	}

	@GetMapping(value = "/crud/{id}")
	public String crud(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		Layer layer = layerService.findById(id, utils.getUserId());
		if (layer != null) {
			model.addAttribute("layer", layer);
			model.addAttribute("ontologyName", layer.getOntology().getIdentification());
			model.addAttribute("schema", layer.getOntology().getJsonSchema());
			return "layers/crud";
		} else {
			utils.addRedirectMessage("ontology.notfound.error", redirect);
			return "redirect:/layers/list";
		}

	}

	@PostMapping(value = { "/crud/insert" }, produces = "text/plain")
	public @ResponseBody String insert(String ontologyID, String body) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.POST.name(), body, "");
			return result;
		} catch (final Exception e) {

			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/crud/update" }, produces = "text/plain")
	public @ResponseBody String update(String ontologyID, String body, String oid) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.PUT.name(), body, oid);
			return result;
		} catch (final Exception e) {

			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/crud/deleteById" }, produces = "application/json")
	public @ResponseBody String deleteById(String ontologyID, String oid) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.DELETE.name(), "", oid);
			return result;
		} catch (final Exception e) {

			return "{\"error\":\"true\"}";
		}
	}

	public String processQuery(String query, String ontologyID, String method, String body, String objectId)
			throws Exception {

		final User user = userService.getUser(utils.getUserId());
		OperationType operationType = null;
		if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			body = query;
			operationType = OperationType.QUERY;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
			operationType = OperationType.INSERT;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
			operationType = OperationType.UPDATE;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
			operationType = OperationType.DELETE;
		} else {
			operationType = OperationType.QUERY;
		}

		final OperationModel model = OperationModel
				.builder(ontologyID, OperationType.valueOf(operationType.name()), user.getUserId(),
						OperationModel.Source.INTERNAL_ROUTER)
				.body(body).queryType(QueryType.SQLLIKE).objectId(objectId).deviceTemplate("").build();
		final NotificationModel modelNotification = new NotificationModel();

		modelNotification.setOperationModel(model);

		final OperationResultModel result = routerService.query(modelNotification);

		if (result != null) {
			if ("ERROR".equals(result.getResult())) {
				String ret = "{\"error\":\"" + result.getMessage() + "\"}";
				return ret;
			}
			return result.getResult();
		} else {
			return null;
		}

	}

}
