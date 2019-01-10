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
package com.minsait.onesait.platform.persistence.hadoop.kudu;

import static com.minsait.onesait.platform.persistence.hadoop.common.HadoopMessages.NOT_IMPLEMENTED;
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.IMPALA_MANAGE_DB_REPO_BEAN_NAME;
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.CommonQuery;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.resultset.KuduResultSetExtractor;
import com.minsait.onesait.platform.persistence.hadoop.resultset.SingleKuduResultSetExtractor;
import com.minsait.onesait.platform.persistence.hadoop.rowmapper.SingleKuduRowMapper;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonRelationalHelperKuduImpl;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.util.BulkWriteResult;
import com.minsait.onesait.platform.persistence.util.MultiDocumentOperationResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@Conditional(HadoopEnabledCondition.class)
public class KuduBasicOpsDBRepository implements BasicOpsDBRepository {

	@Autowired
	@Qualifier(IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate impalaJdbcTemplate;

	@Autowired
	@Qualifier(IMPALA_MANAGE_DB_REPO_BEAN_NAME)
	private ManageDBRepository manageDBRepository;

	@Autowired
	@Qualifier((NameBeanConst.KUDU_QUERY_REPO_BEAN_NAME))
	private QueryAsTextDBRepository queryAsTextDBRepository;

	@Autowired
	private JsonRelationalHelperKuduImpl jsonRelationalHelperKuduImpl;

	@Override
	public String insert(String ontology, String schema, String instance) throws DBPersistenceException {

		log.debug("insert instance " + instance + "into ontology " + ontology);

		try {

			String id = UUID.randomUUID().toString();
			String statement = jsonRelationalHelperKuduImpl.getInsertStatement(ontology, schema, instance, id);
			impalaJdbcTemplate.execute(statement);

			return "{\"" + JsonFieldType.PRIMARY_ID_FIELD + "\":\"" + id + "\"}";

		} catch (Exception e) {
			log.error("error insert instance ", e);
			throw new DBPersistenceException(e);
		}

	}

	@Override
	public List<BulkWriteResult> insertBulk(String ontology, String schema, List<String> instances, boolean order,
			boolean includeIds) throws DBPersistenceException {

		List<BulkWriteResult> result = new ArrayList<>();

		if (instances != null) {
			for (String instance : instances) {

				BulkWriteResult insertResult = new BulkWriteResult();

				try {

					String id = insert(ontology, schema, instance);

					insertResult.setId(id);
					insertResult.setOk(true);

				} catch (Exception e) {
					log.error("error inserting bulk instance " + instance, e);
					insertResult.setOk(false);
				}

				result.add(insertResult);
			}
		}

		return result;
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String findById(String ontology, String objectId) throws DBPersistenceException {

		String sql = String.format(CommonQuery.FIND_BY_ID, ontology, objectId);
		String data = impalaJdbcTemplate.query(sql, new SingleKuduResultSetExtractor());
		return data;
	}

	@Override
	public String querySQLAsJson(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) throws DBPersistenceException {
		return queryAsTextDBRepository.querySQLAsJson(ontology, query, offset);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String findAllAsJson(String ontology) throws DBPersistenceException {
		String sql = String.format(CommonQuery.FIND_ALL, ontology);
		return impalaJdbcTemplate.query(sql, new KuduResultSetExtractor());
	}

	@Override
	public String findAllAsJson(String ontology, int limit) throws DBPersistenceException {
		String query = String.format(CommonQuery.FIND_ALL_WITH_LIMIT, ontology, limit);
		return queryAsTextDBRepository.queryNativeAsJson(ontology, query);
	}

	@Override
	public List<String> findAll(String ontology) throws DBPersistenceException {

		String sql = String.format(CommonQuery.FIND_ALL, ontology);
		List<String> all = impalaJdbcTemplate.query(sql, new SingleKuduRowMapper());
		return all;
	}

	@Override
	public List<String> findAll(String ontology, int limit) throws DBPersistenceException {

		String sql = String.format(CommonQuery.FIND_ALL_WITH_LIMIT, ontology, limit);
		List<String> all = impalaJdbcTemplate.query(sql, new SingleKuduRowMapper());
		return all;
	}

	@Override
	public long count(String ontology) throws DBPersistenceException {

		String sql = String.format(CommonQuery.COUNT, ontology);
		int count = impalaJdbcTemplate.queryForObject(sql, Integer.class);
		return count;
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) throws DBPersistenceException {
		String sql = String.format(CommonQuery.DELETE_ALL, ontology);
		int count = impalaJdbcTemplate.update(sql);

		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(count);
		return result;
	}

	@Override
	public long countNative(String collectionName, String query) throws DBPersistenceException {
		return impalaJdbcTemplate.queryForObject(query, Integer.class);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId)
			throws DBPersistenceException {
		String sql = String.format(CommonQuery.DELETE_BY_ID, ontologyName, objectId);
		int count = impalaJdbcTemplate.update(sql);

		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(count);
		return result;
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

}
