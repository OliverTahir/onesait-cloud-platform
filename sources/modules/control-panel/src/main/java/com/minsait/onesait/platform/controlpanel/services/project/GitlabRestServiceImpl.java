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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.project.exceptions.GitlabException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitlabRestServiceImpl implements GitlabRestService {
	private static final String GITLAB_API_PATH = "/api/v4";
	private static final String GITLAB_OAUTH = "/oauth/token";
	private static final String GITLAB_PROJECTS = "/projects";
	private static final String GITLAB_USERS = "/users?per_page=1000";
	private static final String GITLAB_MEMBERS = "/members";
	private static final String GITLAB_GROUPS = "/groups";
	private static final String DEFAULT_BRANCH_PUSH = "master";
	private static final String INITIAL_COMMIT = "Spring Boot 2 + Vue.js example";
	private static final String GIT_REPO_URL_NODE = "http_url_to_repo";
	private static final String USARNAME_STR = "username";
	@Value("${onesaitplatform.gitlab.scaffolding.directory:/tmp/scaffolding}")
	private String directoryScaffolding;

	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private GitlabRestService gitlabRestService;
	@Autowired
	private GitOperations gitOperations;

	@Override
	public String createGitlabProject(String gitlabConfigId, String projectName, List<String> users, String url,
			boolean scaffolding) throws GitlabException {
		final GitlabConfiguration gitlab = configurationService.getGitlabConfiguration(gitlabConfigId);
		String webUrl = "";
		if (gitlab != null) {
			final String urlGitlab = !StringUtils.isEmpty(url) ? url : gitlab.getSite();
			final String user = gitlab.getUser();
			final String password = gitlab.getPassword();
			boolean projectCreated = false;
			int projectId = 0;
			String accessToken = "";
			if (!StringUtils.isEmpty(urlGitlab) && !StringUtils.isEmpty(user) && !StringUtils.isEmpty(password)) {
				try {
					accessToken = gitlabRestService.getOauthToken(urlGitlab, user, password);
					if (!StringUtils.isEmpty(accessToken)) {
						final int namespaceId = createNamespace(urlGitlab, projectName, accessToken);
						log.info("Namespace created with id: " + namespaceId);
						log.info("Project is going to be created with parameters, url: " + urlGitlab + " accessToken: "
								+ accessToken + " projectName: " + projectName + " namespaceId: " + namespaceId);
						final JsonNode projectInfo = gitlabRestService.createProject(urlGitlab, accessToken,
								projectName, namespaceId);
						projectId = projectInfo.get("id").asInt();
						webUrl = projectInfo.get("web_url").asText();
						projectCreated = true;
						try {
							gitlabRestService.authorizeUsers(urlGitlab, accessToken, projectId, users);
						} catch (final GitlabException e) {
							log.error("Could not add users to project");
						}
						generateScaffolding(projectInfo, gitlab);

						return webUrl;
					}
				} catch (final Exception e) {
					log.error("Could not create Gitlab project {}", e.getMessage());
					if (projectCreated) {
						log.error(
								"Project was created in gitlab but something went wrong, rolling back and destroying project {}",
								projectName);
						deleteProject(urlGitlab, accessToken, projectId);
					}
					throw new GitlabException(e.getMessage());
				}
			}

		} else
			throw new GitlabException("No configuration found for Gitlab");
		return webUrl;

	}

	private void generateScaffolding(JsonNode projectInfo, GitlabConfiguration gitlabConfig) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
		log.info("Directory created");

		gitOperations.unzipScaffolding(directoryScaffolding);
		log.info("Scafolding project unzipped");

		gitOperations.configureGitlabAndInit(gitlabConfig.getUser(), gitlabConfig.getEmail(), directoryScaffolding);
		log.info("Gitlab project configured");

		gitOperations.addOrigin(projectInfo.get(GIT_REPO_URL_NODE).asText(), directoryScaffolding, false);
		log.info("Origin added");

		gitOperations.addAll(directoryScaffolding);
		log.info("Add all");

		gitOperations.commit(INITIAL_COMMIT, directoryScaffolding);
		log.info("Initial commit");

		gitOperations.push(projectInfo.get(GIT_REPO_URL_NODE).asText(), gitlabConfig.getUser(),
				gitlabConfig.getPassword(), DEFAULT_BRANCH_PUSH, directoryScaffolding);
		log.info("Pushed to: " + projectInfo.get(GIT_REPO_URL_NODE).asText());

		gitOperations.deleteDirectory(directoryScaffolding);
		log.info("Deleting temp directory {}", directoryScaffolding);
		log.info("END scafolding project generation");
	}

	@Override
	public String getOauthToken(String url, String user, String password) throws GitlabException {
		final String body = "{\"grant_type\":\"password\",\"username\":\"" + user + "\",\"password\":\"" + password
				+ "\"}";
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_OAUTH), HttpMethod.POST, body, null);
			return response.getBody().get("access_token").asText();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not get authentication token {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not get authentication token", e);
		} catch (final Exception e) {
			throw new GitlabException("Could not get authentication token", e);
		}

	}

	@Override
	public int createNamespace(String url, String projectName, String token) throws GitlabException {
		final String body = "{\"name\":\"" + projectName + "\",\"path\":\""
				+ projectName.toLowerCase().replace(" ", "-") + "\", \"visibility\":\"private\"}";
		int namespaceId = 0;
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS),
					HttpMethod.POST, body, token);
			namespaceId = response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create namespace for project {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not create namespace for project" + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not create namespace for project" + e.getMessage());
		}
		return namespaceId;
	}

	@Override
	public JsonNode createProject(String url, String token, String name, int namespaceId) throws GitlabException {
		final String body = "{\"name\":\"" + name + "\",\"visibility\":\"private\", \"namespace_id\":" + namespaceId
				+ "}";
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS),
					HttpMethod.POST, body, token);
			return response.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException(e.getMessage());
		}

	}

	@Override
	public void deleteProject(String url, String token, int projectId) throws GitlabException {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(
					url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/").concat(String.valueOf(projectId)),
					HttpMethod.DELETE, "", token);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not delete project" + e.getMessage());
		}

	}

	@Override
	public Map<String, Integer> authorizeUsers(String url, String token, int projectId, List<String> users)
			throws IOException, GitlabException, URISyntaxException {
		final ArrayNode repoUsers = (ArrayNode) getRepositoryUsers(url, token);
		final List<String> existingUsers = new ArrayList<>();
		try {
			for (final JsonNode user : repoUsers) {
				log.info("Authorize user: " + user.get(USARNAME_STR).asText());
				if (users.contains(user.get(USARNAME_STR).asText())) {
					authorizeUser(url, projectId, user.get("id").asInt(), token);
					existingUsers.add(user.get(USARNAME_STR).asText());
					log.info("User authorized!! " + user.get(USARNAME_STR).asText());
				}
			}
			final List<String> newUsers = users.stream().filter(s -> !existingUsers.contains(s))
					.collect(Collectors.toList());
			for (final String user : newUsers) {
				try {
					final int newUserId = createNewUser(url, token, userService.getUser(user));
					authorizeUser(url, projectId, newUserId, token);
				} catch (final GitlabException e) {
					log.error("Could not create user {}, cause:", user, e.getMessage());
				}

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not get authentication token {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not authorize users " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not authorize users " + e.getMessage());
		}
		return null;
	}

	private int createNewUser(String url, String token, User user) throws GitlabException {
		final String body = "{\"email\":\"" + user.getEmail() + "\", \"username\":\"" + user.getUserId()
				+ "\",\"name\":\"" + user.getFullName() + "\",\"reset_password\": true}";
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_USERS),
					HttpMethod.POST, body, token);
			return response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create user {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not create user " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not create user, your access level is not Administrator", e);
		}
	}

	private void authorizeUser(String url, int projectId, int userId, String token)
			throws URISyntaxException, IOException {
		final String body = "{\"id\": " + projectId + ", \"access_level\": 30 , \"user_id\":" + userId + "}";
		sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/").concat(String.valueOf(projectId))
				.concat(GITLAB_MEMBERS), HttpMethod.POST, body, token);
	}

	private JsonNode getRepositoryUsers(String url, String token) throws URISyntaxException, IOException {
		final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_USERS),
				HttpMethod.GET, "", token);
		return response.getBody();
	}

	private ResponseEntity<JsonNode> sendHttp(String url, HttpMethod httpMethod, String body, String token)
			throws URISyntaxException, ClientProtocolException, IOException {

		final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (!StringUtils.isEmpty(token))
			headers.add("Authorization", "Bearer " + token);

		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<String>(
				body, headers);
		ResponseEntity<JsonNode> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		response = restTemplate.exchange(new URI(url), httpMethod, request, JsonNode.class);

		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<JsonNode>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

}
