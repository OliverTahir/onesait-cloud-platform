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
package com.minsait.onesait.platform.controlpanel.controller.crud;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.crud.dto.OntologyDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/crud")
@Slf4j
public class CrudController {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private RouterService routerService;

	private static final String ERROR_TRUE = "{\"error\":\"true\"}";

	@GetMapping(value = "/admin/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		final Ontology ontology = ontologyService.getOntologyById(id, utils.getUserId());
		final OntologyDTO ontologyDTO = new OntologyDTO();
		ontologyDTO.setIdentification(ontology.getIdentification());
		ontologyDTO.setJsonSchema(ontology.getJsonSchema());
		ontologyDTO.setDatasource(ontology.getRtdbDatasource().name());
		model.addAttribute("ontology", ontologyDTO);
		return "crud/admin";
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

		final String OUTPUT = "";
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

	@PostMapping(value = { "/query" }, produces = "application/json")
	public @ResponseBody String query(String ontologyID, String query) {

		try {
			final String result = processQuery(query, ontologyID, ApiOperation.Type.GET.name(), "", "");

			return result;
		} catch (final Exception e) {

			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/findById" }, produces = "application/json")
	public @ResponseBody String findById(String ontologyID, String oid) {

		try {
			final String result = processQuery("select * from " + ontologyID + " where  _id = OID(\"" + oid + "\")",
					ontologyID, ApiOperation.Type.GET.name(), "", oid);
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyID, utils.getUserId());
			return findDatesAndReplace(result, ontology.getJsonSchema());
		} catch (final Exception e) {

			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/deleteById" }, produces = "application/json")
	public @ResponseBody String deleteById(String ontologyID, String oid) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.DELETE.name(), "", oid);
			return result;
		} catch (final Exception e) {

			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/insert" }, produces = "text/plain")
	public @ResponseBody String insert(String ontologyID, String body) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.POST.name(), body, "");
			return result;
		} catch (final Exception e) {

			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/update" }, produces = "text/plain")
	public @ResponseBody String update(String ontologyID, String body, String oid) {

		try {
			final String result = processQuery("", ontologyID, ApiOperation.Type.PUT.name(), body, oid);
			return result;
		} catch (final Exception e) {

			return "{\"exception\":\"true\"}";
		}
	}

	private String findDatesAndReplace(String text, String ontology) {
		// find $date on schema
		final String pat = "\\x24date";
		final StringBuffer stringBuffer = new StringBuffer();
		final Pattern pattern = Pattern.compile(pat);
		final Matcher matcher = pattern.matcher(ontology);
		if (matcher.find()) {
			// if $date then find date and replace
			final String patDate = "(\"\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d+\\p{Punct}*\\d*Z\")";
			final Pattern patternDate = Pattern.compile(patDate);
			final Matcher matcherDate = patternDate.matcher(text);
			while (matcherDate.find()) {
				matcherDate.appendReplacement(stringBuffer, "{\"\\$date\":" + matcherDate.group(1) + "}");
			}
			matcherDate.appendTail(stringBuffer);
			return stringBuffer.toString();
		}
		return text;
	}

}
