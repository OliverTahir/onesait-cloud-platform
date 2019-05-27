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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.ManagementRestServices;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologySimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyUpdate;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyUserAccessSimplified;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Ontology Management", tags = { "Ontology management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@Slf4j
public class OntologyManagementController extends ManagementRestServices {
	
	private static final String USER_IS_NOT_AUTH = "The user is not authorized";
	private static final String ONTOLOGY_STR = "Ontology \"";
	private static final String NOT_EXIST = "\" does not exist";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserService userService;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;

	@ApiOperation(value = "Get ontology by identification")
	@GetMapping("/ontologies/{identification}")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	public ResponseEntity<?> get(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			} else {
				final OntologySimplified ontologySimplified = new OntologySimplified(ontology);
				return new ResponseEntity<>(ontologySimplified, HttpStatus.OK);
			}

		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@ApiOperation(value = "Delete ontology by identification")
	@DeleteMapping("/ontologies/{identification}")
	public ResponseEntity<?> delete(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}
			final User user = userService.getUser(utils.getUserId());
			if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
				return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}
			entityDeletionService.deleteOntology(ontology.getId(), utils.getUserId());
		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Ontology deleted successfully", HttpStatus.OK);
	}

	@ApiOperation(value = "Get all ontologies")
	@GetMapping("/ontologies")
	@ApiResponses(@ApiResponse(response = OntologySimplified[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getAll() {

		final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		if (ontologies == null) {
			return new ResponseEntity<>("Ontologies not found", HttpStatus.BAD_REQUEST);
		} else {
			final Set<OntologySimplified> ontologiesList = new TreeSet<>();
			ontologies.forEach(o -> ontologiesList.add(new OntologySimplified(o)));
			return new ResponseEntity<>(ontologiesList, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Create new ontology")
	@PostMapping("/ontologies")
	public ResponseEntity<?> create(
			@ApiParam(value = "OntologyCreate", required = true) @Valid @RequestBody OntologyCreate ontologyCreate,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		final Ontology ontology = new Ontology();
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		ontology.setUser(user);
		ontology.setIdentification(ontologyCreate.getIdentification());
		ontology.setDescription(ontologyCreate.getDescription());
		ontology.setMetainf(ontologyCreate.getMetainf());
		ontology.setActive(ontologyCreate.isActive());
		ontology.setPublic(ontologyCreate.isPublic());
		ontology.setAllowsCypherFields(ontologyCreate.isAllowsCypherFields());
		ontology.setJsonSchema(ontologyCreate.getJsonSchema());
		ontology.setRtdbClean(ontologyCreate.isRtdbClean());
		ontology.setRtdbCleanLapse(ontologyCreate.getRtdbCleanLapse());
		if (ontologyCreate.getRtdbDatasource() == null) {
			ontology.setRtdbDatasource(RtdbDatasource.MONGO);
		} else {
			ontology.setRtdbDatasource(ontologyCreate.getRtdbDatasource());
		}
		ontology.setRtdbToHdb(ontologyCreate.isRtdbToHdb());
		if (ontologyCreate.getRtdbToHdbStorage() == null) {
			ontology.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
		} else {
			ontology.setRtdbToHdbStorage(ontologyCreate.getRtdbToHdbStorage());
		}
		ontology.setAllowsCreateTopic(ontologyCreate.isAllowsCreateTopic());

		final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
		try {
			ontologyBusinessService.createOntology(ontology, utils.getUserId(), ontologyConfig);
		} catch (OntologyDataJsonProblemException jsonException) {
			return new ResponseEntity<>(jsonException.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (OntologyBusinessServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("Ontology created successfully", HttpStatus.OK);
	}

	@ApiOperation(value = "Update an existing ontology")
	@PutMapping("/ontologies")
	public ResponseEntity<?> update(
			@ApiParam(value = "OntologyUpdate", required = true) @Valid @RequestBody OntologyUpdate ontologyUpdate,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(USER_IS_NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyUpdate.getIdentification(),
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyUpdate.getIdentification() + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}

			final String value = queryToolService.querySQLAsJson(utils.getUserId(), ontology.getIdentification(),
					"select count(*) as value from " + ontology.getIdentification(), 0);
			final ObjectMapper mapper = new ObjectMapper();
			final int count = mapper.readValue(value, JsonNode.class).path(0).path("value").asInt();

			if (ontologyUpdate.getDescription() != null) {
				ontology.setDescription(ontologyUpdate.getDescription());
			}
			if (ontologyUpdate.getMetainf() != null) {
				ontology.setMetainf(ontologyUpdate.getMetainf());
			}
			if (ontologyUpdate.getJsonSchema() != null) {
				ontology.setJsonSchema(ontologyUpdate.getJsonSchema());
			}
			if (ontologyUpdate.getActive() != null) {
				ontology.setActive(ontologyUpdate.getActive());
			}
			if (ontologyUpdate.getAllowsCypherFields() != null) {
				ontology.setAllowsCypherFields(ontologyUpdate.getAllowsCypherFields());
			}
			if (ontologyUpdate.getAllowsCreateTopic() != null) {
				ontology.setAllowsCreateTopic(ontologyUpdate.getAllowsCreateTopic());
			}
			if (ontologyUpdate.getRtdbClean() != null) {
				ontology.setRtdbClean(ontologyUpdate.getRtdbClean());
				if (ontology.isRtdbClean()) {
					ontologyUpdate.setRtdbCleanLapse(ontologyUpdate.getRtdbCleanLapse());
				}
			}
			if (ontologyUpdate.getRtdbToHdb() != null) {
				ontology.setRtdbToHdb(ontologyUpdate.getRtdbToHdb());
			}
			final OntologyConfiguration ontologyConfig = new OntologyConfiguration();
			try {
				ontologyService.updateOntology(ontology, utils.getUserId(), ontologyConfig, count > 0 ? true : false);
			} catch (OntologyDataJsonProblemException | OntologyServiceException exception) {
				return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		} catch (final IOException e) {
			return new ResponseEntity<>("Could not check if ontology has documents in RTDB",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("Ontology updated successfully", HttpStatus.OK);
	}

	@ApiOperation(value = "Get users access authorizations for an ontology by ontology identification")
	@GetMapping("/ontologies/{identification}/authorizations")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	public ResponseEntity<?> getOntologyAuthorizations(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification) {

		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}
			if (!ontologyService.hasOntologyUsersAuthorized(ontology.getId())) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + "\" does not have authorizations",
						HttpStatus.BAD_REQUEST);
			}

			final List<OntologyUserAccess> ontologyUserAccessList = ontologyService
					.getOntologyUserAccesses(ontology.getId(), utils.getUserId());
			final Set<OntologyUserAccessSimplified> ontologyUserAccessSimplifiedList = new TreeSet<>();
			ontologyUserAccessList
					.forEach(o -> ontologyUserAccessSimplifiedList.add(new OntologyUserAccessSimplified(o)));

			return new ResponseEntity<>(ontologyUserAccessSimplifiedList, HttpStatus.OK);

		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	@ApiOperation(value = "Set user access authorizations for an ontology by ontology identification")
	@PostMapping("/ontologies/{identification}/authorizations")
	public ResponseEntity<?> setOntologyAuthorizations(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification,
			@Valid @RequestBody OntologyUserAccessSimplified ontologyUserAccessSimplified, Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		try {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(ontologyUserAccessSimplified.getUserId());
			if (user == null) {
				return new ResponseEntity<>("User \"" + ontologyUserAccessSimplified.getUserId() + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}
			try {
				final OntologyUserAccess ontologyUserAccess = ontologyService
						.getOntologyUserAccessByOntologyIdAndUserId(ontology.getId(),
								ontologyUserAccessSimplified.getUserId(), utils.getUserId());
				ontologyService.updateOntologyUserAccess(ontologyUserAccess.getId(),
						ontologyUserAccessSimplified.getTypeName(), utils.getUserId());
				return new ResponseEntity<>("Authorization updated successfully", HttpStatus.OK);
			} catch (IllegalStateException e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			} catch (OntologyServiceException e) {
				ontologyService.createUserAccess(ontology.getId(), user.getUserId(),
						ontologyUserAccessSimplified.getTypeName(), utils.getUserId());
				return new ResponseEntity<>("Authorization created successfully", HttpStatus.OK);
			}

		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Delete user access authorization for an ontology by ontology identification and user id")
	@DeleteMapping("/ontologies/{identification}/authorizations/{userId}")
	public ResponseEntity<?> deleteOntologyAuthorizations(
			@ApiParam(value = "Ontology identification", required = true) @PathVariable("identification") String ontologyIdentification,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {
		try {

			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
					utils.getUserId());
			if (ontology == null) {
				return new ResponseEntity<>(ONTOLOGY_STR + ontologyIdentification + NOT_EXIST,
						HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>("User \"" + userId + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			final OntologyUserAccess ontologyUserAccess = ontologyService
					.getOntologyUserAccessByOntologyIdAndUserId(ontology.getId(), userId, utils.getUserId());
			ontologyService.deleteOntologyUserAccess(ontologyUserAccess.getId(), utils.getUserId());

			return new ResponseEntity<>("Authorization deleted successfully", HttpStatus.OK);

		} catch (OntologyServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
}
