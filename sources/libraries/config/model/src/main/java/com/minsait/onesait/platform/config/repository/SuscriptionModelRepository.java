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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;

public interface SuscriptionModelRepository extends JpaRepository<SuscriptionNotificationsModel, String> {
	@Override
	@CacheEvict(cacheNames = "SuscriptionModelRepository", allEntries = true)
	<S extends SuscriptionNotificationsModel> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "SuscriptionModelRepository")
	void flush();

	@Override
	List<SuscriptionNotificationsModel> findAll();

	@Override
	@CachePut(cacheNames = "SuscriptionModelRepository", key = "#p0.suscriptionId")
	<S extends SuscriptionNotificationsModel> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CachePut(cacheNames = "SuscriptionModelRepository", key = "#p0.suscriptionId")
	SuscriptionNotificationsModel save(SuscriptionNotificationsModel entity);

	@Override
	@CacheEvict(cacheNames = "SuscriptionModelRepository", key = "#p0.suscriptionId")
	void delete(SuscriptionNotificationsModel id);

	@Override
	@CacheEvict(cacheNames = "SuscriptionModelRepository", allEntries = true)
	void deleteAll();

	@CacheEvict(cacheNames = "SuscriptionModelRepository", key = "#p0")
	void deleteBySuscriptionId(String id);

	@CacheEvict(cacheNames = "SuscriptionModelRepository", allEntries = true)
	void deleteByOntologyName(String ontologyName);

	// @Cacheable(cacheNames = "SuscriptionModelRepository", unless = "#result ==
	// null", key = "#p0")
	List<SuscriptionNotificationsModel> findAllByOntologyName(String ontologyName);

	@Cacheable(cacheNames = "SuscriptionModelRepository", unless = "#result == null", key = "#p0")
	SuscriptionNotificationsModel findAllBySuscriptionId(String suscriptionId);

}