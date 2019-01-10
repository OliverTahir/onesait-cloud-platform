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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface ProjectResourceAccessRepository extends JpaRepository<ProjectResourceAccess, String> {

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public ProjectResourceAccess findByResourceAndProjectAndUser(OPResource resource, Project project, User user);

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public ProjectResourceAccess findByResourceAndProjectAndAppRole(OPResource resource, Project project, AppRole role);

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public int countByResource(OPResource resource);

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public List<ProjectResourceAccess> findByResource(OPResource resource);

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public List<ProjectResourceAccess> findByUser(User user);

	@Cacheable(cacheNames = "ProjectResourceAccessRepository", unless = "#result == null")
	public List<ProjectResourceAccess> findByAppRole(AppRole role);
}
