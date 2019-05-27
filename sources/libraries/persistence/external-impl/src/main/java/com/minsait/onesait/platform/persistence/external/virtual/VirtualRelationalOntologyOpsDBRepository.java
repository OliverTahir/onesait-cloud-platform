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
package com.minsait.onesait.platform.persistence.external.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.exception.NotSupportedOperationException;
import com.minsait.onesait.platform.persistence.external.exception.SGDBNotSupportedException;
import com.minsait.onesait.platform.persistence.external.virtual.helper.VirtualOntologyHelper;
import com.minsait.onesait.platform.persistence.external.virtual.parser.JSONResultsetExtractor;
import com.minsait.onesait.platform.persistence.external.virtual.parser.JsonRelationalHelper;
import com.minsait.onesait.platform.persistence.util.BulkWriteResult;
import com.minsait.onesait.platform.persistence.util.MultiDocumentOperationResult;

import lombok.extern.slf4j.Slf4j;

@Component("VirtualOntologyOpsDBRepository")
@Lazy
@Slf4j
public class VirtualRelationalOntologyOpsDBRepository implements VirtualOntologyDBRepository {
	
	private static final String NOT_SUPPORTED_OPERATION = "Operation not supported for Virtual Ontologies";
	private static final String WHERE_STR = " WHERE ";
	private static final String SELECT_FROM = "SELECT * FROM ";
	private static final String OPERATION_NOT_SUPPORTED_FOR_THIS = "Operation not supported for this Virtual Ontology";
	

	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	@Qualifier("OracleVirtualOntologyHelper")
	private VirtualOntologyHelper oracleVirtualOntologyHelper;

	@Autowired
	private JsonRelationalHelper jsonRelationalHelper;

	private Map<String, VirtualDataSourceDescriptor> virtualDatasouces;

	@PostConstruct
	public void init() {
		this.virtualDatasouces = new HashMap<String, VirtualDataSourceDescriptor>();
	}

	private VirtualDataSourceDescriptor getDriverManagerDataSource(String datasourceName) {
		VirtualDataSourceDescriptor datasource = this.virtualDatasouces.get(datasourceName);
		if (null == datasource) {

			datasource = new VirtualDataSourceDescriptor();

			OntologyVirtualDatasource datasourceConfiguration = ontologyVirtualDatasourceRepository
					.findByDatasourceName(datasourceName);

			String driverClassName;
			switch (datasourceConfiguration.getSgdb()) {
			case ORACLE:
				driverClassName = oracle.jdbc.driver.OracleDriver.class.getName();
				datasource.setVirtualDatasourceType(VirtualDatasourceType.ORACLE);
				break;
			default:
				throw new SGDBNotSupportedException("Not supported SGDB: " + datasourceConfiguration.getSgdb());

			}

			String connectionString = datasourceConfiguration.getConnectionString();
			String user = datasourceConfiguration.getUser();
			String password = datasourceConfiguration.getCredentials();

			DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
			driverManagerDatasource.setDriverClassName(driverClassName);
			driverManagerDatasource.setUrl(connectionString);
			driverManagerDatasource.setUsername(user);
			driverManagerDatasource.setPassword(password);

			datasource.setQueryLimit(datasourceConfiguration.getQueryLimit());
			datasource.setDriverManagerDataSource(driverManagerDatasource);

			this.virtualDatasouces.put(datasourceName, datasource);

		}

		return datasource;
	}

	private VirtualOntologyHelper getOntologyHelper(VirtualDatasourceType type) {
		switch (type) {
		case ORACLE:
			return oracleVirtualOntologyHelper;

		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type);
		}

	}

	private OntologyVirtualDatasource getDatasourceForOntology(String ontology) {
		OntologyVirtualDatasource ontologyDatasource = this.ontologyVirtualRepository
				.findOntologyVirtualDatasourceByOntologyIdentification(ontology);

		if (null == ontologyDatasource) {
			throw new DBPersistenceException("Datasource not found for virtual ontology: " + ontology);
		}

		return ontologyDatasource;

	}

	private JdbcTemplate getJdbTemplate(String ontology) {

		String datasourceName = this.getDatasourceForOntology(ontology).getDatasourceName();
		VirtualDataSourceDescriptor datasource = this.getDriverManagerDataSource(datasourceName);

		return new JdbcTemplate(datasource.getDriverManagerDataSource());

	}

	@Override
	public List<String> getTables(String datasourceName) {
		VirtualDataSourceDescriptor datasource = this.getDriverManagerDataSource(datasourceName);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource.getDriverManagerDataSource());

		VirtualOntologyHelper helper = getOntologyHelper(datasource.getVirtualDatasourceType());

		try {
			List<String> lTables = jdbcTemplate.queryForList(helper.getAllTablesStatement(), String.class);
			return lTables;
		} catch (Exception e) {
			log.error("Error listing tables from user in external database", e);
			throw new PersistenceException(e);
		}

	}

	@Override
	public String insert(String ontology, String schema, String instance) throws DBPersistenceException {

		String statement = jsonRelationalHelper.getInsertStatement(ontology, instance);

		JdbcTemplate jdbcTemplate = getJdbTemplate(ontology);

		jdbcTemplate.update(statement);

		String objectId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
		if (objectId != "" && objectId != null) {
			JSONObject json = new JSONObject(instance);
			return json.get(objectId).toString();
		}

		return "";
	}

	@Override
	public List<BulkWriteResult> insertBulk(String ontology, String schema, List<String> instances, boolean order,
			boolean includeIds) throws DBPersistenceException {
		for (String instance : instances) {
			this.insert(ontology, schema, instance);
		}

		return new ArrayList<BulkWriteResult>();

	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds)
			throws DBPersistenceException {
		JdbcTemplate jdbcTemplate = getJdbTemplate(ontology);

		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(jdbcTemplate.update(updateStmt.replaceAll(";", "")));
		return result;

	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds)
			throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds)
			throws DBPersistenceException {
		JdbcTemplate jdbcTemplate = getJdbTemplate(collection);

		int updated = jdbcTemplate.update(query.replaceAll(";", ""));

		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(updated);
		return result;
	}

	@Override
	public List<String> queryNative(String ontology, String query) throws DBPersistenceException {

		OntologyVirtualDatasource datasource = getDatasourceForOntology(ontology);
		VirtualOntologyHelper helper = getOntologyHelper(datasource.getSgdb());

		query = helper.addLimit(query, datasource.getQueryLimit());

		JdbcTemplate jdbcTemplate = getJdbTemplate(ontology);

		return jdbcTemplate.query(query.replaceAll(";", ""), new JSONResultsetExtractor(query));
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) throws DBPersistenceException {
		List<String> result = this.queryNative(ontology, query);

		JSONArray jsonResult = new JSONArray();

		for (String instance : result) {
			JSONObject obj = new JSONObject(instance);
			jsonResult.put(obj);
		}

		return jsonResult.toString();
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String findById(String ontology, String objectId) throws DBPersistenceException {
		String objId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
		if (objId != "" && objId != null) {
			return this.queryNativeAsJson(ontology, SELECT_FROM + ontology + WHERE_STR + objId + " = " + objectId);
		} else {
			throw new NotSupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_THIS);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query) throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) throws DBPersistenceException {
		throw new NotSupportedOperationException(NOT_SUPPORTED_OPERATION);
	}

	@Override
	public String findAllAsJson(String ontology) throws DBPersistenceException {
		return this.queryNativeAsJson(ontology, SELECT_FROM + ontology);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) throws DBPersistenceException {
		return this.queryNativeAsJson(ontology, SELECT_FROM + ontology + " WHERE ROWNUM <= " + limit);
	}

	@Override
	public List<String> findAll(String ontology) throws DBPersistenceException {
		return this.queryNative(ontology, SELECT_FROM + ontology);
	}

	@Override
	public List<String> findAll(String ontology, int limit) throws DBPersistenceException {
		return this.queryNative(ontology, SELECT_FROM + ontology + " WHERE ROWNUM <= " + limit);
	}

	@Override
	public long count(String ontology) throws DBPersistenceException {
		JSONArray result = new JSONArray(this.queryNativeAsJson(ontology, "SELECT COUNT(*) FROM " + ontology));
		if (result != null) {
			Iterator<String> itr = result.getJSONObject(0).keys();
			String key = itr.next();
			return result.getJSONObject(0).getLong(key);
		}
		return 0;
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) throws DBPersistenceException {
		return this.deleteNative(ontology, "DELETE FROM " + ontology, includeIds);
	}

	@Override
	public long countNative(String collectionName, String query) throws DBPersistenceException {
		JSONArray result = new JSONArray(this.queryNativeAsJson(collectionName, query));
		if (result != null) {
			Iterator<String> itr = result.getJSONObject(0).keys();
			String key = itr.next();
			return result.getJSONObject(0).getLong(key);

		}
		return 0;
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId)
			throws DBPersistenceException {
		String objId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontologyName);
		if (objId != "" && objId != null) {
			return this.deleteNative(ontologyName, "DELETE FROM " + ontologyName + WHERE_STR + objId + " = " + objectId,
					false);
		} else {
			throw new NotSupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_THIS);
		}
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) throws DBPersistenceException {
		String objId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontologyName);
		if (objId != "" && objId != null) {
			return this.updateNative(ontologyName,
					"UPDATE " + ontologyName + " SET " + body + WHERE_STR + objId + " = " + objectId, false);
		} else {
			throw new NotSupportedOperationException(OPERATION_NOT_SUPPORTED_FOR_THIS);
		}
	}

	@Override
	public List<String> getInstanceFromTable(String datasource, String query) {
		query = getStatementFromJson(query);

		OntologyVirtualDatasource virtualDatasource = this.ontologyVirtualDatasourceRepository
				.findByDatasourceName(datasource);

		if (log.isDebugEnabled()) {
			log.debug("Receive query for Oracle: {}", query);
		}

		VirtualOntologyHelper helper = getOntologyHelper(virtualDatasource.getSgdb());

		query = helper.addLimit(query, virtualDatasource.getQueryLimit());

		VirtualDataSourceDescriptor sourceDescriptor = this.getDriverManagerDataSource(datasource);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDescriptor.getDriverManagerDataSource());

		return jdbcTemplate.query(query.replaceAll(";", ""), new JSONResultsetExtractor(query));

	}

	private String getStatementFromJson(String statement) {
		if (statement.trim().startsWith("{")) {
			statement = statement.substring(statement.indexOf("{") + 1);
		}
		if (statement.trim().endsWith("}")) {
			statement = statement.substring(0, statement.lastIndexOf("}"));
		}
		return statement;
	}

	@Override
	public String executeQuery(String ontology, String query) {
		if (query.toUpperCase().startsWith("SELECT")) {
			return this.queryNativeAsJson(ontology, query);
		} else if (query.toUpperCase().startsWith("UPDATE")) {
			return String.valueOf(this.updateNative(ontology, query, false));
		} else if (query.toUpperCase().startsWith("DELETE")) {
			return String.valueOf(this.deleteNative(ontology, query, false));
		} else if (query.toUpperCase().startsWith("SELECT COUNT")) {
			return String.valueOf(this.countNative(ontology, query));
		} else {
			throw new NotSupportedOperationException("Operation not supported for Virtual Ontology");
		}
	}

}
