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
package com.minsait.onesait.platform.api.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.opendevl.JFlat;
import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.processor.ApiProcessorDelegate;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.rule.DefaultRuleBase.ReasonType;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.config.model.Api;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiServiceImpl extends ApiManagerService implements ApiServiceInterface, Processor {

	@Autowired
	private RuleManager ruleManager;

	@Autowired
	private ApiProcessorDelegate processorDelegate;

	private static final String APP_JSON = "application/json";

	private static final String TEXT_PLAIN = "text/plain";

	private static final String APP_ATOM_XML = "application/atom+xml";

	private static final String VALUE = "value";

	private static final String TEXT_CSV = "text/csv";

	@Override
	@ApiManagerAuditable
	public void process(Exchange exchange) throws Exception {
		log.info("process,id:" + exchange.getExchangeId());
		try {
			Map<String, Object> data = internalProcess(exchange);
			// Catch status stop and stop method execution, before it was in camel routes
			// xml
			if (!"STOP".equals(exchange.getIn().getHeader("STATUS"))) {
				final Api api = (Api) data.get(ApiServiceInterface.API);
				data = processorDelegate.proxyProcessor(api).process(data, exchange);
				data = processOutput(data, exchange);
			}

		} catch (final Exception e) {
			log.error("Error in process,id:" + exchange.getExchangeId() + " error:", e);
			throw e;
		}

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> internalProcess(Exchange exchange) throws Exception {
		log.info("internalProcess,id:" + exchange.getExchangeId());
		Map<String, Object> data = null;
		try {
			final HttpServletRequest request = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST,
					HttpServletRequest.class);
			final HttpServletResponse response = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_RESPONSE,
					HttpServletResponse.class);

			final Facts facts = new Facts();
			facts.put(RuleManager.REQUEST, request);
			facts.put(RuleManager.RESPONSE, response);

			final Map<String, Object> dataFact = new HashMap<String, Object>();
			dataFact.put(ApiServiceInterface.BODY, exchange.getIn().getBody());

			facts.put(RuleManager.FACTS, dataFact);
			ruleManager.fire(facts);

			data = (Map<String, Object>) facts.get(RuleManager.FACTS);
			final Boolean stopped = (Boolean) facts.get(RuleManager.STOP_STATE);
			String REASON = "";
			String REASON_TYPE;

			if (stopped != null && stopped == true) {
				REASON = ((String) facts.get(RuleManager.REASON));
				REASON_TYPE = ((String) facts.get(RuleManager.REASON_TYPE));

				if (REASON_TYPE.equals(ReasonType.API_LIMIT.name())) {
					exchange.getIn().setHeader(ApiServiceInterface.HTTP_RESPONSE_CODE_HEADER,
							ApiServiceInterface.HTTP_RESPONSE_CODE_API_LIMIT);
				} else if (REASON_TYPE.equals(ReasonType.SECURITY.name())) {
					exchange.getIn().setHeader(ApiServiceInterface.HTTP_RESPONSE_CODE_HEADER,
							ApiServiceInterface.HTTP_RESPONSE_CODE_FORBIDDEN);
				} else {
					exchange.getIn().setHeader(ApiServiceInterface.HTTP_RESPONSE_CODE_HEADER,
							ApiServiceInterface.HTTP_RESPONSE_CODE_INTERNAL_SERVER_ERROR);
				}

				final String messageError = ApiProcessorUtils.generateErrorMessage(REASON_TYPE, "Error in Processing",
						REASON);
				exchange.getIn().setHeader(ApiServiceInterface.CONTENT_TYPE, APP_JSON);

				exchange.getIn().setHeader(ApiServiceInterface.STATUS, "STOP");
				exchange.getIn().setHeader(ApiServiceInterface.REASON, messageError);

				exchange.getIn().setHeader(ApiServiceInterface.REMOTE_ADDRESS,
						data.get(ApiServiceInterface.REMOTE_ADDRESS));
				exchange.getIn().setHeader(ApiServiceInterface.METHOD, data.get(ApiServiceInterface.METHOD));
				exchange.getIn().setHeader(ApiServiceInterface.QUERY, data.get(ApiServiceInterface.QUERY));
				exchange.getIn().setHeader(ApiServiceInterface.USER, data.get(ApiServiceInterface.USER));
				exchange.getIn().setHeader(ApiServiceInterface.ONTOLOGY, data.get(ApiServiceInterface.ONTOLOGY));
				// exchange.getIn().setHeader(ApiServiceInterface.BODY, (String)
				// dataFact.get(ApiServiceInterface.BODY));

				// Add output to body for camel processing without exceptions
				data.put("OUTPUT", messageError);
				exchange.getIn().setBody(data);

			} else {
				exchange.getIn().setHeader(ApiServiceInterface.STATUS, "FOLLOW");
				exchange.getIn().setBody(data);
			}
			return data;

		} catch (final Exception e) {
			log.error("Error in internalProcess,id:" + exchange.getExchangeId() + " error:", e);
			throw e;
		}

	}

	private Map<String, Object> processOutput(Map<String, Object> data, Exchange exchange) throws Exception {
		log.info("processOutput,id:" + exchange.getExchangeId());
		String output = null;
		JSONObject jsonObj = null;
		JSONArray jsonArray = null;
		String objectId = null;
		Boolean queryById = null;
		try {

			final String FORMAT_RESULT = (String) data.get(ApiServiceInterface.FORMAT_RESULT);
			output = (String) data.get(ApiServiceInterface.OUTPUT);
			objectId = (String) data.get(ApiServiceInterface.OBJECT_ID);
			queryById = (Boolean) data.get(ApiServiceInterface.QUERY_BY_ID);

			String CONTENT_TYPE = TEXT_PLAIN;

			if (output == null || output.equalsIgnoreCase("") && queryById != null) {
				output = "{\"RESULT\":\"We can´t find a Resource for ID:" + objectId + "\"}";
				exchange.getIn().setHeader(ApiServiceInterface.HTTP_RESPONSE_CODE_HEADER,
						ApiServiceInterface.HTTP_RESPONSE_CODE_NOT_FOUND);
			}
			if (FORMAT_RESULT.equals("")) {
				CONTENT_TYPE = (String) data.get(ApiServiceInterface.CONTENT_TYPE_OUTPUT);
			}

			if (FORMAT_RESULT.equalsIgnoreCase("JSON") || CONTENT_TYPE.equalsIgnoreCase(APP_JSON)) {
				data.put(ApiServiceInterface.CONTENT_TYPE, APP_JSON);
				CONTENT_TYPE = APP_JSON;
			} else if (FORMAT_RESULT.equalsIgnoreCase("XML") || CONTENT_TYPE.equalsIgnoreCase(APP_ATOM_XML)
					|| CONTENT_TYPE.equalsIgnoreCase("application/xml")) {
				data.put(ApiServiceInterface.CONTENT_TYPE, APP_ATOM_XML);
				jsonObj = toJSONObject(output);
				if (jsonObj == null)
					jsonArray = toJSONArray(output);
				if (jsonObj != null)
					output = XML.toString(jsonObj);
				if (jsonArray != null)
					output = XML.toString(jsonArray);
				CONTENT_TYPE = APP_ATOM_XML;
			} else if (FORMAT_RESULT.equalsIgnoreCase("CSV") || CONTENT_TYPE.equalsIgnoreCase(TEXT_CSV)) {
				data.put(ApiServiceInterface.CONTENT_TYPE, TEXT_CSV);
				jsonObj = toJSONObject(output);
				if (jsonObj == null)
					jsonArray = toJSONArray(output);
				if (jsonObj != null) {
					// output = CDL.toString(new JSONArray("[" + jsonObj + "]"));
					final List<Object[]> json2csv = new JFlat(output).json2Sheet().headerSeparator(".")
							.getJsonAsSheet();
					output = deserializeCSV2D(json2csv);
				}
				if (jsonArray != null) {
					// output = CDL.toString(jsonArray);
					final List<Object[]> json2csv = new JFlat(output).json2Sheet().headerSeparator(".")
							.getJsonAsSheet();
					output = deserializeCSV2D(json2csv);
				}
				CONTENT_TYPE = TEXT_CSV;
			}
			if (output != null && objectId != null && !objectId.equalsIgnoreCase("")) {
				try {
					jsonObj = toJSONObject(output);
					if (jsonObj == null)
						jsonArray = toJSONArray(output);
					if (jsonObj == null && jsonArray != null && jsonArray.length() == 1 && queryById != null)
						jsonObj = jsonArray.getJSONObject(0);
					if (jsonObj != null && jsonObj.get(VALUE) != null)
						output = jsonObj.get(VALUE).toString();
					else if (jsonArray != null && jsonArray.length() > 0) {
						final List<JSONObject> newArray = new ArrayList();
						JSONObject newNode = null;
						for (int i = 0; i < jsonArray.length(); i++) {
							newNode = (JSONObject) jsonArray.get(i);
							newArray.add((JSONObject) newNode.get(VALUE));
						}
						output = newArray.toString();
					}
				} catch (final JSONException e) {
					log.warn("Not value in result...");
				}
			}
			data.put(ApiServiceInterface.OUTPUT, output);
			exchange.getIn().setHeader(ApiServiceInterface.CONTENT_TYPE, CONTENT_TYPE);
			return data;
		} catch (final Exception e) {
			log.error("Error in processOutput,id:" + exchange.getExchangeId(), e);
			throw e;
		}
	}

	private JSONObject toJSONObject(String input) {
		JSONObject jsonObj = null;

		try {
			jsonObj = new JSONObject(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	private JSONArray toJSONArray(String input) {
		JSONArray jsonObj = null;
		try {
			jsonObj = new JSONArray(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("doGet");
		final Facts facts = new Facts();
		try {
			facts.put(RuleManager.REQUEST, request);
			facts.put(RuleManager.ACTION, "GET");
			final Map<String, Object> dataFact = new HashMap<String, Object>();
			facts.put(RuleManager.FACTS, dataFact);
			ruleManager.fire(facts);

			@SuppressWarnings("unchecked")
			final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);
			final Boolean stopped = (Boolean) facts.get(RuleManager.STOP_STATE);
			String REASON = "";

			if (stopped != null && stopped == true) {
				REASON = ((String) facts.get(RuleManager.REASON));
			}
			log.debug(hashPP(data));
			sendResponse(response, HttpServletResponse.SC_OK, hashPP(data) + "\n" + REASON, null, null);
		} catch (final Exception e) {
			log.error("Error in doGet", e);
			throw e;
		}

	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request, response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request, response);
	}

	private void sendResponse(HttpServletResponse response, int status, String message, String formatResult,
			String query) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");
		try {
			response.setContentType(TEXT_PLAIN);
			// message=message.replace("\n", " ");
			response.getWriter().write(message);
		} catch (final IOException e) {
			throw new IOException(e);
		}
		return;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
		final JSONObject jsonData = new JSONObject();
		for (final String key : map.keySet()) {
			Object value = map.get(key);
			if (value instanceof Map<?, ?>) {
				value = getJsonFromMap((Map<String, Object>) value);
			}
			jsonData.put(key, value);
		}
		return jsonData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String hashPP(final Map<String, Object> m, String... offset) {
		String retval = "";
		final String delta = offset.length == 0 ? "" : offset[0];
		for (final Map.Entry<String, Object> e : m.entrySet()) {
			retval += delta + "[" + e.getKey() + "] -> ";
			final Object value = e.getValue();
			if (value instanceof Map) {
				final Map<String, Object> value2 = (Map<String, Object>) value;
				retval += "(Hash)\n" + hashPP(value2, delta + "  ");
			} else if (value instanceof List) {
				retval += "{";
				for (final Object element : (List) value) {
					retval += element + ", ";
				}
				retval += "}\n";
			} else {
				retval += "[" + value.toString() + "]\n";
			}
		}
		return retval + "\n";
	}

	private static String deserializeCSV2D(List<Object[]> matrix) {
		final StringBuilder builder = new StringBuilder();
		final int size = matrix.get(0).length;
		matrix.forEach(a -> {
			final List<Object> columns = Arrays.asList(a);
			for (int i = 0; i < size; i++) {
				builder.append(columns.get(i));
				if (i + 1 != size)
					builder.append(",");
			}
			builder.append(System.getProperty("line.separator"));
		});
		return builder.toString();
	}

}
