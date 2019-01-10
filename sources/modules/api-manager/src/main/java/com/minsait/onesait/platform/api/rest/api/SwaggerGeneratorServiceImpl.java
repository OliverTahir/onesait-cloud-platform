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
package com.minsait.onesait.platform.api.rest.api;

import java.net.InetAddress;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minsait.onesait.platform.api.rest.api.dto.ApiDTO;
import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.rest.swagger.RestSwaggerReader;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;

@Component("swaggerGeneratorServiceImpl")
public class SwaggerGeneratorServiceImpl implements SwaggerGeneratorService {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;

	private static final String BASE_PATH = "/api-manager/server/api";

	@Value("${server.port:19090}")
	private String port;

	@Override
	public Response getApi(String identificacion, String token) throws Exception {

		final ApiDTO apiDto = apiFIQL.toApiDTO(apiService.findApi(identificacion, token));

		final int version = apiDto.getVersion();
		final String vVersion = "v" + version;

		final BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] { "http" });
		config.setBasePath("/api" + "/" + vVersion + "/" + identificacion);

		final RestSwaggerReader reader = new RestSwaggerReader();

		final Swagger swagger = reader.read(apiDto, config);

		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		final String json = mapper.writeValueAsString(swagger);

		return Response.ok(json).build();
	}

	public ApiServiceRest getApiService() {
		return apiService;
	}

	public void setApiService(ApiServiceRest apiService) {
		this.apiService = apiService;
	}

	public ApiFIQL getApiFIQL() {
		return apiFIQL;
	}

	public void setApiFIQL(ApiFIQL apiFIQL) {
		this.apiFIQL = apiFIQL;
	}

	@Override
	public Response getApiWithoutToken(String identificacion) throws Exception {
		final Api api = apiService.getApiMaxVersion(identificacion);
		if (api == null)
			return Response.noContent().status(404).build();

		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final SwaggerParser swaggerParser = new SwaggerParser();
			final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
			swagger.setHost(null);
			addCustomHeaderToPaths(swagger);
			swagger.setBasePath(BASE_PATH + "/v" + api.getNumversion() + "/" + identificacion);
			return Response.ok(mapper.writeValueAsString(swagger)).build();
		}
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);

		final int version = apiDto.getVersion();
		final String vVersion = "v" + version;
		final String hostname = InetAddress.getLocalHost().getHostName();

		final BeanConfig config = new BeanConfig();
		config.setHost(hostname + ":" + port);
		config.setSchemes(new String[] { "http" });
		config.setBasePath("/server/api" + "/" + vVersion + "/" + identificacion);

		final RestSwaggerReader reader = new RestSwaggerReader();

		final Swagger swagger = reader.read(apiDto, config);

		final String json = mapper.writeValueAsString(swagger);

		return Response.ok(json).build();
	}

	private void addCustomHeaderToPaths(Swagger swagger) {
		final Parameter header = new HeaderParameter();
		header.setIn("header");
		header.setDescription("onesait Platform API Key");
		header.setName(ApiServiceInterface.AUTHENTICATION_HEADER);
		header.setRequired(true);
		swagger.getPaths().entrySet().forEach(p -> {
			final Path path = p.getValue();
			path.getOperations().forEach(o -> {
				o.addParameter(header);
			});
		});
	}

}
