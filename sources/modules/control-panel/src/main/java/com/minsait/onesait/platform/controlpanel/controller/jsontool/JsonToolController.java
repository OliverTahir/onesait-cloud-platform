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
package com.minsait.onesait.platform.controlpanel.controller.jsontool;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/jsontool")
@Slf4j
public class JsonToolController {

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private RouterService routerService;

	private final ObjectMapper mapper = new ObjectMapper();

	private static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	private static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";
	private static final String PATH_PROPERTIES = "properties";
	private static final String DEFAULT_META_INF = "imported,json";

	@GetMapping("tools")
	public String show(Model model) {
		model.addAttribute("datasources", ontologyService.getDatasources());
		model.addAttribute("ontologies", ontologyService.getOntologiesByUserId(utils.getUserId()));
		return "json2ontologytool/import";
	}

	@PostMapping("createontology")
	public @ResponseBody String createOntology(Model model, @RequestParam String ontologyIdentification,
			@RequestParam String ontologyDescription, @RequestParam String schema, @RequestParam String instance,
			@RequestParam String datasource) throws IOException {

		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(completeSchema(schema, ontologyIdentification, ontologyDescription).toString());
		ontology.setIdentification(ontologyIdentification);
		ontology.setActive(true);
		ontology.setDataModel(dataModelService.getDataModelByName(DATAMODEL_DEFAULT_NAME));
		ontology.setDescription(ontologyDescription);
		ontology.setUser(userService.getUser(utils.getUserId()));
		ontology.setMetainf(DEFAULT_META_INF);
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(datasource));
		try {
			ontologyBusinessService.createOntology(ontology, ontology.getUser().getUserId(), null);
		} catch (final Exception e) {
			return "{\"result\":\"ko\", \"cause\" :\"" + e.getMessage().replaceAll("\"", "'") + "\"}";
		}

		return "{\"result\":\"ok\"}";
	}

	@PostMapping("importbulkdata")
	public @ResponseBody String importBulkData(Model model, @RequestParam String data,
			@RequestParam String ontologyIdentification) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final JsonNode node = mapper.readTree(data);
			final OperationModel operation;

			operation = new OperationModel.Builder(ontologyIdentification, OperationType.INSERT, utils.getUserId(),
					Source.INTERNAL_ROUTER).body(node.toString()).queryType(QueryType.NATIVE).build();
			final NotificationModel modelNotification = new NotificationModel();
			modelNotification.setOperationModel(operation);

			try {
				final OperationResultModel response = routerService.insert(modelNotification);
				if (response.getResult().equals("OK")) {

					try {
						Integer.parseInt(response.getResult());
						return "{\"result\":\"ok\", \"inserted\" :" + response.getResult() + "}";

					} catch (final NumberFormatException ne) {
						return "{\"result\":\"ok\", \"inserted\" :\"" + response.getResult() + "\"}";
					}

				} else {
					log.error("Error insert BULK data:" + response.getMessage());
					return "{\"result\":\"ERROR\", \"cause\" :\"Error " + response.getMessage() + "\"}";
				}
			} catch (final Exception e) {
				return "{\"result\":\"ERROR\", \"cause\" :\"Error insert BULK data. "
						+ e.getMessage().replaceAll("\"", "'") + "\"}";
			}

		} catch (final IOException e) {
			return "{\"result\":\"ko\", \"cause\" :\"Error parsing JSON. " + e.getMessage().replaceAll("\"", "'")
					+ "\"}";
		}

	}

	@PostMapping("/getParentNodeOfSchema")
	public @ResponseBody String parentNode(@RequestParam String id) throws IOException {
		final Ontology ontology = ontologyService.getOntologyByIdentification(id, utils.getUserId());
		if (ontology != null) {
			final String jsonSchema = ontology.getJsonSchema();

			final JsonNode schema = mapper.readTree(jsonSchema);
			if (schema.path(PATH_PROPERTIES).size() == 1) {
				return schema.path(PATH_PROPERTIES).fieldNames().next();
			}
		}
		return "";
	}

	public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = mapper.readTree(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}
}
