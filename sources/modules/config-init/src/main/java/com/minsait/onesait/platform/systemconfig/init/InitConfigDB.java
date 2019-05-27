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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.BaseLayer;
import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.ConsoleMenu;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DeviceSimulation;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyCategory;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.BaseLayerRepository;
import com.minsait.onesait.platform.config.repository.ClientConnectionRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.ConsoleMenuRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DeviceSimulationRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyCategoryRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.configdb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitConfigDB {

	private static boolean started = false;
	private static User userDeveloper = null;
	private static User userAdministrator = null;
	private static User user = null;
	private static User userAnalytics = null;
	private static User userSysAdmin = null;
	private static User userPartner = null;
	private static User userOperation = null;
	private static Token tokenAdministrator = null;
	private static Ontology ontologyAdministrator = null;
	private static GadgetDatasource gadgetDatasourceDeveloper = null;

	@Autowired
	private InitConfigDB_DigitalTwin initDigitalTwin;

	@Autowired
	ClientConnectionRepository clientConnectionRepository;
	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	ConsoleMenuRepository consoleMenuRepository;
	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	DashboardRepository dashboardRepository;
	@Autowired
	GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	GadgetRepository gadgetRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	OntologyCategoryRepository ontologyCategoryRepository;

	@Autowired
	OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	TokenRepository tokenRepository;
	@Autowired
	UserRepository userCDBRepository;
	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	FlowDomainRepository domainRepository;

	@Autowired
	DigitalTwinTypeRepository digitalTwinTypeRepository;

	@Autowired
	DigitalTwinDeviceRepository digitalTwinDeviceRepository;

	@Autowired
	UserTokenRepository userTokenRepository;

	@Autowired
	MarketAssetRepository marketAssetRepository;

	@Autowired
	NotebookRepository notebookRepository;

	@Autowired
	PipelineRepository pipelineRepository;

	@Autowired
	DeviceSimulationRepository simulationRepository;

	@Autowired
	OntologyVirtualDatasourceRepository ontologyVirtualDataSourceRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppRepository appRepository;

	@Autowired
	private BaseLayerRepository baseLayerRepository;

	@Autowired
	private NotebookUserAccessTypeRepository notebookUserAccessTypeRepository;

	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;

			log.info("Start initConfigDB...");
			// first we need to create users
			init_RoleUser();
			log.info("OK init_RoleUser");
			init_User();
			log.info("OK init_User");
			//
			init_DataModel();
			log.info("OK init_DataModel");
			init_OntologyCategory();
			log.info("OK init_OntologyCategory");
			init_Ontology();
			log.info("OK init_Ontology");
			init_OntologyUserAccess();
			log.info("OK init_OntologyUserAccess");
			init_OntologyUserAccessType();
			log.info("OK init_OntologyUserAccessType");

			init_OntologyCategory();
			log.info("OK init_OntologyCategory");

			//
			init_ClientPlatform();
			log.info("OK init_ClientPlatform");
			init_ClientPlatformOntology();
			log.info("OK init_ClientPlatformOntology");
			init_ClientConnection();
			log.info("OK init_ClientConnection");
			//
			init_Token();
			log.info("OK init_Token");

			init_UserToken();
			log.info("OK USER_Token");

			init_GadgetDatasource();
			log.info("OK init_GadgetDatasource");
			init_Gadget();
			log.info("OK init_Gadget");
			init_GadgetMeasure();
			log.info("OK init_GadgetMeasure");

			init_Dashboard();
			log.info("OK init_Dashboard");
			init_DashboardConf();
			log.info("OK init_DashboardConf");
			init_DashboardUserAccessType();
			log.info("OK init_DashboardUserAccessType");

			init_Menu_ControlPanel();
			log.info("OK init_ConsoleMenu");
			init_Configuration();
			log.info("OK init_Configuration");

			init_FlowDomain();
			log.info("OK init_FlowDomain");

			initDigitalTwin.init_DigitalTwinType();
			log.info("OK init_DigitalTwinType");

			initDigitalTwin.init_DigitalTwinDevice();
			log.info("OK init_DigitalTwinDevice");

			init_MarketPlace();
			log.info("OK init_Market");

			init_notebook();
			log.info("OK init_Notebook");

			init_dataflow();
			log.info("OK init_dataflow");

			init_notebook_user_access_type();
			log.info("OK init_notebook_user_access_type");

			init_simulations();
			log.info("OK init_simulations");

			init_OpenFlight_Sample();
			log.info("OK init_openflight");

			init_BaseLayers();
			log.info("OK init_BaseLayers");

			init_QA_WindTurbines_Sample();
			log.info("OK init_QA_WindTurbines");

			// init_OntologyVirtualDatasource();
			// log.info(" OK init_OntologyVirtualDatasource");
			// init_realms();
		}

	}

	private void init_BaseLayers() {
		long count = baseLayerRepository.count();
		if (count == 0) {
			BaseLayer baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-1");
			baseLayer.setIdentification("osm.Mapnik.Labels");
			baseLayer.setName("Open Street Maps");
			baseLayer.setTechnology("cesium");
			baseLayer.setUrl("https://a.tile.openstreetmap.org/");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-2");
			baseLayer.setIdentification("esri.Topo.Labels");
			baseLayer.setName("ESRI World Topo Map");
			baseLayer.setTechnology("cesium");
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-3");
			baseLayer.setIdentification("esri.Streets.Labels");
			baseLayer.setName("ESRI World Street Map");
			baseLayer.setTechnology("cesium");
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-4");
			baseLayer.setIdentification("esri.Imagery.NoLabels");
			baseLayer.setName("ESRI Imagery");
			baseLayer.setTechnology("cesium");
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer");

			baseLayerRepository.save(baseLayer);
		}
	}

	private void init_realms() {

		final App app = new App();
		app.setAppId("GovConsole");
		app.setName("Governance console realm");
		app.setDescription("This is a realm provided for the governance console");
		AppRole role = new AppRole();
		role.setApp(app);
		role.setDescription("Front-end developer");
		role.setName("FRONT");
		app.getAppRoles().add(role);
		role = new AppRole();
		role.setApp(app);
		role.setDescription("Back-end developer");
		role.setName("BACK");
		app.getAppRoles().add(role);
		role = new AppRole();
		role.setApp(app);
		role.setDescription("Product owner");
		role.setName("P.O.");
		app.getAppRoles().add(role);
		role = new AppRole();
		role.setApp(app);
		role.setDescription("UX designer");
		role.setName("UX-UI");
		app.getAppRoles().add(role);
		role = new AppRole();
		role.setApp(app);
		role.setDescription("Devops CI/CD");
		role.setName("DEVOPS");
		app.getAppRoles().add(role);
		role = new AppRole();
		role.setApp(app);
		role.setDescription("Administrator of the console");
		role.setName("ADMIN");

		final AppUser admin = new AppUser();
		admin.setRole(role);
		admin.setUser(getUserAdministrator());
		// role.getAppUsers().add(admin);
		// app.getAppRoles().add(role);

		role = new AppRole();
		role.setApp(app);
		role.setDescription("User of the console");
		role.setName("USER");

		final AppUser user = new AppUser();
		user.setRole(role);
		user.setUser(getUser());
		// role.getAppUsers().add(user);
		// app.getAppRoles().add(role);

		app.setUserExtraFields(
				"{\"firstName\":\"string\",\"lastName\":\"string\",\"telephone\":\"string\",\"location\":{\"color\":\"string\",\"floor\":\"string\",\"place\":\"string\"}}");
		appRepository.save(app);

	}

	private void init_OntologyVirtualDatasource() {
		OntologyVirtualDatasource datasource = ontologyVirtualDataSourceRepository.findByDatasourceName("oracle4");
		if (datasource == null) {
			datasource = new OntologyVirtualDatasource();
			datasource.setConnectionString("jdbc:oracle:thin:@10.0.0.6:1521:XE");
			datasource.setCredentials("indra2013");
			datasource.setDatasourceName("oracle4");
			datasource.setId("MASTER-OntologyVirtualDatasource-1");
			datasource.setPoolSize("150");
			datasource.setQueryLimit(150);
			datasource.setSgdb(OntologyVirtualDatasource.VirtualDatasourceType.ORACLE);
			datasource.setUser("sys as sysdba");
			datasource.setUserId(getUserAdministrator());

			ontologyVirtualDataSourceRepository.save(datasource);

		}

	}

	private void init_simulations() {
		DeviceSimulation simulation = simulationRepository.findByIdentification("Issue generator");
		if (simulation == null) {
			simulation = new DeviceSimulation();
			simulation.setId("MASTER-DeviceSimulation-1");
			simulation.setActive(false);
			simulation.setCron("0/5 * * ? * * *");
			simulation.setIdentification("Issue generator");
			simulation.setInterval(5);
			simulation.setJson(loadFromResources("simulations/DeviceSimulation_example1.json"));
			simulation.setClientPlatform(clientPlatformRepository.findByIdentification("TicketingApp"));
			simulation.setOntology(ontologyRepository.findByIdentification("Ticket"));
			simulation.setToken(tokenRepository.findByClientPlatform(simulation.getClientPlatform()).get(0));
			simulation.setUser(getUserDeveloper());
			simulationRepository.save(simulation);
		}

	}

	public void init_OpenFlight_Sample() {
		init_Dashboard_OpenFlight();
		init_Gadget_OpenFlight();
		init_GadgetDatasource_OpenFlight();
		init_GadgetMeasure_OpenFlight();
		init_Ontology_OpenFlight();
	}

	public void init_QA_WindTurbines_Sample() {
		init_Dashboard_QA_WindTurbines();
		init_Gadget_QA_WindTurbines();
		init_GadgetDatasource_QA_WindTurbines();
		init_GadgetMeasure_QA_WindTurbines();
		init_Ontology_QA_WindTurbines();
	}

	private void init_FlowDomain() {
		log.info("init_FlowDomain");
		// Domain for administrator
		if (domainRepository.count() == 0) {
			FlowDomain domain = new FlowDomain();
			domain.setId("MASTER-FlowDomain-1");
			domain.setActive(true);
			domain.setIdentification("adminDomain");
			domain.setUser(userCDBRepository.findByUserId("administrator"));
			domain.setHome("/tmp/administrator");
			domain.setState("START");
			domain.setPort(8000);
			domain.setServicePort(7000);
			domainRepository.save(domain);
			// Domain for developer
			domain = new FlowDomain();
			domain.setId("MASTER-FlowDomain-2");
			domain.setActive(true);
			domain.setIdentification("devDomain");
			domain.setUser(userCDBRepository.findByUserId("developer"));
			domain.setHome("/tmp/developer");
			domain.setState("START");
			domain.setPort(8001);
			domain.setServicePort(7001);
			domainRepository.save(domain);
		}
	}

	private void init_Configuration() {
		log.info("init_Configuration");
		if (configurationRepository.count() == 0) {

			Configuration config = new Configuration();
			config = new Configuration();
			config.setId("MASTER-Configuration-1");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setEnvironment("dev");
			config.setYmlConfig(loadFromResources("TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-2");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setSuffix("lmgracia");
			config.setDescription("Twitter");
			config.setYmlConfig(loadFromResources("TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-3");
			config.setType(Configuration.Type.SCHEDULING);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("RtdbMaintainer config");
			config.setYmlConfig(loadFromResources("SchedulingConfiguration_default.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-4");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Endpoints default profile");
			config.setYmlConfig(loadFromResources("EndpointModulesConfigurationDefault.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-5");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment("docker");
			config.setDescription("Endpoints docker profile");
			config.setYmlConfig(loadFromResources("EndpointModulesConfigurationDocker.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-6");
			config.setType(Configuration.Type.MAIL);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("MailConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-7");
			config.setType(Configuration.Type.RTDB);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("RTDBConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-8");
			config.setType(Configuration.Type.MONITORING);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("MonitoringConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-9");
			config.setType(Configuration.Type.RANCHER);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Rancher configuration");
			config.setYmlConfig(loadFromResources("RancherConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-10");
			config.setType(Configuration.Type.OPENSHIFT);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Openshift configuration");
			config.setYmlConfig(loadFromResources("OpenshiftConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-11");
			config.setType(Configuration.Type.DOCKER);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Rancher docker compose configuration");
			config.setSuffix("Rancher");
			config.setYmlConfig(loadFromResources("DockerCompose_Rancher.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-12");
			config.setType(Configuration.Type.NGINX);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Nginx conf template");
			config.setSuffix("Nginx");
			config.setYmlConfig(loadFromResources("nginx-template.conf"));
			configurationRepository.save(config);
		}
		Configuration config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, "default");
		if (config == null) {
			config = new Configuration();
			config.setDescription("onesait Platform global configuration");
			config.setEnvironment("default");
			config.setId("MASTER-Configuration-13");
			config.setType(Type.OPEN_PLATFORM);
			config.setUser(getUserAdministrator());
			config.setYmlConfig(loadFromResources("OpenPlatformConfiguration.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, "docker");
		if (config == null) {
			config = new Configuration();
			config.setDescription("onesait Platform global configuration");
			config.setEnvironment("docker");
			config.setId("MASTER-Configuration-14");
			config.setType(Type.OPEN_PLATFORM);
			config.setUser(getUserAdministrator());
			config.setYmlConfig(loadFromResources("OpenPlatformConfiguration.yml"));
			configurationRepository.save(config);
		}

	}

	public void init_ClientConnection() {
		log.info("init ClientConnection");
		final List<ClientConnection> clients = clientConnectionRepository.findAll();
		final ClientPlatform cp = clientPlatformRepository.findAll().get(0);
		if (clients.isEmpty()) {
			log.info("No clients ...");

			final ClientConnection con = new ClientConnection();
			con.setId("MASTER-ClientConnection-1");
			con.setClientPlatform(cp);
			con.setIdentification("1");
			con.setIpStrict(true);
			con.setStaticIp(false);
			con.setLastIp("192.168.1.89");
			final Calendar date = Calendar.getInstance();
			con.setLastConnection(date);
			con.setClientPlatform(cp);
			clientConnectionRepository.save(con);
		}
	}

	public void init_ClientPlatformOntology() {

		log.info("init ClientPlatformOntology");
		final List<ClientPlatformOntology> cpos = clientPlatformOntologyRepository.findAll();
		if (cpos.isEmpty()) {
			if (clientPlatformRepository.findAll().isEmpty())
				throw new RuntimeException("There must be at least a ClientPlatform with id=1 created");
			if (ontologyRepository.findAll().isEmpty())
				throw new RuntimeException("There must be at least a Ontology with id=1 created");
			log.info("No Client Platform Ontologies");

			ClientPlatformOntology cpo = new ClientPlatformOntology();
			cpo.setId("MASTER-ClientPlatformOntology-1");
			cpo.setClientPlatform(clientPlatformRepository.findByIdentification("TicketingApp"));
			cpo.setOntology(ontologyRepository.findByIdentification("Ticket"));
			cpo.setAccesEnum(ClientPlatformOntology.AccessType.ALL);
			clientPlatformOntologyRepository.save(cpo);
			//
			cpo = new ClientPlatformOntology();
			cpo.setId("MASTER-ClientPlatformOntology-2");
			cpo.setClientPlatform(clientPlatformRepository.findByIdentification("GTKP-Example"));
			cpo.setOntology(ontologyRepository.findByIdentification("HelsinkiPopulation"));
			cpo.setAccesEnum(ClientPlatformOntology.AccessType.ALL);
			clientPlatformOntologyRepository.save(cpo);
		}
	}

	public void init_ClientPlatform() {
		log.info("init ClientPlatform");
		final List<ClientPlatform> clients = clientPlatformRepository.findAll();
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientPlatform client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-1");
			client.setUser(getUserDeveloper());
			client.setIdentification("Client-MasterData");
			client.setEncryptionKey("b37bf11c-631e-4bc4-ae44-910e58525952");
			client.setDescription("ClientPatform created as MasterData");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-2");
			client.setUser(getUserDeveloper());
			client.setIdentification("GTKP-Example");
			client.setEncryptionKey("f9dfe72e-7082-4fe8-ba37-3f569b30a691");
			client.setDescription("ClientPatform created as Example");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-3");
			client.setUser(getUserDeveloper());
			client.setIdentification("TicketingApp");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Platform client for issues and ticketing");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-4");
			client.setUser(getUserDeveloper());
			client.setIdentification("DeviceMaster");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Device template for testing");
			clientPlatformRepository.save(client);
		}

	}

	public void init_Menu_ControlPanel() {
		log.info("init ConsoleMenu");
		final List<ConsoleMenu> menus = consoleMenuRepository.findAll();

		if (!menus.isEmpty()) {
			consoleMenuRepository.deleteAll();
		}

		log.info("No menu elents found...adding");
		try {
			log.info("Adding menu for role ADMIN");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-1");
			menu.setJson(loadFromResources("menu/menu_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ADMIN");
		}
		try {
			log.info("Adding menu for role DEVELOPER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-2");
			menu.setJson(loadFromResources("menu/menu_developer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DEVELOPER");
		}
		try {
			log.info("Adding menu for role USER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-3");

			menu.setJson(loadFromResources("menu/menu_user.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_USER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role USER");
		}
		try {
			log.info("Adding menu for role ANALYTIC");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-4");
			menu.setJson(loadFromResources("menu/menu_analytic.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ANALYTIC");
		}
		try {
			log.info("Adding menu for role DATAVIEWER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-5");
			menu.setJson(loadFromResources("menu/menu_dataviewer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DATAVIEWER");
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

	private byte[] loadFileFromResources(String name) {
		try {
			final Resource resource = resourceLoader.getResource("classpath:" + name);
			final InputStream is = resource.getInputStream();
			return IOUtils.toByteArray(is);

		} catch (final Exception e) {
			log.error("Error loading resource: " + name + ".Please check if this error affect your database");
			log.error(e.getMessage());
			return null;
		}
	}

	public void init_DashboardConf() {
		log.info("init DashboardConf");
		final List<DashboardConf> dashboardsConf = dashboardConfRepository.findAll();
		if (dashboardsConf.isEmpty()) {
			log.info("No dashboardsConf...adding");
			// Default
			final DashboardConf dashboardConfDefault = new DashboardConf();
			final String defaultSchema = "{\"header\":{\"title\":\"My new onesait platform Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfDefault.setId("MASTER-DashboardConf-1");
			dashboardConfDefault.setIdentification("default");
			dashboardConfDefault.setModel(defaultSchema);
			dashboardConfDefault.setDescription("Default style");
			dashboardConfRepository.save(dashboardConfDefault);
			// Iframe
			final DashboardConf dashboardConfNoTitle = new DashboardConf();
			final String notitleSchema = "{\"header\":{\"title\":\"\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfNoTitle.setId("MASTER-DashboardConf-2");
			dashboardConfNoTitle.setIdentification("notitle");
			dashboardConfNoTitle.setModel(notitleSchema);
			dashboardConfNoTitle.setDescription("No title style");
			dashboardConfRepository.save(dashboardConfNoTitle);
		}
	}

	public void init_Dashboard() {
		log.info("init Dashboard");
		final List<Dashboard> dashboards = dashboardRepository.findAll();
		if (dashboards.isEmpty()) {
			log.info("No dashboards...adding");

			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-1");
			dashboard.setIdentification("TempDeveloperDashboard");
			dashboard.setDescription("Dashboard analytics restaurants");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(
					"{\"header\":{\"title\":\"My new s4c Dashboard\",\"enable\":true,\"height\":56,\"logo\":{\"height\":48},\"backgroundColor\":\"hsl(220, 23%, 20%)\",\"textColor\":\"hsl(0, 0%, 100%)\",\"iconColor\":\"hsl(0, 0%, 100%)\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{\"$$hashKey\":\"object:64\"},{\"x\":0,\"y\":0,\"cols\":20,\"rows\":7,\"id\":\""
							+ getGadget().getId()
							+ "\",\"content\":\"bar\",\"type\":\"bar\",\"header\":{\"enable\":true,\"title\":{\"icon\":\"\",\"iconColor\":\"hsl(220, 23%, 20%)\",\"text\":\"My Gadget\",\"textColor\":\"hsl(220, 23%, 20%)\"},\"backgroundColor\":\"hsl(0, 0%, 100%)\",\"height\":\"25\"},\"backgroundColor\":\"white\",\"padding\":0,\"border\":{\"color\":\"#c7c7c7de\",\"width\":1,\"radius\":5},\"$$hashKey\":\"object:107\"}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[],\"livehtml_1526292431685\":[],\"b163b6e4-a8d2-4c3b-b964-5efecf0dd3a0\":[]}}");
			dashboard.setPublic(true);
			dashboard.setUser(getUserDeveloper());

			dashboardRepository.save(dashboard);
		}
	}

	public void init_Dashboard_OpenFlight() {
		if (dashboardRepository.findById("MASTER-Dashboard-2") == null) {
			log.info("init Dashboard OpenFlight");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-2");
			dashboard.setIdentification("Visualize OpenFlights Data");
			dashboard.setDescription("Visualize OpenFlights Data example from notebook data");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/OpenFlight.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAnalytics());

			dashboardRepository.save(dashboard);
		}
	}

	public void init_Dashboard_QA_WindTurbines() {
		if (dashboardRepository.findById("MASTER-Dashboard-3") == null) {
			log.info("init Dashboard QA_WindTurbines");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-3");
			dashboard.setIdentification("QA_WindTurbines_dashboard");
			dashboard.setDescription("Dashboard to visualize data from QA_DETAIL");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/QA_WindTurbines.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAnalytics());

			dashboardRepository.save(dashboard);
		}
	}

	private Gadget getGadget() {
		final List<Gadget> gadgets = gadgetRepository.findAll();
		return gadgets.get(0);
	}

	private User getUserDeveloper() {
		if (userDeveloper == null)
			userDeveloper = userCDBRepository.findByUserId("developer");
		return userDeveloper;
	}

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	private User getUser() {
		if (user == null)
			user = userCDBRepository.findByUserId("user");
		return user;
	}

	private User getUserAnalytics() {
		if (userAnalytics == null)
			userAnalytics = userCDBRepository.findByUserId("analytics");
		return userAnalytics;
	}

	private User getUserPartner() {
		if (userPartner == null)
			userPartner = userCDBRepository.findByUserId("partner");
		return userPartner;
	}

	private User getUserSysAdmin() {
		if (userSysAdmin == null)
			userSysAdmin = userCDBRepository.findByUserId("sysadmin");
		return userSysAdmin;
	}

	private User getUserOperations() {
		if (userOperation == null)
			userOperation = userCDBRepository.findByUserId("operations");
		return userOperation;
	}

	private Token getTokenAdministrator() {
		if (tokenAdministrator == null)
			tokenAdministrator = tokenRepository.findByToken("acbca01b-da32-469e-945d-05bb6cd1552e");
		return tokenAdministrator;
	}

	private Ontology getOntologyAdministrator() {
		if (ontologyAdministrator == null)
			ontologyAdministrator = ontologyRepository.findByIdentification("OntologyMaster");
		return ontologyAdministrator;
	}

	private GadgetDatasource getGadgetDatasourceDeveloper() {
		if (gadgetDatasourceDeveloper == null)
			gadgetDatasourceDeveloper = gadgetDatasourceRepository.findAll().get(0);
		return gadgetDatasourceDeveloper;
	}

	public void init_DataModel() {

		log.info("init DataModel");
		final List<DataModel> dataModels = dataModelRepository.findAll();
		if (dataModels.isEmpty()) {
			log.info("No DataModels ...");
			DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-1");
			dataModel.setName("Alarm");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Alarm.json"));
			dataModel.setDescription("Base Alarm: assetId, timestamp, severity, source, details and status..");
			dataModel.setLabels("Alarm,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-2");
			dataModel.setName("Audit");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Audit.json"));
			dataModel.setDescription("Base Audit");
			dataModel.setLabels("Audit,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-3");
			dataModel.setName("DeviceLog");
			dataModel.setTypeEnum(DataModel.MainType.SYSTEM_ONTOLOGY);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_DeviceLog.json"));
			dataModel.setDescription("Data model for device logging");
			dataModel.setLabels("General,IoT,Log");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-4");
			dataModel.setName("Device");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Device.json"));
			dataModel.setDescription("Base Device");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-5");
			dataModel.setName("EmptyBase");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_EmptyBase.json"));
			dataModel.setDescription("Base DataModel");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-6");
			dataModel.setName("Feed");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Feed.json"));
			dataModel.setDescription("Base Feed");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-7");
			dataModel.setName("Twitter");
			dataModel.setTypeEnum(DataModel.MainType.SOCIAL_MEDIA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Twitter.json"));
			dataModel.setDescription("Twitter DataModel");
			dataModel.setLabels("Twitter,Social Media");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-8");
			dataModel.setName("BasicSensor");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_BasicSensor.json"));
			dataModel.setDescription("DataModel for sensor sending measures for an assetId");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-9");
			dataModel.setName("GSMA-AirQualityObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityObserved.json"));
			dataModel.setDescription("An observation of air quality conditions at a certain place and time");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-10");
			dataModel.setName("GSMA-AirQualityStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityStation.json"));
			dataModel.setDescription("Air Quality Station observing quality conditions at a certain place and time");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-11");
			dataModel.setName("GSMA-AirQualityThreshold");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityThreshold.json"));
			dataModel.setDescription(
					"Provides the air quality thresholds in Europe. Air quality thresholds allow to calculate an air quality index (AQI).");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-12");
			dataModel.setName("GSMA-Device");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Device.json"));
			dataModel.setDescription(
					"A Device is a tangible object which contains some logic and is producer and/or consumer of data. A Device is always assumed to be capable of communicating electronically via a network.");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-13");
			dataModel.setName("GSMA-KPI");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-KPI.json"));
			dataModel.setDescription(
					"Key Performance Indicator (KPI) is a type of performance measurement. KPIs evaluate the success of an organization or of a particular activity in which it engages.");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-14");
			dataModel.setName("GSMA-OffstreetParking");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-OffstreetParking.json"));
			dataModel.setDescription(
					"A site, off street, intended to park vehicles, managed independently and with suitable and clearly marked access points (entrances and exits).");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-15");
			dataModel.setName("GSMA-Road");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Road.json"));
			dataModel.setDescription("Contains a harmonised geographic and contextual description of a road.");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-16");
			dataModel.setName("GSMA-StreetLight");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-StreetLight.json"));
			dataModel.setDescription("GSMA Model that represents an urban streetlight");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-17");
			dataModel.setName("GSMA-Vehicle");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Vehicle.json"));
			dataModel.setDescription("A harmonised description of a Vehicle");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-18");
			dataModel.setName("GSMA-WasteContainer");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WasteContainer.json"));
			dataModel.setDescription("GSMA WasteContainer");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-19");
			dataModel.setName("GSMA-WeatherObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherObserved.json"));
			dataModel.setDescription("An observation of weather conditions at a certain place and time.");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-20");
			dataModel.setName("GSMA-WeatherStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherStation.json"));
			dataModel.setDescription("GSMA Weather Station Model");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-21");
			dataModel.setName("Request");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Request.json"));
			dataModel.setDescription("Request for something.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-22");
			dataModel.setName("Response");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Response.json"));
			dataModel.setDescription("Response for a request.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-23");
			dataModel.setName("MobileElement");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_MobileElement.json"));
			dataModel.setDescription("Generic Mobile Element representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-24");
			dataModel.setName("Log");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Log.json"));
			dataModel.setDescription("Log representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-25");
			dataModel.setName("Issue");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Issue.json"));
			dataModel.setDescription("Issue representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-26");
			dataModel.setName("AuditPlatform");
			dataModel.setTypeEnum(DataModel.MainType.SYSTEM_ONTOLOGY);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_AuditPlatform.json"));
			dataModel.setDescription("System Ontology. Auditory of operations between user and Platform.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-27");
			dataModel.setName("VideoResult");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_VideoResult.json"));
			dataModel.setDescription("Ontology for Video Broker Processor results.");
			dataModel.setLabels("video,processing,iot,ocr,yolo,analytics");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}
	}

	public void init_Gadget() {
		log.info("init Gadget");
		final List<Gadget> gadgets = gadgetRepository.findAll();
		if (gadgets.isEmpty()) {
			log.info("No gadgets ...");

			Gadget gadget = new Gadget();
			gadget.setId("MASTER-Gadget-1");
			gadget.setIdentification("My Gadget");
			gadget.setPublic(false);
			gadget.setDescription("gadget cousin score");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true}}]}}");
			gadget.setUser(getUserDeveloper());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-2");
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("table");
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"5\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#141414\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-3");
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-4");
			gadget.setIdentification("countriesAsDestinationMap");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("map");
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-5");
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-6");
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-7");
			gadget.setIdentification("routesDestTop");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-8");
			gadget.setIdentification("routesOriginTop");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void init_Gadget_OpenFlight() {

		Gadget gadget = new Gadget();

		if (gadgetRepository.findById("MASTER-Gadget-2") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-2");
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("table");
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"5\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#141414\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-3") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-3");
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-4") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-4");
			gadget.setIdentification("countriesAsDestinationMap");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("map");
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-5") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-5");
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-6") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-6");
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-7") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-7");
			gadget.setIdentification("routesDestTop");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-8") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-8");
			gadget.setIdentification("routesOriginTop");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void init_Gadget_QA_WindTurbines() {

		Gadget gadget = new Gadget();

		if (gadgetRepository.findById("MASTER-Gadget-9") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-9");
			gadget.setIdentification("producertbl");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("table");
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"30\",\"trHeightBody\":\"30\",\"trHeightFooter\":\"30\",\"textColorTHead\":\"#555555\",\"textColorBody\":\"#555555\",\"textColorFooter\":\"#555555\"},\"options\":{\"rowSelection\":true,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-10") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-10");
			gadget.setIdentification("producer_errorCat");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("mixed");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Error category\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-11") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-11");
			gadget.setIdentification("trend_errorCat");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("line");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Date\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-12") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-12");
			gadget.setIdentification("errorsBySite");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Site\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-13") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-13");
			gadget.setIdentification("producer_errorType");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("mixed");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Error code\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-14") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-14");
			gadget.setIdentification("trend_errorType");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("line");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Date\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById("MASTER-Gadget-15") == null) {
			gadget = new Gadget();
			gadget.setId("MASTER-Gadget-15");
			gadget.setIdentification("tableerrordetail");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("table");
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"30\",\"trHeightBody\":\"30\",\"trHeightFooter\":\"30\",\"textColorTHead\":\"#000000\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void init_GadgetDatasource() {

		log.info("init GadgetDatasource");
		final List<GadgetDatasource> gadgetDatasource = gadgetDatasourceRepository.findAll();
		if (gadgetDatasource.isEmpty()) {
			log.info("No gadget querys ...");

			final GadgetDatasource gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-1");
			gadgetDatasources.setIdentification("DsRawRestaurants");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from Restaurants");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(150);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);

		}

	}

	public void init_GadgetDatasource_OpenFlight() {
		GadgetDatasource gadgetDatasources = new GadgetDatasource();

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-2") == null) {
			gadgetDatasources.setId("MASTER-GadgetDatasource-2");
			gadgetDatasources.setIdentification("routesOriginTop");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select r.routes.src as src,count(r) as count from routes as r group by r.routes.src order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(20);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-3") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-3");
			gadgetDatasources.setIdentification("routesDestTop");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select r.routes.dest as dest,count(r) as count from routes as r group by r.routes.dest order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(20);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-4") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-4");
			gadgetDatasources.setIdentification("countriesAsDestination");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select re.routesexten.countrysrc,re.routesexten.countrydest,count(re) as count from routesexten As re group by re.routesexten.countrysrc,re.routesexten.countrydest order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-5") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-5");
			gadgetDatasources.setIdentification("countriesAsDestinationMap");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select re.countrysrc,re.countrydest,re.count, iso.ISO3166.latitude , iso.ISO3166.longitude from(\r\nselect routesexten.routesexten.countrysrc As countrysrc, routesexten.routesexten.countrydest As countrydest, count(re.routesexten.countrysrc) As count from routesexten group by routesexten.routesexten.countrysrc, routesexten.routesexten.countrydest order by count desc) As re inner join ISO3166_1 As iso on re.countrydest = iso.ISO3166.name group by re.countrysrc, re.countrydest order by re.count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(500);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-6") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-6");
			gadgetDatasources.setIdentification("airportsCountByCountryTop10");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select airp.airportsdata.country, count(airp.airportsdata.country) AS count from airportsdata AS airp group by airp.airportsdata.country order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-7") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-7");
			gadgetDatasources.setIdentification("airportsCountByCountry");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select airp.airportsdata.country as acountry, count(*) AS count from airportsdata AS airp group by airp.airportsdata.country");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(300);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-8") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-8");
			gadgetDatasources.setIdentification("distinctCountries");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select distinct routesexten.routesexten.countrysrc as country from routesexten order by country");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(500);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}
	}

	public void init_GadgetDatasource_QA_WindTurbines() {
		GadgetDatasource gadgetDatasources = new GadgetDatasource();

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9") == null) {
			gadgetDatasources.setId("MASTER-GadgetDatasource-9");
			gadgetDatasources.setIdentification("QA_overview");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from QA_OVERVIEW");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-10") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-10");
			gadgetDatasources.setIdentification("producer_errorCat");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select nameCat1,nameCat2,nameCat3, process_date,idAdaptador, totalLoaded, structural, integrity, business,\" Ok\" as refCat from Producer_ErrorCat");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-11") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-11");
			gadgetDatasources.setIdentification("trend_errorCat");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from errorsOnDate");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-12") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-12");
			gadgetDatasources.setIdentification("errorsBySite");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select qa.process_date, qa.idAdaptador, qa.errors, (qa.errors*0.15 - qa.errors*0.15%1) as meteor, (qa.errors*0.16 - qa.errors*0.16%1) as forecast, site.name from ((select process_date,idAdaptador,siteCode,count(*) as errors from QA_DETAIL group by process_date,idAdaptador,siteCode) as qa inner join SITES as site on site.siteCode = qa.siteCode)");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-13");
			gadgetDatasources.setIdentification("producer_errorType");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery(
					"select '100' as nameType0, '101' as nameType1, '102' as nameType2,  '103' as nameType3, '104' as nameType4, '105' as nameType5,  '106' as nameType6, '107' as nameType7, '108' as nameType8,  '109' as nameType9, '110' as nameType10,  process_date,idAdaptador,sum(CASE errorCode WHEN 100 THEN 1 ELSE 0 END) as e100,sum(CASE errorCode WHEN 101 THEN 1 ELSE 0 END) as e101,sum(CASE errorCode WHEN 102 THEN 1 ELSE 0 END) as e102,sum(CASE errorCode WHEN 103 THEN 1 ELSE 0 END) as e103,sum(CASE errorCode WHEN 104 THEN 1 ELSE 0 END) as e104,sum(CASE errorCode WHEN 105 THEN 1 ELSE 0 END) as e105,sum(CASE errorCode WHEN 106 THEN 1 ELSE 0 END) as e106,sum(CASE errorCode WHEN 107 THEN 1 ELSE 0 END) as e107,sum(CASE errorCode WHEN 108 THEN 1 ELSE 0 END) as e108,sum(CASE errorCode WHEN 109 THEN 1 ELSE 0 END) as e109,sum(CASE errorCode WHEN 110 THEN 1 ELSE 0 END) as e110 from QA_DETAIL group by process_date,idAdaptador");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(1000);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-14");
			gadgetDatasources.setIdentification("trend_errorType");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from errorsTypeOnDate");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId("MASTER-GadgetDatasource-15");
			gadgetDatasources.setIdentification("listerroraxpo");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from QA_DETAIL_EXTENDED");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(2000);
			gadgetDatasources.setConfig(
					"[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}
	}

	public void init_GadgetMeasure() {
		log.info("init GadgetMeasure");
		final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository.findAll();
		if (gadgetMeasures.isEmpty()) {
			log.info("No gadget measures ...");

			final GadgetMeasure gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-1");
			gadgetMeasure.setDatasource(getGadgetDatasourceDeveloper());
			gadgetMeasure.setConfig(
					"{\"fields\":[\"Restaurant.cuisine\",\"Restaurant.grades[0].score\"],\"name\":\"score\",\"config\":{\"backgroundColor\":\"#000000\",\"borderColor\":\"#000000\",\"pointBackgroundColor\":\"#000000\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(getGadget());
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void init_GadgetMeasure_OpenFlight() {
		GadgetMeasure gadgetMeasure;

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-2").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-2");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-7"));
			gadgetMeasure.setConfig("{\"fields\":[\"count\"],\"name\":\"Country\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-2"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-3").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-3");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-7"));
			gadgetMeasure.setConfig("{\"fields\":[\"acountry\"],\"name\":\"Number of Airports\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-2"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-4").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-4");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-6"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"country\",\"count\"],\"name\":\"Top 10 Countries By Airports\",\"config\":{\"backgroundColor\":\"#2d60b5\",\"borderColor\":\"#2d60b5\",\"pointBackgroundColor\":\"#2d60b5\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-3"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-5").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-5");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-5"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"latitude\",\"longitude\",\"countrydest\",\"countrydest\",\"count\"],\"name\":\"\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-4"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-6").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-6");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-4"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrydest\",\"count\"],\"name\":\"Top Country Destinations\",\"config\":{\"backgroundColor\":\"#e8cb6a\",\"borderColor\":\"#e8cb6a\",\"pointBackgroundColor\":\"#e8cb6a\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-5"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-7").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-7");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-4"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrysrc\",\"count\"],\"name\":\"Top Country Origins\",\"config\":{\"backgroundColor\":\"#879dda\",\"borderColor\":\"#879dda\",\"pointBackgroundColor\":\"#879dda\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-6"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-8").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-8");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-3"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dest\",\"count\"],\"name\":\"Top Destination Airports\",\"config\":{\"backgroundColor\":\"#4e851b\",\"borderColor\":\"#4e851b\",\"pointBackgroundColor\":\"#4e851b\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-7"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-9").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-9");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-2"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"src\",\"count\"],\"name\":\"Top Origin Airports\",\"config\":{\"backgroundColor\":\"#b02828\",\"borderColor\":\"#b02828\",\"pointBackgroundColor\":\"#b02828\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-8"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void init_GadgetMeasure_QA_WindTurbines() {
		GadgetMeasure gadgetMeasure;

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-10").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-10");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"idAdaptador\"],\"name\":\"Producer\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-9"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-11").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-11");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dataLost\"],\"name\":\"Missed Data (%)\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-9"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-12").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-12");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"bad\"],\"name\":\"Wrong Records\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-9"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-13").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-13");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"good\"],\"name\":\"Right Records\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-9"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-14").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-14");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-9"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"totalLoaded\"],\"name\":\"Data Loaded\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-9"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-15").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-15");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-10"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat1\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-10"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-16").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-16");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-10"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat2\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-10"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-17").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-17");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-10"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat3\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-10"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-18").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-18");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-10"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"refCat\",\"totalLoaded\"],\"name\":\" Ok \",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-10"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-19").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-19");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-11"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-11"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-20").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-20");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-11"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"totalLoaded\"],\"name\":\"Ok\",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-11"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-21").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-21");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-11"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-11"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-22").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-22");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-11"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-11"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-23").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-23");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-12"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"forecast\"],\"name\":\"Production Forecast\",\"config\":{\"backgroundColor\":\"rgba(223,94,255,0.62)\",\"borderColor\":\"rgba(223,94,255,0.62)\",\"pointBackgroundColor\":\"rgba(223,94,255,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-12"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-24").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-24");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-12"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"errors\"],\"name\":\"WTG\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-12"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-25").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-25");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-12"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"meteor\"],\"name\":\"Meter\",\"config\":{\"backgroundColor\":\"rgba(17,245,149,0.62)\",\"borderColor\":\"rgba(17,245,149,0.62)\",\"pointBackgroundColor\":\"rgba(17,245,149,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-12"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-26").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-26");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType2\",\"e102\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-27").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-27");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType5\",\"e105\"],\"name\":\"105 Invalid numeric format \",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-28").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-28");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType9\",\"e109\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-29").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-29");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType4\",\"e104\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-30").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-30");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType3\",\"e103\"],\"name\":\"103 Mandatory fields \",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-31").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-31");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType6\",\"e106\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-32").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-32");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType0\",\"e100\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-33").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-33");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType10\",\"e110\"],\"name\":\"110 Decimal precision \",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-34").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-34");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType8\",\"e108\"],\"name\":\"108 Out of bounds sup \",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-35").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-35");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType7\",\"e107\"],\"name\":\"107 Out of bounds inf \",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-36").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-36");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-13"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType1\",\"e101\"],\"name\":\"101 Max null values per hour\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-13"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-37").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-37");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"duplicated\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-38").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-38");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"decimalPrecision\"],\"name\":\"110 Decimal precision\",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-39").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-39");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"numericFormat\"],\"name\":\"105 Invalid numeric format\",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-40").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-40");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"nullValues\"],\"name\":\"101 Null values\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-41").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-41");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"dateFormat\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-42").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-42");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-43").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-43");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"mandatoryFields\"],\"name\":\"103 Mandatory fields\",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-44").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-44");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"raw\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-45").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-45");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsInf\"],\"name\":\"107 Out of bounds inf\",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-46").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-46");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"frozenData\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.72)\",\"borderColor\":\"rgba(0,108,168,0.72)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.72)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.72)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-47").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-47");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-14"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsSup\"],\"name\":\"108 Out of bounds sup\",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-14"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-48").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-48");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorCategory\"],\"name\":\"Error Category\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-49").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-49");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"raw\"],\"name\":\"Original raw content\",\"config\":{\"position\":\"5\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-50").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-50");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"assetName\"],\"name\":\"WTG Name\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-51").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-51");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"siteName\"],\"name\":\"Site Name\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-52").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-52");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorDescription\"],\"name\":\"Error Description\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-53").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-53");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById("MASTER-GadgetDatasource-15"));
			gadgetMeasure
					.setConfig("{\"fields\":[\"timestamp\"],\"name\":\"Timestamp\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById("MASTER-Gadget-15"));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void init_OntologyCategory() {

		log.info("init OntologyCategory");
		final List<OntologyCategory> categories = ontologyCategoryRepository.findAll();
		if (categories.isEmpty()) {
			log.info("No ontology categories found..adding");
			final OntologyCategory category = new OntologyCategory();
			category.setId(1);
			category.setIdentificator("ontologias_categoria_cultura");
			category.setDescription("ontologias_categoria_cultura_desc");
			ontologyCategoryRepository.save(category);
		}

	}

	public void init_Ontology() {

		log.info("init Ontology");
		List<DataModel> dataModels;

		log.info("No ontologies..adding");
		Ontology ontology = new Ontology();

		if (ontologyRepository.findByIdentification("OntologyMaster") == null) {
			ontology.setId("MASTER-Ontology-1");
			ontology.setDataModel(dataModelRepository.findByName("EmptyBase").get(0));
			ontology.setJsonSchema(dataModelRepository.findByName("EmptyBase").get(0).getJsonSchema());
			ontology.setIdentification("OntologyMaster");
			ontology.setDescription("Ontology created as Master Data");
			ontology.setMetainf("OntologyMaster");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification("Ticket") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-2");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Ticket.json"));
			ontology.setDescription("Ontology created for Ticketing");
			ontology.setIdentification("Ticket");
			ontology.setMetainf("Ticket");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);

			ontology.setDataModel(dataModelRepository.findByName("EmptyBase").get(0));
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification("ContPerf") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-3");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ContPerf.json"));
			ontology.setDescription("Ontology created for performance testing");
			ontology.setIdentification("ContPerf");
			ontology.setMetainf("ContPerf");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);

			ontology.setDataModel(dataModelRepository.findByName("EmptyBase").get(0));
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification("HelsinkiPopulation") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-4");
			// ontology.setJsonSchema(loadFromResources("examples/OntologySchema_HelsinkiPopulation.json"));
			ontology.setJsonSchema(loadFromResources("examples/HelsinkiPopulation-schema.json"));
			ontology.setDescription("Ontology HelsinkiPopulation for testing");
			ontology.setIdentification("HelsinkiPopulation");
			ontology.setMetainf("HelsinkiPopulation");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

		}
		if (ontologyRepository.findByIdentification("TweetSentiment") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-5");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_TweetSentiment.json"));
			ontology.setDescription("TweetSentiment");
			ontology.setIdentification("TweetSentiment");
			ontology.setMetainf("TweetSentiment");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("GeoAirQuality") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-6");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_GeoAirQuality.json"));
			ontology.setDescription("Air quality retrieved from https://api.waqi.info/search");
			ontology.setIdentification("GeoAirQuality");
			ontology.setMetainf("GeoAirQuality");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("CityPopulation") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-7");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_CityPopulation.json"));
			ontology.setDescription(
					"Population of Urban Agglomerations with 300,000 Inhabitants or More in 2014, by Country, 1950-2030 (thousands)");
			ontology.setIdentification("CityPopulation");
			ontology.setMetainf("CityPopulation");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("AirQuality_gr2") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-8");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality_gr2.json"));
			ontology.setDescription("AirQuality_gr2");
			ontology.setIdentification("AirQuality_gr2");
			ontology.setMetainf("AirQuality_gr2");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("AirQuality") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-9");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality.json"));
			ontology.setDescription("AirQuality");
			ontology.setIdentification("AirQuality");
			ontology.setMetainf("AirQuality");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("AirCOMeter") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-10");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirCOMeter.json"));
			ontology.setDescription("AirCOMeter");
			ontology.setIdentification("AirCOMeter");
			ontology.setMetainf("AirCOMeter");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("TwinPropertiesTurbine") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-11");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Turbine.json"));
			ontology.setDescription("Digital Twin Shadow");
			ontology.setIdentification("TwinPropertiesTurbine");
			ontology.setMetainf("TwinPropertiesTurbine");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(RtdbDatasource.DIGITAL_TWIN);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("TwinPropertiesSensehat") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-16");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_SenseHat.json"));
			ontology.setDescription("Digital Twin Shadow");
			ontology.setIdentification("TwinPropertiesSensehat");
			ontology.setMetainf("TwinPropertiesSensehat");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(RtdbDatasource.DIGITAL_TWIN);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("NativeNotifKeys") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-25");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_NativeNotifKeys.json"));
			ontology.setDescription("Contains user tokens from end-devices connected to the notification system");
			ontology.setIdentification("NativeNotifKeys");
			ontology.setMetainf("notifications");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification("notificationMessage") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-26");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_notificationMessage.json"));
			ontology.setDescription("Ontology to store outbound notification messages");
			ontology.setIdentification("notificationMessage");
			ontology.setMetainf("notifications");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	public void init_Ontology_OpenFlight() {
		Ontology ontology;
		List<DataModel> dataModels;

		if (ontologyRepository.findByIdentification("routes") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-12");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_routes.json"));
			ontology.setDescription("Ontology for notebook-dashboard example");
			ontology.setIdentification("routes");
			ontology.setMetainf("routes");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("routesexten") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-13");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_routesexten.json"));
			ontology.setDescription("Ontology for notebook-dashboard example");
			ontology.setIdentification("routesexten");
			ontology.setMetainf("routesexten");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("ISO3166_1") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-14");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ISO3166_1.json"));
			ontology.setDescription("Ontology defining the standard alpha codes and number for all the countries");
			ontology.setIdentification("ISO3166_1");
			ontology.setMetainf("ISO3166_1");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("ISO3166_2") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-17");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ISO3166_2.json"));
			ontology.setDescription("Ontology defining the standard alpha codes for provincess");
			ontology.setIdentification("ISO3166_2");
			ontology.setMetainf("ISO3166_2");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("airportsdata") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-15");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_airportsdata.json"));
			ontology.setDescription("Ontology for notebook-dashboard example");
			ontology.setIdentification("airportsdata");
			ontology.setMetainf("airportsdata");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	public void init_Ontology_QA_WindTurbines() {
		Ontology ontology;
		List<DataModel> dataModels;

		if (ontologyRepository.findByIdentification("QA_OVERVIEW") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-18");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_OVERVIEW.json"));
			ontology.setDescription("QA_OVERVIEW DM");
			ontology.setIdentification("QA_OVERVIEW");
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("Producer_ErrorCat") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-19");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Producer_ErrorCat.json"));
			ontology.setDescription("Producer_ErrorCat desc");
			ontology.setIdentification("Producer_ErrorCat");
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("errorsOnDate") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-20");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_errorsOnDate.json"));
			ontology.setDescription("Different errors clasified by category and date");
			ontology.setIdentification("errorsOnDate");
			ontology.setMetainf("error,category");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("QA_DETAIL") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-21");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_DETAIL.json"));
			ontology.setDescription("Detail about quality");
			ontology.setIdentification("QA_DETAIL");
			ontology.setMetainf("QA,detail");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("errorsTypeOnDate") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-22");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_errorsTypeOnDate.json"));
			ontology.setDescription("Different errors clasified by error type and date");
			ontology.setIdentification("errorsTypeOnDate");
			ontology.setMetainf("error,type");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("QA_DETAIL_EXTENDED") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-23");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_DETAIL_EXTENDED.json"));
			ontology.setDescription("A version of QA_DETAIL with assets and sites names");
			ontology.setIdentification("QA_DETAIL_EXTENDED");
			ontology.setMetainf("qa,detail,extended");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("SITES") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-24");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_SITES.json"));
			ontology.setDescription("Info about a wind park");
			ontology.setIdentification("SITES");
			ontology.setMetainf("site,wind,park");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	public void init_OntologyUserAccess() {
		log.info("init OntologyUserAccess");
		/*
		 * List<OntologyUserAccess> users=this.ontologyUserAccessRepository.findAll();
		 * if(users.isEmpty()) { log.info("No users found...adding"); OntologyUserAccess
		 * user=new OntologyUserAccess(); user.setUser("6");
		 * user.setOntology(ontologyRepository.findAll().get(0));
		 * user.setOntologyUserAccessTypeId(ontologyUserAccessTypeId);
		 * this.ontologyUserAccessRepository.save(user); }
		 */
	}

	public void init_OntologyUserAccessType() {

		log.info("init OntologyUserAccessType");
		final List<OntologyUserAccessType> types = ontologyUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			OntologyUserAccessType type = new OntologyUserAccessType();
			type.setId(1);
			type.setName("ALL");
			type.setDescription("Todos los permisos");
			ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId(2);
			type.setName("QUERY");
			type.setDescription("Todos los permisos");
			ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId(3);
			type.setName("INSERT");
			type.setDescription("Todos los permisos");
			ontologyUserAccessTypeRepository.save(type);
		}

	}

	public void init_DashboardUserAccessType() {

		log.info("init DashboardUserAccessType");
		final List<DashboardUserAccessType> types = dashboardUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			DashboardUserAccessType type = new DashboardUserAccessType();
			type.setId(1);
			type.setName("EDIT");
			type.setDescription("view and edit access");
			dashboardUserAccessTypeRepository.save(type);
			type = new DashboardUserAccessType();
			type.setId(2);
			type.setName("VIEW");
			type.setDescription("view access");
			dashboardUserAccessTypeRepository.save(type);

		}

	}

	public void init_RoleUser() {
		log.info("init init_RoleUser");
		final List<Role> types = roleRepository.findAll();
		if (types.isEmpty()) {
			try {

				log.info("No roles en tabla.Adding...");
				Role type = new Role();
				type.setIdEnum(Role.Type.ROLE_ADMINISTRATOR);
				type.setName("Administrator");
				type.setDescription("Administrator of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVELOPER);
				type.setName("Developer");
				type.setDescription("Advanced User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_USER);
				type.setName("User");
				type.setDescription("Basic User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATASCIENTIST);
				type.setName("Analytics");
				type.setDescription("Analytics User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_PARTNER);
				type.setName("Partner");
				type.setDescription("Partner in the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_SYS_ADMIN);
				type.setName("SysAdmin");
				type.setDescription("System Administradot of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_OPERATIONS);
				type.setName("Operations");
				type.setDescription("Operations for the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVOPS);
				type.setName("DevOps");
				type.setDescription("DevOps for the Platform");
				roleRepository.save(type);
				//
				// UPDATE of the ROLE_ANALYTICS
				final Role typeSon = roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString());
				final Role typeParent = roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString());
				typeSon.setRoleParent(typeParent);
				roleRepository.save(typeSon);

				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATAVIEWER);
				type.setName("DataViewer");
				type.setDescription("DataViewer User of the Platform");
				roleRepository.save(type);

			} catch (final Exception e) {
				log.error("Error initRoleType:" + e.getMessage());
				roleRepository.deleteAll();
				throw new RuntimeException("Error creating Roles...Stopping");
			}

		}
	}

	public void init_Token() {
		log.info("init token");
		final List<Token> tokens = tokenRepository.findAll();
		if (tokens.isEmpty()) {
			log.info("No Tokens, adding ...");
			if (clientPlatformRepository.findAll().isEmpty())
				throw new RuntimeException("You need to create ClientPlatform before Token");

			ClientPlatform client = clientPlatformRepository.findByIdentification("TicketingApp");
			final Set<Token> hashSetTokens = new HashSet<Token>();

			Token token = new Token();
			token.setId("MASTER-Token-1");
			token.setClientPlatform(client);
			token.setToken("e7ef0742d09d4de5a3687f0cfdf7f626");
			token.setActive(true);
			hashSetTokens.add(token);
			client.setTokens(hashSetTokens);
			tokenRepository.save(token);

			client = clientPlatformRepository.findByIdentification("DeviceMaster");
			token = new Token();
			token.setId("MASTER-Token-2");
			token.setClientPlatform(client);
			token.setToken("a16b9e7367734f04bc720e981fcf483f");
			tokenRepository.save(token);

			client = clientPlatformRepository.findByIdentification("GTKP-Example");
			token = new Token();
			token.setId("MASTER-Token-3");
			token.setClientPlatform(client);
			token.setToken("690662b750274c8ba8748d7d55e9db5b");
			tokenRepository.save(token);
		}

	}

	public void init_UserToken() {
		log.info("init user token");
		final List<UserToken> tokens = userTokenRepository.findAll();
		if (tokens.isEmpty()) {
			final List<User> userList = userCDBRepository.findAll();
			int i = 1;
			for (final Iterator<User> iterator = userList.iterator(); iterator.hasNext(); i++) {
				final User user = iterator.next();
				final UserToken userToken = new UserToken();
				userToken.setId("MASTER-UserToken-" + i);
				userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
				userToken.setUser(user);
				userToken.setCreatedAt(Calendar.getInstance().getTime());
				try {
					userTokenRepository.save(userToken);
				} catch (final Exception e) {
					log.info("Could not create user token for user " + user.getUserId());
				}
			}
		}
	}

	public void init_User() {
		log.info("init UserCDB");
		final List<User> types = userCDBRepository.findAll();
		User type = null;
		if (types.isEmpty()) {
			try {
				log.info("No types en tabla.Adding...");
				type = new User();
				type.setUserId("administrator");
				type.setPassword("Changed!");
				type.setFullName("A Administrator of the Platform");
				type.setEmail("administrator@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("developer");
				type.setPassword("Changed!");
				type.setFullName("A Developer of the Platform.");
				type.setEmail("developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_developer");
				type.setPassword("changeIt!");
				type.setFullName("Demo Developer of the Platform");
				type.setEmail("demo_developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("user");
				type.setPassword("changeIt!");
				type.setFullName("Generic User of the Platform");
				type.setEmail("user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_user");
				type.setPassword("changeIt!");
				type.setFullName("Demo User of the Platform");
				type.setEmail("demo_user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("analytics");
				type.setPassword("changeIt!");
				type.setFullName("Generic Analytics User of the Platform");
				type.setEmail("analytics@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("partner");
				type.setPassword("changeIt!");
				type.setFullName("Generic Partner of the Platform");
				type.setEmail("partner@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_PARTNER.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("sysadmin");
				type.setPassword("changeIt!");
				type.setFullName("Generic SysAdmin of the Platform");
				type.setEmail("sysadmin@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_SYS_ADMIN.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("operations");
				type.setPassword("changeIt!");
				type.setFullName("Operations of the Platform");
				type.setEmail("operations@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_OPERATIONS.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("dataviewer");
				type.setPassword("changeIt!");
				type.setFullName("DataViewer User of the Platform");
				type.setEmail("dataviewer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
				userCDBRepository.save(type);
				//
			} catch (final Exception e) {
				log.error("Error UserCDB:" + e.getMessage());
				userCDBRepository.deleteAll();
				throw new RuntimeException("Error creating users...ignoring creation rest of Tables");
			}
		}
	}

	public void init_MarketPlace() {
		log.info("init MarketPlace");
		final List<MarketAsset> marketAssets = marketAssetRepository.findAll();
		if (marketAssets.isEmpty()) {
			log.info("No market Assets...adding");
			MarketAsset marketAsset = new MarketAsset();
			// Getting Started Guide
			marketAsset.setId("MASTER-MarketAsset-1");
			marketAsset.setIdentification("GettingStartedGuide");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/GettingStartedGuide.json"));
			marketAsset.setImage(loadFileFromResources("market/img/asset.jpg"));
			marketAsset.setImageType("jpg");
			marketAssetRepository.save(marketAsset);

			// Architecture
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-2");
			marketAsset.setIdentification("onesaitPlatformArchitecture");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/onesaitPlatformArchitecture.json"));
			marketAsset.setImage(loadFileFromResources("market/img/asset.jpg"));
			marketAsset.setImageType("jpg");
			marketAssetRepository.save(marketAsset);

			// onesaitPlatform WITH DOCKER
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-3");
			marketAsset.setIdentification("onesaitPlatformWithDocker");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/onesaitPlatformWithDocker.json"));
			marketAsset.setImage(loadFileFromResources("market/img/docker.png"));
			marketAsset.setImageType("png");
			marketAssetRepository.save(marketAsset);

			// API JAVA
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-4");
			marketAsset.setIdentification("API JAVA");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/JavaAPI.json"));
			marketAsset.setImage(loadFileFromResources("market/img/jar-file.jpg"));
			marketAsset.setImageType("jpg");
			marketAsset.setContent(loadFileFromResources("market/docs/java-client.zip"));
			marketAsset.setContentId("java-client.zip");
			marketAssetRepository.save(marketAsset);

			// DIGITAL TWIN
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-5");
			marketAsset.setIdentification("DIGITAL TWIN EXAMPLE");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/DigitalTwin.json"));
			marketAsset.setImage(loadFileFromResources("market/img/gears.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/TurbineHelsinki.zip"));
			marketAsset.setContentId("TurbineHelsinki.zip");
			marketAssetRepository.save(marketAsset);

			// API NodeRED
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-6");
			marketAsset.setIdentification("API NodeRED");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/API-NodeRED.json"));
			marketAsset.setImage(loadFileFromResources("market/img/gears.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/API-NodeRED-onesait-Platform.zip"));
			marketAsset.setContentId("API-NodeRED-onesait-Platform.zip");
			marketAssetRepository.save(marketAsset);

			// OAUTH2 Authentication
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-7");
			marketAsset.setIdentification("OAuth2AndJWT");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/Oauth2Authentication.json"));
			marketAsset.setContent(loadFileFromResources("market/docs/oauth2-authentication.zip"));
			marketAsset.setContentId("oauth2-authentication.zip");
			marketAssetRepository.save(marketAsset);

			// Device simulator Jar
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-8");
			marketAsset.setIdentification("Device Simulator");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setImage(loadFileFromResources("market/img/jar-file.jpg"));
			marketAsset.setImageType("jpg");
			marketAsset.setJsonDesc(loadFromResources("market/details/DeviceSimulator.json"));
			marketAsset.setContent(loadFileFromResources("market/docs/device-simulator.zip"));
			marketAsset.setContentId("device-simulator.zip");
			marketAssetRepository.save(marketAsset);

			// JSON document example for Data import tool
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-9");
			marketAsset.setIdentification("Countries JSON");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/Countries.json"));
			marketAsset.setImage(loadFileFromResources("market/img/json.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/countries.json"));
			marketAsset.setContentId("countries.json");
			marketAssetRepository.save(marketAsset);

			// Stress Application
			createMarketAsset("MASTER-MarketAsset-10", "StressApplication", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.URLAPPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/StressApplication.json", null, null, null, null);
			// Chat bot
			createMarketAsset("MASTER-MarketAsset-11", "ChatBot", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.URLAPPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/ChatBot.json", null, null, null, null);

			// Digital Twin Web
			createMarketAsset("MASTER-MarketAsset-12", "SenseHatDemo", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.WEBPROJECT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/SenseHatDemo.json", null, null, null, null);

			// Digital Twin Sense Hat
			createMarketAsset("MASTER-MarketAsset-13", "DigitalTwinSenseHat", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.APPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/DigitalTwinSenseHat.json", "market/img/jar-file.jpg", "jpg",
					"market/docs/SensehatHelsinki.zip", "SensehatHelsinki.zip");

			// videos
			createMarketAsset("MASTER-MarketAsset-14", "Tutorials", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/Tutorials.json", null, null, null, null);

			// Health Check Android Application
			createMarketAsset("MASTER-MarketAsset-15", "HealthCheckAndroidApplication",
					MarketAsset.MarketAssetState.APPROVED, MarketAsset.MarketAssetType.APPLICATION,
					MarketAsset.MarketAssetPaymentMode.FREE, true, "market/details/HealthCheckApplication.json", null,
					null, "market/docs/HealthCheckApp.zip", "HealthCheckApp.zip");

			createMarketAsset("MASTER-MarketAsset-16", "management", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.WEBPROJECT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/IssueManagement.json", null, null, null, null);

			createMarketAsset("MASTER-MarketAsset-17", "QuickviewPlatform", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/QuickviewPlatform.json", null, null, null, null);

			createMarketAsset("MASTER-MarketAsset-18", "Binary Repository CRUD example",
					MarketAsset.MarketAssetState.APPROVED, MarketAsset.MarketAssetType.APPLICATION,
					MarketAsset.MarketAssetPaymentMode.FREE, true, "market/details/BinaryRepositoryAPI.json",
					"market/img/jar-file.jpg", "jpg", "market/docs/device-simulator.zip", "binary-repository-rest.zip");

		}
	}

	private void createMarketAsset(String id, String identification, MarketAsset.MarketAssetState state,
			MarketAsset.MarketAssetType assetType, MarketAsset.MarketAssetPaymentMode paymentMode, boolean isPublic,
			String jsonDesc, String image, String imageType, String content, String contentId) {

		final MarketAsset marketAsset = new MarketAsset();
		marketAsset.setId(id);
		marketAsset.setIdentification(identification);
		marketAsset.setUser(getUserAdministrator());
		marketAsset.setPublic(isPublic);
		marketAsset.setState(state);
		marketAsset.setMarketAssetType(assetType);
		marketAsset.setPaymentMode(paymentMode);
		marketAsset.setJsonDesc(loadFromResources(jsonDesc));
		if (image != null) {
			marketAsset.setImage(loadFileFromResources(image));
			marketAsset.setImageType(imageType);
		}
		if (content != null) {
			marketAsset.setContent(loadFileFromResources(content));
			marketAsset.setContentId(contentId);
		}
		marketAssetRepository.save(marketAsset);
	}

	public void init_notebook() {
		log.info("init notebook");
		final List<Notebook> notebook = notebookRepository.findAll();
		if (notebook.isEmpty()) {
			try {
				final User user = getUserAnalytics();
				final Notebook n = new Notebook();
				n.setId("MASTER-Notebook-1");
				n.setUser(user);
				n.setIdentification("Analytics s4c notebook tutorial");
				// Default zeppelin notebook tutorial ID
				n.setIdzep("2A94M5J1Z");
				notebookRepository.save(n);
			} catch (final Exception e) {
				log.info("Could not create notebook by:" + e.getMessage());
			}

		}
	}

	public void init_dataflow() {
		log.info("init dataflow");
		final List<Pipeline> pipeline = pipelineRepository.findAll();
		if (pipeline.isEmpty()) {
			try {
				final User user = getUserAnalytics();
				final Pipeline p = new Pipeline();
				p.setId("MASTER-Dataflow-1");
				p.setUser(user);
				p.setIdentification("Dataflow Example");
				p.setIdstreamsets("0000001");
				pipelineRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create dataflow by:" + e.getMessage());
			}

		}
	}

	public void init_notebook_user_access_type() {
		log.info("init notebook access type");
		final List<NotebookUserAccessType> notebook_uat = notebookUserAccessTypeRepository.findAll();
		if (notebook_uat.isEmpty()) {
			try {
				final NotebookUserAccessType p = new NotebookUserAccessType();
				p.setId("ACCESS-TYPE-1");
				p.setDescription("Edit Access");
				p.setName("EDIT");
				notebookUserAccessTypeRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create notebook access type by:" + e.getMessage());
			}

		}
	}
}
