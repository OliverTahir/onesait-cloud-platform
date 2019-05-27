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
package com.minsait.onesait.platform.controlpanel.controller.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
@RequestMapping("/projects")
public class ProjectController {

	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private WebProjectService webprojectService;
	@Autowired
	private AppService appService;

	private static final String ALL_USERS = "ALL";
	private static final String PROJECT_OBJ_STR = "projectObj";
	private static final String ERROR_403 = "error/403";
	private static final String REDIRECT_PROJ_LIST = "redirect:/projects/list";
	private static final String PROJ_FRAG_USERTAB = "project/fragments/users-tab";
	private static final String PROJ_FRAG_RESTAB = "project/fragments/resources-tab";

	@GetMapping("list")

	public String list(Model model) {
		model.addAttribute("projects", projectService.getProjectsForUser(utils.getUserId()));
		return "project/list";
	}

	@GetMapping("create")
	@PreAuthorize("hasRole('DEVELOPER') OR hasRole('DATASCIENTIST')")
	public String create(Model model) {
		model.addAttribute(PROJECT_OBJ_STR, new Project());
		model.addAttribute("projectTypes", Project.ProjectType.values());
		return "project/create";
	}

	@GetMapping("show/{id}")
	public String show(Model model, @PathVariable("id") String projectId) {
		if (!projectService.isUserInProject(utils.getUserId(), projectId) && !utils.isAdministrator())
			return ERROR_403;
		final String creator = projectService.getById(projectId).getUser().toString();
		final boolean isCreator = creator.equals(utils.getUserId());
		model.addAttribute("urlsMap", getUrlsMap());
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		model.addAttribute("userRole", utils.getRole());
		if (utils.getRole().equals("ROLE_ADMINISTRATOR") || isCreator) {
			SortedSet<ProjectResourceAccess> pr = projectService.getById(projectId).getProjectResourceAccesses();
			Collection<List<ProjectResourceAccess>> prfil = pr.stream()
					.collect(Collectors.groupingBy(ProjectResourceAccess::getResource)).values();
			List<ProjectResourceAccess> pra = new ArrayList<ProjectResourceAccess>();
			for (List<ProjectResourceAccess> elem : prfil) {
				pra.add(elem.get(0));
			}
			model.addAttribute("objResources", pra);
		} else {
			model.addAttribute("objResources",
					projectService.getResourcesAccessesForUser(projectId, utils.getUserId()));
		}
		return "project/show";
	}

	@PostMapping("create")
	@PreAuthorize("hasRole('DEVELOPER') OR hasRole('DATASCIENTIST')")
	public String createProject(Model model, @Valid Project project) {
		project.setUser(userService.getUser(utils.getUserId()));
		projectService.createProject(project);
		return REDIRECT_PROJ_LIST;
	}

	@GetMapping("update/{id}")
	@PreAuthorize("hasRole('DEVELOPER') OR hasRole('ADMINISTRATOR') OR hasRole('DATASCIENTIST')")
	public String update(Model model, @PathVariable("id") String id) {
		if (!projectService.isUserAuthorized(id, utils.getUserId()))
			return ERROR_403;
		populateUsertabData(model, id);
		model.addAttribute("projectTypes", Project.ProjectType.values());
		model.addAttribute("resourceTypes", Resources.values());
		return "project/create";
	}

	@PutMapping("update/{id}")
	@PreAuthorize("hasRole('DEVELOPER') OR hasRole('ADMINISTRATOR') OR hasRole('DATASCIENTIST')")
	public String updateProject(Model model, @Valid Project project, @PathVariable("id") String id) {
		if (!projectService.isUserAuthorized(id, utils.getUserId()))
			return ERROR_403;
		projectService.updateWithParameters(project);
		return REDIRECT_PROJ_LIST;
	}

	@PostMapping("setrealm")
	public String setRealm(Model model, @RequestParam("realm") String realmId,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.setRealm(realmId, projectId);
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("unsetrealm")
	public String unsetRealm(Model model, @RequestParam("realm") String realmId,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.unsetRealm(realmId, projectId);
		populateUsertabData(model, projectId);

		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("adduser")
	public String addUser(Model model, @RequestParam("project") String projectId, @RequestParam("user") String userId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.addUserToProject(userId, projectId);
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("removeuser")
	public String removeUser(Model model, @RequestParam("project") String projectId,
			@RequestParam("user") String userId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.removeUserFromProject(userId, projectId);
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("addwebproject")
	public String addWebProject(Model model, @RequestParam("webProject") String webProjectId,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.addWebProject(webProjectId, projectId, utils.getUserId());
		populateWebProjectTabData(model, projectId);
		return "project/fragments/webprojects-tab";
	}

	@PostMapping("removewebproject")
	public String removeWebProject(Model model, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.removeWebProject(projectId);
		populateWebProjectTabData(model, projectId);
		return "project/fragments/webprojects-tab";
	}

	@GetMapping("resources")
	public String getResources(Model model, @RequestParam("identification") String identification,
			@RequestParam("type") Resources resource, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		model.addAttribute("resourcesMatch", getAllResourcesDTO(identification, resource, projectId));
		populateResourcesModal(model, projectId);
		return "project/fragments/resources-modal";
	}

	@GetMapping("authorizations")
	public String authorizationsTab(Model model, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		return PROJ_FRAG_RESTAB;
	}

	@PostMapping("authorizations")
	public String insertAuthorization(Model model, @RequestBody @Valid ProjectResourceAccessDTO authorization) {
		if (!projectService.isUserAuthorized(authorization.getProject(), utils.getUserId()))
			return ERROR_403;
		final Project project = projectService.getById(authorization.getProject());
		final Set<ProjectResourceAccess> accesses = new HashSet<>();
		if (project.getApp() != null) {
			if (authorization.getAuthorizing().equals(ALL_USERS)) {
				projectService.getProjectRoles(authorization.getProject())
						.forEach(ar -> accesses.add(ProjectResourceAccess.builder().access(authorization.getAccess())
								.appRole(appService.findRole(ar.getId()))
								.resource(resourceService.getResourceById(authorization.getResource())).project(project)
								.build()));
				resourceService.insertAuthorizations(accesses);

			} else
				resourceService
						.createUpdateAuthorization(ProjectResourceAccess.builder().access(authorization.getAccess())
								.appRole(appService.findRole(Long.parseLong(authorization.getAuthorizing())))
								.resource(resourceService.getResourceById(authorization.getResource())).project(project)
								.build());
		} else {
			if (authorization.getAuthorizing().equals(ALL_USERS)) {
				project.getUsers()
						.forEach(u -> accesses.add(ProjectResourceAccess.builder().access(authorization.getAccess())
								.user(u).resource(resourceService.getResourceById(authorization.getResource()))
								.project(project).build()));
				resourceService.insertAuthorizations(accesses);
			} else
				resourceService.createUpdateAuthorization(ProjectResourceAccess.builder()
						.access(authorization.getAccess()).user(userService.getUser(authorization.getAuthorizing()))
						.resource(resourceService.getResourceById(authorization.getResource())).project(project)
						.build());
		}
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(authorization.getProject()));
		return PROJ_FRAG_RESTAB;
	}

	@DeleteMapping("authorizations")
	public String deleteAuthorization(Model model, @RequestParam("id") String id,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		resourceService.removeAuthorization(id, projectId);
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		return PROJ_FRAG_RESTAB;
	}

	@PreAuthorize("hasRole('DEVELOPER') OR hasRole('ADMINISTRATOR')")
	@DeleteMapping("{id}")
	public String delete(Model model, @PathVariable("id") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId()))
			return ERROR_403;
		projectService.deleteProject(projectId);
		return REDIRECT_PROJ_LIST;
	}

	private void populateUsertabData(Model model, String projectId) {
		final Project project = projectService.getById(projectId);
		final List<?> members = projectService.getProjectMembers(project.getId());
		model.addAttribute("members", getMembersDTO(members));
		model.addAttribute("realms", projectService.getAvailableRealms());
		model.addAttribute("accesses", ResourceAccessType.values());
		model.addAttribute("users",
				userService.getAllActiveUsers().stream()
						.filter(u -> !u.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
						.collect(Collectors.toList()));
		model.addAttribute("webprojects",
				webprojectService.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), "", ""));
		model.addAttribute(PROJECT_OBJ_STR, project);
	}

	private void populateResourcesModal(Model model, String projectId) {
		final Project project = projectService.getById(projectId);
		final List<?> members = projectService.getProjectMembers(project.getId());
		model.addAttribute("accesses", ResourceAccessType.values());
		model.addAttribute(PROJECT_OBJ_STR, project);
		model.addAttribute("members", getMembersDTO(members));
		if (project.getApp() != null)
			model.addAttribute("roles", projectService.getProjectRoles(projectId));
	}

	private void populateWebProjectTabData(Model model, String projectId) {
		final Project project = projectService.getById(projectId);
		model.addAttribute("webprojects",
				webprojectService.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), "", ""));
		model.addAttribute(PROJECT_OBJ_STR, project);

	}

	private List<ProjectUserDTO> getMembersDTO(List<?> members) {
		return members.stream().map(o -> {
			if (o instanceof AppUser)
				return ProjectUserDTO.builder().userId(((AppUser) o).getUser().getUserId())
						.roleName(((AppUser) o).getRole().getName()).fullName(((AppUser) o).getUser().getFullName())
						.realm(((AppUser) o).getRole().getApp().getAppId()).build();
			else if (o instanceof User)
				return ProjectUserDTO.builder().userId(((User) o).getUserId()).roleName(((User) o).getRole().getId())
						.fullName(((User) o).getFullName()).build();
			else
				return null;
		}).filter(p -> p != null).collect(Collectors.toList());
	}

	private List<ProjectResourceDTO> getAllResourcesDTO(String identification, Resources type, String projectId) {
		final Collection<OPResource> resources = resourceService.getResources(utils.getUserId(), identification);
		String type_resource;
		if (type.name().equals("DATAFLOW")) {
			type_resource = "PIPELINE";
		} else {
			type_resource = type.name();
		}
		return resources.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type_resource))
				.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
						.type(r.getClass().getSimpleName()).build())
				.collect(Collectors.toList());

	}

	private Map<String, String> getUrlsMap() {
		final Map<String, String> urls = new HashMap<>();
		urls.put(Resources.API.name(), "apimanager");
		urls.put(Resources.CLIENTPLATFORM.name(), "devices");
		urls.put(Resources.DASHBOARD.name(), "dashboards");
		urls.put(Resources.GADGET.name(), "gadgets");
		urls.put(Resources.DIGITALTWINDEVICE.name(), "digitaltwindevices");
		urls.put(Resources.FLOWDOMAIN.name(), "flows");
		urls.put(Resources.NOTEBOOK.name(), "notebooks");
		urls.put(Resources.ONTOLOGY.name(), "ontologies");
		urls.put(Resources.DATAFLOW.name(), "dataflow");
		return urls;
	}
}
