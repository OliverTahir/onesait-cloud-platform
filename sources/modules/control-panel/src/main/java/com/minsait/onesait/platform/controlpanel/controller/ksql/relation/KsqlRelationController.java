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
package com.minsait.onesait.platform.controlpanel.controller.ksql.relation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.services.exceptions.KsqlRelationServiceException;
import com.minsait.onesait.platform.config.services.ksql.flow.KsqlFlowService;
import com.minsait.onesait.platform.config.services.ksql.relation.KsqlRelationService;
import com.minsait.onesait.platform.config.services.ksql.resource.KsqlResourceService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ksql/relation")
@Slf4j
public class KsqlRelationController {

	@Autowired
	private KsqlRelationService ksqlRelationService;
	@Autowired
	private KsqlFlowService ksqlFlowService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private KsqlRelationUtil ksqlRelationUtil;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private KsqlResourceService ksqlResourceService;

	private static final String MSG_STR = "{\"msg\":\"";

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DEVELOPER')")
	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.ksqlRelationService.getAllIdentifications();
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DEVELOPER')")
	@PostMapping(value = {
			"/create" }, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> create(@RequestBody KsqlResourceDTO ksqlResourceDTO) {

		// Check user permissions over FlowId
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(ksqlResourceDTO.getFlowId());
		if (ksqlFlow == null) {
			return new ResponseEntity<String>("{\"msg\":\"ksql.relation.creation.error.flow.not.found\"}",
					HttpStatus.NOT_FOUND);
		}
		if (!userService.getUser(utils.getUserId()).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				&& !ksqlFlow.getUser().getUserId().equals(utils.getUserId())) {
			// No permissions for this operation
			return new ResponseEntity<String>("{\"msg\":\"ksql.relation.creation.error.no.permissions\"}",
					HttpStatus.UNAUTHORIZED);
		}
		// Create Resource from DTO
		KsqlResource ksqlResource = null;
		try {
			ksqlResource = ksqlRelationUtil.convertFromDTO(ksqlResourceDTO, utils.getUserId());
		} catch (Exception e) {
			log.error("convertFromDTO Error:" + e.getMessage());
			return new ResponseEntity<String>("{\"msg\":\"ksql.relation.creation.error.dup.relation\"}",
					HttpStatus.BAD_REQUEST);
		}
		// Persist in CDB
		try {
			ksqlRelationService.createKsqlRelation(ksqlFlow, ksqlResource);
		} catch (KsqlRelationServiceException e) {
			// Duplicated Identification for resource
			String error = utils.getMessage("ksql.relation.creation.error.dup.relation",
					"Duplicated Resource in this KSQL Flow.");
			return new ResponseEntity<String>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			// Adapt exception to HttpStatus -> SKQL Syntax error (most likely)
			String error = utils.getMessage("ksql.relation.creation.error.server",
					"KSQL Syntax error. Please check KSQL Statemet.");
			return new ResponseEntity<String>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		}
		// Prepare response
		String success = utils.getMessage("ksql.relation.creation.ok", "KSQL Resource successfully created.");
		return new ResponseEntity<String>(MSG_STR + success + "\"}", HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public @ResponseBody ResponseEntity<String> delete(Model model, @PathVariable("id") String id,
			RedirectAttributes redirect) {
		KsqlRelation relation = this.ksqlRelationService.getKsqlRelationWithId(id);
		if (relation == null) {
			return new ResponseEntity<String>("{\"msg\":\"ksql.relation.delete.error.not.found\"}",
					HttpStatus.NOT_FOUND);
		}

		if (relation.getKsqlFlow().getUser().getUserId().equals(utils.getUserId())) {
			// Avoid Administrator deleting other users KsqlFlows
			this.ksqlRelationService.deleteKsqlRelation(relation);
		} else {
			log.debug("Admin cannot delete other users");
			String error = utils.getMessage("ksql.relation.deletion.error.no.permissions",
					"UNAUTHORIZED. Not enough permissions for this operation.");
			return new ResponseEntity<String>(MSG_STR + error + "\"}", HttpStatus.UNAUTHORIZED);
		}
		String success = utils.getMessage("ksql.relation.deletion.ok", "KSQL Resource sucecssfully deleted.");
		return new ResponseEntity<String>(MSG_STR + success + "\"}", HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String getRelations(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "flowId") String flowId) {
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(flowId);
		model.addAttribute("ksqlFlow", ksqlFlow);
		// Relations Info
		List<KsqlRelation> ksqlRelations = ksqlRelationService
				.getKsqlRelationsWithFlowId(userService.getUser(utils.getUserId()), flowId);
		model.addAttribute("ksqlRelations", ksqlRelations);
		// Available Ontologies Info
		List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		model.addAttribute("ontologies", ontologies);

		return "redirect:/ksql/flow/update/" + flowId + "#ksqlRelations";
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}")
	public @ResponseBody ResponseEntity<String> updateFlow(Model model, @PathVariable("id") String id,
			@RequestBody KsqlResourceDTO ksqlResourceDTO, BindingResult bindingResult, RedirectAttributes redirect) {

		// Check user permissions over FlowId
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(ksqlResourceDTO.getFlowId());
		if (ksqlFlow == null) {
			String error = utils.getMessage("ksql.relation.creation.error.flow.not.found", "KSQL Flow not found.");
			return new ResponseEntity<String>(MSG_STR + error + "\"}", HttpStatus.NOT_FOUND);
		}
		if (!userService.getUser(utils.getUserId()).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				&& !ksqlFlow.getUser().getUserId().equals(utils.getUserId())) {
			// No permissions for this operation
			String error = utils.getMessage("ksql.relation.creation.error.no.permissions",
					"Unauthorized. You have no permissions for this operation.");
			return new ResponseEntity<String>(MSG_STR + error + "\"}", HttpStatus.UNAUTHORIZED);
		}

		KsqlResource ksqlResource = ksqlRelationUtil.getOriginalFromDTO(ksqlResourceDTO, utils.getUserId());

		ksqlResourceService.updateKsqlResource(ksqlResource);
		String success = utils.getMessage("ksql.relation.update.ok", "KSQL Resource successfully updated.");
		return new ResponseEntity<String>(MSG_STR + success + "\"}", HttpStatus.OK);
	}
}
