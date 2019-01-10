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
package com.minsait.onesait.platform.persistence.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.common.DescribeColumnData;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESCountService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESDeleteService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("ElasticSearchManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class ElasticSearchManageDBRepository implements ManageDBRepository {

	private static final String NOT_IMPLEMENTED_ALREADY = "Not Implemented Already";

	@Autowired
	private ESBaseApi connector;
	@Autowired
	private ESCountService eSCountService;

	@Autowired
	private ESDeleteService eSDeleteService;

	@Value("${onesaitplatform.database.elasticsearch.dump.path:null}")
	@Getter
	@Setter
	private String dumpPath;

	@Value("${onesaitplatform.database.elasticsearch.elasticdump.path:null}")
	@Getter
	@Setter
	private String elasticDumpPath;

	@Value("${onesaitplatform.database.elasticsearch.sql.connector.http.endpoint:http://localhost:9300}")
	@Getter
	@Setter
	private String elasticSearchEndpoint;

	private String createTestIndex(String index) {
		final String res = connector.createIndex(index);
		log.info("ElasticSearchManageDBRepository createTestIndex :" + index + " res: " + res);
		return res;
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String createTable4Ontology(String ontology, String schema) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		try {
			final String res = connector.createIndex(ontology);
			log.info("Index result :  " + res);

		} catch (final Exception e) {
			log.info("Resource already exists ");
		}
		if (JSONPersistenceUtilsElasticSearch.isJSONSchema(schema)) {
			try {
				schema = JSONPersistenceUtilsElasticSearch.getElasticSearchSchemaFromJSONSchema(ontology, schema);
			} catch (final JSONException e) {
				log.error("Cannot generate ElasticSearch effective Schema, turn to default " + e.getMessage(), e);
				schema = "{}";
			}
		}

		else if (schema.equals(""))
			schema = "{}";
		else if (schema.equals("{}")) {

			log.info("No schema is declared");
		}

		connector.createType(ontology, ontology, schema);

		return ontology;

	}

	@Override
	public List<String> getListOfTables() throws DBPersistenceException {
		final List<String> list = new ArrayList<>();
		final String result = connector.getIndexes();
		if (result != null)
			list.add(result);
		return list;

	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		final List<String> list = new ArrayList<>();
		final String result = connector.getIndexes();
		if (result != null && result.indexOf(ontology) != .1) {
			list.add(ontology);
		}
		return list;
	}

	@Override
	public void removeTable4Ontology(String ontology) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		eSDeleteService.deleteAll(ontology, ontology);

	}

	@Override
	public void createIndex(String ontology, String attribute) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String sentence) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void dropIndex(String ontology, String indexName) throws DBPersistenceException {

		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public List<String> getListIndexes(String ontology) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public String getIndexes(String ontology) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void validateIndexes(String ontology, String schema) throws DBPersistenceException {
		ontology = ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String pathToFile)
			throws DBPersistenceException {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM-hh-mm");
		final String query = "--searchBody {\\\"query\\\":{\\\"range\\\":{\\\"contextData.timestampMillis\\\":{\\\"lte\\\":"
				+ startDateMillis + "}}}}";
		final String queryElastic = "{\r\n" + "\"query\" : {\r\n"
				+ "    \"range\" : {\r\n  \"contextData.timestampMillis\" : {\r\n \"lte\" : " + startDateMillis
				+ " \r\n} \r\n} \r\n" + "  }\r\n" + "}";
		final String path;

		if (pathToFile.equals("default"))
			path = dumpPath + ontology.toLowerCase() + format.format(new Date()) + ".json";
		else
			path = pathToFile;
		if (eSCountService.getQueryCount(queryElastic, ontology.toLowerCase()) > 0) {
			final ProcessBuilder pb;

			pb = new ProcessBuilder("elasticdump", "--input=" + elasticSearchEndpoint + "/" + ontology.toLowerCase(),
					"--output=" + path, query, "--delete=true");
			try {
				final Process p = pb.start();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				final StringBuilder builder = new StringBuilder();
				String line = null;
				p.waitFor();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append(System.getProperty("line.separator"));
				}
				builder.toString();
				log.info("Cmd elasticdump output: " + builder.toString());
				log.info("Created export file for ontology {} at {}", ontology, path);
			} catch (IOException | InterruptedException e) {
				log.error("Could not execute command {}", e.getMessage());
				throw new DBPersistenceException("Could not execute command: " + pb.command().toString() + e);
			}
		}
		return ExportData.builder().filterQuery(queryElastic).path(path).build();
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		return eSDeleteService.deleteByQuery(ontology.toLowerCase(), ontology.toLowerCase(), query, false).getCount();
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

}
