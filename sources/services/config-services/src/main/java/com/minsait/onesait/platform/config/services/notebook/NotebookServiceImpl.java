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
package com.minsait.onesait.platform.config.services.notebook;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.notebook.configuration.NotebookServiceConfiguration;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotebookServiceImpl implements NotebookService {

	@Autowired
	private NotebookServiceConfiguration configuration;

	@Autowired
	private NotebookRepository notebookRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private UserService userService;

	@Autowired
	private NotebookUserAccessRepository notebookUserAccessRepository;

	@Autowired
	private NotebookUserAccessTypeRepository notebookUserAccessTypeRepository;

	private static final String WITHNAME_STR = " with name: ";
	private static final String URI_POST_ERROR = "The URI of the endpoint is invalid in creation POST";
	private static final String URI_POST2_ERROR = "The URI of the endpoint is invalid in creation POST: ";
	private static final String POST_ERROR = "Exception in POST in creation POST";
	private static final String POST2_ERROR = "Exception in POST in creation POST: ";
	private static final String POST_EXECUTING_ERROR = "Exception executing creation POST, status code: ";
	private static final String NAME_STR = "{'name': '";
	private static final String API_NOTEBOOK_STR = "/api/notebook/";

	private String encryptRestUserpass() {
		String key = configuration.getRestUsername() + ":" + configuration.getRestPass();
		final String encryptedKey = new String(Base64.encode(key.getBytes()), Charset.forName("UTF-8"));
		key = "Basic " + encryptedKey;
		return key;
	}

	private Notebook sendZeppelinCreatePost(String path, String body, String name, User user) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String idzep;
		ResponseEntity<String> responseEntity;

		log.info("Creating notebook for user: " + user.getUserId() + WITHNAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.POST, body, headers);
		} catch (final URISyntaxException e) {
			log.error(URI_POST_ERROR);
			throw new NotebookServiceException(URI_POST2_ERROR + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new NotebookServiceException(POST2_ERROR, e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();
		/* 200 zeppelin 8, 201 zeppelin 7 */
		if (statusCode / 100 != 2) {
			log.error(POST_EXECUTING_ERROR + statusCode);
			throw new NotebookServiceException(POST_EXECUTING_ERROR + statusCode);
		}

		try {
			final JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idzep = createResponseObj.getString("body");
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create post");
			throw new NotebookServiceException("Exception parsing answer in create post: ", e);
		}

		final Notebook nt = saveDBNotebook(name, idzep, user);
		log.info("Notebook for user: " + user.getUserId() + WITHNAME_STR + name + ", successfully created");
		return nt;
	}

	private String sendZeppelinCreatePostWithoutDBC(String path, String body, String name, User user) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String idzep;
		ResponseEntity<String> responseEntity;

		log.info("Creating notebook for user: " + user.getUserId() + WITHNAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.POST, body, headers);
		} catch (final URISyntaxException e) {
			log.error(URI_POST_ERROR);
			throw new NotebookServiceException(URI_POST2_ERROR + e);
		} catch (final IOException e) {
			log.error(POST_ERROR);
			throw new NotebookServiceException(POST2_ERROR, e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();
		/* 200 zeppelin 8, 201 zeppelin 7 */
		if (statusCode / 100 != 2) {
			log.error(POST_EXECUTING_ERROR + statusCode);
			throw new NotebookServiceException(POST_EXECUTING_ERROR + statusCode);
		}

		try {
			final JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idzep = createResponseObj.getString("body");
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create post");
			throw new NotebookServiceException("Exception parsing answer in create post: ", e);
		}

		return idzep;
	}

	@Override
	public Notebook saveDBNotebook(String name, String idzep, User user) {
		final Notebook nt = new Notebook();
		nt.setIdentification(name);
		nt.setIdzep(idzep);
		nt.setUser(user);
		notebookRepository.save(nt);
		return nt;
	}

	@Override
	public Notebook createEmptyNotebook(String name, String userId) {
		final User user = userRepository.findByUserId(userId);
		return sendZeppelinCreatePost("/api/notebook", NAME_STR + name + "'}", name, user);
	}

	@Override
	public Notebook importNotebook(String name, String data, String userId) {
		final User user = userRepository.findByUserId(userId);
		return sendZeppelinCreatePost("/api/notebook/import", data, name, user);
	}

	@Override
	public Notebook cloneNotebook(String name, String idzep, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(idzep);
		final User user = userRepository.findByUserId(userId);
		if (hasUserPermissionInNotebook(nt, user)) {
			return sendZeppelinCreatePost(API_NOTEBOOK_STR + idzep, NAME_STR + name + "'}", name, user);
		} else {
			return null;
		}
	}

	@Override
	public String cloneNotebookOnlyZeppelin(String name, String idzep, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(idzep);
		final User user = userRepository.findByUserId(userId);
		if (hasUserPermissionInNotebook(nt, user)) {
			return sendZeppelinCreatePostWithoutDBC(API_NOTEBOOK_STR + idzep, NAME_STR + name + "'}", name, user);
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<byte[]> exportNotebook(String id, String user) {
		final Notebook nt = notebookRepository.findByIdentification(id);
		ResponseEntity<String> responseEntity;
		JSONObject notebookJSONObject;

		if (hasUserPermissionInNotebook(nt, user)) {
			try {
				responseEntity = sendHttp("/api/notebook/export/" + nt.getIdzep(), HttpMethod.GET, "");
			} catch (final URISyntaxException e) {
				log.error(URI_POST_ERROR);
				throw new NotebookServiceException(URI_POST2_ERROR + e);
			} catch (final IOException e) {
				log.error(POST_ERROR);
				throw new NotebookServiceException(POST2_ERROR, e);
			}

			final int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error("Exception executing export notebook, status code: " + statusCode);
				throw new NotebookServiceException("Exception executing export notebook, status code: " + statusCode);
			}

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("Content-Disposition", "attachment; filename=\"" + nt.getIdentification() + ".json\"");
			try {
				final JSONObject responseJSONObject = new JSONObject(responseEntity.getBody());
				notebookJSONObject = new JSONObject(responseJSONObject.getString("body"));
			} catch (final JSONException e) {
				log.error("Exception parsing answer in download notebook");
				throw new NotebookServiceException("Exception parsing answer in download notebook: ", e);
			}
			return new ResponseEntity<byte[]>(notebookJSONObject.toString().getBytes(Charset.forName("UTF-8")), headers,
					HttpStatus.OK);

		} else {
			log.error("Exception executing export notebook, permission denied");
			throw new NotebookServiceException("Error export notebook, permission denied");
		}
	}

	@Override
	public void removeNotebook(String id, String user) {
		ResponseEntity<String> responseEntity;
		final Notebook nt = notebookRepository.findByIdentification(id);
		final String name = nt.getIdentification();
		if (resourceService.isResourceSharedInAnyProject(nt))
			throw new OPResourceServiceException(
					"This Notebook is shared within a Project, revoke access from project prior to deleting");

		log.info("Delete notebook for user: " + user + WITHNAME_STR + name);

		if (hasUserPermissionInNotebook(nt, user)) {

			try {
				responseEntity = sendHttp(API_NOTEBOOK_STR + nt.getIdzep(), HttpMethod.DELETE, "");
			} catch (final URISyntaxException e) {
				log.error("The URI of the endpoint is invalid in delete notebook");
				throw new NotebookServiceException("The URI of the endpoint is invalid in delete notebook: " + e);
			} catch (final IOException e) {
				log.error(POST_ERROR);
				throw new NotebookServiceException("Exception in POST in delete notebook: ", e);
			}

			final int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error("Exception executing delete notebook, status code: " + statusCode);
				throw new NotebookServiceException("Exception executing delete notebook, status code: " + statusCode);
			}

			notebookRepository.delete(nt);
			log.info("Notebook for user: " + user + WITHNAME_STR + name + ", successfully deleted");
		} else {
			log.error("Exception executing delete notebook, permission denied");
			throw new NotebookServiceException("Error delete notebook, permission denied");
		}
	}

	@Override
	public String loginOrGetWSToken() {
		return loginOrGetWSTokenWithUserPass(configuration.getZeppelinShiroUsername(),
				configuration.getZeppelinShiroPass());
	}

	@Override
	public String loginOrGetWSTokenAdmin() {
		return loginOrGetWSTokenWithUserPass(configuration.getZeppelinShiroAdminUsername(),
				configuration.getZeppelinShiroAdminPass());
	}

	private String loginOrGetWSTokenWithUserPass(String username, String password) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		ResponseEntity<String> responseEntity;

		try {
			responseEntity = sendHttp("api/login", HttpMethod.POST, "userName=" + username + "&password=" + password,
					headers);
		} catch (final URISyntaxException e) {
			log.error("The URI of the endpoint is invalid in authentication POST");
			throw new NotebookServiceException("The URI of the endpoint is invalid in authentication POST: " + e);
		} catch (final IOException e) {
			log.error("Exception in POST in authentication POST");
			throw new NotebookServiceException("Exception in POST in authentication POST: ", e);
		}

		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode != 200) {
			log.error("Exception executing authentication POST, status code: " + statusCode);
			throw new NotebookServiceException("Exception executing authentication POST, status code: " + statusCode);
		}

		return responseEntity.getBody();

	}

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body)
			throws URISyntaxException, ClientProtocolException, IOException {
		return sendHttp(requestServlet.getServletPath(), httpMethod, body);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body)
			throws URISyntaxException, ClientProtocolException, IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return sendHttp(url, httpMethod, body, headers);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers)
			throws URISyntaxException, ClientProtocolException, IOException {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		headers.add("Authorization", encryptRestUserpass());
		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<String>(
				body, headers);
		log.debug("Sending method " + httpMethod.toString() + " Notebook");
		ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("api"))), httpMethod,
					request, String.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Notebook");
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<String>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	@Override
	public Notebook getNotebook(String identification, String userId) {
		final Notebook nt = notebookRepository.findByIdentification(identification);
		if (hasUserPermissionInNotebook(nt, userId)) {
			return nt;
		} else {
			return null;
		}
	}

	@Override
	public List<Notebook> getNotebooks(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (!user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return notebookRepository.findByUserAndAccess(user);
		} else {
			return notebookRepository.findAllByOrderByIdentificationAsc();
			// return notebookRepository.findByUser(user);
			// return null;

		}
	}

	@Override
	public boolean hasUserPermissionInNotebook(Notebook nt, String userId) {
		final User user = userRepository.findByUserId(userId);
		return hasUserPermissionInNotebook(nt, user);
	}

	private boolean hasUserPermissionInNotebook(Notebook nt, User user) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| nt.getUser().getUserId().equals(user.getUserId()))
			return true;
		else { // TO-DO differentiate between access MANAGE/VIEW
			for (NotebookUserAccess notebookUserAccess : notebookUserAccessRepository.findByNotebookAndUser(nt, user)) {
				if (notebookUserAccess.getNotebookUserAccessType().getId().equals("ACCESS-TYPE-1")) {
					return true;
				}
			}
			return false;
		}
		// return resourceService.hasAccess(user.getUserId(), nt.getId(),
		// ResourceAccessType.VIEW);}
	}

	@Override
	public void changePublic(Notebook nt) {
		if (nt != null) {
			if (nt.isPublic())
				nt.setPublic(false);
			else
				nt.setPublic(true);
			notebookRepository.save(nt);
		}

	}

	@Override
	public boolean hasUserPermissionForNotebook(String zeppelinId, String userId) {
		final Notebook nt = notebookRepository.findByIdzep(zeppelinId);
		if (nt != null)
			return this.hasUserPermissionInNotebook(nt, userId);
		return false;
	}

	@Override
	public ResponseEntity<String> runParagraph(String zeppelinId, String paragraphId, String body)
			throws ClientProtocolException, URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;
		responseEntity = sendHttp("/api/notebook/run/".concat(zeppelinId).concat("/").concat(paragraphId),
				HttpMethod.POST, body);
		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			responseEntity = sendHttp(API_NOTEBOOK_STR.concat(zeppelinId).concat("/paragraph/").concat(paragraphId),
					HttpMethod.GET, "");
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<String> runAllParagraphs(String zeppelinId)
			throws ClientProtocolException, URISyntaxException, IOException {
		return sendHttp("/api/notebook/job/".concat(zeppelinId), HttpMethod.POST, "");
	}

	@Override
	public ResponseEntity<String> getParagraphResult(String zeppelinId, String paragraphId)
			throws ClientProtocolException, URISyntaxException, IOException {
		return sendHttp(API_NOTEBOOK_STR.concat(zeppelinId).concat("/paragraph/").concat(paragraphId), HttpMethod.GET,
				"");
	}

	@Override
	public ResponseEntity<String> getAllParagraphStatus(String zeppelinId)
			throws ClientProtocolException, URISyntaxException, IOException {
		ResponseEntity<String> responseEntity;
		responseEntity = sendHttp("/api/notebook/job/".concat(zeppelinId), HttpMethod.GET, "");

		return responseEntity;
	}

	@Override
	public void createUserAccess(String notebookId, String userId, String accessType) {
		if (notebookId != "" && notebookId != null && userId != "" && userId != null && accessType != ""
				&& accessType != null) {
			final User user = userRepository.findByUserId(userId);
			final Notebook notebook = notebookRepository.findById(notebookId);
			final NotebookUserAccessType notebookUserAccessType = notebookUserAccessTypeRepository.findById(accessType);

			final NotebookUserAccess notebookUserAccess = new NotebookUserAccess();
			notebookUserAccess.setNotebook(notebook);
			notebookUserAccess.setUser(user);
			notebookUserAccess.setNotebookUserAccessType(notebookUserAccessType);
			notebookUserAccessRepository.save(notebookUserAccess);

		}
	}

	@Override
	public void deleteUserAccess(String notebookUserAccessId) {
		notebookUserAccessRepository.delete(notebookUserAccessId);
	}

}
