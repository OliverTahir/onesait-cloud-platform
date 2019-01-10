package com.minsait.onesait.platform.config.services.opresources;

import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.Project.ProjectType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

@Category(IntegrationTest.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OPResourceTest {

	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@Transactional
	@Test
	public void whenAssigningProjectResourceToAppUser_ThenTheUserHasAccessToThatResource() {
		final User user = userRepository.findByUserId("developer");

		final Project project = new Project();
		project.setDescription("Example project");
		project.setName("THis is the project name");
		project.setType(ProjectType.ENGINE);
		project.setUser(user);
		Project pdb = projectRepository.save(project);
		App realm = new App();
		realm.setName("Realm test");
		final AppRole role = new AppRole();
		realm.setAppId("TestRealm");
		role.setApp(realm);
		role.setName("DEVOPS");
		role.getAppUsers().addAll(userRepository.findAll().stream()
				.map(u -> AppUser.builder().user(u).role(role).build()).collect(Collectors.toSet()));
		realm.getAppRoles().add(role);
		realm = appRepository.save(realm);
		pdb.setApp(realm);
		realm.setProject(pdb);
		realm = appRepository.save(realm);
		final OPResource resource = ((Set<OPResource>) resourceService.getResources("developer", "")).iterator().next();
		final ProjectResourceAccess pra = ProjectResourceAccess.builder().access(ResourceAccessType.VIEW)
				.appRole(realm.getAppRoles().iterator().next()).project(pdb).resource(resource).build();
		pdb.getProjectResourceAccesses().add(pra);
		pdb = projectRepository.save(pdb);
		Assert.assertTrue(!resourceService.hasAccess(user.getUserId(), resource.getId(), ResourceAccessType.MANAGE));
		Assert.assertTrue(resourceService.hasAccess(user.getUserId(), resource.getId(), ResourceAccessType.VIEW));
		Assert.assertTrue(!resourceService.getResourceAccess(user.getUserId(), resource.getId())
				.equals(ResourceAccessType.MANAGE));
		realm.setProject(null);
		pdb.setApp(null);
		appRepository.delete(realm);
		projectRepository.delete(pdb);

	}

}
