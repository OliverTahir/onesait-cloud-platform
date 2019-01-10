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
package com.minsait.onesait.platform.config.services.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;

import avro.shaded.com.google.common.collect.Lists;

@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private AppService appService;
	@Autowired
	private UserService userService;
	@Autowired
	private WebProjectService webProjectService;

	@Override
	public void createProject(Project project) {
		projectRepository.save(project);

	}

	@Override
	public void deleteProject(String projectId) {
		final Project project = projectRepository.findOne(projectId);
		final App app = project.getApp();
		if (app != null) {
			project.setApp(null);
			app.setProject(null);
			appService.updateApp(app);
		}
		projectRepository.delete(project);
	}

	@Override
	public List<Project> getAllProjects() {
		return projectRepository.findAll();
	}

	@Override
	public Project getById(String id) {
		return projectRepository.findOne(id);
	}

	@Override
	public void updateProject(Project project) {
		projectRepository.save(project);
	}

	@Override
	public void updateWithParameters(Project project) {
		final Project projectDb = projectRepository.findOne(project.getId());
		projectDb.setDescription(project.getDescription());
		projectDb.setType(project.getType());
		projectRepository.save(projectDb);

	}

	@Override
	public List<?> getProjectMembers(String projectId) {
		final Project project = projectRepository.findOne(projectId);
		if (project != null) {
			if (project.getApp() != null) {
				final List<AppUser> users = new ArrayList<>();
				final App app = project.getApp();
				if (!CollectionUtils.isEmpty(app.getChildApps())) {
					app.getChildApps().forEach(a -> a.getAppRoles().forEach(r -> {
						r.getAppUsers().forEach(au -> {
							if (!users.contains(au))
								users.add(au);
						});

					}));
				}
				app.getAppRoles().forEach(r -> {
					r.getAppUsers().forEach(au -> {
						if (!users.contains(au))
							users.add(au);
					});
				});
				return users;
			} else
				return Lists.newArrayList(project.getUsers());

		} else
			return new ArrayList<>();
	}

	@Override
	public List<App> getAvailableRealms() {
		return appService.getAllApps().stream().filter(a -> a.getProject() == null).collect(Collectors.toList());
	}

	@Override
	public List<Project> getProjectsForUser(String userId) {
		final List<Project> projects = projectRepository.findAll();
		final User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return projects;
		final List<Project> filteredProjects = projects.stream()
				.filter(p -> p.getUser().equals(user) || p.getUsers().contains(user)).collect(Collectors.toList());
		projects.forEach(p -> {
			if (p.getApp() != null) {
				final App app = p.getApp();
				if (!CollectionUtils.isEmpty(app.getChildApps())) {
					app.getChildApps().forEach(a -> a.getAppRoles().forEach(r -> {
						final List<User> users = r.getAppUsers().stream().map(au -> au.getUser())
								.collect(Collectors.toList());
						if (users.contains(user) && !filteredProjects.contains(p))
							filteredProjects.add(p);

					}));
				}
				app.getAppRoles().forEach(r -> {
					final List<User> users = r.getAppUsers().stream().map(au -> au.getUser())
							.collect(Collectors.toList());
					if (users.contains(user) && !filteredProjects.contains(p))
						filteredProjects.add(p);
				});
			}
		});
		return filteredProjects;
	}

	@Override
	public void addUserToProject(String userId, String projectId) {
		final User user = userService.getUser(userId);
		final Project project = projectRepository.findOne(projectId);
		if (user != null && project != null) {
			if (project.getApp() == null) {
				project.getUsers().add(user);
				user.getProjects().add(project);
				projectRepository.save(project);
				userService.saveExistingUser(user);
			}
		}
	}

	@Override
	public void removeUserFromProject(String userId, String projectId) {
		final User user = userService.getUser(userId);
		final Project project = projectRepository.findOne(projectId);
		if (user != null && project != null) {
			if (project.getApp() == null) {
				project.getUsers().remove(user);
				user.getProjects().remove(project);
				project.getProjectResourceAccesses().removeIf(pra -> pra.getUser().equals(user));
				projectRepository.save(project);
				userService.saveExistingUser(user);
			}
		}
	}

	@Override
	public void setRealm(String realmId, String projectId) {
		final App app = appService.getByIdentification(realmId);
		if (app != null) {
			final Project project = projectRepository.findOne(projectId);
			if (project.getUsers().size() > 0)
				project.getUsers().clear();
			project.setApp(app);
			app.setProject(project);
			projectRepository.save(project);
			appService.updateApp(app);
		}

	}

	@Override
	public void unsetRealm(String realmId, String projectId) {
		final App app = appService.getByIdentification(realmId);
		if (app != null) {
			final Project project = projectRepository.findOne(projectId);
			project.setApp(null);
			app.setProject(null);
			project.getProjectResourceAccesses().clear();
			projectRepository.save(project);
			appService.updateApp(app);
		}

	}

	@Override
	public void addWebProject(String webProjectId, String projectId, String userId) {
		final Project project = projectRepository.findOne(projectId);
		final WebProject webProject = webProjectService.getWebProjectById(webProjectId, userId);
		if (webProject != null && project != null) {
			project.setWebProject(webProject);
			projectRepository.save(project);
		}

	}

	@Override
	public void removeWebProject(String projectId) {
		final Project project = projectRepository.findOne(projectId);
		if (project != null) {
			project.setWebProject(null);
			projectRepository.save(project);
		}

	}

	@Override
	public boolean isUserInProject(String userId, String projectId) {
		final User user = userService.getUser(userId);
		final Project project = projectRepository.findOne(projectId);
		if (project.getUser().equals(user))
			return true;
		else if (project.getApp() != null)
			return project.getApp().getAppRoles().stream()
					.map(ar -> ar.getAppUsers().stream().map(au -> au.getUser().equals(user))
							.filter(b -> b.booleanValue() == true).findFirst().orElse(false))
					.filter(b -> b.booleanValue() == true).findFirst().orElse(false);
		else
			return project.getUsers().contains(user);
	}

	@Override
	public Set<ProjectResourceAccess> getResourcesAccessesForUser(String projectId, String userId) {
		final User user = userService.getUser(userId);
		final Project project = projectRepository.findOne(projectId);
		if (project.getApp() != null) {
			final Set<AppRole> roles = project
					.getApp().getAppRoles().stream().filter(ar -> null != ar.getAppUsers().stream()
							.map(ap -> ap.getUser()).filter(u -> u.equals(user)).findFirst().orElse(null))
					.collect(Collectors.toSet());
			return project.getProjectResourceAccesses().stream().filter(pra -> roles.contains(pra.getAppRole()))
					.collect(Collectors.toSet());
		} else
			return project.getProjectResourceAccesses().stream().filter(pra -> pra.getUser().equals(user))
					.collect(Collectors.toSet());
	}

	@Override
	public Set<OPResource> getResourcesForUser(String projectId, String userId) {
		final User user = userService.getUser(userId);
		final Project project = projectRepository.findOne(projectId);
		final String role_user = user.getRole().getId();
		if (user.equals(project.getUser()) || role_user.equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			// if (user.equals(project.getUser()))
			return project.getProjectResourceAccesses().stream().map(pra -> pra.getResource())
					.collect(Collectors.toSet());
		else
			return getResourcesAccessesForUser(projectId, userId).stream().map(pra -> pra.getResource())
					.collect(Collectors.toSet());
	}

	@Override
	public Set<AppRole> getProjectRoles(String projectId) {
		final Set<AppRole> roles = new HashSet<>();
		final Project project = projectRepository.findOne(projectId);
		roles.addAll(project.getApp().getAppRoles());
		project.getApp().getChildApps().forEach(a -> roles.addAll(a.getAppRoles()));
		return roles;
	}

	@Override
	public boolean isUserAuthorized(String projectId, String userId) {
		final Project project = projectRepository.findOne(projectId);
		final User user = userService.getUser(userId);
		if (project.getUser().equals(user) || user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		else
			return false;

	}

}
