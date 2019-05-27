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
package com.minsait.onesait.platform.config.services.ksql.resource;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.repository.KsqlResourceRepository;
import com.minsait.onesait.platform.config.services.exceptions.KsqlResourceServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KsqlResourceServiceImpl implements KsqlResourceService {

	@Autowired
	private KsqlResourceRepository ksqlResourceRepository;

	@PostConstruct
	private void init() {
		// TODO Read Properties
		// TODO Start KSQL Client
	}

	@Override
	public KsqlResource getKsqlResourceByIdentification(String identification) {
		return ksqlResourceRepository.findByIdentification(identification);
	}

	@Override
	public List<KsqlResource> getKsqlResourceByKafkaTopic(String kafkaTopic) {
		return ksqlResourceRepository.findByKafkaTopic(kafkaTopic);
	}

	@Override
	public void validateNewKsqlResource(KsqlResource ksqlResource) {
		// ¿Is the ResourceName free?
		if (ksqlResourceRepository.findByIdentification(ksqlResource.getIdentification()) != null) {
			log.error("The KsqlResource Identification is already being used. Identification = {}.",
					ksqlResource.getIdentification());
			throw new KsqlResourceServiceException(
					"The KsqlResource Identification is already being used. Identification = "
							+ ksqlResource.getIdentification());
		}
		// ¿Is the KafkaTopic being used by an other Stream/Table?
		// DESTINY type can share KafkaTopic so we won't check them
		if (ksqlResource.getResourceType() != FlowResourceType.DESTINY) {
			List<KsqlResource> resources = ksqlResourceRepository.findByKafkaTopic(ksqlResource.getKafkaTopic());
			if (resources != null && !resources.isEmpty()) {
				log.error(
						"The Kafka Topic defined for the KsqlResource is already being used by an other resource. Identification = {}, KafkaTopic = {}",
						ksqlResource.getIdentification(), ksqlResource.getKafkaTopic());
				throw new KsqlResourceServiceException(
						"The Kafka Topic defined for the KsqlResource is already being used by an other resource. Identification = "
								+ ksqlResource.getIdentification() + ", KfkaTopic = " + ksqlResource.getKafkaTopic());
			}
		}
	}

	@Override
	@Transactional
	public void createKsqlResource(KsqlResource ksqlResource) {

		validateNewKsqlResource(ksqlResource);
		// TODO Send info to KSQL Client to activate the resource
		ksqlResourceRepository.save(ksqlResource);
	}

	@Override
	@Transactional
	public void updateKsqlResource(KsqlResource ksqlResource) {
		// Check if exits by id/identification
		KsqlResource preModifKsqlResource = ksqlResourceRepository
				.findByIdentification(ksqlResource.getIdentification());
		if (preModifKsqlResource == null) {
			log.error("KsqlResource does not exist. Identification = {}", ksqlResource.getIdentification());
			throw new KsqlResourceServiceException(
					"KsqlResource does not exist. Identification = " + ksqlResource.getIdentification());
		}
		if (!preModifKsqlResource.getStatemetnText().equals(ksqlResource.getStatemetnText())) {
			// Validate ksqlResource
			ksqlResource.parseStatementTextAndGetDependencies();
			this.validateNewKsqlResource(ksqlResource);
			// TODO Delete from KSQL Server
			// TODO Create in KSQL server
		}
		// Update in CDB
		ksqlResourceRepository.save(ksqlResource);
	}

	@Override
	@Transactional
	public void deleteKsqlResource(KsqlResource ksqlResource) {
		// Check if exits by id/identification
		if (existsByIdentification(ksqlResource.getIdentification())) {
			// TODO Delete from KSQL Server
			// TODO Delete Predecessor/sucessor dependencies
			// TODO Delete Relation with Flow
			// Delete Resource if no more Relationis are
			ksqlResourceRepository.delete(ksqlResource);
		} else {
			log.info("Error while deleting resource. Resource {} does not exist.", ksqlResource.getIdentification());
			throw new KsqlResourceServiceException("Resource " + ksqlResource.getIdentification() + " does not exist.");
		}

	}

	private boolean existsByIdentification(String identification) {
		return ksqlResourceRepository.findByIdentification(identification) != null;
	}

	@Override
	public KsqlResource getKsqlResourceById(String id) {
		return ksqlResourceRepository.findById(id);
	}

}
