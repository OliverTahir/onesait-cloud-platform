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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.persistence.PersistenceException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.quasar.connector.QuasarMongoDBbHttpConnector;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplateImpl;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author minsait by Indra
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
// @ContextConfiguration(classes = EmbeddedMongoConfiguration.class)
public class MongoByQuasarIntegrationTest {

	static final String DATABASE = "onesaitplatform_rtdb";

	@Autowired
	private MongoDbTemplateImpl connect;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	private BasicOpsDBRepository repository;

	@Autowired
	private QuasarMongoDBbHttpConnector connector;

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(name).toURI())),
					Charset.forName("UTF-8"));
		} catch (final Exception e) {
			try {
				return new String(
						IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						Charset.forName("UTF-8"));
			} catch (final IOException e1) {
				log.error("Error loading resource: " + name + ".Please check if this error affect your database", e);
				return null;
			}
		}
	}

	@Before
	public void setUpOnlyOnce() throws PersistenceException, IOException {
		if (!connect.collectionExists(DATABASE, "HelsinkiPopulation_Test"))
			connect.createCollection(DATABASE, "HelsinkiPopulation_Test");

		if (!connect.collectionExists(DATABASE, "Restaurants_Test"))
			connect.createCollection(DATABASE, "Restaurants_Test");

		if (!connect.collectionExists(DATABASE, "Ticket_Test"))
			connect.createCollection(DATABASE, "Ticket_Test");

		if (!connect.collectionExists(DATABASE, "ISO3166_1_Test"))
			connect.createCollection(DATABASE, "ISO3166_1_Test");

		// Load HesinkiPopulation
		Scanner scanner = new Scanner(loadFromResources("HelsinkiPopulation-dataset.json"));
		while (scanner.hasNextLine()) {
			repository.insert("HelsinkiPopulation_Test", "", scanner.nextLine());
		}
		scanner.close();

		// Load Restaurants_Test
		scanner = new Scanner(loadFromResources("Restaurants-dataset.json"));
		while (scanner.hasNextLine()) {
			repository.insert("Restaurants_Test", "", scanner.nextLine());
		}
		scanner.close();

		// Load Ticket_Test
		scanner = new Scanner(loadFromResources("Ticket-dataset.json"));
		while (scanner.hasNextLine()) {
			repository.insert("Ticket_Test", "", scanner.nextLine());
		}
		scanner.close();

		// Load HesinkiPopulation
		scanner = new Scanner(loadFromResources("ISO3166_1-dataset.json"));
		while (scanner.hasNextLine()) {
			repository.insert("ISO3166_1_Test", "", scanner.nextLine());
		}
		scanner.close();
	}

	@After
	public void tearDownOnlyOnce() {
		connect.dropCollection(DATABASE, "HelsinkiPopulation_Test");
		connect.dropCollection(DATABASE, "Restaurants_Test");
		connect.dropCollection(DATABASE, "Ticket_Test");
		connect.dropCollection(DATABASE, "ISO3166_1_Test");
	}

	@Test
	public void test_SelectAll_HelsinkiPopulation() {
		try {
			final String query = "select * from HelsinkiPopulation_Test";
			final String result = connector.queryAsJson("HelsinkiPopulation_Test", query, 0, 1000);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 50);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectPage_HelsinkiPopulation() {
		try {
			final String query = "select * from HelsinkiPopulation_Test";
			final String result = connector.queryAsJson("HelsinkiPopulation_Test", query, 0, 10);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() == 10);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_QueryWithWhere_HelsinkiPopulation() {
		try {
			final String query = "select * from HelsinkiPopulation_Test where Helsinki.year=1880";
			final String result = connector.queryAsJson("HelsinkiPopulation_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() == 1);
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectGroupBy_Restaurants() {
		try {
			final String query = "select Restaurant.cuisine,SUM(Restaurant.cuisine) from Restaurants_Test group by borough";
			final String result = connector.queryAsJson("Restaurants_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 5);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectWhere_Restaurants() {
		try {
			// String query = "select _id,cuisine,borough from Restaurants where
			// address.zipCode=\"'10462'\"";
			final String query = "select * from Restaurants_Test where Restaurant.cuisine='American'";
			final String result = connector.queryAsJson("Restaurants_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 2);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectWhereLike_Restaurants() {
		try {
			// String query = "select _id,cuisine,borough from Restaurants where
			// address.zipCode=\"'10462'\"";
			final String query = "select _id,cuisine,borough from Restaurants_Test where Restaurant.cuisine like '%American%'";
			final String result = connector.queryAsJson("Restaurants_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 2);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectWhere_ISO3166s() {
		try {
			final String query = "select ISO3166.name from ISO3166_1_Test where ISO3166.alpha2='ZM'";
			final String result = connector.queryAsJson("ISO3166_1_Test", query, 0, 50);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() == 2);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectWhereContextData_ISO3166s() {
		try {
			final String query = "select * from ISO3166_1_Test where contextData.user='analytics'";
			final String result = connector.queryAsJson("ISO3166_1_Test", query, 0, 50);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() == 50);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_SelectAll_Ticket() {
		try {
			final String query = "select * from Ticket_Test";
			final String result = connector.queryAsJson("Ticket_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 5);
			//
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	@Test
	public void test_QueryWithWhere_Ticket() {
		try {
			// String query = "select * from Ticket where Ticket.Status='PENDING'"; //NO
			// FUNCIONA POR ESTAR EN MAYÚSCULA
			final String query = "select * from Ticket_Test where contextData.user='administrator'";
			final String result = connector.queryAsJson("Ticket_Test", query, 0, 100);
			log.info("Returned:" + result);
			final JSONArray jsonResult = new JSONArray(result);
			Assert.assertTrue(jsonResult.length() >= 1);
		} catch (final Exception e) {
			Assert.fail("Error Query MongoDB by Quasar. " + e.getMessage());
		}
	}

	// @Test
	public void testQueryAsTable() {
		try {
			final String query = "select * from HelsinkiPopulation";
			final String result = connector.queryAsTable(query, 0, 100);
			// Assert.assertTrue(result.indexOf("|") != -1);
			Assert.assertTrue(true);
		} catch (final Exception e) {
			Assert.fail("No connection with MongoDB by Quasar. " + e);
		}
	}

}
