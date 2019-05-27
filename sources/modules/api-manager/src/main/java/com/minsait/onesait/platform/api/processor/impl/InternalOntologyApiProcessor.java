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
package com.minsait.onesait.platform.api.processor.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.processor.ApiProcessor;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InternalOntologyApiProcessor implements ApiProcessor {

	@Autowired
	private RouterService routerService;

	@Value("${onesaitplatform.apimanager.cacheable:false}")
	private static boolean CACHEABLE;

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data, Exchange exchange) throws Exception {
		data = processQuery(data, exchange);
		data = postProcess(data, exchange);
		return data;
	}

	@ApiManagerAuditable
	private Map<String, Object> processQuery(Map<String, Object> data, Exchange exchange) throws Exception {
		log.info("processQuery,id:" + exchange.getExchangeId());
		String OUTPUT = "";
		try {
			final Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);
			final User user = (User) data.get(ApiServiceInterface.USER);
			final String METHOD = (String) data.get(ApiServiceInterface.METHOD);
			final String BODY = (String) data.get(ApiServiceInterface.BODY);
			final String QUERY_TYPE = (String) data.get(ApiServiceInterface.QUERY_TYPE);
			final String QUERY = (String) data.get(ApiServiceInterface.QUERY);
			final String OBJECT_ID = (String) data.get(ApiServiceInterface.OBJECT_ID);
			// String CACHEABLE = (String) data.get(ApiServiceInterface.CACHEABLE);
			String body = BODY;
			OperationType operationType = null;

			if (METHOD.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
				body = QUERY;
				operationType = OperationType.QUERY;
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
				operationType = OperationType.INSERT;
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
				operationType = OperationType.UPDATE;
			} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
				operationType = OperationType.DELETE;
			} else {
				operationType = OperationType.QUERY;
			}

			final OperationModel model = OperationModel
					.builder(ontology.getIdentification(), OperationType.valueOf(operationType.name()),
							user.getUserId(), OperationModel.Source.APIMANAGER)
					.body(body).queryType(QueryType.valueOf(QUERY_TYPE)).objectId(OBJECT_ID).deviceTemplate("")
					.cacheable(CACHEABLE).build();

			final NotificationModel modelNotification = new NotificationModel();
			modelNotification.setOperationModel(model);
			final OperationResultModel result = routerService.query(modelNotification);

			if (result != null) {
				if ("ERROR".equals(result.getResult())) {
					exchange.getIn().setHeader("content-type", "text/plain");
					exchange.getIn().setHeader(ApiServiceInterface.STATUS, "STOP");
					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR Output from Router Processing", "Stopped Execution, Error from Router",
							result.getMessage());
					exchange.getIn().setHeader(ApiServiceInterface.REASON, messageError);
				} else {
					OUTPUT = result.getResult();
					data.put(ApiServiceInterface.OUTPUT, OUTPUT);
				}
			} else {
				exchange.getIn().setHeader(ApiServiceInterface.STATUS, "STOP");
				final String messageError = ApiProcessorUtils.generateErrorMessage(
						"ERROR Output from Router Processing", "Stopped Execution", "Null Result From Router");
				exchange.getIn().setHeader(ApiServiceInterface.REASON, messageError);
			}
			return data;
		} catch (final Exception e) {
			log.error("Error in processQuery,id:" + exchange.getExchangeId(), e);
			throw e;
		}
	}

	private Map<String, Object> postProcess(Map<String, Object> data, Exchange exchange) throws Exception {
		log.info("postProcess,id:" + exchange.getExchangeId());
		try {
			final ApiOperation apiOperation = ((ApiOperation) data.get(ApiServiceInterface.API_OPERATION));
			if (apiOperation != null) {
				final String postProcessScript = apiOperation.getPostProcess();
				if (postProcessScript != null && !"".equals(postProcessScript)) {
					final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
					try {
						final String scriptPostprocessFunction = "function postprocess(data){ " + postProcessScript
								+ " }";

						final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
								scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));

						engine.eval(new InputStreamReader(scriptInputStream));
						final Invocable inv = (Invocable) engine;
						Object result;
						result = inv.invokeFunction("postprocess", data.get(ApiServiceInterface.OUTPUT));
						data.put(ApiServiceInterface.OUTPUT, result);
					} catch (final ScriptException e) {
						log.error("Execution logic for postprocess error", e);
						exchange.getIn().setHeader(ApiServiceInterface.STATUS, "STOP");
						final String messageError = ApiProcessorUtils.generateErrorMessage(
								"ERROR from Scripting Post Process", "Execution logic for Postprocess error",
								e.getCause().getMessage());
						exchange.getIn().setHeader(ApiServiceInterface.REASON, messageError);

					} catch (final Exception e) {
						exchange.getIn().setHeader(ApiServiceInterface.STATUS, "STOP");
						final String messageError = ApiProcessorUtils.generateErrorMessage(
								"ERROR from Scripting Post Process", "Exception detected", e.getCause().getMessage());
						exchange.getIn().setHeader(ApiServiceInterface.REASON, messageError);
					}
				}
			}
			return data;
		} catch (final Exception e) {
			log.error("Error in postProcess,id:" + exchange.getExchangeId(), e);
			throw e;
		}
	}

	@Override
	public List<ApiType> getApiProcessorTypes() {
		return Arrays.asList(ApiType.IOT, ApiType.INTERNAL_ONTOLOGY);
	}

}
