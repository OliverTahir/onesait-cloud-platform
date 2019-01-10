package com.minsait.onesait.platform.config.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@DataJpaTest
public class UserJsonSerializeTest {

	@Test
	@Transactional
	public void givenBidirectionRelation_whenUsingJacksonReferenceAnnotation_thenCorrect()
			throws JsonProcessingException {
		final User user = new User();
		user.setUserId("administrator");
		final Project project = new Project();
		project.setId("1");
		final Set<User> users = new HashSet<>();
		users.add(user);
		project.setUsers(users);

		final Set<Project> projects = new HashSet<>();
		projects.add(project);
		user.setProjects(projects);

		final String serialization = new ObjectMapper().writeValueAsString(user);
		final String s = new ObjectMapper().writeValueAsString(project);
	}

}
