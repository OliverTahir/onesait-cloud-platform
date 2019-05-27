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
package com.minsait.onesait.platform.controlpanel.controller.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.app.dto.AppAssociatedCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppDTO;
import com.minsait.onesait.platform.config.services.app.dto.RoleAppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.UserAppCreateDTO;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.security.ldap.ri.service.LdapUserService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/apps")
@PreAuthorize("!hasRole('USER')")
@Slf4j
public class AppController {

	@Autowired
	private AppService appService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private AppRoleRepository appRoleRepository;
	@Autowired
	private ProjectService projectService;

	private final static String NO_APP_CREATION = "Cannot create app";
	private final static String REDIRECT_APPS_CREATE = "redirect:/apps/create";
	private final static String REDIRECT_APPS_LIST = "redirect:/apps/list";
	private final static String REDIRECT_APPS_UPDATE = "redirect:/apps/update/";

	@Autowired(required = false)
	private LdapUserService ldapUserService;
	@Value("${onesaitplatform.authentication.provider}")
	private String provider;
	@Value("${ldap.base}")
	private String ldapBaseDn;
	private static final String LDAP = "ldap";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification) {

		List<App> apps = new ArrayList<App>();

		if (identification == null || identification.equals("")) {
			apps = appService.getAllApps();
		} else {
			final App app = appService.getByIdentification(identification);
			if (app != null) {
				apps.add(app);
			}
		}
		populateAppList(model, apps);

		return "apps/list";

	}

	private void populateAppList(Model model, List<App> apps) {

		final List<AppDTO> appsDTO = new ArrayList<AppDTO>();

		if (apps != null && apps.size() > 0) {
			for (final App app : apps) {
				final AppDTO appDTO = new AppDTO();
				appDTO.setAppId(app.getAppId());
				appDTO.setDateCreated(app.getCreatedAt());
				appDTO.setDescription(app.getDescription());
				appDTO.setName(app.getName());

				if (app.getAppRoles() != null && app.getAppRoles().size() > 0) {
					final List<String> list = new ArrayList<String>();
					for (final AppRole appRole : app.getAppRoles()) {
						list.add(appRole.getName());
					}
					appDTO.setRoles(StringUtils.arrayToDelimitedString(list.toArray(), ", "));
				}
				appsDTO.add(appDTO);
			}
		}

		model.addAttribute("apps", appsDTO);

	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("app", new AppCreateDTO());
		return "apps/create";
	}

	private AppRole dto2appRole(RoleAppCreateDTO role, App app) {
		final AppRole appRole = new AppRole();
		appRole.setApp(app);
		appRole.setDescription(role.getDescription());
		appRole.setName(role.getName());

		return appRole;
	}

	@PostMapping(value = { "/create" })
	public String createApp(Model model, @Valid AppCreateDTO app, BindingResult bindingResult,
			RedirectAttributes redirect) {

		try {

			final App napp = new App();
			napp.setAppId(app.getAppId());
			napp.setName(app.getName());
			napp.setSecret(app.getSecret());
			napp.setDescription(app.getDescription());
			napp.setTokenValiditySeconds(app.getTokenValiditySeconds());

			final ObjectMapper mapper = new ObjectMapper();
			final List<RoleAppCreateDTO> roles = new ArrayList<RoleAppCreateDTO>(
					mapper.readValue(app.getRoles(), new TypeReference<List<RoleAppCreateDTO>>() {
					}));
			roles.stream().map(x -> dto2appRole(x, napp)).forEach(x -> napp.getAppRoles().add(x));
			appService.createApp(napp);

		} catch (final AppServiceException e) {
			log.debug(NO_APP_CREATION);
			utils.addRedirectException(e, redirect);
			return REDIRECT_APPS_CREATE;
		} catch (final JsonParseException e) {
			log.debug(NO_APP_CREATION);
			utils.addRedirectException(e, redirect);
			return REDIRECT_APPS_CREATE;
		} catch (final JsonMappingException e) {
			log.debug(NO_APP_CREATION);
			utils.addRedirectException(e, redirect);
			return REDIRECT_APPS_CREATE;
		} catch (final IOException e) {
			log.debug(NO_APP_CREATION);
			utils.addRedirectException(e, redirect);
			return REDIRECT_APPS_CREATE;
		}
		return REDIRECT_APPS_LIST;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		final App app = appService.getByIdentification(id);

		if (app != null) {

			final AppCreateDTO appDTO = new AppCreateDTO();
			appDTO.setAppId(app.getAppId());
			appDTO.setName(app.getName());
			appDTO.setSecret(app.getSecret());
			appDTO.setDescription(app.getDescription());
			appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

			final List<UserAppCreateDTO> usersList = new ArrayList<UserAppCreateDTO>();
			final List<AppAssociatedCreateDTO> appsAssociatedList = new ArrayList<AppAssociatedCreateDTO>();

			if (app.getAppRoles() != null && app.getAppRoles().size() > 0) {
				final List<String> rolesList = new ArrayList<String>();
				for (final AppRole appRole : app.getAppRoles()) {
					rolesList.add(appRole.getName());
					if (appRole.getAppUsers() != null && !appRole.getAppUsers().isEmpty()) {
						for (final AppUser appUser : appRole.getAppUsers()) {
							final UserAppCreateDTO userAppDTO = new UserAppCreateDTO();
							userAppDTO.setId(String.valueOf(appUser.getId()));
							userAppDTO.setRoleName(appUser.getRole().getName());
							userAppDTO.setUser(appUser.getUser().getUserId());
							usersList.add(userAppDTO);
						}
						appDTO.setUsers(StringUtils.arrayToDelimitedString(usersList.toArray(), ", "));
					}
				}
				appDTO.setRoles(StringUtils.arrayToDelimitedString(rolesList.toArray(), ", "));
			}

			// Si la app es padre:
			if (app.getChildApps() != null && app.getChildApps().size() > 0) {
				for (final AppRole fatherAppRole : app.getAppRoles()) {
					if (fatherAppRole.getChildRoles() != null && fatherAppRole.getChildRoles().size() > 0) {
						for (final AppRole childAppRole : fatherAppRole.getChildRoles()) {
							final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
							appAssociatedDTO.setId(fatherAppRole.getName() + ':' + childAppRole.getName());
							appAssociatedDTO.setFatherAppId(app.getAppId());
							appAssociatedDTO.setChildAppId(childAppRole.getApp().getAppId());
							appAssociatedDTO.setFatherRoleName(fatherAppRole.getName());
							appAssociatedDTO.setChildRoleName(childAppRole.getName());
							appsAssociatedList.add(appAssociatedDTO);
						}
					}
				}
			}

			// Si la app es hija:
			for (final AppRole appRole : app.getAppRoles()) {
				for (final AppRole role : appService.getAllRoles()) {
					if (role.getChildRoles() != null && role.getChildRoles().contains(appRole)) {
						final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
						appAssociatedDTO.setId(role.getName() + ':' + appRole.getName());
						appAssociatedDTO.setFatherAppId(role.getApp().getAppId());
						appAssociatedDTO.setChildAppId(app.getAppId());
						appAssociatedDTO.setFatherRoleName(role.getName());
						appAssociatedDTO.setChildRoleName(appRole.getName());
						appsAssociatedList.add(appAssociatedDTO);
					}
				}
			}

			mapRolesAndUsersToJson(app, appDTO);
			final List<User> users = userService.getAllUsers();
			List<App> appsToChoose = new ArrayList<App>();

			// Si nuestra app es hija de otra no podremos asociarle una app
			for (final App appToChoose : appService.getAllApps()) {
				if (appToChoose.getChildApps() != null) {
					if (appToChoose.getChildApps().contains(app)) {
						appsToChoose.clear();
						break;
					} else {
						// Quitamos del 'select' de apps hijas: la propia app y las apps que ya tienen
						// apps hijas
						appsToChoose = appService.getAllApps().stream()
								.filter(appToSelect -> ((!appToSelect.getAppId().equals(app.getAppId()))
										&& (appToSelect.getChildApps() == null
												|| appToSelect.getChildApps().size() == 0)))
								.collect(Collectors.toList());
					}
				}
			}
			if (app.getProject() != null)
				model.addAttribute("project", app.getProject());
			else
				model.addAttribute("project", new Project());
			model.addAttribute("app", appDTO);
			model.addAttribute("roles", app.getAppRoles());
			model.addAttribute("users", users);
			model.addAttribute("appsChild", appsToChoose);
			model.addAttribute("authorizations", usersList);
			model.addAttribute("associations", appsAssociatedList);
			model.addAttribute("ldapEnabled", ldapActive());
			model.addAttribute("baseDn", ldapBaseDn);
			model.addAttribute("projectTypes", Project.ProjectType.values());
			model.addAttribute("projects",
					projectService.getAllProjects().stream()
							.filter(p -> p.getApp() == null && CollectionUtils.isEmpty(p.getUsers()))
							.collect(Collectors.toList()));

			return "apps/create";
		} else {
			return REDIRECT_APPS_LIST;
		}
	}

	private void mapRolesAndUsersToJson(App app, AppCreateDTO appDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		final ArrayNode arrayNodeUser = mapper.createArrayNode();

		for (final AppRole appRole : app.getAppRoles()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("name", appRole.getName());
			on.put("description", appRole.getDescription());
			arrayNode.add(on);
			if (appRole.getAppUsers() != null && !appRole.getAppUsers().isEmpty())
				for (final AppUser appUser : appRole.getAppUsers()) {
					final ObjectNode onUser = mapper.createObjectNode();
					onUser.put("user", appUser.getUser().getUserId());
					onUser.put("roleName", appUser.getRole().getName());
					arrayNodeUser.add(onUser);
				}
		}

		try {
			appDTO.setRoles(mapper.writer().writeValueAsString(arrayNode));
			appDTO.setUsers(mapper.writer().writeValueAsString(arrayNodeUser));
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateApp(Model model, @PathVariable("id") String id, @Valid AppCreateDTO appDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some app properties missing");
			utils.addRedirectMessage("app.validation.error", redirect);
			return REDIRECT_APPS_UPDATE + id;
		}

		try {
			appService.updateApp(appDTO);
		} catch (final AppServiceException e) {
			log.debug("Cannot update app");
			utils.addRedirectMessage("app.update.error", redirect);
			return REDIRECT_APPS_CREATE;
		}

		return REDIRECT_APPS_LIST;
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final App app = appService.getByIdentification(id);

		if (app != null) {

			final AppCreateDTO appDTO = new AppCreateDTO();
			appDTO.setAppId(app.getAppId());
			appDTO.setName(app.getName());
			appDTO.setSecret(app.getSecret());
			appDTO.setDescription(app.getDescription());
			appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

			final List<UserAppCreateDTO> usersList = new ArrayList<UserAppCreateDTO>();
			final List<AppAssociatedCreateDTO> associations = new ArrayList<AppAssociatedCreateDTO>();

			if (app.getAppRoles() != null && app.getAppRoles().size() > 0) {
				final List<String> rolesList = new ArrayList<String>();
				for (final AppRole appRole : app.getAppRoles()) {
					rolesList.add(appRole.getName());
					if (appRole.getAppUsers() != null && !appRole.getAppUsers().isEmpty()) {
						for (final AppUser appUser : appRole.getAppUsers()) {
							final UserAppCreateDTO userAppDTO = new UserAppCreateDTO();
							userAppDTO.setId(String.valueOf(appUser.getId()));
							userAppDTO.setRoleName(appUser.getRole().getName());
							userAppDTO.setUser(appUser.getUser().getUserId());
							userAppDTO.setUserName(appUser.getUser().getFullName());
							usersList.add(userAppDTO);
						}
						appDTO.setUsers(StringUtils.arrayToDelimitedString(usersList.toArray(), ", "));
					}
				}
				appDTO.setRoles(StringUtils.arrayToDelimitedString(rolesList.toArray(), ", "));
			}

			if (app.getChildApps() != null && !app.getChildApps().isEmpty()) {
				for (final AppRole role : app.getAppRoles()) {
					if (role.getChildRoles() != null && !role.getChildRoles().isEmpty()) {
						for (final AppRole childRole : role.getChildRoles()) {
							final AppAssociatedCreateDTO associatedAppDTO = new AppAssociatedCreateDTO();
							associatedAppDTO.setId(role.getName() + ':' + childRole.getName());
							associatedAppDTO.setFatherAppId(app.getAppId());
							associatedAppDTO.setFatherRoleName(role.getName());
							associatedAppDTO.setChildAppId(childRole.getApp().getAppId());
							associatedAppDTO.setChildRoleName(childRole.getName());
							associations.add(associatedAppDTO);
						}
					}
				}
			}

			mapRolesAndUsersToJson(app, appDTO);

			model.addAttribute("app", appDTO);
			model.addAttribute("roles", app.getAppRoles());
			model.addAttribute("authorizations", usersList);
			model.addAttribute("associations", associations);
			return "apps/show";
		} else {
			return REDIRECT_APPS_LIST;
		}

	}

	@DeleteMapping("/{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {

			appService.deleteApp(id);
		} catch (final Exception e) {
			utils.addRedirectMessage("app.delete.error", redirect);
			return "/controlpanel/apps/list";
		}

		return "/controlpanel/apps/list";
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserAppCreateDTO> createAuthorization(@RequestParam String roleId, @RequestParam String appId,
			@RequestParam String userId) {
		try {

			final Long appUserId = appService.createUserAccess(appId, userId, roleId);
			final UserAppCreateDTO appUserDTO = new UserAppCreateDTO();
			appUserDTO.setId(String.valueOf(appUserId));
			appUserDTO.setRoleName(appRoleRepository.findOne(Long.valueOf(roleId)).getName());
			appUserDTO.setUser(userId);
			appUserDTO.setRoleId(roleId);

			return new ResponseEntity<UserAppCreateDTO>(appUserDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<UserAppCreateDTO>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/ldap", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserAppCreateDTO> createAuthorizationLdap(@RequestParam String roleId,
			@RequestParam String appId, @RequestParam String userId, @RequestParam String dn) {
		try {
			if (userService.getUser(userId) == null)
				ldapUserService.createUser(userId, dn);
			final Long appUserId = appService.createUserAccess(appId, userId, roleId);
			final UserAppCreateDTO appUserDTO = new UserAppCreateDTO();
			appUserDTO.setId(String.valueOf(appUserId));
			appUserDTO.setRoleName(appRoleRepository.findOne(Long.valueOf(roleId)).getName());
			appUserDTO.setUser(userId);
			appUserDTO.setRoleId(roleId);

			return new ResponseEntity<UserAppCreateDTO>(appUserDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<UserAppCreateDTO>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {
			appService.deleteUserAccess(Long.parseLong(id));
			return new ResponseEntity<String>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/association", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AppAssociatedCreateDTO> createAssociation(@RequestParam String fatherRoleId,
			@RequestParam String childRoleId) {
		try {

			final Map<String, String> result = appService.createAssociation(fatherRoleId, childRoleId);
			final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
			appAssociatedDTO.setId(result.get("fatherRoleName") + ':' + result.get("childRoleName"));
			appAssociatedDTO.setFatherAppId(result.get("fatherAppId"));
			appAssociatedDTO.setFatherRoleName(result.get("fatherRoleName"));
			appAssociatedDTO.setChildAppId(result.get("childAppId"));
			appAssociatedDTO.setChildRoleName(result.get("childRoleName"));

			return new ResponseEntity<AppAssociatedCreateDTO>(appAssociatedDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<AppAssociatedCreateDTO>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/association/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAssociation(@RequestParam String fatherRoleName,
			@RequestParam String childRoleName, String fatherAppId, String childAppId) {

		try {
			appService.deleteAssociation(fatherRoleName, childRoleName, fatherAppId, childAppId);
			return new ResponseEntity<String>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getRoles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Map<Long, String>> getRolesByApp(@RequestParam String appId) {
		final App app = appService.getByIdentification(appId);
		final Map<Long, String> roles = new HashMap<Long, String>();
		for (final AppRole role : app.getAppRoles()) {
			roles.put(role.getId(), role.getName());
		}
		return new ResponseEntity<Map<Long, String>>(roles, HttpStatus.CREATED);

	}

	@GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getUsers(@RequestParam("dn") String dn) throws InvalidNameException {
		final List<User> users = ldapUserService.getAllUsers(dn);
		return new ResponseEntity<>(users.stream().map(u -> u.getUserId()).collect(Collectors.toList()), HttpStatus.OK);

	}

	@GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getGroups(@RequestParam("dn") String dn) throws InvalidNameException {
		final List<String> groups = ldapUserService.getAllGroups(dn);
		return new ResponseEntity<>(groups, HttpStatus.OK);

	}

	@GetMapping(value = "/groups/{group}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getUsersInGroup(@RequestParam("dn") String dn,
			@PathVariable("group") String group) throws InvalidNameException {
		final List<User> users = ldapUserService.getAllUsersFromGroup(dn, group);
		return new ResponseEntity<>(users.stream().map(u -> u.getUserId()).collect(Collectors.toList()), HttpStatus.OK);

	}

	@PostMapping("/project")
	public String createProject(Model model, @Valid Project project, @RequestParam("appId") String appId,
			@RequestParam(value = "existingProject", required = false) String existingProject,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors() && StringUtils.isEmpty(existingProject)) {

			return REDIRECT_APPS_UPDATE + appId;
		} else {
			final App realm = appService.getByIdentification(appId);
			if (!StringUtils.isEmpty(project.getName()) && !StringUtils.isEmpty(project.getDescription())) {
				project.setApp(realm);
				project.setUser(userService.getUser(utils.getUserId()));
				realm.setProject(project);
				projectService.createProject(project);
			} else {
				final Project projectDB = projectService.getById(existingProject);
				realm.setProject(projectDB);
				projectDB.setApp(realm);
				projectService.updateProject(projectDB);
			}

			appService.updateApp(realm);
			return REDIRECT_APPS_UPDATE + appId;
		}

	}

	private boolean ldapActive() {
		if (LDAP.equals(provider))
			return true;
		else
			return false;
	}

}
