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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.User;

public interface ConfigurationRepository extends JpaRepository<Configuration, String> {

	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository")
	<S extends Configuration> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository")
	void flush();

	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository", key = "#p0.id")
	<S extends Configuration> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository", key = "#p0.id")
	Configuration save(Configuration entity);

	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository", key = "#p0.id")
	void delete(Configuration id);

	@Override
	@CacheEvict(cacheNames = "ConfigurationRepository")
	void deleteAll();

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	List<Configuration> findByUser(User user);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	Configuration findById(String id);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	Configuration findByDescription(String description);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	List<Configuration> findByType(Type type);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	List<Configuration> findByTypeAndUser(Type type, User user);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	Configuration findByTypeAndEnvironmentAndSuffix(Type type, String environment, String suffix);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	List<Configuration> findByUserAndType(User userId, Type type);

	@CacheEvict(cacheNames = "ConfigurationRepository")
	void deleteById(String id);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	Configuration findByTypeAndEnvironment(Type type, String environment);

	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	Configuration findByTypeAndSuffixIgnoreCase(Type type, String suffix);

	@Override
	@Cacheable(cacheNames = "ConfigurationRepository", unless = "#result == null")
	List<Configuration> findAll();

}
