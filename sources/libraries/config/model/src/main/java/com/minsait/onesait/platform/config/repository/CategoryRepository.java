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

import com.minsait.onesait.platform.config.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Category findById(String id);

	List<Category> findByIdentification(String identification);

	List<Category> findByDescription(String description);

	List<Category> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Category> findByIdentificationContaining(String identification);

	List<Category> findByDescriptionContaining(String description);

	List<Category> findAllByOrderByIdentificationAsc();

	List<Category> findByIdentificationAndDescription(String identification, String description);

	List<Category> findByIdentificationLikeAndDescriptionLike(String identification, String description);

}