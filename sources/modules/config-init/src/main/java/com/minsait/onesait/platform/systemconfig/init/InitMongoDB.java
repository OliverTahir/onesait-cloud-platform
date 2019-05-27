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
package com.minsait.onesait.platform.systemconfig.init;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.OSDetector;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplateImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.mongodb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitMongoDB {

	private static boolean started = false;

	@Autowired
	MongoDbTemplateImpl connect;

	@Autowired
	@Qualifier("MongoManageDBRepository")
	ManageDBRepository manageDb;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository basicOps;

	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	UserRepository userCDBRepository;

	private final String USER_DIR = "user.dir";
	private final String MONGO_IMPORT_BINARY = "s:/tools/mongo/bin/mongoimport";
	private final String UNIX_FILEPATH = "/tmp/";
	
	private final String AUDIT_GENERAL = "AuditGeneral";
	private final String RESTAURANTS_STR = "Restaurants";
	private final String HELSINKI_POP = "HelsinkiPopulation";
	private final String TWIN_LOGS = "TwinLogs";
	private final String TWIN_EVENTS = "TwinEvents";
	private final String TWIN_ACTIONS_TURBINE = "TwinActionsTurbine";
	private final String TWIN_PROPERTIES_TURBINE = "TwinPropertiesTurbine";
	private final String TICKET_STR = "Ticket";
	private final String ROUTES_STR = "routes";
	private final String ROUTESEXTEN_STR = "routesexten";
	private final String AIRPORT_DATA = "airportsdata";
	private final String ISO3166_1_STR = "ISO3166_1";
	private final String ISO3166_2_STR = "ISO3166_2";
	private final String QA_OVERVIEW_STR = "QA_OVERVIEW";
	private final String PRODUCER_ERRORCAT_STR = "Producer_ErrorCat";
	private final String ERRORS_ON_DATE = "errorsOnDate";
	private final String QA_DETAIL_STR = "QA_DETAIL";
	private final String ERRORS_TYPE_DATE = "errorsTypeOnDate";
	private final String QA_DETAIL_EXTENDED_STR = "QA_DETAIL_EXTENDED";
	private final String SITES_STR = "SITES";
	private final String READING_JSON = "Reading JSON into Database...";
	private final String EMPTYBASE_STR = "EmptyBase";
	private final String CREATING_ERROR = "Error creating DataSet...ignoring";
	private final String ANDROID_IOT_FRAME = "androidIoTFrame";

	@Value("${onesaitplatform.database.mongodb.username:platformadmin}")
	private String mongodb_username;

	@Value("${onesaitplatform.database.mongodb.password:0pen-platf0rm-2018!}")
	private String mongodb_password;

	@Value("${onesaitplatform.database.mongodb.authenticationDatabase:admin}")
	private String mongodb_authdb;

	@Value("${spring.data.mongodb.host:realtimedb}")
	private String mongodb_host;

	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String mongodb_name;

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;
			deleteCollections();
			init_AuditGeneral();
			init_RestaurantsDataSet();
			init_HelsinkiPopulationDataSet();
			init_DigitalTwinLogs();
			init_DigitalTwinEvents();
			init_DigitalTwinActionsTurbine();
			init_DigitalTwinPropertiesTurbine();
			init_TicketDataSet();
			init_OpenflightsDataSet();
			init_ISO3166();
			init_QA_WindTurbinesDataSet();
			log.info("initMongoDB correctly...");
		}
	}

	private void deleteCollections() {
		try {
			if (connect.collectionExists(mongodb_name, AUDIT_GENERAL))
				connect.dropCollection(mongodb_name, AUDIT_GENERAL);
			if (connect.collectionExists(mongodb_name, RESTAURANTS_STR))
				connect.dropCollection(mongodb_name, RESTAURANTS_STR);
			if (connect.collectionExists(mongodb_name, HELSINKI_POP))
				connect.dropCollection(mongodb_name, HELSINKI_POP);
			if (connect.collectionExists(mongodb_name, TWIN_LOGS))
				connect.dropCollection(mongodb_name, TWIN_LOGS);
			if (connect.collectionExists(mongodb_name, TWIN_EVENTS))
				connect.dropCollection(mongodb_name, TWIN_EVENTS);
			if (connect.collectionExists(mongodb_name, TWIN_ACTIONS_TURBINE))
				connect.dropCollection(mongodb_name, TWIN_ACTIONS_TURBINE);
			if (connect.collectionExists(mongodb_name, TWIN_PROPERTIES_TURBINE))
				connect.dropCollection(mongodb_name, TWIN_PROPERTIES_TURBINE);
			if (connect.collectionExists(mongodb_name, TICKET_STR))
				connect.dropCollection(mongodb_name, TICKET_STR);
			if (connect.collectionExists(mongodb_name, ROUTES_STR))
				connect.dropCollection(mongodb_name, ROUTES_STR);
			if (connect.collectionExists(mongodb_name, ROUTESEXTEN_STR))
				connect.dropCollection(mongodb_name, ROUTESEXTEN_STR);
			if (connect.collectionExists(mongodb_name, AIRPORT_DATA))
				connect.dropCollection(mongodb_name, AIRPORT_DATA);
			if (connect.collectionExists(mongodb_name, ISO3166_1_STR))
				connect.dropCollection(mongodb_name, ISO3166_1_STR);
			if (connect.collectionExists(mongodb_name, ISO3166_2_STR))
				connect.dropCollection(mongodb_name, ISO3166_2_STR);
			if (connect.collectionExists(mongodb_name, QA_OVERVIEW_STR))
				connect.dropCollection(mongodb_name, QA_OVERVIEW_STR);
			if (connect.collectionExists(mongodb_name, PRODUCER_ERRORCAT_STR))
				connect.dropCollection(mongodb_name, PRODUCER_ERRORCAT_STR);
			if (connect.collectionExists(mongodb_name, ERRORS_ON_DATE))
				connect.dropCollection(mongodb_name, ERRORS_ON_DATE);
			if (connect.collectionExists(mongodb_name, QA_DETAIL_STR))
				connect.dropCollection(mongodb_name, QA_DETAIL_STR);
			if (connect.collectionExists(mongodb_name, ERRORS_TYPE_DATE))
				connect.dropCollection(mongodb_name, ERRORS_TYPE_DATE);
			if (connect.collectionExists(mongodb_name, QA_DETAIL_EXTENDED_STR))
				connect.dropCollection(mongodb_name, QA_DETAIL_EXTENDED_STR);
			if (connect.collectionExists(mongodb_name, SITES_STR))
				connect.dropCollection(mongodb_name, SITES_STR);
			log.info("Deleted collections...");

		} catch (final Exception e) {
			log.error("Error deleting Collections", e);
		}
	}

	private User getUserDeveloper() {
		final User userCollaborator = userCDBRepository.findByUserId("developer");
		return userCollaborator;
	}

	private void insertIntoOntology(String ontology, String dataset) {
		final Scanner scanner = new Scanner(loadFromResources(dataset));
		while (scanner.hasNextLine()) {
			basicOps.insert(ontology, "", scanner.nextLine());
		}
		scanner.close();

	}

	public void init_RestaurantsDataSet() {
		try {
			final String dataSet = "examples/restaurants-dataset.json";
			final String schema = "examples/Restaurants-schema.json";
			log.info("init RestaurantsDataSet");
			if (basicOps.count(RESTAURANTS_STR) == 0) {
				// Load Restaurants_Test
				insertIntoOntology(RESTAURANTS_STR, dataSet);
				log.info(READING_JSON);
			}
			if (manageDb.getListOfTables4Ontology(RESTAURANTS_STR).isEmpty()) {
				log.info("No Collection Restaurants, creating...");
				manageDb.createTable4Ontology(RESTAURANTS_STR, "{}");
			}
			if (ontologyRepository.findByIdentification(RESTAURANTS_STR) == null) {
				final Ontology ontology = new Ontology();
				if (OSDetector.isWindows()) {
					ontology.setJsonSchema(loadFromResources(schema));
				} else {
					ontology.setJsonSchema(UNIX_FILEPATH + schema);
				}
				ontology.setId("MASTER-Ontology-Restaurant-1");
				ontology.setIdentification(RESTAURANTS_STR);
				ontology.setDescription("Ontology Restaurants for testing");
				ontology.setMetainf(RESTAURANTS_STR);
				ontology.setActive(true);
				ontology.setRtdbClean(true);
				ontology.setDataModel(dataModelRepository.findByName(EMPTYBASE_STR).get(0));
				ontology.setRtdbToHdb(true);
				ontology.setPublic(true);
				ontology.setUser(getUserDeveloper());
				ontologyRepository.save(ontology);

			}

		} catch (final Exception e) {
			log.error("Error creating Restaurants DataSet...ignoring", e);
		}
	}

	public void init_HelsinkiPopulationDataSet() {
		try {
			final String dataSet = "examples/HelsinkiPopulation-dataset.json";
			final String schema = "examples/HelsinkiPopulation-schema.json";
			log.info("init init_HelsinkiPopulationDataSet");
			if (basicOps.count(HELSINKI_POP) == 0) {
				// Load data
				insertIntoOntology(HELSINKI_POP, dataSet);
				log.info(READING_JSON);
				log.info(READING_JSON);
			}
			if (manageDb.getListOfTables4Ontology(HELSINKI_POP).isEmpty()) {
				log.info("No Collection HelsinkiPopulation, creating...");
				manageDb.createTable4Ontology(HELSINKI_POP, "{}");
			}
			if (ontologyRepository.findByIdentification(HELSINKI_POP) == null) {
				final Ontology ontology = new Ontology();

				ontology.setId("MASTER-Ontology-HelsinkiPopulation-1");

				if (OSDetector.isWindows()) {
					ontology.setJsonSchema(loadFromResources(schema));
				} else {
					ontology.setJsonSchema(UNIX_FILEPATH + schema);
				}

				ontology.setIdentification(HELSINKI_POP);
				ontology.setDescription("Ontology HelsinkiPopulation for testing");
				ontology.setActive(true);
				ontology.setMetainf(HELSINKI_POP);
				ontology.setRtdbClean(true);
				ontology.setDataModel(dataModelRepository.findByName(EMPTYBASE_STR).get(0));
				ontology.setRtdbToHdb(true);
				ontology.setPublic(false);
				ontology.setUser(getUserDeveloper());
				ontologyRepository.save(ontology);
			}

		} catch (final Exception e) {
			log.error("Error creating HelsinkiPopulation DataSet...ignoring", e);
		}
	}

	public void init_OpenflightsDataSet() {
		try {
			String dataSet = "examples/routes-dataset.json";
			log.info("init init_OpenflightsDataSet");
			if (basicOps.count(ROUTES_STR) == 0) {
				// Load data
				insertIntoOntology(ROUTES_STR, dataSet);
				log.info(READING_JSON);
				log.info(READING_JSON);
			}
			dataSet = "examples/routesexten-dataset.json";
			if (basicOps.count(ROUTESEXTEN_STR) == 0) {
				insertIntoOntology(ROUTESEXTEN_STR, dataSet);
				log.info(READING_JSON);
			}

			dataSet = "examples/airportsdata-dataset.json";
			if (basicOps.count(AIRPORT_DATA) == 0) {
				insertIntoOntology(AIRPORT_DATA, dataSet);
				log.info(READING_JSON);
			}

		} catch (final Exception e) {
			log.error(CREATING_ERROR, e);
		}
	}

	public void init_ISO3166() {
		try {
			String dataSet = "examples/ISO3166_1-dataset.json";
			if (basicOps.count(ISO3166_1_STR) == 0) {
				insertIntoOntology(ISO3166_1_STR, dataSet);
				log.info(READING_JSON);
			}

			dataSet = "examples/ISO3166_2-dataset.json";
			if (basicOps.count(ISO3166_2_STR) == 0) {
				insertIntoOntology(ISO3166_2_STR, dataSet);
				log.info(READING_JSON);
			}

		} catch (final Exception e) {
			log.error(CREATING_ERROR, e);
		}
	}
	
	public void init_QA_WindTurbinesDataSet() {
		try {
			String dataSet = "examples/QA_OVERVIEW-dataset.json";
			log.info("init init_QA_WindTurbinesDataSet");
			if (basicOps.count(QA_OVERVIEW_STR) == 0) {
				// Load data
				insertIntoOntology(QA_OVERVIEW_STR, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/Producer_ErrorCat-dataset.json";
			if (basicOps.count(PRODUCER_ERRORCAT_STR) == 0) {
				insertIntoOntology(PRODUCER_ERRORCAT_STR, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/errorsOnDate-dataset.json";
			if (basicOps.count(ERRORS_ON_DATE) == 0) {
				insertIntoOntology(ERRORS_ON_DATE, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/QA_DETAIL-dataset.json";
			if (basicOps.count(QA_DETAIL_STR) == 0) {
				insertIntoOntology(QA_DETAIL_STR, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/errorsTypeOnDate-dataset.json";
			if (basicOps.count(ERRORS_TYPE_DATE) == 0) {
				insertIntoOntology(ERRORS_TYPE_DATE, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/QA_DETAIL_EXTENDED-dataset.json";
			if (basicOps.count(QA_DETAIL_EXTENDED_STR) == 0) {
				insertIntoOntology(QA_DETAIL_EXTENDED_STR, dataSet);
				log.info(READING_JSON);
			}
			dataSet = "examples/SITES-dataset.json";
			if (basicOps.count(SITES_STR) == 0) {
				insertIntoOntology(SITES_STR, dataSet);
				log.info(READING_JSON);
			}

		} catch (final Exception e) {
			log.error(CREATING_ERROR, e);
		}
	}

	public void init_AndroidIoTFrame(String path) {
		try {
			final String schema = "examples/androidIoTFrame-schema.json";
			log.info("init init_androidIoTFrame");
			if (manageDb.getListOfTables4Ontology(ANDROID_IOT_FRAME).isEmpty()) {
				log.info("No Collection androidIoTFrame, creating...");
				manageDb.createTable4Ontology(ANDROID_IOT_FRAME, "{}");
			}
			if (ontologyRepository.findByIdentification(ANDROID_IOT_FRAME) == null) {
				final Ontology ontology = new Ontology();

				ontology.setId("MASTER-Ontology-androidIoTFrame-1");

				if (OSDetector.isWindows()) {
					ontology.setJsonSchema(loadFromResources(schema));
				} else {
					ontology.setJsonSchema(UNIX_FILEPATH + schema);
				}

				ontology.setIdentification(ANDROID_IOT_FRAME);
				ontology.setDescription("Ontology androidIoTFrame for measures");
				ontology.setMetainf(ANDROID_IOT_FRAME);
				ontology.setActive(true);
				ontology.setRtdbClean(true);
				ontology.setDataModel(dataModelRepository.findByName(EMPTYBASE_STR).get(0));
				ontology.setRtdbToHdb(true);
				ontology.setPublic(false);
				ontology.setUser(getUserDeveloper());
				ontologyRepository.save(ontology);
			}

		} catch (final Exception e) {
			log.error("Error creating init_androidIoTFrame DataSet...ignoring", e);
		}
	}

	public void init_TicketDataSet() {
		final String dataSet = "examples/Ticket-dataset.json";
		final String schema = "examples/OntologySchema_Ticket.json";
		log.info("init init_TicketDataSet");
		Ontology ticket = ontologyRepository.findByIdentification(TICKET_STR);
		if (ticket == null) {
			ticket = new Ontology();

			ticket.setId("MASTER-Ontology-Ticket-1");

			if (OSDetector.isWindows()) {
				ticket.setJsonSchema(loadFromResources(schema));
			} else {
				ticket.setJsonSchema(UNIX_FILEPATH + schema);
			}

			ticket.setDescription("Ontology created for Ticketing");
			ticket.setIdentification(TICKET_STR);
			ticket.setMetainf(TICKET_STR);
			ticket.setActive(true);
			ticket.setRtdbClean(true);
			ticket.setRtdbToHdb(true);
			ticket.setPublic(true);
			ticket.setDataModel(dataModelRepository.findByName(EMPTYBASE_STR).get(0));
			ticket.setUser(getUserDeveloper());
			ticket.setAllowsCypherFields(false);
			ontologyRepository.save(ticket);
		}

		try {
			if (basicOps.count(TICKET_STR) == 0) {
				manageDb.createTable4Ontology(ticket.getIdentification(), ticket.getJsonSchema());
				insertIntoOntology(TICKET_STR, dataSet);
				log.info(READING_JSON);
			}
		} catch (final Exception e) {
			log.error("Error creating Ticket DataSet...ignoring", e);
		}

	}

	public void init_AuditGeneral() {
		log.info("init AuditGeneral");
		/*
		 * db.createCollection(AUDIT_GENERAL); db.AuditGeneral.createIndex({type: 1});
		 * db.AuditGeneral.createIndex({user: 1});
		 * db.AuditGeneral.createIndex({ontology: 1}); db.AuditGeneral.createIndex({kp:
		 * 1});
		 */
		if (manageDb.getListOfTables4Ontology(AUDIT_GENERAL).isEmpty()) {
			try {
				log.info("No Collection AuditGeneral...");
				manageDb.createTable4Ontology(AUDIT_GENERAL, "{}");
				manageDb.createIndex(AUDIT_GENERAL, "type");
				manageDb.createIndex(AUDIT_GENERAL, "user");
				manageDb.createIndex(AUDIT_GENERAL, "ontology");
				manageDb.createIndex(AUDIT_GENERAL, "kp");
			} catch (final Exception e) {
				log.error("Error init_AuditGeneral:" + e.getMessage());
				manageDb.removeTable4Ontology(AUDIT_GENERAL);
			}
		}
	}

	public void init_DigitalTwinActionsTurbine() {
		log.info("init TwinActionsTurbine for Digital Twin");
		/*
		 * db.createCollection("Logs");
		 */
		if (basicOps.count(TWIN_ACTIONS_TURBINE) == 0) {
			try {
				log.info("No Collection TwinActionsTurbine...");
				manageDb.createTable4Ontology(TWIN_ACTIONS_TURBINE, "{}");
			} catch (final Exception e) {
				log.error("Error init_DigitalTwinActionsTurbine:" + e.getMessage());
				manageDb.removeTable4Ontology(TWIN_ACTIONS_TURBINE);
			}
		}
	}

	public void init_DigitalTwinPropertiesTurbine() {
		log.info("init TwinPropertiesTurbine for Digital Twin");
		/*
		 * db.createCollection("Logs");
		 */
		if (basicOps.count(TWIN_PROPERTIES_TURBINE) == 0) {
			try {
				log.info("No Collection Logs...");
				manageDb.createTable4Ontology(TWIN_PROPERTIES_TURBINE, "{}");
			} catch (final Exception e) {
				log.error("Error init_DigitalTwinPropertiesTurbine:" + e.getMessage());
				manageDb.removeTable4Ontology(TWIN_PROPERTIES_TURBINE);
			}
		}
	}

	public void init_DigitalTwinLogs() {
		log.info("init TwinLogs for Digital Twin");
		/*
		 * db.createCollection("Logs");
		 */
		if (basicOps.count(TWIN_LOGS) == 0) {
			try {
				log.info("No Collection Logs...");
				manageDb.createTable4Ontology(TWIN_LOGS, "{}");
			} catch (final Exception e) {
				log.error("Error init_DigitalTwinLogs:" + e.getMessage());
				manageDb.removeTable4Ontology(TWIN_LOGS);
			}
		}
	}

	public void init_DigitalTwinEvents() {
		log.info("init TwinEvents for Digital Twin");
		/*
		 * db.createCollection("Logs");
		 */
		if (basicOps.count(TWIN_EVENTS) == 0) {
			try {
				log.info("No Collection TwinEvents...");
				manageDb.createTable4Ontology(TWIN_EVENTS, "{}");
			} catch (final Exception e) {
				log.error("Error init_DigitalTwinEvents:" + e.getMessage());
				manageDb.removeTable4Ontology(TWIN_EVENTS);
			}
		}
	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					Charset.forName("UTF-8"));

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						Charset.forName("UTF-8"));
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

}
