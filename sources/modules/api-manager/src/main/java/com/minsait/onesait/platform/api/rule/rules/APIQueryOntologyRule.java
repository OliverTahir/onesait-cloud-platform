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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;

@Component
@Rule
public class APIQueryOntologyRule extends DefaultRuleBase {

	@Autowired
	private ApiManagerService apiManagerService;

	private static final String SQL_LIKE_STR = "SQLLIKE";

	@Priority
	public int getPriority() {
		return 4;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = (HttpServletRequest) facts.get(RuleManager.REQUEST);
		if ((request != null) && canExecuteRule(facts))
			return true;
		else
			return false;
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		@SuppressWarnings("unchecked")
		String queryDb = "";
		String targetDb = "";
		final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);
		final HttpServletRequest request = (HttpServletRequest) facts.get(RuleManager.REQUEST);

		final Api api = (Api) data.get(ApiServiceInterface.API);
		final String pathInfo = (String) data.get(ApiServiceInterface.PATH_INFO);
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		final String body = (String) data.get(ApiServiceInterface.BODY);
		String queryType = (String) data.get(ApiServiceInterface.QUERY_TYPE);

		final Ontology ontology = api.getOntology();
		if (ontology != null) {
			data.put(ApiServiceInterface.IS_EXTERNAL_API, false);

			final ApiOperation customSQL = apiManagerService.getCustomSQL(pathInfo, api, method);

			final String objectId = apiManagerService.getObjectidFromPathQuery(pathInfo);
			if (customSQL == null && !objectId.equals("") && (queryType.equals("") || queryType.equals("NONE"))) {
				final RtdbDatasource dataSource = ontology.getRtdbDatasource();
				// TODO: Move this from here
				if (dataSource.equals(RtdbDatasource.MONGO))
					queryDb = "select *, _id from " + ontology.getIdentification() + " as c where  _id = OID(\""
							+ objectId + "\")";
				else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH))
					queryDb = "select * from " + ontology.getIdentification() + " where _id = IDS_QUERY("
							+ ontology.getIdentification() + "," + objectId + ")";

				data.put(ApiServiceInterface.QUERY_TYPE, SQL_LIKE_STR);
				queryType = SQL_LIKE_STR;
				data.put(ApiServiceInterface.QUERY, queryDb);
				data.put(ApiServiceInterface.QUERY_BY_ID, Boolean.TRUE);
				// }

			} else if (customSQL == null && objectId.equals("") && (queryType.equals("") || queryType.equals("NONE"))) {
				final RtdbDatasource dataSource = ontology.getRtdbDatasource();
				// TODO: Move this from here
				if (dataSource.equals(RtdbDatasource.MONGO))
					queryDb = "select " + ontology.getIdentification() + " as " + ontology.getIdentification()
							+ ", contextData, _id from " + ontology.getIdentification() + " as c ";
				else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH))
					queryDb = "select * from " + ontology.getIdentification();

				data.put(ApiServiceInterface.QUERY_TYPE, SQL_LIKE_STR);
				queryType = SQL_LIKE_STR;
				data.put(ApiServiceInterface.QUERY, queryDb);
				data.put(ApiServiceInterface.QUERY_BY_ID, Boolean.TRUE);
			}

			final HashSet<ApiQueryParameter> queryParametersCustomQuery = new HashSet<ApiQueryParameter>();
			HashMap<String, String> queryParametersValues = new HashMap<String, String>();
			if (customSQL != null) {

				data.put(ApiServiceInterface.API_OPERATION, customSQL);

				for (final ApiQueryParameter queryparameter : customSQL.getApiqueryparameters()) {
					final String name = queryparameter.getName();
					final String value = queryparameter.getValue();

					if (matchParameter(name, ApiServiceInterface.QUERY))
						queryDb = value;
					else if (matchParameter(name, ApiServiceInterface.QUERY_TYPE))
						queryType = value;
					else if (matchParameter(name, ApiServiceInterface.TARGET_DB_PARAM))
						targetDb = value;
					// else if (matchParameter(name,ApiServiceInterface.FORMAT_RESULT))
					// formatResult=value;
					else
						queryParametersCustomQuery.add(queryparameter);

				}

				queryParametersValues = apiManagerService.getCustomParametersValues(request, body,
						queryParametersCustomQuery, customSQL);

				if (body.equals("")) {
					queryDb = apiManagerService.buildQuery(queryDb, queryParametersValues);
				} else
					queryDb = body;
			}

			data.put(ApiServiceInterface.QUERY_TYPE, queryType);
			data.put(ApiServiceInterface.QUERY, queryDb);
			data.put(ApiServiceInterface.TARGET_DB_PARAM, targetDb);
			// data.put(ApiServiceInterface.FORMAT_RESULT, formatResult);

			data.put(ApiServiceInterface.OBJECT_ID, objectId);
			data.put(ApiServiceInterface.ONTOLOGY, ontology);

			// Guess type of operation!!!

		} else {
			data.put(ApiServiceInterface.IS_EXTERNAL_API, true);

		}
	}

	private static boolean matchParameter(String name, String match) {
		final String variable = match.replace("$", "");

		if (name.equalsIgnoreCase(match) || name.equalsIgnoreCase(variable))
			return true;
		else
			return false;
	}

}