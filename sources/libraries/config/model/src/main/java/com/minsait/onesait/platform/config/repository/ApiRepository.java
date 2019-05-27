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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface ApiRepository extends JpaRepository<Api, String> {

	@Override
	@CacheEvict(cacheNames = "ApiRepository")
	<S extends Api> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "ApiRepository")
	void flush();

	@Override
	@CacheEvict(cacheNames = "ApiRepository")
	<S extends Api> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "UserTokenRepository")
	Api save(Api entity);

	@Override
	@CacheEvict(cacheNames = "ApiRepository")
	void delete(Api id);

	@Override
	@CacheEvict(cacheNames = "ApiRepository")
	void deleteAll();

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationIgnoreCase(String identification);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByDescription(String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentification(String identification);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByDescriptionContaining(String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationContaining(String identification);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUser(User user);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationAndUser(String identification, User user);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUserAndIdentificationLikeAndDescriptionLike(User user, String identification, String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUserAndIdentificationContaining(User user, String identification);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUserAndDescriptionContaining(User user, String description);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationAndNumversionAndApiType(String identification, Integer apiVersion, ApiType apiType);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationAndNumversion(String identification, Integer apiVersion);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByIdentificationAndApiType(String identification, ApiType apiType);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	Api findById(String id);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByUserAndIsPublicTrue(User userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId OR a.identification LIKE %:apiId%)")
	List<Api> findApisByIdentificationOrUser(@Param("apiId") String apiId, @Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId OR a.identification LIKE %:apiId% OR a.state = :state)")
	List<Api> findApisByIdentificationOrStateOrUser(@Param("apiId") String apiId, @Param("state") ApiStates state,
			@Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api AS a WHERE (a.user.userId LIKE %:userId% AND (a.identification LIKE %:apiId% OR a.state LIKE %:state%)) AND a.isPublic IS true")
	List<Api> findApisByIdentificationOrStateAndUserAndIsPublicTrue(@Param("apiId") String apiId,
			@Param("state") String state, @Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId) OR (a.state = 'PUBLISHED' AND a.isPublic=true)")
	List<Api> findMyApisOrApisPublicAndPublished(@Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api as a WHERE a.isPublic = false AND (a.state = 'PUBLISHED' or a.state = 'DEVELOPMENT') ORDER BY a.identification asc")
	List<Api> findApisNotPublicAndPublishedOrDevelopment();

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api as a WHERE a.user.userId = :userId AND a.isPublic = false AND (a.state = 'PUBLISHED' or a.state = 'DEVELOPMENT') ORDER BY a.identification asc")
	List<Api> findApisByUserNotPublicAndPublishedOrDevelopment(@Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api as a WHERE (((:userloggedRole = 'ROLE_ADMINISTRATOR') OR (a.user.userId = :userloggedId) OR ((a.isPublic IS true) AND (a.state != 'CREATED' AND a.state != 'DELETED')) OR ((a.id IN (SELECT ua.api.id FROM UserApi AS ua WHERE ua.api.id = a.id and ua.user.userId = :userloggedId)) AND (a.state != 'CREATED' AND a.state != 'DELETED'))) AND (a.identification LIKE %:apiId% AND a.user.userId LIKE %:userId%)) ORDER BY a.identification asc")
	List<Api> findApisByIdentificationOrUserForAdminOrOwnerOrPublicOrPermission(
			@Param("userloggedId") String userloggedId, @Param("userloggedRole") String userloggedRole,
			@Param("apiId") String apiId, @Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	@Query("SELECT a FROM Api as a WHERE (((:userloggedRole = 'ROLE_ADMINISTRATOR') OR (a.user.userId = :userloggedId) OR ((a.isPublic IS true) AND (a.state != 'CREATED' AND a.state != 'DELETED')) OR ((a.id IN (SELECT ua.api.id FROM UserApi AS ua WHERE ua.api.id = a.id and ua.user.userId = :userloggedId)) AND (a.state != 'CREATED' AND a.state != 'DELETED'))) AND (a.identification LIKE %:apiId% AND a.state = :state AND a.user.userId LIKE %:userId%)) ORDER BY a.identification asc")
	List<Api> findApisByIdentificationOrStateOrUserForAdminOrOwnerOrPublicOrPermission(
			@Param("userloggedId") String userloggedId, @Param("userloggedRole") String userloggedRole,
			@Param("apiId") String apiId, @Param("state") ApiStates state, @Param("userId") String userId);

	@Cacheable(cacheNames = "ApiRepository", unless = "#result == null")
	List<Api> findByOntology(Ontology ontology);

}