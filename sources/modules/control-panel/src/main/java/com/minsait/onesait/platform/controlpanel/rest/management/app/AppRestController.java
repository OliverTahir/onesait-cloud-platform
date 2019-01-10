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
package com.minsait.onesait.platform.controlpanel.rest.management.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.ManagementRestServices;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.Realm;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmAssociation;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmRole;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmUpdate;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmUser;
import com.minsait.onesait.platform.controlpanel.rest.management.app.model.RealmUserAuth;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Realm Management", tags = { "Realm management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@PreAuthorize("!hasRole('USER')")
public class AppRestController extends ManagementRestServices {
	
	private static final String USER_STR = "User \"";
	private static final String NOT_EXIST = "\" does not exist";
	private static final String REALM_STR = "Realm \"";
	private static final String OP_REALM = "/realms";

	@Autowired
	private AppService appService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper mapper;

	@ApiOperation(value = "Get single realm info")
	@GetMapping(OP_REALM + "/{id}")
	@ApiResponses(@ApiResponse(response = Realm.class, code = 200, message = "OK"))
	public ResponseEntity<?> getRealm(
			@ApiParam(value = "Realm id", required = true) @PathVariable("id") String realmId) {
		final List<AppRole> allRoles = appService.getAllRoles();
		final App realm = appService.getByIdentification(realmId);
		if (realm != null)
			return new ResponseEntity<>(new Realm(realm, allRoles), HttpStatus.OK);
		else
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);

	}

	@ApiOperation(value = "Get realm's configured user extra fields")
	@GetMapping(OP_REALM + "/{id}" + "/user-extra-fields")
	@ApiResponses(@ApiResponse(response = JsonNode.class, code = 200, message = "OK"))
	public ResponseEntity<?> getRealmUserExtraFields(
			@ApiParam(value = "Realm id", required = true) @PathVariable("id") String realmId) throws IOException {

		final App realm = appService.getByIdentification(realmId);
		if (realm != null)
			if (StringUtils.isEmpty(realm.getUserExtraFields()))
				return new ResponseEntity<>(REALM_STR + realmId + "\" does not have user extra fields defined",
						HttpStatus.OK);
			else
				return new ResponseEntity<>(mapper.readValue(realm.getUserExtraFields(), JsonNode.class),
						HttpStatus.OK);
		else
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);

	}

	@ApiOperation(value = "Update realm's user extra fields JSON config")
	@PatchMapping(OP_REALM + "/{id}" + "/user-extra-fields")
	public ResponseEntity<?> patchRealmUserExtraFields(
			@ApiParam("Realm user's extra fields") @RequestBody String userExtraFields,
			@ApiParam(value = "Realm id", required = true) @PathVariable("id") String realmId, Errors errors) {

		final App realm = appService.getByIdentification(realmId);
		if (realm != null) {
			try {
				final JsonNode extrasJson = mapper.readTree(userExtraFields);
				realm.setUserExtraFields(mapper.writeValueAsString(extrasJson));
			} catch (final IOException e) {
				return new ResponseEntity<>("Input is not valid JSON", HttpStatus.BAD_REQUEST);
			}
			appService.updateApp(realm);
			return new ResponseEntity<>(HttpStatus.OK);
		} else
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);

	}

	@ApiOperation(value = "Get all realms info")
	@GetMapping(OP_REALM)
	@ApiResponses(@ApiResponse(response = Realm[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getRealms() {
		final List<AppRole> allRoles = appService.getAllRoles();
		final List<Realm> realms = appService.getAllApps().stream().map(a -> new Realm(a, allRoles))
				.collect(Collectors.toList());
		return new ResponseEntity<>(realms, HttpStatus.OK);

	}

	@ApiOperation(value = "Create a realm")
	@PostMapping(OP_REALM)
	public ResponseEntity<?> create(@ApiParam(value = "Realm", required = true) @RequestBody @Valid RealmCreate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App app = new App();
		app.setAppId(realm.getRealmId());
		app.setDescription(realm.getDescription());
		app.setName(realm.getName());
		realm.getRoles().stream().map(r -> realmRole2AppRole(r, app)).forEach(r -> app.getAppRoles().add(r));
		appService.createApp(app);
		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	@ApiOperation(value = "Updates a realm")
	@PutMapping(OP_REALM + "/{id}")
	public ResponseEntity<?> update(@ApiParam(value = "Realm id", required = true) @PathVariable("id") String realmId,
			@ApiParam(value = "New Realm Description", required = true) @RequestBody @Valid RealmUpdate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		try {
			final App appDb = appService.getByIdentification(realmId);
			if (appDb == null)
				return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
			appDb.setDescription(realm.getDescription());
			appDb.setName(realm.getName());
			appService.updateApp(appDb);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Deletes a realm")
	@DeleteMapping(OP_REALM + "/{id}")
	public ResponseEntity<?> delete(@ApiParam(value = "Realm id", required = true) @PathVariable("id") String realmId) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		try {
			appService.deleteApp(realmId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Authorizes user with a role in a existing Realm")
	@PostMapping(OP_REALM + "/authorization")
	public ResponseEntity<?> createAuthorization(
			@ApiParam(value = "Realm Authorization", required = true) @Valid @RequestBody RealmUserAuth authorization,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		if (appService.getByIdentification(authorization.getRealmId()) == null)
			return new ResponseEntity<>(REALM_STR + authorization.getRealmId() + NOT_EXIST,
					HttpStatus.BAD_REQUEST);
		if (userService.getUserByIdentification(authorization.getUserId()) == null)
			return new ResponseEntity<>(USER_STR + authorization.getUserId() + NOT_EXIST,
					HttpStatus.BAD_REQUEST);
		try {
			final AppRole role = appService.getByRoleNameAndApp(authorization.getRoleName(),
					appService.getByIdentification(authorization.getRealmId()));
			if (role == null)
				return new ResponseEntity<>("Role \"" + authorization.getRoleName() + "\" does not exist in Realm "
						+ authorization.getRealmId(), HttpStatus.BAD_REQUEST);
			appService.createUserAccess(authorization.getRealmId(), authorization.getUserId(), role.getId());
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Invalidates user authorization for a Realm")
	@DeleteMapping(OP_REALM + "/authorization/realm/{realmId}/user/{userId}")
	public ResponseEntity<?> deleteAuthorization(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		try {
			final AppRole role = appService
					.getByIdentification(realmId).getAppRoles().stream().filter(r -> r.getAppUsers().stream()
							.filter(u -> u.getUser().getUserId().equals(userId)).findAny().orElse(null) != null)
					.findAny().orElse(null);
			if (role == null)
				return new ResponseEntity<>(
						"Authorization for user \"" + userId + "\" in Realm \"" + realmId + "\" not found.",
						HttpStatus.BAD_REQUEST);
			appService.deleteUserAccess(role.getAppUsers().stream().filter(u -> u.getUser().getUserId().equals(userId))
					.findFirst().get().getId());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Creates a realm association given parent and child realms, as well as respective roles")
	@PostMapping(OP_REALM + "/association")
	public ResponseEntity<?> createAssociation(
			@ApiParam(value = "Realm Association", required = true) @Valid @RequestBody RealmAssociation association,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		if (appService.getByIdentification(association.getChildRealmId()) == null
				|| appService.getByIdentification(association.getParentRealmId()) == null)
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		try {
			appService.createAssociation(association.getParentRoleName(), association.getChildRoleName(),
					association.getParentRealmId(), association.getChildRealmId());
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Deletes a realm's association")
	@DeleteMapping(OP_REALM
			+ "/association/parent-realm/{parentRealmId}/parent-role/{parentRole}/child-realm/{childRealmId}/child-role/{childRole}")
	public ResponseEntity<?> deleteAssociation(
			@ApiParam(value = "Parent Realm id", required = true) @PathVariable("parentRealmId") String parentRealmId,
			@ApiParam(value = "Child Realm id", required = true) @PathVariable("childRealmId") String childRealmId,
			@ApiParam(value = "Parent role", required = true) @PathVariable("parentRole") String parentRole,
			@ApiParam(value = "Child role", required = true) @PathVariable("childRole") String childRole) {

		if (appService.getByIdentification(childRealmId) == null
				|| appService.getByIdentification(parentRealmId) == null)
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		try {
			appService.deleteAssociation(parentRole, childRole, parentRealmId, childRealmId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get all roles in a Realm")
	@GetMapping(OP_REALM + "/{realmId}/roles")
	@ApiResponses(@ApiResponse(response = RealmRole[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getRoles(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId) {
		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		final List<RealmRole> roles = appService.getByIdentification(realmId).getAppRoles().stream()
				.map(r -> new RealmRole(r.getName(), r.getDescription())).collect(Collectors.toList());
		return new ResponseEntity<>(roles, HttpStatus.OK);
	}

	@ApiOperation(value = "Creates a role in a Realm")
	@PostMapping(OP_REALM + "/{realmId}/roles")
	public ResponseEntity<?> addRole(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId,
			@ApiParam(value = "Realm role", required = true) @Valid @RequestBody RealmRole role) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		final App app = appService.getByIdentification(realmId);
		final AppRole newRole = new AppRole();
		newRole.setApp(app);
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		app.getAppRoles().add(newRole);
		appService.updateApp(app);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@ApiOperation(value = "Deletes a role in a Realm")
	@DeleteMapping(OP_REALM + "/{realmId}/roles/{roleName}")
	public ResponseEntity<?> deleteRole(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId,
			@ApiParam(value = "Role name", required = true) @PathVariable("roleName") String roleName) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		final App app = appService.getByIdentification(realmId);
		final AppRole role = appService.getByRoleNameAndApp(roleName, app);
		if (role == null)
			return new ResponseEntity<>("Role \"" + roleName + "\" does not exist in realm \"" + realmId + "\"",
					HttpStatus.BAD_REQUEST);
		appService.deleteRole(role);
		appService.updateApp(app);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get all users in a Realm")
	@GetMapping(OP_REALM + "/{realmId}/users")
	@ApiResponses(@ApiResponse(response = RealmUser[].class, code = 200, message = "OK"))
	public ResponseEntity<?> getUsers(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);
		final List<RealmUser> users = new ArrayList<>();
		appService.getByIdentification(realmId).getAppRoles().forEach(r -> users.addAll(r.getAppUsers().stream()
				.map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
						.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
						.username(u.getUser().getUserId()).build())
				.collect(Collectors.toList())));
		return new ResponseEntity<>(users, HttpStatus.OK);

	}

	@ApiOperation(value = "Gets a user in  a Realm")
	@GetMapping(OP_REALM + "/{realmId}/users/{userId}")
	@ApiResponses(@ApiResponse(response = RealmUser.class, code = 200, message = "OK"))
	public ResponseEntity<?> getUser(
			@ApiParam(value = "Realm id", required = true) @PathVariable("realmId") String realmId,
			@ApiParam(value = "User id", required = true) @PathVariable("userId") String userId) {

		if (appService.getByIdentification(realmId) == null)
			return new ResponseEntity<>(REALM_STR + realmId + NOT_EXIST, HttpStatus.BAD_REQUEST);

		final AppRole role = appService
				.getByIdentification(realmId).getAppRoles().stream().filter(r -> r.getAppUsers().stream()
						.map(u -> u.getUser().getUserId()).collect(Collectors.toList()).contains(userId))
				.findFirst().orElse(null);
		if (role != null) {
			final AppUser user = role.getAppUsers().stream().filter(u -> u.getUser().getUserId().equals(userId))
					.findFirst().orElse(null);
			if (user == null)
				return new ResponseEntity<>(USER_STR + userId + "\" does not exist in realm \"" + realmId + "\"",
						HttpStatus.BAD_REQUEST);
			else
				return new ResponseEntity<>(RealmUser.builder().avatar(user.getUser().getAvatar())
						.extraFields(user.getUser().getExtraFields()).fullName(user.getUser().getFullName())
						.mail(user.getUser().getEmail()).role(user.getRole().getName())
						.username(user.getUser().getUserId()).build(), HttpStatus.OK);
		} else
			return new ResponseEntity<>(USER_STR + userId + NOT_EXIST, HttpStatus.BAD_REQUEST);

	}

	private AppRole realmRole2AppRole(RealmRole role, App app) {
		final AppRole appRole = new AppRole();
		appRole.setApp(app);
		appRole.setDescription(role.getDescription());
		appRole.setName(role.getName());

		return appRole;
	}

}
