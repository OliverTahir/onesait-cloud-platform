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
package com.minsait.onesait.platform.persistence.mongodb.quasar.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.http.BaseHttpClient;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component
@Lazy
@Slf4j
public class QuasarMongoDBbHttpImpl extends BaseHttpClient implements QuasarMongoDBbHttpConnector {

	@Value("${onesaitplatform.database.mongodb.quasar.maxHttpConnections:10}")
	private int maxHttpConnections;
	@Value("${onesaitplatform.database.mongodb.quasar.maxHttpConnectionsPerRoute:10}")
	private int maxHttpConnectionsPerRoute;

	@Value("${onesaitplatform.database.mongodb.quasar.connector.http.endpoint:http://localhost:18200/query/fs/}")
	private String quasarEndpoint;
	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String database;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private OntologyRepository repository;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private ObjectMapper mapper;

	private static final String CONTEXT_DATA = "contextData";
	private static final String PATH_TO_COMPILED_QUERY = "physicalPlan";
	private static final String BUILDING_ERROR = "Error building URL";

	@PostConstruct
	public void init() {
		build(maxHttpConnections, maxHttpConnectionsPerRoute, getTimeout());
	}

	private int getTimeout() {
		return ((Integer) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
				.get("mongodb-quasar-timeout")).intValue();
	}

	private boolean compileQueries() {
		try {
			final boolean b = ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-quasar-compile")).booleanValue();
			return b;
		} catch (final RuntimeException e) {
			return true;
		}

	}

	@Override
	public String queryAsJson(String collection, String query, int offset, int limit) throws DBPersistenceException {
		String url;
		try {
			if (query.contains("*"))
				query = replaceAsterisk(collection, query);
			url = buildUrl(query, offset, limit);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		if (compileQueries())
			handleCompileQuery(url);
		final String result = invokeSQLPlugin(url, ACCEPT_APPLICATION_JSON, null);
		return result;
	}

	@Override
	public String queryAsTable(String query, int offset, int limit) throws DBPersistenceException {
		String url;
		try {
			url = buildUrl(query, offset, limit);
		} catch (final UnsupportedEncodingException e) {
			log.error(BUILDING_ERROR, e);
			throw new DBPersistenceException(BUILDING_ERROR, e);
		}
		final String result = invokeSQLPlugin(url, ACCEPT_TEXT_CSV, null);
		return result;

	}

	/**
	 * FORMAT QUERY:
	 * /query/fs/[path]?q=[query]&offset=[offset]&limit=[limit]&var.[foo]=[value]
	 *
	 * @param query
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String buildUrl(String query, int offset, int limit) throws UnsupportedEncodingException {
		if (query.contains("_id"))
			query = replaceObjectId(query);
		query = query.replace("'", "\"");
		String params = "q=" + URLEncoder.encode(query, "UTF-8");// URLEncoder.encode(query,
		// "UTF-8");
		if (offset > 0) {
			params += "&offset=" + offset;
		}
		if (limit > 0) {
			params += "&limit=" + limit;
		}

		final String url = quasarEndpoint + database + "/?" + params;// URLEncoder.encode(params, "UTF-8");
		return url;
	}

	private String replaceObjectId(String query) {
		final String oid = "_id";
		final String newOid = "`_id`";
		log.debug("input query with _id {}", query);
		query = query.replaceAll(oid, newOid);

		query = query.replaceAll("``", "`");
		log.debug("replaced query with _id {}", query);

		return query;
	}

	private String replaceAsterisk(String collection, String query) {
		final Ontology ontology = repository.findByIdentification(collection);
		JsonNode schema;
		try {
			schema = mapper.readTree(ontology.getJsonSchema());
			if (!ontologyDataService.refJsonSchema(schema).equals("")) {
				log.debug("Modifying query that contains * {}:", query);
				final String parentNode = schema.at("/required/0").asText();
				if (parentNode != null && parentNode.trim().length() > 0) {
					query = query.replaceAll("count\\(.*?\\*.*?\\)", "count\\(" + parentNode + "\\)");
					query = query.replaceAll("\\.\\*", "");
					final Pattern pattern = Pattern.compile("(select.*?\\*.*?from)");
					final Matcher matcher = pattern.matcher(query);
					while (matcher.find()) {
						final String found = matcher.group(1);
						final String predicateQuery = query.substring(found.length(), query.length()).trim();
						String foundReplace = "";
						final Pattern p2 = Pattern.compile("(.*?\\*.*?)");
						final Matcher m2 = p2.matcher(found);
						while (m2.find()) {
							final String f2 = m2.group();
							if (!(f2.contains("{*") || f2.contains("[*"))) {

								// if (query.contains(" as "))
								foundReplace = f2.replaceAll("\\*",
										parentNode + " as " + parentNode + " ," + CONTEXT_DATA);
								// else
								// foundReplace = f2.replaceAll("\\*", parentNode + " as " + parentNode + " ");
								query = query.replace(f2, foundReplace);

							}

						}
						if (predicateQuery.startsWith(collection)) {
							final Pattern p3 = Pattern.compile("(" + collection + ".*?as.*?)");
							final Matcher m3 = p3.matcher(predicateQuery);
							if (!m3.matches()) {
								query = query.substring(0, foundReplace.length()) + " from "
										+ predicateQuery.replace(collection, collection + " as c");
							}
						}

					}
				}
				log.debug("Modified query that contains * {}:", query);
			} else {
				log.error("Query for ontology {} contains * please indicate explicitly the fields you want to query",
						collection);
				throw new DBPersistenceException("Query for ontology " + collection
						+ " contains *, please indicate explicitly the fields you want to query");
			}
			return query;
		} catch (final Exception e) {
			return query;
		}

	}

	private String handleCompileQuery(String url) throws DBPersistenceException {
		try {
			final String compileResult = invokeSQLPlugin(url.replace("/query/", "/compile/"), ACCEPT_APPLICATION_JSON,
					null);
			final JsonNode compile = mapper.readTree(compileResult);
			String nativeQuery = compile.path(PATH_TO_COMPILED_QUERY).asText();
			if (!StringUtils.isEmpty(nativeQuery)) {
				nativeQuery = nativeQuery.replaceAll("\\n", "");
				log.info("Quasar is about to execute native query: {}", nativeQuery);
				return compile.path(PATH_TO_COMPILED_QUERY).asText();
			} else {
				log.info("Quasar is about to execute native query: {}", compileResult);
				return compileResult;
			}

		} catch (final IOException e) {
			throw new DBPersistenceException("Could not compile query");
		}

	}
}
