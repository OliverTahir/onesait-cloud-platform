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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.DataModel;

public interface DataModelRepository extends JpaRepository<DataModel, String> {

	@Override
	@Cacheable("DataModelRepository")
	List<DataModel> findAll();

	DataModel findById(String id);

	List<DataModel> findByName(String name);

	List<DataModel> findByType(String type);

	long countByType(String type);

	@Query("SELECT o " + "FROM DataModel AS o " + "WHERE o.id LIKE %:id% OR " + "o.name LIKE %:name% OR "
			+ "o.description LIKE %:description%")
	List<DataModel> findByIdOrNameOrDescription(@Param(value = "id") String id, @Param(value = "name") String name,
			@Param(value = "description") String description);

	@Query("SELECT o " + "FROM DataModel AS o " + "WHERE o.name LIKE %:name% ")
	DataModel findDatamodelsByName(@Param(value = "name") String name);

}
