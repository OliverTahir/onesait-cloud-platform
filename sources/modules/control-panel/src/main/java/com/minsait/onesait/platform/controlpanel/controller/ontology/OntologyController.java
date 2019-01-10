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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException.Error;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyKPIDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.ontology.OntologyKPIService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ontologies")
@Slf4j
public class OntologyController {

	@Autowired
	private OntologyService ontologyConfigService;

	@Autowired
	private OntologyKPIService ontologyKPIService;

	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private UserService userService;

	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private OntologyKPIRepository ontologyKPIRepository;

	@Autowired
	private DataModelService dataModelService;

	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ONTOLOGY_STR = "ontology";
	private static final String ONTOLOGY_REST_STR = "ontologyRest";
	private static final String ONTOLOGIES_CREATE = "ontologies/create";
	private static final String ERROR_STR = "error";
	private static final String STATUS_STR = "status";
	private static final String VAL_ERROR = "validation error";
	private static final String ONT_VAL_ERROR = "ontology.validation.error";
	private static final String CAUSE_STR = "cause";
	private static final String REDIRECT_STR = "redirect";
	private static final String GEN_INTERN_ERROR_CREATE_ONT = "Generic internal error creating ontology: ";
	private static final String REDIRECT_ONTOLOGIES_LIST = "redirect:/ontologies/list";
	private static final String DATA_MODELS_STR = "dataModels";
	private static final String DATA_MODEL_TYPES_STR = "dataModelTypes";
	private static final String RTDBS = "rtdbs";
	private static final String QUERY_RESULT = "queryResult";
	private static final String QUERY_TOOL_SHOW_QUERY = "querytool/show :: query";
	private static final String ERROR_IN_RUNQUERY = "Error in runQuery";

	private final ObjectMapper mapper = new ObjectMapper();

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null) {
			if (identification.equals(""))
				identification = null;
		}
		if (description != null) {
			if (description.equals(""))
				description = null;
		}

		final List<Ontology> ontologies = ontologyConfigService.getOntologiesByUserAndAccess(utils.getUserId(),
				identification, description);
		model.addAttribute(ONTOLOGIES_STR, ontologies);
		return "ontologies/list";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return ontologyConfigService.getAllIdentificationsByUser(utils.getUserId());
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(ONTOLOGY_STR, new Ontology());
		model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());
		populateForm(model);
		return ONTOLOGIES_CREATE;
	}

	@GetMapping(value = "/createwizard", produces = "text/html")
	public String createWizard(Model model) {
		Ontology ontology = (Ontology) model.asMap().get(ONTOLOGY_STR);
		if (ontology == null) {
			ontology = new Ontology();
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		} else {
			ontology.setId(null);
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		}

		populateForm(model);
		return "ontologies/createwizard";
	}

	@GetMapping(value = "/createapirest", produces = "text/html")
	public String createAPIREST(Model model) {

		model.addAttribute(ONTOLOGY_STR, new Ontology());
		populateFormApiRest(model);
		return "ontologies/createapirest";
	}

	@GetMapping(value = "/createvirtual", produces = "text/html")
	public String createVirtual(Model model) {

		model.addAttribute(ONTOLOGY_STR, new Ontology());
		populateFormVirtual(model);
		return "ontologies/createvirtual";
	}

	@GetMapping(value = "/createkpi", produces = "text/html")
	public String createkpi(Model model) {
		OntologyKPIDTO ontology = (OntologyKPIDTO) model.asMap().get(ONTOLOGY_STR);
		if (ontology == null) {
			ontology = new OntologyKPIDTO();
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		} else {
			ontology.setId(null);
			ontology.setPublic(false);
			model.addAttribute(ONTOLOGY_STR, ontology);
		}

		populateKPIForm(model);
		return "ontologies/createkpi";
	}

	@PostMapping(value = { "/create", "/createwizard", "/createapirest", "createvirtual" })
	public ResponseEntity<?> createOntology(Model model, @Valid Ontology ontology, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			ontology.setOntologyKPI(null);
			final OntologyConfiguration config = new OntologyConfiguration(request);
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), config);

			if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				response.put(REDIRECT_STR, "/controlpanel/ontologies/list");
			}
			response.put(STATUS_STR, "ok");
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);

		} catch (final OntologyBusinessServiceException e) {
			final Error error = e.getError();
			switch (error) {
			case ILLEGAL_ARGUMENT:
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
			case NO_VALID_SCHEMA:
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, "Invalid json schema: " + e.getMessage());
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
			case KAFKA_TOPIC_CREATION_ERROR:
			case CONFIG_CREATION_ERROR:
			case CONFIG_CREATION_ERROR_UNCLEAN:
			case PERSISTENCE_CREATION_ERROR:
			case PERSISTENCE_CREATION_ERROR_UNCLEAN:
				log.error("Cannot create ontology because of: " + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			default:
				log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final Exception e) {
			log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = { "/createkpi" })
	public ResponseEntity<?> createKPIOntology(Model model, @Valid OntologyKPIDTO ontologyKPIDTO,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {

		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		if (!ontologyKPIDTO.isNewOntology()) {
			// Find Existing ontology and validate if has a KPI information.
			final Ontology ontology = ontologyConfigService.getOntologyByIdentification(ontologyKPIDTO.getId(),
					utils.getUserId());
			List<OntologyKPI> kpis = ontologyKPIRepository.findByOntology(ontology);
			// if exist ontology with kpi
			if (kpis.size() > 0) {
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, "the ontology has a kpi associated"));
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
			}

			return createKPIinDB(ontologyKPIDTO, response, ontology, utils.getUserId());
		} else {

			try {

				final Ontology ontology = createOntologyKPI(ontologyKPIDTO, request);

				return createKPIinDB(ontologyKPIDTO, response, ontology, utils.getUserId());

			} catch (final OntologyBusinessServiceException e) {
				final Error error = e.getError();
				switch (error) {
				case ILLEGAL_ARGUMENT:
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
					return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
				case NO_VALID_SCHEMA:
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, "Invalid json schema: " + e.getMessage());
					return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
				case KAFKA_TOPIC_CREATION_ERROR:
				case CONFIG_CREATION_ERROR:
				case CONFIG_CREATION_ERROR_UNCLEAN:
				case PERSISTENCE_CREATION_ERROR:
				case PERSISTENCE_CREATION_ERROR_UNCLEAN:
					log.error("Cannot create ontology because of: " + e.getMessage());
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, e.getMessage());
					return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				default:
					log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
					response.put(STATUS_STR, ERROR_STR);
					response.put(CAUSE_STR, e.getMessage());
					return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (final Exception e) {
				log.error(GEN_INTERN_ERROR_CREATE_ONT + e.getMessage());
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, e.getMessage());
				return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

	}

	private Ontology createOntologyKPI(OntologyKPIDTO ontologyKPIDTO, HttpServletRequest request)
			throws IOException, OntologyBusinessServiceException {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(ontologyBusinessService
				.completeSchema(ontologyKPIDTO.getSchema(), ontologyKPIDTO.getName(), ontologyKPIDTO.getDescription())
				.toString());
		ontology.setIdentification(ontologyKPIDTO.getName());
		ontology.setActive(ontologyKPIDTO.isActive());
		ontology.setPublic(ontologyKPIDTO.isPublic());
		ontology.setDataModel(dataModelService.getDataModelByName(ontologyConfigService.DATAMODEL_DEFAULT_NAME));
		ontology.setDescription(ontologyKPIDTO.getDescription());
		ontology.setUser(userService.getUser(utils.getUserId()));
		ontology.setMetainf(ontologyKPIDTO.getMetainf());
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(ontologyKPIDTO.getDatasource()));

		final OntologyConfiguration config = new OntologyConfiguration(request);
		ontologyBusinessService.createOntology(ontology, utils.getUserId(), config);
		return ontology;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontology != null) {

				final List<OntologyUserAccess> authorizations = ontologyConfigService
						.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();

				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
				}

				final List<User> users = userService.getAllActiveUsers();

				model.addAttribute("authorizations", authorizationsDTO);
				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute("users", users);

				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					final OntologyRest ontologyRest = ontologyConfigService.getOntologyRestByOntologyId(ontology);
					populateRestForm(model, ontologyRest);
					populateFormApiRest(model);
					return "ontologies/createapirest";
				}
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ontologyVirtual = ontologyConfigService
							.getOntologyVirtualByOntologyId(ontology);
					populateFormVirtual(model);
					model.addAttribute("datasource", ontologyVirtual.getDatasourceId());
					model.addAttribute("objId", ontologyVirtual.getObjectId());
					return "ontologies/createvirtual";
				} else {
					model.addAttribute(ONTOLOGY_REST_STR, new OntologyRestDTO());
					populateForm(model);
				}

				return "ontologies/createwizard";

			} else
				return ONTOLOGIES_CREATE;
		} catch (final RuntimeException e) {
			return ONTOLOGIES_CREATE;
		}
	}

	private void populateRestForm(Model model, OntologyRest ontologyRest) {

		final List<OntologyRestOperationDTO> lOperationsDTO = new ArrayList<OntologyRestOperationDTO>();

		final OntologyRestSecurity security = ontologyConfigService.getOntologyRestSecurityByOntologyRest(ontologyRest);
		final OntologyRestHeaders headers = ontologyConfigService.getOntologyRestHeadersByOntologyRest(ontologyRest);
		final List<OntologyRestOperation> lOperations = ontologyConfigService.getOperationsByOntologyRest(ontologyRest);
		for (final OntologyRestOperation operation : lOperations) {
			final List<OntologyRestOperationParam> lOperationsParams = ontologyConfigService
					.getOperationsParamsByOperation(operation);

			final List<OntologyRestOperationParamDTO> params = new ArrayList<OntologyRestOperationParamDTO>();

			for (final OntologyRestOperationParam operationParam : lOperationsParams) {
				params.add(new OntologyRestOperationParamDTO(operationParam.getIndexParam(), operationParam.getName(),
						operationParam.getType().name()));
			}

			lOperationsDTO.add(new OntologyRestOperationDTO(operation.getName(), operation.getType().name(),
					operation.getOrigin(), operation.getDescription(), params));

		}

		final OntologyRestDTO ontologyRestDTO = new OntologyRestDTO(ontologyRest.getBaseUrl(),
				ontologyRest.getSecurityType().name(), security.getConfig(), ontologyRest.isInferOps(),
				headers.getConfig(), lOperationsDTO, ontologyRest.getJsonSchema());

		model.addAttribute(ONTOLOGY_REST_STR, ontologyRestDTO);

	}

	@PutMapping(value = "/update/{id}")
	public ResponseEntity<?> updateOntology(Model model, @PathVariable("id") String id, @Valid Ontology ontology,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			// utils.addRedirectMessage(ONT_VAL_ERROR, redirect);
			// return "redirect:/ontologies/update/" + id;
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONT_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			final String value = queryToolService.querySQLAsJson(utils.getUserId(), ontology.getIdentification(),
					"select count(*) as value from " + ontology.getIdentification(), 0);
			final int count = mapper.readValue(value, JsonNode.class).path(0).path("value").asInt();
			final OntologyConfiguration config = new OntologyConfiguration(request);

			// Get KPI ID by looking for the ontology in DDBB because HTML doesn't retrieve
			// it
			final Ontology ontologyFound = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontologyFound != null && ontologyFound.getOntologyKPI() != null
					&& ontologyFound.getOntologyKPI().getId() != null) {
				ontology.getOntologyKPI().setId(ontologyFound.getOntologyKPI().getId());
			}

			if (ontology != null && ontology.getOntologyKPI() != null && ontology.getOntologyKPI().getId() == null) {
				ontology.setOntologyKPI(null);
			}
			ontologyConfigService.updateOntology(ontology, utils.getUserId(), config, count > 0 ? true : false);

		} catch (final OntologyServiceException | OntologyDataJsonProblemException e) {
			log.error("Cannot update ontology {}", e.getMessage());
			// utils.addRedirectMessage("ontology.update.error", redirect);
			// return "redirect:/ontologies/create";
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final IOException e) {
			log.error("Could not check if ontology has documents in RTDB");
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		response.put(STATUS_STR, "ok");
		response.put(REDIRECT_STR, "/controlpanel/ontologies/show/" + id);
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.ACCEPTED);
		// return "redirect:/ontologies/show/" + id;

	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
		if (ontology != null) {
			try {
				if (ontology.getOntologyKPI() != null) {
					ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
				}
				entityDeletionService.deleteOntology(id, utils.getUserId());

			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("ontology.delete.error", e.getMessage(), redirect);
				log.error("Error deleting ontology. ", e);
				return "redirect:/ontologies/update/" + id;
			}
			return REDIRECT_ONTOLOGIES_LIST;
		} else {
			return REDIRECT_ONTOLOGIES_LIST;
		}
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());
			if (ontology != null) {

				final List<OntologyUserAccess> authorizations = ontologyConfigService
						.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
				final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();

				for (final OntologyUserAccess authorization : authorizations) {
					if (authorization.getUser().isActive()) {
						authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
					}
				}

				final List<User> users = userService.getAllActiveUsers();

				model.addAttribute(ONTOLOGY_STR, ontology);
				model.addAttribute("authorizations", authorizationsDTO);
				model.addAttribute("users", users);

				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					final OntologyRest ontologyRest = ontologyConfigService.getOntologyRestByOntologyId(ontology);

					if (ontologyRest != null) {
						populateRestForm(model, ontologyRest);
						return "ontologies/showapirest";
					} else {
						utils.addRedirectMessage("ontology.notfound.error", redirect);
						return REDIRECT_ONTOLOGIES_LIST;
					}
				} else {
					model.addAttribute("isOntologyRest", false);
				}

				return "ontologies/show";

			} else {
				utils.addRedirectMessage("ontology.notfound.error", redirect);
				return REDIRECT_ONTOLOGIES_LIST;
			}
		} catch (final OntologyServiceException e) {
			return REDIRECT_ONTOLOGIES_LIST;
		}
	}

	@GetMapping("/getFromId/{identification}")
	public @ResponseBody Ontology getFromIdentification(@PathVariable("identification") String identification) {
		try {
			final Ontology ontology = ontologyConfigService.getOntologyByIdentification(identification,
					utils.getUserId());
			if (ontology != null) {
				return ontology;
			} else {
				return null;
			}
		} catch (final OntologyServiceException e) {
			return null;
		}
	}

	private void populateForm(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getAllDataModels());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute(ONTOLOGIES_STR, ontologyConfigService.getOntologiesByUserId(utils.getUserId()));
		model.addAttribute("modes", Ontology.RtdbToHdbStorage.values());
	}

	private void populateKPIForm(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getAllDataModels());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		model.addAttribute(ONTOLOGIES_STR, ontologyConfigService.getOntologiesByUserId(utils.getUserId()));

	}

	private void populateFormApiRest(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
	}

	private void populateFormVirtual(Model model) {
		model.addAttribute(DATA_MODELS_STR, ontologyConfigService.getEmptyBaseDataModel());
		model.addAttribute(DATA_MODEL_TYPES_STR, ontologyConfigService.getAllDataModelTypes());
		model.addAttribute(RTDBS, ontologyConfigService.getDatasources());
		if (ontologyConfigService.getDatasourcesRelationals().size() == 0) {
			model.addAttribute("datasources", new ArrayList<OntologyVirtualDatasource>());
			model.addAttribute("collectionNames", new ArrayList<String>());
		} else {
			model.addAttribute("datasources", ontologyConfigService.getDatasourcesRelationals());
			final List<String> collections = ontologyConfigService
					.getTablesFromDatasource(ontologyConfigService.getDatasourcesRelationals().get(0));
			Collections.sort(collections);
			model.addAttribute("collectionNames", collections);
		}
		model.addAttribute("datasource", new OntologyVirtualDatasource());

	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<OntologyUserAccessDTO> createAuthorization(@RequestParam String accesstype,
			@RequestParam String ontology, @RequestParam String user) {

		try {
			ontologyConfigService.createUserAccess(ontology, user, accesstype, utils.getUserId());
			final OntologyUserAccess ontologyUserAccessCreated = ontologyConfigService
					.getOntologyUserAccessByOntologyIdAndUserId(ontology, user, utils.getUserId());
			final OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<OntologyUserAccessDTO>(ontologyUserAccessDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<OntologyUserAccessDTO>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {
			ontologyConfigService.deleteOntologyUserAccess(id, utils.getUserId());
			return new ResponseEntity<String>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<OntologyUserAccessDTO> updateAuthorization(@RequestParam String id,
			@RequestParam String accesstype) {

		try {
			ontologyConfigService.updateOntologyUserAccess(id, accesstype, utils.getUserId());
			final OntologyUserAccess ontologyUserAccessCreated = ontologyConfigService.getOntologyUserAccessById(id,
					utils.getUserId());
			final OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<OntologyUserAccessDTO>(ontologyUserAccessDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<OntologyUserAccessDTO>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<List<OntologyUserAccessDTO>> getAuthorizations(@RequestParam("id") String id) {

		try {
			final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

			final List<OntologyUserAccess> authorizations = ontologyConfigService
					.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
			final List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();
			for (final OntologyUserAccess authorization : authorizations) {
				if (authorization.getUser().isActive()) {
					authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
				}
			}
			return new ResponseEntity<List<OntologyUserAccessDTO>>(authorizationsDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<List<OntologyUserAccessDTO>>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getTables/{datasource}")
	public @ResponseBody List<String> getTables(@PathVariable("datasource") String datasource) {

		return ontologyConfigService.getTablesFromDatasource(datasource);
	}

	@GetMapping(value = "/getInstance/{datasource}/{collection}")
	public @ResponseBody String getInstance(@PathVariable("datasource") String datasource,
			@PathVariable("collection") String collection) {

		return ontologyConfigService.getInstance(datasource, collection);
	}

	@GetMapping(value = "/schema/{identification}", produces = "application/json")
	public ResponseEntity<?> getSchema(@PathVariable("identification") String identification) {

		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(identification, utils.getUserId());
		if (ontology != null) {
			try {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				final JsonNode schema = mapper.readTree(ontology.getJsonSchema());
				return new ResponseEntity<>(mapper.writeValueAsString(schema), HttpStatus.OK);
			} catch (final IOException e) {
				return new ResponseEntity<>("Ontology schema is not valid Json", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else
			return new ResponseEntity<>("No existing ontology with id " + identification, HttpStatus.NOT_FOUND);

	}

	@GetMapping("/{id}/properties/type/{type}")
	public ResponseEntity<Map<String, String>> getPropertiesWithPath(@PathVariable("id") String identification,
			@PathVariable("type") String type) {

		final Map<String, String> properties = ontologyDataService.getOntologyPropertiesWithPath4Type(identification,
				type);
		return new ResponseEntity<>(properties, HttpStatus.OK);

	}

	@PostMapping("queryKPI")
	public String runQuery(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;

		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		try {
			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (manageDB.getListOfTables4Ontology(ontologyIdentification).size() == 0) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}");
				}
				if (queryType.toUpperCase().equals(ontologyConfigService.QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query, 0);
					model.addAttribute(QUERY_RESULT, queryResult);
					return QUERY_TOOL_SHOW_QUERY;

				} else if (queryType.toUpperCase().equals(ontologyConfigService.QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					model.addAttribute(QUERY_RESULT, queryResult);
					return QUERY_TOOL_SHOW_QUERY;
				} else {
					return utils.getMessage("querytool.querytype.notselected", "Please select queryType Native or SQL");
				}
			} else
				return utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology");

		} catch (final DBPersistenceException e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("queryKPIOne")
	public @ResponseBody String runQueryOne(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;
		query = query + " limit 1";
		final Ontology ontology = ontologyConfigService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		try {
			if (ontologyConfigService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (manageDB.getListOfTables4Ontology(ontologyIdentification).size() == 0) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}");
				}
				if (queryType.toUpperCase().equals(ontologyConfigService.QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query, 0);

					return queryResult;

				} else if (queryType.toUpperCase().equals(ontologyConfigService.QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					return queryResult;
				} else {
					return utils.getMessage("querytool.querytype.notselected", "Please select queryType Native or SQL");
				}
			} else
				return utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology");

		} catch (final DBPersistenceException e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error(ERROR_IN_RUNQUERY, e);
			model.addAttribute(QUERY_RESULT, utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	private ResponseEntity<?> createKPIinDB(OntologyKPIDTO ontologyKPIDTO, Map<String, String> response,
			Ontology ontology, String userID) {

		OntologyKPI oKPI = new OntologyKPI();
		oKPI.setCron(ontologyKPIDTO.getCron());
		oKPI.setDateFrom(ontologyKPIDTO.getDateFrom());
		oKPI.setDateTo(ontologyKPIDTO.getDateTo());
		oKPI.setActive(Boolean.FALSE);
		oKPI.setOntology(ontology);
		oKPI.setQuery(ontologyKPIDTO.getQuery());
		oKPI.setUser(userService.getUser(userID));
		oKPI.setPostProcess(ontologyKPIDTO.getPostProcess());
		ontologyKPIRepository.save(oKPI);
		ontologyKPIService.scheduleKpi(oKPI);
		response.put(REDIRECT_STR, "/controlpanel/ontologies/list");
		response.put(STATUS_STR, "ok");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		final Ontology ontology = ontologyConfigService.getOntologyById(id, utils.getUserId());

		if (ontology.getOntologyKPI() != null) {
			if (ontology.getOntologyKPI().isActive()) {
				ontologyKPIService.unscheduleKpi(ontology.getOntologyKPI());
			} else {
				ontologyKPIService.scheduleKpi(ontology.getOntologyKPI());
			}
		}
		final List<Ontology> ontologies = ontologyConfigService.getOntologiesByUserAndAccess(utils.getUserId(), null,
				null);
		model.addAttribute(ONTOLOGIES_STR, ontologies);
		return "ontologies/list";
	}

}
