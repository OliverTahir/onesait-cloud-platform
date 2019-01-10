/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
package com.minsait.onesait.platform.persistence.mongodb;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.ContextData;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplateImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class MongoNativeManageDBRepositoryIntegrationTest {

	@Autowired
	@Qualifier("MongoManageDBRepository")
	ManageDBRepository repository;

	@Autowired
	MongoDbTemplateImpl connect;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository basicOps;
	
	static final String DATABASE = "onesaitplatform_rtdb";

	/*
	 * public void createIndex(String ontology, String attribute) throws
	 * DBPersistenceException;
	 * 
	 * public List<String> createIndex(String sentence) throws
	 * DBPersistenceException;
	 * 
	 * public void dropIndex(String ontology, String indexName) throws
	 * DBPersistenceException;
	 * 
	 * public List<String> getListIndexes(String ontology) throws
	 * DBPersistenceException;
	 * 
	 * public void validateIndexes(String ontology, String schema) throws
	 * DBPersistenceException;
	 */

	@Test
	public void given_MongoDb_When_StatusIsRequested_Then_TheStatusOfSofia2DatabaseIsOk() {
		try {
			Assert.assertEquals(repository.getStatusDatabase().get(DATABASE), Boolean.TRUE);
		} catch (Exception e) {
			Assert.fail("No connection with MongoDB");
		}
	}

//	@Test
//	public void given_MongoDb_When_TablesForMensajesPlataformaOntologyAreRequested_Then_OneIsReturned() {
//		try {
//			Assert.assertEquals(repository.getListOfTables4Ontology("MensajesPlataforma").size(), 1);
//		} catch (Exception e) {
//			Assert.fail("No connection with MongoDB");
//		}
//	}

	@Test
	public void given_MongoDb_When_OneCollectionIsCreated_Then_TheCollectionIsCreated() {
		try {
			int size1 = repository.getListOfTables().size();
			repository.createTable4Ontology("ONT_TODELETE_1",
					"{\"$schema\": \"http://json-schema.org/draft-04/schema#\",\"title\": \"Test Schema\"}");
			int size2 = repository.getListOfTables().size();
			Assert.assertEquals(size2, size1 + 1);
			repository.removeTable4Ontology("ONT_TODELETE_1");
			size2 = repository.getListOfTables().size();
			Assert.assertEquals(size2, size1);

		} catch (Exception e) {
			Assert.fail("test_createCollection:" + e.getMessage());
		}
	}

	@Test
	public void given_MongoDb_When_AnIndexIsCreatedForACollection_Then_TheIndexIsCreated() {
		try {
			if (connect.collectionExists(DATABASE, "contextData"))
				connect.dropCollection(DATABASE, "contextData");
			connect.createCollection(DATABASE, "contextData");
			ContextData data = ContextData
					.builder("user1", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();

			ObjectMapper mapper = new ObjectMapper();
			basicOps.insert("contextData", "", mapper.writeValueAsString(data));

			data = ContextData
					.builder("user2", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();

			basicOps.insert("contextData", "", mapper.writeValueAsString(data));
			Assert.assertEquals(basicOps.count("contextData"), 2);
			int numIndex = repository.getListIndexes("contextData").size();
			repository.createIndex("contextData", "user");
			Assert.assertEquals(basicOps.count("contextData"), 2);
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex + 1);
			connect.dropCollection(DATABASE, "contextData");
		} catch (Exception e) {
			Assert.fail("test1_createIndex:" + e.getMessage());
		}
	}

	@Test
	public void given_MongoDb_When_AnIndexIsCreatedAndThenDroped_Then_TheIndexIsCreatedAndFinallyDropped() {
		try {
			if (connect.collectionExists(DATABASE, "contextData"))
				connect.dropCollection(DATABASE, "contextData");
			connect.createCollection(DATABASE, "contextData");
			ContextData data = ContextData
					.builder("user1", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
			ObjectMapper mapper = new ObjectMapper();
			basicOps.insert("contextData", "", mapper.writeValueAsString(data));

			data = ContextData
					.builder("user2", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();

			basicOps.insert("contextData", "", mapper.writeValueAsString(data));
			Assert.assertEquals(basicOps.count("contextData"), 2);
			int numIndex = repository.getListIndexes("contextData").size();
			repository.createIndex("contextData", "user_i", "user");
			Assert.assertEquals(basicOps.count("contextData"), 2);
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex + 1);
			repository.dropIndex("contextDAta", "user_i");
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex);
			connect.dropCollection(DATABASE, "contextData");
		} catch (Exception e) {
			Assert.fail("test1_createIndex:" + e.getMessage());
		}
	}

	@Test
	public void given_MongoDb_WhenOneNativeIndexIsCreated_Then_TheIndexIsCreated() {
		try {
			if (connect.collectionExists(DATABASE, "contextData"))
				connect.dropCollection(DATABASE, "contextData");
			connect.createCollection(DATABASE, "contextData");
			ContextData data = ContextData
					.builder("user1", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();

			ObjectMapper mapper = new ObjectMapper();
			basicOps.insert("contextData", "", mapper.writeValueAsString(data));
			data = ContextData
					.builder("user1", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
							System.currentTimeMillis(), "Testing")
					.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
					.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();

			basicOps.insert("contextData", "", mapper.writeValueAsString(data));
			Assert.assertEquals(basicOps.count("contextData"), 2);
			int numIndex = repository.getListIndexes("contextData").size();
			repository.createIndex("db.contextData.createIndex({'user':1},{'name':'user_i'})");
			Assert.assertEquals(basicOps.count("contextData"), 2);
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex + 1);
			repository.dropIndex("contextData", "user_i");
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex);
			repository.createIndex("db.contextData.createIndex({'user':1})");
			Assert.assertEquals(repository.getListIndexes("contextData").size(), numIndex + 1);
			connect.dropCollection(DATABASE, "contextData");
		} catch (Exception e) {
			Assert.fail("test1_createNativeIndex:" + e.getMessage());
		}
	}

	@Test
	public void test1_createTestOntollogy() {
		try {
			int size1 = repository.getListOfTables().size();
			if (repository.getListOfTables4Ontology("test_ontology").size() == 0) {
				repository.createTable4Ontology("test_ontology",
						"{\"$schema\": \"http://json-schema.org/draft-04/schema#\",\"title\": \"Test Schema\"}");
				int size2 = repository.getListOfTables().size();
				Assert.assertEquals(size2, size1 + 1);
			}

		} catch (Exception e) {
			Assert.fail("test_createCollection:" + e.getMessage());
		}
	}

}
