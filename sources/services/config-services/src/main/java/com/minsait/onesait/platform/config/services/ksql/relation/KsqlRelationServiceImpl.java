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
package com.minsait.onesait.platform.config.services.ksql.relation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.KsqlRelationRepository;
import com.minsait.onesait.platform.config.services.exceptions.KsqlRelationServiceException;
import com.minsait.onesait.platform.config.services.ksql.resource.KsqlResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KsqlRelationServiceImpl implements KsqlRelationService {

	@Autowired
	private KsqlResourceService ksqlResourceService;

	@Autowired
	private KsqlRelationRepository ksqlRelationRepository;

	@Override
	@Transactional
	public void createKsqlRelation(KsqlFlow ksqlFlow, KsqlResource ksqlResource) {

		KsqlResource existingKsqlResource = ksqlResourceService
				.getKsqlResourceByIdentification(ksqlResource.getIdentification());

		if (existingKsqlResource == null) {
			// If resource does not exists in CDB. create it
			ksqlResourceService.createKsqlResource(ksqlResource);
		} else {
			ksqlResource = existingKsqlResource;
			// Check if already existing relation ?
			if (ksqlRelationRepository.findByKsqlFlowAndKsqlResource(ksqlFlow, ksqlResource) != null) {
				log.error("Relation already exists. KsqlFlow=%s, KsqlResource=%s.", ksqlFlow.getIdentification(),
						ksqlResource.getIdentification());
				throw new KsqlRelationServiceException("Relation already exists. KsqlFlow="
						+ ksqlFlow.getIdentification() + ", KsqlResource=" + ksqlResource.getIdentification());
			}
		}

		// Create Relation
		KsqlRelation newKsqlRelation = new KsqlRelation();
		newKsqlRelation.setKsqlFlow(ksqlFlow);
		newKsqlRelation.setKsqlResource(ksqlResource);
		ksqlRelationRepository.save(newKsqlRelation);
		// Get Predecessors By Resource identification (Stream/table name)
		List<String> dependencies = ksqlResource.parseStatementTextAndGetDependencies();
		// Set predecessors and successors to relations
		for (String dependency : dependencies) {
			List<KsqlRelation> predecessors = ksqlRelationRepository
					.findByKsqlFlowAndKsqlResourceIdentification(ksqlFlow, dependency);
			for (KsqlRelation predecessor : predecessors) {
				// Set new Relation as sucessor
				predecessor.addSucessor(newKsqlRelation);
				// Set predecessor in new Relation
				newKsqlRelation.addPredecessor(predecessor);
				ksqlRelationRepository.save(predecessor);
			}
		}

		// Save new Relation
		ksqlRelationRepository.save(newKsqlRelation);

	}

	@Override
	@Transactional
	public void deleteKsqlRelation(KsqlRelation relation) {
		KsqlResource ksqlResource = relation.getKsqlResource();
		relation.getPredecessors().forEach(predecessor -> {
			predecessor.removeSucessor(relation);
			ksqlRelationRepository.save(predecessor);
		});
		relation.getSuccessors().forEach(successor -> {
			successor.removePredecessor(relation);
			ksqlRelationRepository.save(successor);
		});
		// Delete Relation
		ksqlRelationRepository.delete(relation);

		// Delete KsqlResource if it has no more KsqlRelations in any Flow
		boolean resourceDeletion = true;
		if (ksqlResource.getResourceType() == FlowResourceType.ORIGIN) {
			List<KsqlRelation> relations = ksqlRelationRepository
					.findByKsqlResourceIdentification(ksqlResource.getIdentification());
			if (relations != null && !relations.isEmpty()) {
				resourceDeletion = false;
			}
		}
		if (resourceDeletion) {
			ksqlResourceService.deleteKsqlResource(ksqlResource);
		}
	}

	@Override
	@Transactional
	public void updateKsqlRelation(KsqlFlow ksqlFlow, KsqlResource ksqlResource) {

		KsqlRelation relation = ksqlRelationRepository.findByKsqlFlowAndKsqlResource(ksqlFlow, ksqlResource);
		if (relation == null) {
			log.error("Unable to find Relation for the Resource Update command. KsqlFlow = {}, KsqlResource = {}.",
					ksqlFlow.getIdentification(), ksqlResource.getIdentification());
			throw new KsqlRelationServiceException(
					"Unable to find Relation for the Resource Update command. KsqlFlow = "
							+ ksqlFlow.getIdentification() + ", KsqlResource = " + ksqlResource.getIdentification());
		}
		// Unset all "old" predecessors
		relation.getPredecessors().forEach(predecessor -> {
			predecessor.removeSucessor(relation);
			relation.removePredecessor(predecessor);
			ksqlRelationRepository.save(predecessor);
		});
		// Get new Predecessors
		List<String> dependencies = ksqlResource.parseStatementTextAndGetDependencies();
		// Set new Predecessors
		for (String dependency : dependencies) {
			List<KsqlRelation> predecessors = ksqlRelationRepository
					.findByKsqlFlowAndKsqlResourceIdentification(ksqlFlow, dependency);
			for (KsqlRelation predecessor : predecessors) {
				predecessor.addSucessor(relation);
				relation.addPredecessor(predecessor);
				ksqlRelationRepository.save(predecessor);
			}
		}
		// Save Relation
		ksqlRelationRepository.save(relation);
		// Update KsqlResource
		ksqlResourceService.updateKsqlResource(ksqlResource);
	}

	@Override
	public List<String> getAllIdentifications() {
		return ksqlRelationRepository.findAllIdentifications();
	}

	@Override
	public List<KsqlRelation> getKsqlRelationsWithFlowIdDescriptionAndIdentification(User sessionUser, String id,
			String identification, String description) {
		List<KsqlRelation> ksqlRelations = new ArrayList<>();

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ksqlRelations = ksqlRelationRepository
					.findByKsqlFlowIdAndKsqlResourceIdentificationContainingAndKsqlResourceDescriptionContaining(
							identification, id, description);
		} else {
			ksqlRelations = ksqlRelationRepository
					.findByKsqlFlowUserAndKsqlFlowIdAndKsqlResourceIdentificationContainingAndKsqlResourceDescriptionContaining(
							sessionUser, id, identification, description);
		}

		return ksqlRelations;
	}

	@Override
	public List<KsqlRelation> getKsqlRelationsWithFlowId(User user, String flowId) {
		return getKsqlRelationsWithFlowIdDescriptionAndIdentification(user, flowId, null, null);
	}

	@Override
	public KsqlRelation getKsqlRelationWithId(String id) {
		return ksqlRelationRepository.findById(id);
	}

}
