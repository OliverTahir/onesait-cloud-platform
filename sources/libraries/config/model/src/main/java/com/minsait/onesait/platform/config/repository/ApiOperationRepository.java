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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;

public interface ApiOperationRepository extends JpaRepository<ApiOperation, String> {

	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	<S extends ApiOperation> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	void flush();

	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	<S extends ApiOperation> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	ApiOperation save(ApiOperation entity);

	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	void delete(ApiOperation id);

	@Override
	@CacheEvict(cacheNames = "ApiOperationRepository")
	void deleteAll();

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public ApiOperation findById(String id);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByIdentificationIgnoreCase(String identification);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByDescription(String description);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByIdentification(String identification);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByDescriptionContaining(String description);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByIdentificationContaining(String identification);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByApiIdOrderByOperationDesc(String identification);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByApiOrderByOperationDesc(Api api);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findAllByApi(Api api);

	@Cacheable(cacheNames = "ApiOperationRepository", unless = "#result == null")
	public List<ApiOperation> findByApiAndOperation(Api api, Type operation);

}