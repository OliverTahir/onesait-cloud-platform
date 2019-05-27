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
package com.minsait.onesait.platform.controlpanel.rest.deployment;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.controlpanel.rest.DeploymentRestServices;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.services.project.GitlabRestService;
import com.minsait.onesait.platform.controlpanel.services.project.OpenshiftService;
import com.minsait.onesait.platform.controlpanel.services.project.RancherService;
import com.minsait.onesait.platform.controlpanel.services.project.exceptions.GitlabException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Deployment", tags = { "Deployment service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@Slf4j
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class DeploymentRestService extends DeploymentRestServices {
	
	private static final String NO_CONF_FOR_RANCHER = "There's no configuration for Rancher on Realm ";
	private static final String DEFAULT_STR = "default";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private RancherService rancherService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private OpenshiftService openshiftService;
	@Autowired
	private GitlabRestService gitlabService;

	@ApiOperation("Deploys onesaitplatform environment")
	@PostMapping("deploy")
	public ResponseEntity<?> deploy(@ApiParam("Deploy Configuration") @RequestBody @Valid Deploy deploy,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		if (!utils.isAdministrator())
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		if (deploy.getCaasPlatform().equals(CaasPlatform.RANCHER)) {
			final String rancherConfigId;
			try {
				rancherConfigId = configurationService
						.getConfiguration(Configuration.Type.RANCHER, DEFAULT_STR, deploy.getRealm()).getId();
			} catch (final RuntimeException e) {
				log.error("There's no configuration for Rancher on Realm {}", deploy.getRealm());
				return new ResponseEntity<>(NO_CONF_FOR_RANCHER + deploy.getRealm(),
						HttpStatus.BAD_REQUEST);
			}
			try {
				rancherService.deployRancherEnvironment(rancherConfigId, deploy.getEnvironment(),
						deploy.getServices().stream().collect(Collectors.toMap(s -> s.toString(), s -> 1)),
						deploy.getUrl(), deploy.getProjectName());
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not deploy on Rancher environment " + deploy.getEnvironment()
						+ " reason: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else if (deploy.getCaasPlatform().equals(CaasPlatform.OPEN_SHIFT)) {
			String openshiftConfigId;
			try {
				openshiftConfigId = configurationService
						.getConfiguration(Configuration.Type.OPENSHIFT, DEFAULT_STR, deploy.getRealm()).getId();

			} catch (final RuntimeException e) {
				log.error("There's no configuration for Openshift on Realm {}", deploy.getRealm());
				return new ResponseEntity<>("There's no configuration for Openshift on Realm " + deploy.getRealm(),
						HttpStatus.BAD_REQUEST);
			}
			try {
				openshiftService.deployOpenshiftProject(openshiftConfigId, deploy.getProjectName(),
						deploy.getEnvironment(),
						deploy.getServices().stream().map(ps -> ps.name()).collect(Collectors.toList()),
						deploy.getUrl());
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not deploy on OpenShift project " + deploy.getEnvironment()
						+ " reason: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}

		return new ResponseEntity<>("Environment successfully deployed in " + deploy.getUrl(), HttpStatus.OK);
	}

	@ApiOperation("Get a list of existing environments/projects with pre-allocated resources")
	@GetMapping("/{caas}/environments/{realm}")
	@ApiResponse(code = 200, response = String[].class, message = "OK")
	public ResponseEntity<?> getEnvironments(@ApiParam("CaaS") @PathVariable("caas") CaasPlatform caasPlatform,
			@ApiParam("Url of deployment") @RequestParam("url") String url,
			@ApiParam("Realm or suffix for the configuration retrieval") @PathVariable("realm") String realm) {
		if (!utils.isAdministrator())
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		if (caasPlatform.equals(CaasPlatform.RANCHER)) {
			final String rancherConfigId;
			try {
				rancherConfigId = configurationService.getConfiguration(Configuration.Type.RANCHER, DEFAULT_STR, realm)
						.getId();
				final List<String> environments = rancherService.getRancherEnvironments(rancherConfigId, url);
				return new ResponseEntity<>(environments, HttpStatus.OK);
			} catch (final RuntimeException e) {
				log.error("There's no configuration for Rancher on Realm {}", realm);
				return new ResponseEntity<>(NO_CONF_FOR_RANCHER + realm,
						HttpStatus.BAD_REQUEST);
			}

		} else {
			try {
				final String openshiftConfigId = configurationService
						.getConfiguration(Configuration.Type.OPENSHIFT, DEFAULT_STR, realm).getId();
				final List<String> environments = openshiftService.getOpenshiftProjects(openshiftConfigId, url);
				return new ResponseEntity<>(environments, HttpStatus.OK);
			} catch (final RuntimeException e) {
				log.error("There's no configuration for Openshift on Realm {}", realm);
				return new ResponseEntity<>("There's no configuration for Openshift on Realm " + realm,
						HttpStatus.BAD_REQUEST);
			}

		}

	}

	@ApiOperation("Create Gitlab Project, add users, and authorize non-existing users")
	@PostMapping("/gitlab")
	@ApiResponse(code = 200, response = Integer.class, message = "The id of the project just created")
	public ResponseEntity<?> createGitlabProject(
			@ApiParam("Gitlab Parameters") @Valid @RequestBody GitlabInput gitlabInput, Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		if (!utils.isAdministrator())
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		String gitlabConfigId;
		try {
			gitlabConfigId = configurationService.getConfiguration(Type.GITLAB, gitlabInput.getRealm()).getId();
		} catch (final RuntimeException e) {
			log.error("There's no configuration for Girtbal on Realm {}", gitlabInput.getRealm());
			return new ResponseEntity<>(NO_CONF_FOR_RANCHER + gitlabInput.getRealm(),
					HttpStatus.BAD_REQUEST);
		}
		try {
			final String projectId = gitlabService.createGitlabProject(gitlabConfigId, gitlabInput.getName(),
					gitlabInput.getContributors(), gitlabInput.getUrl(), gitlabInput.isScaffolding());
			return new ResponseEntity<>(projectId, HttpStatus.OK);
		} catch (final GitlabException e) {
			log.error("Could not create project {}", e.getMessage());
			return new ResponseEntity<>("Could not create GitlabProject " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
