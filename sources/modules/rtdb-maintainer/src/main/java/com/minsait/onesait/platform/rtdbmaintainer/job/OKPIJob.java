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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OKPIJob {

	@Autowired
	private RouterService routerService;

	public void execute(JobExecutionContext context) throws IOException {

		final String user = context.getJobDetail().getJobDataMap().getString("userId");

		final String query = context.getJobDetail().getJobDataMap().getString("query");

		final String ontology = context.getJobDetail().getJobDataMap().getString("ontology");

		try {
			executeQuery(user, query, ontology, context);
			log.debug("Job KPI ontology for ontology", ontology);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error generating the ontology instance for user:" + user + " and query:" + query, e);

		}

	}

	public void executeQuery(String user, String query, String ontology, JobExecutionContext context) throws Exception {

		// send query
		log.debug("Send query for ontology: " + ontology + " query:" + query + " for user:" + query);
		final String result = processQuery(query, getOntologyFromQuery(query), ApiOperation.Type.GET.name(), "", "",
				user);
		
		final String postProcessScript = context.getJobDetail().getJobDataMap().getString("postProcess");

		if (result != null && !result.equals("error")) {
			// insert data
			log.debug("Insert result query for ontology: " + ontology + "  query:" + query + " for user:" + query);
			String output = result;
			if (postProcessScript != null && !"".equals(postProcessScript)){
				ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
				try {
					String scriptPostprocessFunction = "function postprocess(data){ " + postProcessScript + " }";
					engine.eval(scriptPostprocessFunction);		
					Invocable inv = (Invocable) engine;
					output = (String) inv.invokeFunction("postprocess", result);
				} catch(ScriptException e) {
					log.error("ERROR from Scripting Post Process, Exception detected", e.getCause().getMessage());
				}
			}			
			processQuery("", ontology, ApiOperation.Type.POST.name(), output, "", user);
		}
	}

	public String processQuery(String query, String ontologyID, String method, String body, String objectId,
			String user) throws Exception {

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
				.builder(ontologyID, OperationType.valueOf(operationType.name()), user,
						OperationModel.Source.INTERNAL_ROUTER)
				.body(body).queryType(QueryType.SQLLIKE).objectId(objectId).deviceTemplate("").build();
		final NotificationModel modelNotification = new NotificationModel();

		modelNotification.setOperationModel(model);

		final OperationResultModel result = routerService.query(modelNotification);

		if (result != null) {
			if ("ERROR".equals(result.getResult())) {
				String ret = "error";
				return ret;
			}
			return result.getResult();
		} else {
			return null;
		}

	}

	private static String getOntologyFromQuery(String query) {
		query = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ");
		query = query.trim().replaceAll(" +", " ");
		String[] list = query.split("from ");
		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				if (!list[i].startsWith("(")) {
					int indexOf = list[i].toLowerCase().indexOf(" ", 0);
					int indexOfCloseBracket = list[i].toLowerCase().indexOf(")", 0);
					indexOf = (indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf) ? indexOfCloseBracket
							: indexOf;
					if (indexOf == -1) {
						indexOf = list[i].length();
					}
					return list[i].substring(0, indexOf).trim();
				}
			}
		}
		return "";
	}

}
