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
/*******************************************************************************

 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;

public interface UserRepository extends JpaRepository<User, String> {

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	<S extends User> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	void flush();

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	<S extends User> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "UserRepository")
	User save(User entity);

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	void delete(User id);

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	void delete(Iterable<? extends User> entities);

	@Override
	@CacheEvict(cacheNames = "UserRepository")
	void deleteAll();

	@Override
	@Cacheable(cacheNames = "UserRepository")
	List<User> findAll();

	@Override
	@Cacheable(cacheNames = "UserRepository")
	List<User> findAll(Iterable<String> ids);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	@Query("SELECT o FROM User AS o WHERE o.active=true")
	List<User> findAllActiveUsers();

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	@Query("SELECT o FROM User AS o WHERE o.email=:email")
	List<User> findByEmail(@Param("email") String email);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	User findByUserId(String userId);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	User findUserByEmail(String email);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	User findByUserIdAndPassword(String userId, String password);

	@CacheEvict(cacheNames = "UserRepository")
	@Transactional
	void deleteByUserId(String userId);

	@Cacheable(cacheNames = "UserRepository")
	@Query("SELECT o FROM User AS o WHERE o.role !='ADMINISTRATOR'")
	List<User> findUsersNoAdmin();

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE %:userId% OR o.fullName LIKE %:fullName% OR o.email LIKE %:email% OR o.role.name =:role)")
	List<User> findByUserIdOrFullNameOrEmailOrRoleType(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE %:userId% OR o.fullName LIKE %:fullName% OR o.email LIKE %:email% OR o.role.name =:role) AND (o.active=:active)")
	List<User> findByUserIdOrFullNameOrEmailOrRoleTypeAndActive(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role,
			@Param("active") boolean active);

	@Cacheable(cacheNames = "UserRepository", unless = "#result == null")
	@Query("SELECT o FROM User AS o WHERE (o.userId != :userId AND o.role.id != :rolId) ORDER BY o.userId)")
	List<User> findUserByIdentificationAndNoRol(@Param("userId") String userId, @Param("rolId") String rolId);
}
