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
package com.minsait.onesait.platform.config.services.dataflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import org.springframework.web.util.UriUtils;

import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dataflow.configuration.DataflowServiceConfiguration;
import com.minsait.onesait.platform.config.services.exceptions.DataflowServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataflowServiceImpl implements DataflowService {

	@Autowired
	private DataflowServiceConfiguration configuration;

	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private UserRepository userRepository;

	private final static String WITH_NAME_STR = " with name: ";

	private String encryptRestUserpass(User user) {
		String key;
		if (!user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			key = configuration.getDataflowUsername() + ":" + configuration.getDataflowPass();
		} else {
			key = configuration.getDataflowAdminUsername() + ":" + configuration.getDataflowAdminPass();
		}

		String encryptedKey = new String(Base64.encode(key.getBytes()), Charset.forName("UTF-8"));
		key = "Basic " + encryptedKey;
		return key;
	}

	private Pipeline sendStreamsetsCreatePut(String path, String name, String user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<String> lHeaderRequestBy = new ArrayList<String>();
		lHeaderRequestBy.add("Data Collector");
		headers.put("X-Requested-By", lHeaderRequestBy);
		String idstreamsets;
		ResponseEntity<String> responseEntity;

		log.info("Creating pipeline for user: " + user + WITH_NAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.PUT, "", headers, user);
		} catch (URISyntaxException e) {
			log.error("The URI of the endpoint is invalid in creation PUT");
			throw new DataflowServiceException("The URI of the endpoint is invalid in creation PUT: " + e);
		} catch (IOException e) {
			log.error("Exception in PUT in creation PUT");
			throw new DataflowServiceException("Exception in PUT in creation PUT: ", e);
		}

		int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode / 100 != 2) {
			log.error("Exception executing creation PUT, status code: " + statusCode);
			throw new DataflowServiceException("Exception executing creation PUT, status code: " + statusCode);
		}

		try {
			JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idstreamsets = createResponseObj.getString("pipelineId");
		} catch (JSONException e) {
			log.error("Exception parsing answer in create post");
			throw new DataflowServiceException("Exception parsing answer in create post: ", e);
		}

		Pipeline pl = saveDBPipeline(name, idstreamsets, user);
		log.info("Pipeline for user: " + user + WITH_NAME_STR + name + ", successfully created");
		return pl;
	}

	public Pipeline saveDBPipeline(String name, String idzep, String user) {
		Pipeline pl = new Pipeline();
		pl.setIdentification(name);
		pl.setIdstreamsets(idzep);
		pl.setUser(userRepository.findByUserId(user));
		pipelineRepository.save(pl);
		return pl;
	}

	public Pipeline createPipeline(String name, String description, String userId) throws UnsupportedEncodingException {
		return sendStreamsetsCreatePut("rest/v1/pipeline/" + UriUtils.encode(name, "UTF-8")
				+ "?autoGeneratePipelineId=true&description=" + description, name, userId);
	}

	/*
	 * public Notebook importNotebook(String name, String data, String userId) {
	 * User user = userRepository.findByUserId(userId); return
	 * sendZeppelinCreatePost("/api/notebook/import", data, name, user); }
	 * 
	 * public Notebook cloneNotebook(String name, String idzep, String userId) {
	 * Notebook nt = notebookRepository.findByIdzep(idzep); User user =
	 * userRepository.findByUserId(userId); if (hasUserPermissionInNotebook(nt,
	 * user)) { return sendZeppelinCreatePost("/api/notebook/" + idzep, "{'name': '"
	 * + name + "'}", name, user); } else { return null; } }
	 * 
	 * public ResponseEntity<byte[]> exportNotebook(String id, String user) {
	 * Notebook nt = notebookRepository.findByIdentification(id);
	 * ResponseEntity<String> responseEntity; JSONObject notebookJSONObject;
	 * 
	 * if (hasUserPermissionInNotebook(nt, user)) { try { responseEntity =
	 * sendHttp("/api/notebook/export/" + nt.getIdzep(), HttpMethod.GET, ""); }
	 * catch (URISyntaxException e) {
	 * log.error("The URI of the endpoint is invalid in creation POST"); throw new
	 * NotebookServiceException("The URI of the endpoint is invalid in creation POST: "
	 * + e); } catch (IOException e) {
	 * log.error("Exception in POST in creation POST"); throw new
	 * NotebookServiceException("Exception in POST in creation POST: ", e); }
	 * 
	 * int statusCode = responseEntity.getStatusCodeValue();
	 * 
	 * if (statusCode != 200) {
	 * log.error("Exception executing export notebook, status code: " + statusCode);
	 * throw new
	 * NotebookServiceException("Exception executing export notebook, status code: "
	 * + statusCode); }
	 * 
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.setContentType(MediaType.TEXT_PLAIN);
	 * headers.set("Content-Disposition", "attachment; filename=\"" +
	 * nt.getIdentification() + ".json\""); try { JSONObject responseJSONObject =
	 * new JSONObject(responseEntity.getBody()); notebookJSONObject = new
	 * JSONObject(responseJSONObject.getString("body")); } catch (JSONException e) {
	 * log.error("Exception parsing answer in download notebook"); throw new
	 * NotebookServiceException("Exception parsing answer in download notebook: ",
	 * e); } return new
	 * ResponseEntity<byte[]>(notebookJSONObject.toString().getBytes(Charset.forName
	 * ("UTF-8")), headers, HttpStatus.OK);
	 * 
	 * } else { log.error("Exception executing export notebook, permission denied");
	 * throw new
	 * NotebookServiceException("Error export notebook, permission denied"); } }
	 */

	public void removePipeline(String id, String userId) {
		ResponseEntity<String> responseEntity;
		Pipeline pl = pipelineRepository.findByIdentification(id);
		String name = pl.getIdentification();
		User user = userRepository.findByUserId(userId);
		log.info("Delete pipeline for user: " + user + WITH_NAME_STR + name);

		if (hasUserPermissionInPipeline(pl, user)) {

			try {
				responseEntity = sendHttp("rest/v1/pipeline/" + pl.getIdstreamsets(), HttpMethod.DELETE, "", userId);
			} catch (URISyntaxException e) {
				log.error("The URI of the endpoint is invalid in delete pipeline");
				throw new DataflowServiceException("The URI of the endpoint is invalid in delete pipeline: " + e);
			} catch (IOException e) {
				log.error("Exception in DELETE in creation DELETE");
				throw new DataflowServiceException("Exception in DELETE in delete pipeline: ", e);
			}

			int statusCode = responseEntity.getStatusCodeValue();

			if (statusCode != 200) {
				log.error("Exception executing delete pipeline, status code: " + statusCode);
				throw new DataflowServiceException("Exception executing delete pipeline, status code: " + statusCode);
			}

			pipelineRepository.delete(pl);
			log.info("Pipeline for user: " + user + WITH_NAME_STR + name + ", successfully deleted");
		} else {
			log.error("Exception executing delete pipeline, permission denied");
			throw new DataflowServiceException("Error delete pipeline, permission denied");
		}
	}

	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body,
			String user) throws URISyntaxException, ClientProtocolException, IOException {
		return sendHttp(
				requestServlet.getServletPath()
						+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : ""),
				httpMethod, body, user, requestServlet.getContentType() == null ? MediaType.APPLICATION_JSON.toString()
						: requestServlet.getContentType());
	}

	public ResponseEntity<byte[]> sendHttpBinary(HttpServletRequest requestServlet, HttpMethod httpMethod, String body,
			String user) throws URISyntaxException, ClientProtocolException, IOException {
		return sendHttpBinary(requestServlet.getServletPath(), httpMethod, body, user);
	}

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String user,
			String contentType) throws URISyntaxException, ClientProtocolException, IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		return sendHttp(url, httpMethod, body, headers, user);
	}

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return sendHttp(url, httpMethod, body, headers, user);
	}

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, String user)
			throws URISyntaxException, ClientProtocolException, IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return sendHttpBinary(url, httpMethod, body, headers, user);
	}

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			String userId) throws URISyntaxException, ClientProtocolException, IOException {
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		User user = userRepository.findByUserId(userId);
		headers.add("Authorization", encryptRestUserpass(user));

		List<String> lHeaderRequestBy = new ArrayList<String>();
		lHeaderRequestBy.add("Data Collector");
		headers.put("X-Requested-By", lHeaderRequestBy);

		org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<String>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString() + " Dataflow");
		ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("rest"))), httpMethod,
					request, String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Dataflow");
		return new ResponseEntity<String>(response.getBody(), response.getHeaders(),
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			String userId) throws URISyntaxException, ClientProtocolException, IOException {
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		User user = userRepository.findByUserId(userId);
		headers.add("Authorization", encryptRestUserpass(user));

		org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<String>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString() + " Dataflow");
		ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("rest"))), httpMethod,
					request, byte[].class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Dataflow");
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<byte[]>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	public Pipeline getPipeline(String identification, String userId) {
		Pipeline pl = pipelineRepository.findByIdentification(identification);
		if (hasUserPermissionInPipeline(pl, userId)) {
			return pl;
		} else {
			return null;
		}
	}

	public List<Pipeline> getPipelines(String userId) {
		User user = userRepository.findByUserId(userId);
		if (!user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return pipelineRepository.findByUser(user);
		} else {
			return pipelineRepository.findAll();
		}
	}

	private boolean hasUserPermissionInPipeline(Pipeline pl, String userId) {
		User user = userRepository.findByUserId(userId);
		return hasUserPermissionInPipeline(pl, user);
	}

	private boolean hasUserPermissionInPipeline(Pipeline pl, User user) {
		return user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| pl.getUser().getUserId().equals(user.getUserId());
	}

	@Override
	public boolean hasUserPermissionForPipeline(String pipelineId, String userId) {
		Pipeline pl = this.pipelineRepository.findByIdstreamsets(pipelineId);
		if (pl != null)
			return this.hasUserPermissionInPipeline(pl, userId);
		return false;
	}

	/*
	 * @Override public ResponseEntity<String> runParagraph(String zeppelinId,
	 * String paragraphId, String body) throws ClientProtocolException,
	 * URISyntaxException, IOException { ResponseEntity<String> responseEntity;
	 * responseEntity =
	 * sendHttp("/api/notebook/run/".concat(zeppelinId).concat("/").concat(
	 * paragraphId), HttpMethod.POST, body); if (responseEntity.getStatusCode() ==
	 * HttpStatus.OK) { responseEntity =
	 * sendHttp("/api/notebook/".concat(zeppelinId).concat("/paragraph/").concat(
	 * paragraphId), HttpMethod.GET, ""); } return responseEntity; }
	 * 
	 * @Override public ResponseEntity<String> runAllParagraphs(String zeppelinId)
	 * throws ClientProtocolException, URISyntaxException, IOException { return
	 * sendHttp("/api/notebook/job/".concat(zeppelinId), HttpMethod.POST, ""); }
	 * 
	 * @Override public ResponseEntity<String> getParagraphResult(String zeppelinId,
	 * String paragraphId) throws ClientProtocolException, URISyntaxException,
	 * IOException { return
	 * sendHttp("/api/notebook/".concat(zeppelinId).concat("/paragraph/").concat(
	 * paragraphId), HttpMethod.GET, ""); }
	 */

}
