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
package com.minsait.onesait.platform.controlpanel.controller.graph;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GraphUtil {

	private String urlClientPlatform;
	private String urlDashboard;
	private String urlGadget;
	private String urlOntology;
	private String urlApis;
	private String urlFlows;
	private String urlDigitalTwin;
	private String urlWebProjects;
	private String urlNotebook;
	private String urlDataflow;
	private final String genericUserName = "USER";
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private IntegrationResourcesService intregationResourcesService;
	@Autowired
	private UserService userService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private FlowDomainService flowService;
	@Autowired
	private WebProjectService webProjectService;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private DataflowService dataflowService;
	private String url;
	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private final String rootWWW = "";

	private static final String SCHEMA_BASE_PATH = "/schema/";
	private static final String CREATE_STR = "create";
	private static final String SHOW_STR = "show/";
	private static final String ONTOLOGY_STR = "ontology";
	private static final String LICENSING_STR = "licensing";
	private static final String COLUMNS_STR = "columns";

	@PostConstruct
	public void init() {
		// initialize URLS

		url = intregationResourcesService.getUrl(Module.controlpanel, ServiceUrl.base);
		urlClientPlatform = url + "/devices/show/";
		urlGadget = url + "/gadgets/";
		urlDashboard = url + "/dashboards/";
		urlOntology = url + "/ontologies/";
		urlApis = url + "/apimanager/";
		urlFlows = url + "/flows/";
		urlDigitalTwin = url + "/digitaltwindevices/";
		urlWebProjects = url + "/webprojects/";
		urlNotebook = url + "/notebooks/";
		urlDataflow = url + "/dataflow/";

	}

	public List<GraphDTO> constructGraphWithOntologies(List<Ontology> ontologies) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.ontologies", "ONTOLOGIES");
		final String description = utils.getMessage("tooltip_ontologies", null);

		arrayLinks.add(new GraphDTO(genericUserName, name, null, urlOntology + "list", genericUserName, "ONTOLOGIES",
				utils.getUserId(), name, "suit", description, urlOntology + CREATE_STR));

		if (ontologies == null) {
			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
				ontologies = ontologyRepository.findAll();
			else
				ontologies = ontologyRepository
						.findByUserAndOntologyUserAccessAndAllPermissions(userService.getUser(utils.getUserId()));

		}
		for (final Ontology ont : ontologies) {
			final Set<OntologyRelation> relations = new TreeSet<>();
			try {
				addOntologyReferenceLinks(ont, arrayLinks, relations);
			} catch (final IOException e) {
				log.error("Not adding ontology {} references, cause: ", ont.getIdentification(), e.getMessage());

			}
			arrayLinks.add(new GraphDTO(name, ont.getId(), urlOntology + "list", urlOntology + SHOW_STR + ont.getId(),
					name, ONTOLOGY_STR, name, ont.getIdentification(), LICENSING_STR, relations));
		}
		return arrayLinks;
	}

	public void addOntologyReferenceLinks(Ontology ontology, List<GraphDTO> arrayLinks, Set<OntologyRelation> relations)
			throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaOrigin = mapper.readTree(ontology.getJsonSchema());
		if (!schemaOrigin.path("_references").isMissingNode()) {
			schemaOrigin.path("_references").forEach(r -> {
				String originAtt = getOriginAtt(r.get("self").asText());
				String targetAtt = r.get("target").asText().split("#")[1].replaceAll("properties.", "");
				final String targetOntology = getTargetOntology(r.get("target").asText());
				final Ontology target = ontologyRepository.findByIdentification(targetOntology);
				final String refOrigin = refJsonSchema(schemaOrigin);
				if (!"".equals(refOrigin))
					originAtt = originAtt.replaceAll(refOrigin.replace("/", ""),
							schemaOrigin.at("/required/0").asText());
				if (target == null)
					throw new RuntimeException(
							"Target ontology of " + ontology.getIdentification() + " not found on platform");
				try {
					final JsonNode schemaTarget = mapper.readTree(target.getJsonSchema());
					final String refTarget = refJsonSchema(schemaTarget);
					if (!"".equals(refTarget))
						targetAtt = targetAtt.replaceAll(refTarget.replace("/", ""),
								schemaTarget.at("/required/0").asText());
				} catch (final IOException e) {
					log.debug("No $ref");
				}
				relations.add(new OntologyRelation(ontology.getIdentification(), target.getIdentification(), originAtt,
						targetAtt));
				arrayLinks.add(new GraphDTO(ontology.getId(), target.getId(), urlOntology + SHOW_STR + ontology.getId(),
						urlOntology + SHOW_STR + target.getId(), ONTOLOGY_STR, ONTOLOGY_STR,
						ontology.getIdentification(), target.getIdentification(), LICENSING_STR));

			});
		}
	}

	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path("properties").fields();
		String reference = "";
		while (elements.hasNext()) {
			final Entry<String, JsonNode> entry = elements.next();
			if (!entry.getValue().path("$ref").isMissingNode()) {
				final String ref = entry.getValue().path("$ref").asText();
				reference = ref.substring(ref.lastIndexOf("#/")).substring(1);
			}
		}
		return reference;
	}

	public String getOriginAtt(String self) {
		return self.replaceAll("properties.", "");
	}

	public String getTargetOntology(String target) {
		return target.replaceFirst("ontologies/schema/", "").split("#")[0];
	}

	public List<GraphDTO> constructGraphWithAPIs(List<Api> apis) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.apis", "APIS");
		final String description = utils.getMessage("tooltip_apis", null);

		if (apis == null) {
			apis = apiManagerService.loadAPISByFilter(null, null, null, utils.getUserId());
		}

		try {
			arrayLinks.add(new GraphDTO(genericUserName, name, null, urlApis + "list", genericUserName, "apis",
					utils.getUserId(), name, "suit", description, urlApis + CREATE_STR));
			apis.forEach(a -> {
				arrayLinks.add(new GraphDTO(name, a.getId(), urlApis + "list", urlApis + SHOW_STR + a.getId(), name,
						"api", name, a.getIdentification(), LICENSING_STR));
				arrayLinks.add(new GraphDTO(a.getId(), a.getOntology().getId(), urlApis + SHOW_STR + a.getId(),
						urlOntology + SHOW_STR + a.getOntology().getId(), "api", ONTOLOGY_STR, a.getIdentification(),
						a.getOntology().getIdentification(), LICENSING_STR));
			});
		} catch (Exception e) {
			log.error("An error has ocurred loading graph with apis", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithFlows(List<FlowDomain> domains) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.flows", "FLOWS");
		final String description = utils.getMessage("tooltip_flows", null);

		if (domains == null) {
			domains = flowService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		}

		try {
			arrayLinks.add(new GraphDTO(genericUserName, name, null, urlFlows + "list", genericUserName, "flows",
					utils.getUserId(), name, "suit", description, urlFlows + CREATE_STR));
			domains.forEach(d -> {
				arrayLinks.add(
						new GraphDTO(name, d.getId(), urlFlows + "list", urlFlows + SHOW_STR + d.getIdentification(),
								name, "flow", name, d.getIdentification(), LICENSING_STR));
			});
		} catch (Exception e) {
			log.error("An error has ocurred loading graph with flows", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithWebProjects(List<WebProject> projects) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.webprojects", "WEB PROJECTS");
		final String description = utils.getMessage("tooltip_webprojects", null);

		// projects =
		// webProjectService.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(),
		// null, null);
		arrayLinks.add(new GraphDTO(genericUserName, name, null, urlWebProjects + "list", genericUserName,
				"webprojects", utils.getUserId(), name, "suite", description, urlWebProjects + CREATE_STR));
		if (projects != null) {
			try {
				projects.forEach(p -> {
					arrayLinks.add(new GraphDTO(name, p.getIdentification(), urlWebProjects + "list",
							intregationResourcesService.getUrl(Module.domain, ServiceUrl.base) + "web/"
									+ p.getIdentification() + "/" + p.getMainFile(),
							name, "webproject", name, p.getIdentification(), LICENSING_STR));
				});
			} catch (Exception e) {
				log.error("An error has ocurred loading graph with web projects", e);
			}
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithDigitalTwins(List<DigitalTwinDevice> twins) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.digitaltwin", "DIGITAL TWINS");
		final String description = utils.getMessage("tooltip_digitaltwin", null);

		if (twins == null) {
			twins = digitalTwinDeviceService.getAllByUserId(utils.getUserId());
		}

		try {
			arrayLinks.add(new GraphDTO(genericUserName, name, null, urlDigitalTwin + "list", genericUserName,
					"digitaltwins", utils.getUserId(), name, "suit", description, urlDigitalTwin + CREATE_STR));
			twins.forEach(dt -> {
				arrayLinks.add(
						new GraphDTO(name, dt.getId(), urlDigitalTwin + "list", urlDigitalTwin + SHOW_STR + dt.getId(),
								name, "digitaltwin", name, dt.getIdentification(), LICENSING_STR));
			});
		} catch (Exception e) {
			log.error("An error has ocurred loading graph with digital twins", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithClientPlatforms(List<ClientPlatform> clientPlatforms) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.clients", "PLATFORM CLIENTS");
		final String description = utils.getMessage("tooltip_clients", null);

		// carga de nodo clientPlatform
		arrayLinks.add(new GraphDTO(genericUserName, name, null, urlClientPlatform + "list", genericUserName,
				"deviceandsystems", utils.getUserId(), name, "suit", description, urlClientPlatform + CREATE_STR));

		if (clientPlatforms == null) {
			clientPlatforms = clientPlatformRepository.findByUser(userService.getUser(utils.getUserId()));
		}

		try {
			for (final ClientPlatform clientPlatform : clientPlatforms) {
				// Creación de enlaces
				arrayLinks.add(new GraphDTO(name, clientPlatform.getId(), urlClientPlatform + "list",
						urlClientPlatform + clientPlatform.getIdentification(), name, "clientplatform", name,
						clientPlatform.getIdentification(), LICENSING_STR));

				if (clientPlatform.getClientPlatformOntologies() != null) {
					final List<ClientPlatformOntology> clientPlatformOntologies = new LinkedList<>(
							clientPlatform.getClientPlatformOntologies());
					for (final ClientPlatformOntology clientPlatformOntology : clientPlatformOntologies) {
						final Ontology ontology = clientPlatformOntology.getOntology();
						// Crea link entre ontologia y clientPlatform

						arrayLinks.add(new GraphDTO(ontology.getId(), clientPlatform.getId(),
								urlOntology + ontology.getId(), urlClientPlatform + clientPlatform.getIdentification(),
								ONTOLOGY_STR, "clientplatform", ontology.getIdentification(),
								clientPlatform.getIdentification(), LICENSING_STR));
					}
				}
			}
		} catch (Exception e) {
			log.error("An error has ocurred loading graph with client platforms", e);
		}

		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithGadgets(String visualizationId, String visualizationName,
			List<Gadget> gadgets) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.gadgets", "GADGETS");

		// carga de nodo gadget dependiente de visualizacion
		arrayLinks.add(new GraphDTO(visualizationId, name, null, urlGadget + "list", visualizationId, name,
				visualizationName, name, "suit", null, urlGadget + "selectWizard"));

		if (gadgets == null) {
			gadgets = gadgetRepository.findByUser(userService.getUser(utils.getUserId()));
		}

		if (gadgets != null) {
			try {
				for (final Gadget gadget : gadgets) {
					// Creación de enlaces
					arrayLinks.add(new GraphDTO(name, gadget.getId(), urlGadget + "list",
							urlGadget + "update/" + gadget.getId(), name, "gadget", name, gadget.getIdentification(),
							LICENSING_STR));

				}
				gadgets.clear();
			} catch (Exception e) {
				log.error("An error has ocurred loading graph with gadgets", e);
			}
		}

		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithDashboard(String visualizationId, String visualizationName,
			List<Dashboard> dashboards) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.dashboards", "DASHBOARDS");

		arrayLinks.add(new GraphDTO(visualizationId, name, null, urlDashboard + "list", visualizationId, name,
				visualizationName, name, "suit", null, urlDashboard + CREATE_STR));

		if (dashboards == null) {
			dashboards = dashboardRepository.findByUser(userService.getUser(utils.getUserId()));
		}

		try {
			for (final Dashboard dashboard : dashboards) {
				try {
					arrayLinks.add(new GraphDTO(name, dashboard.getId(), urlDashboard + "list",
							urlDashboard + "view/" + dashboard.getId(), name, "dashboard", name,
							dashboard.getIdentification(), LICENSING_STR));
					final List<String> gadgetIds = getGadgetIdsFromModel(dashboard.getModel());
					for (final String gadget : gadgetIds) {
						arrayLinks.add(new GraphDTO(gadget, dashboard.getId(), urlGadget + "update/" + gadget,
								urlDashboard + dashboard.getId(), "gadget", "dashboard", null,
								dashboard.getIdentification(), LICENSING_STR));
					}
				} catch (final Exception e) {

				}
			}
		} catch (Exception e) {
			log.error("An error has ocurred loading graph with dashboards", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithVisualization(List<Gadget> gadgets, List<Dashboard> dashboards) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.visualization", "VISUALIZATIONS");
		final String description = utils.getMessage("tooltip_visualization", null);
		// carga de nodo gadget
		arrayLinks.add(new GraphDTO(genericUserName, name, null, null, genericUserName, "VISUALIZATIONS",
				utils.getUserId(), name, "suit", description, null));

		arrayLinks.addAll(constructGraphWithGadgets(name, name, gadgets));

		arrayLinks.addAll(constructGraphWithDashboard(name, name, dashboards));

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithNotebooks(List<Notebook> notebooks) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final String name = utils.getMessage("name.notebook", "NOTEBOOKS");
			final String description = utils.getMessage("tooltip_notebooks", null);

			if (notebooks == null) {
				notebooks = notebookService.getNotebooks(utils.getUserId());
			}

			try {
				arrayLinks.add(new GraphDTO(genericUserName, name, null, urlNotebook + "list", genericUserName,
						"notebooks", utils.getUserId(), name, "suit", description, urlNotebook + CREATE_STR));
				notebooks.forEach(dt -> {
					arrayLinks.add(
							new GraphDTO(name, dt.getId(), urlNotebook + "list", urlNotebook + SHOW_STR + dt.getId(),
									name, "notebook", name, dt.getIdentification(), LICENSING_STR));
				});
			} catch (Exception e) {
				log.error("An error has ocurred loading graph with notebooks", e);
			}
		}
		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithDataFlows(List<Pipeline> dataflows) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final String name = utils.getMessage("name.dataflow", "DATAFLOWS");
			final String description = utils.getMessage("tooltip_dataflows", null);

			if (dataflows == null) {
				dataflows = dataflowService.getPipelines(utils.getUserId());
			}

			try {
				arrayLinks.add(new GraphDTO(genericUserName, name, null, urlDataflow + "list", genericUserName,
						"pipelines", utils.getUserId(), name, "suit", description, urlDataflow + CREATE_STR));
				dataflows.forEach(dt -> {
					arrayLinks.add(
							new GraphDTO(name, dt.getId(), urlDataflow + "list", urlDataflow + SHOW_STR + dt.getId(),
									name, "pipeline", name, dt.getIdentification(), LICENSING_STR));
				});
			} catch (Exception e) {
				log.error("An error has ocurred loading graph with dataflows", e);
			}
		}
		return arrayLinks;

	}

	public List<String> getGadgetIdsFromModel(String modelJson) throws JsonProcessingException, IOException {
		final List<String> gadgetIds = new LinkedList<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode jsonNode = objectMapper.readTree(modelJson);
		final int rows = jsonNode.path("rows").size();
		for (int i = 0; i < rows; i++) {
			final int columns = jsonNode.path("rows").path(i).path(COLUMNS_STR).size();
			for (int j = 0; j < columns; j++) {
				final int widgets = jsonNode.path("rows").path(i).path(COLUMNS_STR).path(j).path("widgets").size();
				for (int k = 0; k < widgets; k++) {
					String gadgetId = jsonNode.path("rows").path(i).path(COLUMNS_STR).path(j).path("widgets").path(k)
							.path("config").get("gadgetId").asText();
					gadgetId = gadgetId.split("_")[0];
					gadgetIds.add(gadgetId);
				}
			}

		}
		return gadgetIds;

	}

}