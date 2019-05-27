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

import java.io.IOException;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface OntologyRepository extends JpaRepository<Ontology, String> {

	@Override
	long count();

	@Override
	@CacheEvict(cacheNames = "OntologyRepository")
	<S extends Ontology> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "OntologyRepository")
	void flush();

	@Override
	@CacheEvict(cacheNames = "OntologyRepository", key = "#p0.id")
	<S extends Ontology> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = { "OntologyRepository" }, key = "#p0.id")
	Ontology save(Ontology entity);

	@Override
	@CacheEvict(cacheNames = "OntologyRepository", key = "#p0.id")
	void delete(Ontology id);

	@Override
	@Cacheable(cacheNames = "OntologyRepository")
	List<Ontology> findAll();

	@Override
	@CacheEvict(cacheNames = "OntologyRepository", allEntries = true)
	void deleteAll();

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	List<Ontology> findByIdentificationIgnoreCase(String identification);

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	List<Ontology> findByDescription(String description);

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	Ontology findByIdentification(String identification);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null",
	// key="findAllByOrderByIdentificationAsc")
	List<Ontology> findAllByOrderByIdentificationAsc();

	@Cacheable(cacheNames = "OntologyRepository", key = "#p0.ontology.id")
	List<Ontology> findByUserOrderByIdentificationAsc(User user);

	List<Ontology> findByDescriptionContaining(String description);

	public List<Ontology> findByIdentificationContaining(String identification);

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null", key = "#p0.userId")
	List<Ontology> findByUser(User user);

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null", key = "#p0.userId")
	List<Ontology> findByUserAndActiveTrue(User user);

	List<Ontology> findByIdentificationLikeAndDescriptionLikeAndActiveTrue(String identification, String description);

	List<Ontology> findByUserAndIdentificationLikeAndDescriptionLikeAndActiveTrue(User user, String identification,
			String description);

	List<Ontology> findByIdentificationContainingAndDescriptionContainingAndActiveTrue(String identification,
			String description);

	List<Ontology> findByIdentificationContainingAndDescriptionContainingAndActiveTrueOrderByIdentificationAsc(
			String identification, String description);

	List<Ontology> findByUserAndIdentificationContainingAndDescriptionContainingAndActiveTrue(User user,
			String identification, String description);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	List<Ontology> findByUserAndIdentificationContaining(User user, String identification);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	List<Ontology> findByUserAndDescriptionContaining(User user, String description);

	// @Cacheable(cacheNames = "OntologyRepository")
	List<Ontology> findByActiveTrueAndIsPublicTrue();

	// @Cacheable(cacheNames = "OntologyRepository")
	List<Ontology> findByActiveTrue();

	@Cacheable(cacheNames = "OntologyRepository", key = "#p0")
	Ontology findById(String id);

	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null", key = "#p0.userId")
	List<Ontology> findByUserAndIsPublicTrue(User user);

	long countByActiveTrueAndIsPublicTrue();

	long countByIdentificationLikeOrDescriptionLikeOrMetainfLike(String identification, String description,
			String metainf);

	long countByActiveTrueAndIsPublicTrueAndMetainfIsNull();

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	@Query("SELECT o FROM Ontology AS o WHERE (o.user=:user OR o.isPublic=TRUE OR o.id IN (SELECT uo.ontology.id FROM OntologyUserAccess AS uo WHERE uo.user=:user)) AND o.active=true ORDER BY o.identification ASC")
	List<Ontology> findByUserAndOntologyUserAccessAndAllPermissions(@Param("user") User user);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	@Query("SELECT o FROM Ontology AS o WHERE (o.user=:user OR o.isPublic=TRUE OR o.id IN (SELECT uo.ontology.id FROM OntologyUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<Ontology> findByUserAndAccess(@Param("user") User user);

	// @Cacheable("OntologyRepository")
	@Query("SELECT o " + "FROM Ontology AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.ontology.id " + "FROM OntologyUserAccess AS uo " + "WHERE uo.user=:user)) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<Ontology> findByUserAndPermissionsANDIdentificationAndDescription(@Param("user") User user,
			@Param("identification") String identification, @Param("description") String description);

	// @Cacheable("OntologyRepository")
	@Query("SELECT o " + "FROM Ontology AS o "
			+ "WHERE (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<Ontology> findByIdentificationLikeAndDescriptionLike(@Param("identification") String identification,
			@Param("description") String description);

	// @Cacheable("OntologyRepository")
	@Query("SELECT o " + "FROM Ontology AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.ontology.id " + "FROM OntologyUserAccess AS uo " + "WHERE uo.user=:user)) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) AND o.active=true ORDER BY o.identification ASC")
	List<Ontology> findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(@Param("user") User user,
			@Param("identification") String identification, @Param("description") String description);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	@Query("SELECT o FROM Ontology AS o WHERE o.isPublic=TRUE OR o.user=:user OR o.id IN (SELECT uo.ontology.id FROM OntologyUserAccess AS uo WHERE uo.user=:user AND (uo.ontologyUserAccessType='ALL' OR uo.ontologyUserAccessType='QUERY')) AND o.active=true ORDER BY o.identification ASC")
	List<Ontology> findByUserAndOntologyUserAccessAndPermissionsQuery(@Param("user") User user);

	// @Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	@Query("SELECT o FROM Ontology AS o WHERE o.user=:user OR o.id IN (SELECT uo.ontology.id FROM OntologyUserAccess AS uo WHERE uo.user=:user AND (uo.ontologyUserAccessType='ALL' OR uo.ontologyUserAccessType='INSERT')) AND o.active=true ORDER BY o.identification ASC")
	List<Ontology> findByUserAndOntologyUserAccessAndPermissionsInsert(@Param("user") User user);

	@CacheEvict(cacheNames = "OntologyRepository", key = "#p0")
	void deleteById(String id);

	// @Cacheable(cacheNames = "OntologyRepository")
	List<Ontology> findByRtdbCleanTrueAndRtdbCleanLapseNotNull();

	List<Ontology> findByIdentificationStartingWith(String identification);

	final ObjectMapper mapper = new ObjectMapper();

	// This method is required to cache the parsed jsonSchema.
	// The goal of use this method is to improve the performance.
	@Cacheable(cacheNames = "OntologyRepository", unless = "#result == null")
	default JsonNode getSchemaAsJsonNode(Ontology ontology) throws JsonProcessingException, IOException {
		final JsonNode jsonSchema = mapper.readTree(ontology.getJsonSchema());
		return jsonSchema;
	}

}
