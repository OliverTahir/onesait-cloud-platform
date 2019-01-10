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
package com.minsait.onesait.platform.persistence.hadoop.missing;

import static com.minsait.onesait.platform.persistence.hadoop.common.HadoopMessages.NOT_SUPPORTED;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.util.BulkWriteResult;
import com.minsait.onesait.platform.persistence.util.MultiDocumentOperationResult;

@Repository
public class DefaultBasicOpsDBRepository implements BasicOpsDBRepository {

	@Override
	public String insert(String ontology, String schema, String instance) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<BulkWriteResult> insertBulk(String ontology, String schema, List<String> instances, boolean order,
			boolean includeIds) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findById(String ontology, String objectId) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findAllAsJson(String ontology) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> findAll(String ontology) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> findAll(String ontology, int limit) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public long count(String ontology) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public long countNative(String collectionName, String query) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

}
