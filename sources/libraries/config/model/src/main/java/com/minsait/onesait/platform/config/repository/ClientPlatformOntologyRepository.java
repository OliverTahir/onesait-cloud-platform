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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;

public interface ClientPlatformOntologyRepository extends JpaRepository<ClientPlatformOntology, String> {

	@Query("SELECT o " + "FROM ClientPlatformOntology AS o " + "WHERE o.ontology.identification = :ontologId AND "
			+ "o.clientPlatform.identification = :clientPlatformId")
	ClientPlatformOntology findByOntologyAndClientPlatform(@Param("ontologId") String ontologId,
			@Param("clientPlatformId") String clientPlatformId);

	List<ClientPlatformOntology> findByClientPlatform(ClientPlatform clientPlatform);

	List<ClientPlatformOntology> findById(String id);

	List<ClientPlatformOntology> findByOntology(Ontology ontology);

	@Transactional
	@Modifying
	void deleteByOntology(Ontology ontology);

	@Transactional
	@Modifying
	void deleteById(String id);

}