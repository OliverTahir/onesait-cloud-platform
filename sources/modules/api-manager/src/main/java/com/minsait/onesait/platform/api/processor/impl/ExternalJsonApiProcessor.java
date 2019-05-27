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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.processor.ApiProcessor;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExternalJsonApiProcessor implements ApiProcessor {

	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private com.minsait.onesait.platform.config.services.apimanager.ApiManagerService apiManagerServiceConfig;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data, Exchange exchange) throws Exception {
		data = proxyHttp(data, exchange);
		data = postProcess(data, exchange);
		return data;
	}

	@Override
	public List<ApiType> getApiProcessorTypes() {
		return Collections.singletonList(ApiType.EXTERNAL_FROM_JSON);
	}

	private Map<String, Object> proxyHttp(Map<String, Object> data, Exchange exchange) {
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		final String pathInfo = (String) data.get(ApiServiceInterface.PATH_INFO);
		final String body = (String) data.get(ApiServiceInterface.BODY);
		final Api api = (Api) data.get(ApiServiceInterface.API);
		final HttpServletRequest request = (HttpServletRequest) data.get(ApiServiceInterface.REQUEST);
		@SuppressWarnings("unchecked")
		final Map<String, String[]> queryParams = (Map<String, String[]>) data.get(ApiServiceInterface.QUERY_PARAMS);
		final Swagger swagger = ApiProcessorUtils.getSwaggerFromJson(api.getSwaggerJson());
		// TO-DO check whether http/https
		String url = getUrl(swagger, pathInfo);
		url = addExtraQueryParameters(url, swagger, pathInfo, queryParams);
		// TO-DO headers?
		String result = "";
		final HttpHeaders headers = new HttpHeaders();
		addHeaders(headers, request, swagger);
		final HttpEntity<String> entity = new HttpEntity<String>(body, headers);

		switch (method) {
		case "GET":
			result = restTemplate.getForEntity(url, String.class).getBody();
			break;
		case "POST":
			result = restTemplate.postForEntity(url, entity, String.class).getBody();
			break;
		case "PUT":
			result = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class).getBody();
			break;
		case "DELETE":
			result = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class).getBody();
			break;
		default:
			break;

		}

		data.put(ApiServiceInterface.OUTPUT, result);
		return data;
	}

	private Map<String, Object> postProcess(Map<String, Object> data, Exchange exchange) {
		final Api api = (Api) data.get(ApiServiceInterface.API);
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		if (apiManagerServiceConfig.postProcess(api) && method.equalsIgnoreCase("get")) {
			final String postProcess = apiManagerServiceConfig.getPostProccess(api);
			if (!StringUtils.isEmpty(postProcess)) {
				final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
				try {
					final String scriptPostprocessFunction = "function postprocess(data){ " + postProcess + " }";

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
	}

	private String getUrl(Swagger swagger, String pathInfo) {
		String scheme = ApiServiceInterface.HTTPS.toLowerCase();
		if (!swagger.getSchemes().stream().map(s -> s.name()).collect(Collectors.toList())
				.contains(ApiServiceInterface.HTTPS))
			scheme = ApiServiceInterface.HTTP.toLowerCase();
		final String url = scheme + "://" + swagger.getHost() + swagger.getBasePath();
		final String apiIdentifier = apiManagerService.getApiIdentifier(pathInfo);
		final String swaggerPath = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length(),
				pathInfo.length() - 1);
		return url.concat(swaggerPath);
	}

	private String addExtraQueryParameters(String url, Swagger swagger, String pathInfo,
			Map<String, String[]> queryParams) {
		final StringBuilder sb = new StringBuilder(url);
		if (queryParams.size() > 0) {
			sb.append("?");
			queryParams.entrySet().forEach(e -> {
				final String param = e.getKey() + "=" + String.join("", e.getValue());
				sb.append(param).append("&&");
			});
		}

		return sb.toString();
	}

	private HttpHeaders addHeaders(HttpHeaders headers, HttpServletRequest request, Swagger swagger) {
		swagger.getPaths().entrySet().forEach(e -> {
			final Path path = e.getValue();
			path.getOperationMap().entrySet().forEach(op -> {
				final Operation operation = op.getValue();
				operation.getParameters().stream().filter(p -> p instanceof HeaderParameter).forEach(p -> {
					final String header = request.getHeader(p.getName());
					if (!StringUtils.isEmpty(header))
						headers.add(p.getName(), header);
				});
			});
		});
		final String contentType = request.getContentType();
		if (contentType == null)
			headers.setContentType(MediaType.APPLICATION_JSON);
		else
			headers.setContentType(MediaType.valueOf(contentType));
		return headers;
	}

}
