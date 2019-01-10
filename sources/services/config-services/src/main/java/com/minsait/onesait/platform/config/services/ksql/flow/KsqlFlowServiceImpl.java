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
package com.minsait.onesait.platform.config.services.ksql.flow;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.KsqlFlowRepository;
import com.minsait.onesait.platform.config.repository.KsqlRelationRepository;
import com.minsait.onesait.platform.config.repository.KsqlResourceRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.KsqlFlowServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KsqlFlowServiceImpl implements KsqlFlowService {

	@Autowired
	private KsqlFlowRepository ksqlFlowRepository;
	@Autowired
	private KsqlRelationRepository ksqlRelationRepository;
	@Autowired
	private KsqlResourceRepository ksqlResourceRepository;
	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	private void init() {
		// TODO check all KsqlFlow's KsqlResources. ¿Needed?
		// If not created in KSQL Server, then create.
		// Find out order from Origins of flows an successors
	}

	@Transactional
	@Override
	public void deleteKsqlFlow(String id) {
		KsqlFlow flow = ksqlFlowRepository.findById(id);

		List<KsqlRelation> elements = ksqlRelationRepository.findByKsqlFlow(flow);

		for (KsqlRelation element : elements) {
			element.getPredecessors().forEach(predecessor -> {
				predecessor.removeSucessor(element);
				ksqlRelationRepository.save(predecessor);
			});
			element.getSuccessors().forEach(successor -> {
				successor.removePredecessor(element);
				ksqlRelationRepository.save(successor);
			});

			KsqlResource relationResource = element.getKsqlResource();
			ksqlRelationRepository.deleteByKsqlFlowIdentificationAndKsqlResourceIdentification(flow.getIdentification(),
					relationResource.getIdentification());
			if (relationResource.getResourceType() != FlowResourceType.ORIGIN) {
				// Delete because is not used in multiple flows
				ksqlResourceRepository.deleteByIdentification(relationResource.getIdentification());
			} else {
				// If no other relations are found(regardless from the Flow),
				// then remove it
				List<KsqlRelation> otherRelations = ksqlRelationRepository
						.findByKsqlResourceIdentification(relationResource.getIdentification());
				if (otherRelations == null || otherRelations.isEmpty()) {
					ksqlResourceRepository.deleteByIdentification(relationResource.getIdentification());
				}
			}
		}

		ksqlFlowRepository.deleteById(id);
	}

	@Override
	public List<KsqlFlow> getKsqlFlowsWithDescriptionAndIdentification(User sessionUser, String identification,
			String description) {
		List<KsqlFlow> ksqlFlows = new ArrayList<>();

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ksqlFlows = ksqlFlowRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
		} else {
			ksqlFlows = ksqlFlowRepository.findByUserAndIdentificationContainingAndDescriptionContaining(sessionUser,
					identification, description);
		}

		return ksqlFlows;

	}

	@Override
	public List<String> getAllIdentifications() {
		List<KsqlFlow> ksqlFlows = this.ksqlFlowRepository.findAllByOrderByIdentificationAsc();
		List<String> identifications = new ArrayList<String>();
		for (KsqlFlow ksqlFlow : ksqlFlows) {
			identifications.add(ksqlFlow.getIdentification());

		}
		return identifications;
	}

	@Override
	public boolean identificationIsAvailable(User sessionUser, String identification) {
		KsqlFlow matchingKsqlFlow = this.ksqlFlowRepository.findByUserAndIdentification(sessionUser, identification);
		return matchingKsqlFlow == null;
	}

	@Override
	public void createKsqlFlow(KsqlFlow ksqlFlow) {
		if (identificationIsAvailable(ksqlFlow.getUser(), ksqlFlow.getIdentification())) {
			ksqlFlowRepository.save(ksqlFlow);
		} else {
			log.error("Identification is already being used for a previous KSQL Flow. Identification = {}.",
					ksqlFlow.getIdentification());
			throw new KsqlFlowServiceException(
					"Identification is already being used for a previous KSQL Flow. Identification = "
							+ ksqlFlow.getIdentification());
		}
	}

	@Override
	public KsqlFlow getKsqlFlowWithId(String id) {
		return ksqlFlowRepository.findById(id);
	}

	@Override
	public void updateKsqlFlow(String id, KsqlFlow ksqlFlow, String userId) {
		if (hasUserPermission(id, userId) && id.equals(ksqlFlow.getId())) {
			ksqlFlowRepository.save(ksqlFlow);
		} else {
			throw new KsqlFlowServiceException(
					"Cannot update KSQL Flow that does not exist or user does not have permissions.");
		}
	}

	private boolean hasUserPermission(String id, String userId) {
		User user = userRepository.findByUserId(userId);
		if (user.getRole().getName().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			return true;
		} else {
			return ksqlFlowRepository.findById(id).getUser().getUserId().equals(userId);
		}
	}

}
