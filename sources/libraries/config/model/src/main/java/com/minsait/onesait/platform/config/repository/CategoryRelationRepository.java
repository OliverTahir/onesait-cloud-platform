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

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.CategoryRelation.Type;

public interface CategoryRelationRepository extends JpaRepository<CategoryRelation, Long> {

	CategoryRelation findById(String id);

	List<CategoryRelation> findByType(Type type);

	List<CategoryRelation> findByCategory(String category);

	List<CategoryRelation> findByCategoryAndSubcategory(String category, String subcategory);

	CategoryRelation findByTypeId(String typeId);

	List<CategoryRelation> findByTypeIdAndType(String typeId, Type type);
}
