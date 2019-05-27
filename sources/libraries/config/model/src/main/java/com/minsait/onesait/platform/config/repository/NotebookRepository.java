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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.User;

public interface NotebookRepository extends JpaRepository<Notebook, String> {
	
	List<Notebook> findAllByOrderByIdentificationAsc();

	Notebook findByIdentification(String notebookId);

	Notebook findById(String notebookId);

	@Query("SELECT o FROM Notebook AS o WHERE o.user=:user ORDER BY o.identification ASC")
	List<Notebook> findByUser(@Param("user") User user);

	@Cacheable(cacheNames = "NotebookRepository", unless = "#result == null")
	@Query("SELECT o FROM Notebook AS o WHERE (o.user=:user OR o.isPublic=TRUE OR o.id IN (SELECT uo.notebook.id FROM NotebookUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<Notebook> findByUserAndAccess(@Param("user") User user);

	List<Notebook> findByIdentificationAndIdzep(String notebookId, String idzep);

	Notebook findByIdzep(String idzep);

}
