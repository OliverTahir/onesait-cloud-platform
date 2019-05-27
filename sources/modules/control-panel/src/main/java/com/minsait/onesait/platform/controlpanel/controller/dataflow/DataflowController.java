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
package com.minsait.onesait.platform.controlpanel.controller.dataflow;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dataflow")
@Controller
@Slf4j
public class DataflowController {

	@Autowired
	private DataflowService dataflowService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	ServletContext context;

	@Transactional
	// @PreAuthorize("hasRole('ROL_ADMINISTRADOR') or hasRole('ROL_ANALYTICS')")
	@RequestMapping(value = "/app/rest/v1/pipeline/{name}", method = RequestMethod.PUT)
	@ResponseBody
	public String createPipeline(@PathVariable("name") String name, @RequestParam("autoGeneratePipelineId") boolean autoGeneratePipelineId, @RequestParam("description") String description) {
		String idstreamsets = "fail";
		try {
			idstreamsets = dataflowService.createPipeline(name, description, utils.getUserId()).getIdstreamsets();
		} catch (Exception e) {
			log.error("Cannot create pipeline: ", e);
			return "fail";
		}
		return idstreamsets;
	}

	@Transactional
	// @PreAuthorize("hasRole('ROL_ADMINISTRADOR') or hasRole('ROL_ANALYTICS')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
	public String removeNotebook(@PathVariable("id") String id, Model uiModel) {
		dataflowService.removePipeline(id, utils.getUserId());
		uiModel.asMap().clear();
		return "redirect:/dataflow/list";
	}

	// @PreAuthorize("hasRole('ROL_ADMINISTRADOR') or hasRole('ROL_ANALYTICS')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel) {
		uiModel.addAttribute("lpl", dataflowService.getPipelines(utils.getUserId()));
		uiModel.addAttribute("user", utils.getUserId());
		return "dataflow/list";
	}
	
	@RequestMapping(value = { "/app/rest/pipeline/{id}/**" }, method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> pipelineRestUserJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body, @PathVariable("id") String id)
			throws ClientProtocolException, URISyntaxException, IOException {
		if(utils.isAdministrator() || dataflowService.hasUserPermissionForPipeline(id,utils.getUserId())) {
			return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
		}
		else {
			return null;
		}
		
	}
	
	@RequestMapping(value = { "/app/rest/pipelines/**" }, method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> pipelineRestAdminJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws ClientProtocolException, URISyntaxException, IOException {
		if(utils.isAdministrator()) {
			return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
		}
		else {
			return null;
		}
		
	}

	// @PreAuthorize("hasRole('ROL_ADMINISTRADOR')")
	@RequestMapping(value = { "/app/rest/**" }, method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> adminAppRestPutJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws ClientProtocolException, URISyntaxException, IOException {
		return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
	}
	
	@RequestMapping(value = { "/app/rest/v1/definitions/stages/{lib}/{id}/icon"}, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> analyAppRestBinary(Model uiModel, HttpServletRequest request)
			throws ClientProtocolException, URISyntaxException, IOException {
		return dataflowService.sendHttpBinary(request, HttpMethod.GET, "", utils.getUserId());
	}
	
	//@PreAuthorize("hasRole('ROL_ADMINISTRADOR') or hasRole('ROL_ANALYTICS')")
	@RequestMapping(value = {"/app/collector/pipeline/{id}", "/app/collector/logs/{name}/{id}" })
	public String indexAppViewPipeline(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {
		if(utils.isAdministrator() || dataflowService.hasUserPermissionForPipeline(id,utils.getUserId())) {
			return "dataflow/index";
		}
		else {
			return "redirect:/403";
		}
	}

	//@PreAuthorize("hasRole('ROL_ADMINISTRADOR')")
	@RequestMapping(value = {"/app", "/app/collector/jvmMetrics", "/app/collector/logs", "/app/collector/configuration", "/app/collector/packageManager" })
	public String indexAppRedirectNoPath(Model uiModel, HttpServletRequest request) {
		if(utils.isAdministrator()) {
			return "dataflow/index";
		}
		else {
			return "redirect:/403";
		}
	}
	
	

}