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
package com.minsait.onesait.platform.controlpanel.controller.querytool;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/querytool")
@Slf4j
public class QueryToolController {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyDataService ontologyDataService;

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private AppWebUtils utils;

	public static final String QUERY_SQL = "SQL";
	public static final String QUERY_NATIVE = "NATIVE";
	private static final String QUERY_RESULT_STR = "queryResult";
	private static final String QUERY_TOOL_SHOW_QUERY = "querytool/show :: query";

	@GetMapping("show")
	public String show(Model model) {
		List<Ontology> ontologies = null;
		ontologies = ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(), null, null);
		model.addAttribute("ontologies", ontologies);

		return "querytool/show";

	}

	@PostMapping("query")
	public String runQuery(Model model, @RequestParam String queryType, @RequestParam String query,
			@RequestParam String ontologyIdentification) throws JsonProcessingException {
		String queryResult = null;

		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		try {
			if (ontologyService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (manageDB.getListOfTables4Ontology(ontologyIdentification).size() == 0) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}");
				}
				if (queryType.toUpperCase().equals(QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query, 0);
					model.addAttribute(QUERY_RESULT_STR, queryResult);
					return QUERY_TOOL_SHOW_QUERY;

				} else if (queryType.toUpperCase().equals(QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					model.addAttribute(QUERY_RESULT_STR, queryResult);
					return QUERY_TOOL_SHOW_QUERY;
				} else {
					return utils.getMessage("querytool.querytype.notselected", "Please select queryType Native or SQL");
				}
			} else
				return utils.getMessage("querytool.ontology.access.denied.json",
						"You don't have permissions for this ontology");

		} catch (final DBPersistenceException e) {
			log.error("Error in runQuery", e);
			model.addAttribute(QUERY_RESULT_STR, e.getMessage());
			return QUERY_TOOL_SHOW_QUERY;
		} catch (final Exception e) {
			log.error("Error in runQuery", e);
			model.addAttribute(QUERY_RESULT_STR,
					utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return QUERY_TOOL_SHOW_QUERY;
		}

	}

	@PostMapping("ontologyfields")
	public String getOntologyFields(Model model, @RequestParam String ontologyIdentification)
			throws JsonProcessingException, IOException {

		model.addAttribute("fields",
				ontologyService.getOntologyFieldsQueryTool(ontologyIdentification, utils.getUserId()));
		return "querytool/show :: fields";

	}

	@PostMapping("relations")
	public String getOntologyRelations(Model model, @RequestParam String ontologyIdentification) throws IOException {
		model.addAttribute("relations", ontologyDataService.getOntologyReferences(ontologyIdentification));
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());
		if (ontology != null)
			model.addAttribute("datasource", ontology.getRtdbDatasource().name());
		return "querytool/show :: relations";
	}

	@GetMapping("/rtdb/{ontology}")
	public @ResponseBody String getRtdb(Model model, @PathVariable("ontology") String ontologyIdentification) {
		return ontologyService.getRtdbFromOntology(ontologyIdentification);
	}

}
