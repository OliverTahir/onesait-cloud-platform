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
package com.minsait.onesait.platform.api.service.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.service.exception.BadRequestException;
import com.minsait.onesait.platform.api.service.exception.ForbiddenException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiManagerService {

	private static final String WRONG_PARAMETER_TYPE = "com.indra.sofia2.api.service.wrongparametertype";

	@Autowired
	private ApiRepository apiRepository;

	@Autowired
	private ApiOperationRepository apiOperationRepository;

	public ApiRepository getApiRepository() {
		return apiRepository;
	}

	public void setApiRepository(ApiRepository apiRepository) {
		this.apiRepository = apiRepository;
	}

	public Api getApi(String pathInfo, User user) {
		final ApiType apitipo = null;
		final String apiVersion = getApiVersion(pathInfo);
		final String apiIdentifier = getApiIdentifier(pathInfo);

		final Api api = getApi(apiIdentifier, Integer.parseInt(apiVersion), apitipo);
		return api;
	}

	public String getApiVersion(String pathInfo) throws BadRequestException {
		String version = "1";
		final Pattern pattern = Pattern.compile("(.*)/api/v(.*)/");
		final Matcher matcher = pattern.matcher(pathInfo);
		if (matcher.find()) {
			final String param = matcher.group(2);
			version = param.substring(0, param.indexOf('/'));
			return version;
		} else {
			version = pathInfo;

			if (version.startsWith("/")) {
				version = version.substring(1);
			}

			final int slashIndex = version.indexOf('/');

			if (slashIndex == -1) {
				throw new BadRequestException("com.indra.sofia2.api.service.notvalidformat");
			}

			version = version.substring(0, slashIndex);
			if (version.startsWith("v")) {
				version = version.substring(1);
			}

			if (version == null || version.equals("")) {
				throw new BadRequestException("com.indra.sofia2.api.service.notapiversion");
			}

			return version;
		}
	}

	public String getApiIdentifier(String pathInfo) throws BadRequestException {

		final String apiVersion = getApiVersion(pathInfo);

		String apiIdentifier = pathInfo.substring(pathInfo.indexOf(apiVersion + "/") + (apiVersion + "/").length());

		int slashIndex = apiIdentifier.indexOf('/');
		if (slashIndex == -1) {
			slashIndex = apiIdentifier.length();
		}

		apiIdentifier = apiIdentifier.substring(0, slashIndex);
		if (apiIdentifier == null || apiIdentifier.equals("")) {
			throw new BadRequestException("com.indra.sofia2.api.service.notapiid");
		}

		return apiIdentifier;
	}

	public Api getApi(String apiIdentifier, int apiVersion, ApiType tipoapi)
			throws BadRequestException, ForbiddenException {

		List<Api> api = null;

		if (tipoapi != null) {
			api = apiRepository.findByIdentificationAndNumversionAndApiType(apiIdentifier, apiVersion, tipoapi);
		} else {
			api = apiRepository.findByIdentificationAndNumversion(apiIdentifier, apiVersion);
		}
		return api.get(0);
	}

	public boolean isPathQuery(String pathInfo) {

		final String apiIdentifier = getApiIdentifier(pathInfo);
		final String objectId = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + (apiIdentifier).length());

		if (objectId.length() == 0 || !objectId.startsWith("/")) {
			return false;
		} else {
			return true;
		}
	}

	public ApiOperation getCustomSQL(String pathInfo, Api api, String operation) {

		final String apiIdentifier = getApiIdentifier(pathInfo);

		String opIdentifier = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + (apiIdentifier).length());
		if (opIdentifier.startsWith("\\") || opIdentifier.startsWith("/")) {
			opIdentifier = opIdentifier.substring(1);
			// opIdentifier= opIdentifier.replace("/", "");
		}

		opIdentifier = opIdentifier.replace("/", "");

		final List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);

		/*
		 * String match = apiIdentifier + "_" + operation;
		 *
		 * if (!opIdentifier.equals("")) { match += "_" + opIdentifier; }
		 */

		for (final ApiOperation operacion : operaciones) {
			if (operacion.getIdentification().equals(opIdentifier)
					|| opIdentifier.contains(operacion.getIdentification())) {
				return operacion;
			}
		}
		return null;
	}

	public ApiOperation getCustomSQLDefault(String pathInfo, Api api, String operation) {

		final String apiIdentifier = getApiIdentifier(pathInfo);

		final String opIdentifier = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + (apiIdentifier).length());

		final List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);

		final String match = apiIdentifier + "_" + operation;

		/*
		 * if (!opIdentifier.equals("")) { match += "_" + opIdentifier; }
		 */

		for (final ApiOperation operacion : operaciones) {
			if (operacion.getIdentification().equals(match)) {
				return operacion;
			}
		}
		return null;
	}

	public HashMap<String, String> getCustomParametersValues(HttpServletRequest request, String body,
			HashSet<ApiQueryParameter> queryParametersCustomQuery, ApiOperation customSQL) {

		final HashMap<String, String> customqueryparametersvalues = new HashMap<>();
		for (final ApiQueryParameter customqueryparameter : queryParametersCustomQuery) {
			String paramvalue = request.getParameter(customqueryparameter.getName());
			if (paramvalue == null) {
				if (customqueryparameter.getHeaderType().name()
						.equalsIgnoreCase(ApiQueryParameter.HeaderType.BODY.name())) {
					paramvalue = body;
				} else if (customqueryparameter.getHeaderType().name()
						.equalsIgnoreCase(ApiQueryParameter.HeaderType.PATH.name())) {
					final String apiIdentifier = getApiIdentifier(request.getRequestURI());
					final String relativePath = request.getRequestURI()
							.substring(request.getRequestURI().indexOf(apiIdentifier) + apiIdentifier.length());
					final String[] splittedParams = customSQL.getPath().split("/");

					for (int i = 0; i < splittedParams.length; i++) {
						if (splittedParams[i].contains(customqueryparameter.getName()) && paramvalue == null) {
							paramvalue = relativePath.split("/")[i + 1];
						}

					}
				}
			}

			if (paramvalue != null) {
				if (customqueryparameter.getDataType().name()
						.equalsIgnoreCase(ApiQueryParameter.DataType.DATE.name())) {
					try {
						final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						df.parse(paramvalue);
						paramvalue = "\"" + paramvalue + "\"";
					} catch (final Exception e) {
						final Object parametros[] = { "$" + customqueryparameter.getName(), "Date" };
						throw new BadRequestException(
								"com.indra.sofia2.api.service.wrongparametertype " + parametros[0]);
					}
				} else if (customqueryparameter.getDataType().name()
						.equalsIgnoreCase(ApiQueryParameter.DataType.STRING.name())) {
					try {
						// paramvalue.toString();
						paramvalue = "\"" + paramvalue + "\"";
					} catch (final Exception e) {
						final Object parametros[] = { "$" + customqueryparameter.getName(), "String" };
						throw new BadRequestException(WRONG_PARAMETER_TYPE + parametros[0]);
					}
				} else if (customqueryparameter.getDataType().name()
						.equalsIgnoreCase(ApiQueryParameter.DataType.NUMBER.name())) {
					try {
						Double.parseDouble(paramvalue);
					} catch (final Exception e) {
						final Object parametros[] = { "$" + customqueryparameter.getName(), "Integer" };
						throw new BadRequestException(WRONG_PARAMETER_TYPE + parametros[0]);
					}
				} else if (customqueryparameter.getDataType().name().equalsIgnoreCase("boolean")) {
					if (!paramvalue.equalsIgnoreCase("true") && !paramvalue.equalsIgnoreCase("false")) {
						final Object parametros[] = { "$" + customqueryparameter.getName(), "Boolean" };
						throw new BadRequestException(WRONG_PARAMETER_TYPE + parametros[0]);
					}
				}
				customqueryparametersvalues.put(customqueryparameter.getName(), paramvalue);
			}
		}
		return customqueryparametersvalues;
	}

	public String buildQuery(String queryDb, HashMap<String, String> queryParametersValues) {
		for (final String param : queryParametersValues.keySet()) {
			queryDb = queryDb.replace("{$" + param + "}", queryParametersValues.get(param));
		}
		return queryDb;
	}

	public String getObjectidFromPathQuery(String pathInfo) {
		final String apiIdentifier = getApiIdentifier(pathInfo);

		String objectId = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + (apiIdentifier).length());

		if (!objectId.startsWith("/")) {
			return null;
		}
		objectId = objectId.substring(1);

		int slashIndex = objectId.indexOf('/');
		final int parentIndex = objectId.indexOf('(');
		if (slashIndex == -1) {
			slashIndex = objectId.length();
		}
		// En el caso de OData se informa /ontologia(oid)/
		if (parentIndex != -1 && parentIndex < slashIndex) {
			slashIndex = parentIndex;
		}
		return objectId.substring(0, slashIndex);

	}

	public boolean isSQLLIKE(String query, String queryType) {
		if (query != null && query.length() > 0 && queryType != null && queryType.length() > 0) {
			return queryType.startsWith("SQL");
		}
		return false;
	}

	public String readPayload(HttpServletRequest request) {
		final StringBuilder buffer = new StringBuilder();
		BufferedReader reader;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (final IOException e) {
			log.error("Error reading payload", e);
		}
		return buffer.toString();
	}

}
