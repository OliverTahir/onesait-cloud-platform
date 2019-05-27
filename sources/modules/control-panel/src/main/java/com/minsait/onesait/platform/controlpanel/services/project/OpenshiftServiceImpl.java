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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.config.components.GitlabConfiguration;
import com.minsait.onesait.platform.config.components.OpenshiftConfiguration;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.controlpanel.services.project.exceptions.GitlabException;

import avro.shaded.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenshiftServiceImpl implements OpenshiftService {
	
	private static final String LINE_SEPARATOR = "line.separator";
	private static final String COULD_NOT_EXECUTE_COMMAND = "Could not execute command {}";
	
	@Value("${onesaitplatform.docker.mandatory-services:elasticdb,configdb,configinit,quasar,realtimedb,controlpanelservice,schedulerdb,monitoringuiservice,loadbalancerservice,routerservice,cacheservice}")
	private String[] MANDATORY_SERVICES;
	@Value("${onesaitplatform.docker.openshift.imagenamespace:onesait}")
	private String IMAGENAMESPACE;
	@Value("${onesaitplatform.docker.openshift.module_tag}")
	private String MODULE_TAG;
	@Value("${onesaitplatform.docker.openshift.infra_tag}")
	private String INFRA_TAG;
	@Value("${onesaitplatform.docker.openshift.persistence_tag}")
	private String PERSISTENCE_TAG;
	@Value("${onesaitplatform.docker.openshift.persistence_tag_mongodb}")
	private String PERSISTENCE_TAG_MONGODB;
	@Value("${onesaitplatform.docker.openshift.server_name}")
	private String SERVER_NAME;
	@Value("${onesaitplatform.docker.openshift.realtimedbuseauth}")
	private boolean REALTIMEDBUSEAUTH;
	@Value("${onesaitplatform.docker.openshift.authdb}")
	private String AUTHDB;
	@Value("${onesaitplatform.docker.openshift.authparams}")
	private String AUTHPARAMS;
	@Value("${onesaitplatform.docker.openshift.replicas}")
	private int REPLICAS;
	@Value("${onesaitplatform.docker.openshift.persistent}")
	private boolean PERSISTENT;

	@Value("${onesaitplatform.docker.openshift.templates.git_path}")
	private String GIT_PATH;
	@Value("${onesaitplatform.docker.openshift.templates.tmp_path}")
	private String TMP_PATH;
	@Value("${onesaitplatform.docker.openshift.templates.origin}")
	private String GIT_ORIGIN;
	@Autowired
	private GitOperations gitOperations;

	static final ImmutableMap<String, String> SERVICE_TO_TEMPLATE = new ImmutableMap.Builder<String, String>()
			.put("quasar", "/modulestemplates/11-template-quasar.yml")
			.put("controlpanelservice", "/modulestemplates/12-template-controlpanel-persistent.yml")
			.put("apimanagerservice", "/modulestemplates/13-template-apimanager.yml")
			.put("flowengineservice", "17-template-flowengine-persistent.yml")
			.put("iotbrokerservice", "/modulestemplates/14-template-iotbroker.yml")
			.put("cacheservice", "/modulestemplates/21-template-cacheserver.yml")
			.put("loadbalancerservice", "/modulestemplates/20-template-loadbalancer-persistent.yml")
			.put("routerservice", "/modulestemplates/22-template-semanticinfbroker.yml")
			.put("oauthservice", "/modulestemplates/23-template-oauthserver.yml")
			.put("configinit", "/modulestemplates/24-template-configinit.yml")
			.put("dashboardengineservice", "/modulestemplates/18-template-dashboardengine.yml")
			.put("monitoringuiservice", "/modulestemplates/19-template-monitoringui.yml")
			.put("zeppelin", "/modulestemplates/25-template-notebook.yml")
			.put("configdb", "/persistencetemplates/11-template-configdb-persistent.yml")
			.put("schedulerdb", "/persistencetemplates/12-template-schedulerdb-persistent.yml")
			.put("realtimedb", "/persistencetemplates/13-template-realtimedb-persistent.yml")
			.put("elasticdb", "/persistencetemplates/14-template-elasticdb-persistent.yml")
			.put("kafka", "/persistencetemplates/16-template-kafka.yml")
			.put("zookeeper", "/persistencetemplates/15-template-zookeeper.yml").build();
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public List<String> getOpenshiftProjects(String openshiftConfigId, String url) {
		loginOc(openshiftConfigId, url);
		final ProcessBuilder pb = new ProcessBuilder("oc", "projects", "--short=true");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			return Arrays.asList(builder.toString().split("\n"));
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
		return new ArrayList<>();
	}

	@Override
	public String createOpenshiftProject(String openshiftConfigId, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	private void loginOc(String openshiftConfigId, String url) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);

		final ProcessBuilder pb;
		if (!StringUtils.isEmpty(configuration.getToken()))
			pb = new ProcessBuilder("oc", "login", url, "--token", configuration.getToken(),
					"--insecure-skip-tls-verify");
		else
			pb = new ProcessBuilder("oc", "login", url, "-u", configuration.getUser(), "-p",
					configuration.getPassword(), "--insecure-skip-tls-verify");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			log.info("Logging into openshift: {}", builder.toString());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	@Override
	public List<String> getOpenshiftProjects(String openshiftConfigId) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);
		return getOpenshiftProjects(openshiftConfigId, configuration.getInstance());
	}

	@Override
	public String deployOpenshiftProject(String openshiftConfigId, String project, String realm,
			List<String> services) {
		final OpenshiftConfiguration configuration = configurationService.getOpenshiftConfiguration(openshiftConfigId);
		return deployOpenshiftProject(openshiftConfigId, project, realm, services, configuration.getInstance());
	}

	@Override
	public String deployOpenshiftProject(String openshiftConfigId, String project, String realm, List<String> services,
			String url) {
		// include mandatory services if not present
		final List<String> allServices = services;
		Arrays.asList(MANDATORY_SERVICES).stream().forEach(s -> {
			if (!allServices.contains(s))
				allServices.add(s);
		});

		log.info("Deploying in openshift with following services");
		allServices.forEach(s -> log.info(s));

		try {

			log.debug("Pulling templates from repo");
			pullTemplatesFromRepo();

			final List<String> templates = allServices.stream().map(
					s -> "/tmp/oc-templates/devops/build-deploy/openshift/onesaitplatform" + SERVICE_TO_TEMPLATE.get(s))
					.collect(Collectors.toList());

			log.debug("Login to oc");
			loginOc(openshiftConfigId, url);
			log.debug("Setting project {}", project);
			setProject(project);
			log.debug("Proccesing templates and creating deploy + service in oc");
			templates.forEach(s -> processTemplate(s, allServices.contains("kafka")));
			log.debug("Deleting template directory {}", TMP_PATH);
			gitOperations.deleteDirectory(TMP_PATH);

		} catch (final GitlabException e) {
			log.error("Could not download oc templates from repository, aborting deployment");
			throw new RuntimeException("Could not download oc templates from repository, aborting deployment");
		}

		return null;
	}

	private void pullTemplatesFromRepo() throws GitlabException {
		final GitlabConfiguration gitlabConfig = configurationService.getGitlabConfiguration(
				configurationService.getConfiguration(Configuration.Type.GITLAB, "onesaitPlatform").getId());
		if (gitlabConfig == null)
			throw new GitlabException("No gitlab configuration found for the platform credentials");
		log.debug("Creating directory {}", TMP_PATH);
		gitOperations.createDirectory(TMP_PATH);
		log.debug("Configure git with username: {} , email: {}", gitlabConfig.getUser(), gitlabConfig.getEmail());
		gitOperations.configureGitlabAndInit(gitlabConfig.getUser(), gitlabConfig.getEmail(), TMP_PATH);
		log.debug("Setting sparseCheckout true");
		gitOperations.sparseCheckoutConfig(TMP_PATH);
		final String compiledOrigin = getCompiledGitOrigin(gitlabConfig.getUser(), gitlabConfig.getPassword());
		log.debug("Adding origin {}", compiledOrigin);
		gitOperations.addOrigin(compiledOrigin, TMP_PATH, true);
		log.debug("Adding path {} to sparse checkout file", GIT_PATH);
		gitOperations.sparseCheckoutAddPath(GIT_PATH, TMP_PATH);
		log.debug("Checkin out on branch master");
		gitOperations.checkout("master", TMP_PATH);

	}

	private String getCompiledGitOrigin(String username, String password) {
		final HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("username", username);
		scopes.put("password", password);
		final Writer writer = new StringWriter();
		final StringReader reader = new StringReader(GIT_ORIGIN);
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "origin");
		mustache.execute(writer, scopes);
		return writer.toString();
	}

	private void setProject(String project) {
		final ProcessBuilder pb = new ProcessBuilder("oc", "project", project);
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			log.info("Executed command {} with output {}", pb.command(), builder.toString());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}

	private void processTemplate(String template, boolean kafkaEnabled) {
		final Yaml yaml = new Yaml();
		try (final FileWriter writter = new FileWriter(template, false)) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> yamlMap = (Map<String, Object>) yaml
					.load(new FileInputStream(new File(template)));
			if (yamlMap.containsKey("parameters"))
				yamlMap.remove("parameters");
			final String yml = yaml.dump(yamlMap);
			final Map<String, String> parameters = new HashMap<>();
			parameters.put("${MODULE_TAG}", MODULE_TAG);
			parameters.put("${INFRA_TAG}", INFRA_TAG);
			parameters.put("${PERSISTENCE_TAG}", PERSISTENCE_TAG);
			parameters.put("${PERSISTENCE_TAG_MONGODB}", PERSISTENCE_TAG_MONGODB);
			parameters.put("${SERVER_NAME}", SERVER_NAME);
			parameters.put("${REALTIMEDBUSEAUTH}", Boolean.toString(REALTIMEDBUSEAUTH));
			parameters.put("${AUTHDB}", AUTHDB);
			parameters.put("${AUTHPARAMS}", AUTHPARAMS);
			parameters.put("${REPLICAS}", Integer.toString(REPLICAS));
			parameters.put("${PERSISTENT}", Boolean.toString(PERSISTENT));
			parameters.put("${KAFKAENABLED}", Boolean.toString(kafkaEnabled));
			parameters.put("${IMAGENAMESPACE}", IMAGENAMESPACE);

			final StringBuffer sb = new StringBuffer();
			final Pattern linuxParam = Pattern.compile("(\\$\\{[^}]+\\})");
			final Matcher matcher = linuxParam.matcher(yml);
			while (matcher.find()) {
				final String repString = parameters.get(matcher.group(1));
				if (repString != null)
					matcher.appendReplacement(sb, repString);
			}
			matcher.appendTail(sb);

			// final FileWriter writter = new FileWriter(template, false);
			writter.write(sb.toString());
			writter.flush();
			writter.close();

			createPod(template);

		} catch (final IOException e) {
			log.error("Error while operating with file {}, {}", template, e.getMessage());
		}
	}

	private void createPod(String template) {
		final ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c",
				"oc process -f " + template + " | oc create -f -");
		try {
			pb.redirectErrorStream(true);
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			log.info("Executed command {} with output {}", pb.command(), builder.toString());
		} catch (IOException | InterruptedException e) {
			log.error(COULD_NOT_EXECUTE_COMMAND, pb.command());
		}
	}
}
