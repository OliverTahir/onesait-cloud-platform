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
package com.minsait.onesait.platform.controlpanel.rest.management.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.UserApiDTO;
import com.minsait.onesait.platform.controlpanel.rest.ManagementRestServices;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Api(value = "APIs management", tags = { "APIs management service" })
@RestController
@Slf4j
public class APIManagementController extends ManagementRestServices {

	@Autowired
	ClientPlatformService clientPlatformService;
	@Autowired
	ApiManagerService apiManagerService;
	@Autowired
	JWTService jwtService;

	@ApiOperation(value = "Authorize user for api")
	@PostMapping(value = "/authorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> authorize(
			@ApiParam(value = "Api Id  ", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@RequestHeader("Authorization") String authorization) {

		final String loggedUser = jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService
				.loadAPISByFilter(apiId, "", loggedUser, loggedUser).stream()
				.filter(a -> a.getUser().getUserId().equals(loggedUser) && a.getId().equals(apiId))
				.collect(Collectors.toList());

		UserApi userApi = null;
		if (!apis.isEmpty()) {
			for (final com.minsait.onesait.platform.config.model.Api api : apis) {
				userApi = apiManagerService.updateAuthorization(api.getId(), userId);
			}
			if (userApi != null) {
				final UserApiDTO userApiDTO = new UserApiDTO(userApi);
				return new ResponseEntity<UserApiDTO>(userApiDTO, HttpStatus.CREATED);
			}
		}
		return new ResponseEntity<UserApiDTO>(HttpStatus.BAD_REQUEST);

	}

	@ApiOperation(value = "Authorize user for api")
	@PostMapping(value = "/deauthorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> deauthorize(
			@ApiParam(value = "Api Id ", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@RequestHeader("Authorization") String authorization) {
		final String loggedUser = jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService
				.loadAPISByFilter(apiId, "", loggedUser, loggedUser).stream()
				.filter(a -> a.getUser().getUserId().equals(loggedUser) && a.getId().equals(apiId))
				.collect(Collectors.toList());
		if (!apis.isEmpty()) {
			apiManagerService.removeAuthorizationByApiAndUser(apiId, userId);
			return new ResponseEntity<String>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} else
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

	}
}
