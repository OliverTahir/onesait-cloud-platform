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
package com.minsait.onesait.platform.config.services.flowdomain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowDomain.State;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.FlowNodeRepository;
import com.minsait.onesait.platform.config.repository.FlowRepository;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowDomainServiceImpl implements FlowDomainService {

	@Value("${onesaitplatform.flowengine.port.domain.min:8000}")
	private int domainPortMin;
	@Value("${onesaitplatform.flowengine.port.domain.max:8500}")
	private int domainPortMax;
	@Value("${onesaitplatform.flowengine.port.service.min:7000}")
	private int servicePortMin;
	@Value("${onesaitplatform.flowengine.port.service.max:7500}")
	private int servicePortMax;
	@Value("${onesaitplatform.flowengine.home.base:/tmp/}")
	private String homeBase;

	@Autowired
	public FlowDomainRepository domainRepository;

	@Autowired
	private FlowRepository flowRepository;

	@Autowired
	private FlowNodeRepository nodeRepository;

	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private UserService userService;

	@Override
	public List<FlowDomain> getFlowDomainByUser(User user) {
		if (Role.Type.ROLE_ADMINISTRATOR.name().equalsIgnoreCase(user.getRole().getId())) {
			return domainRepository.findAll();
		}
		final List<FlowDomain> domains = new ArrayList<>();
		final FlowDomain domain = domainRepository.findByUser_userId(user.getUserId());
		if (domain != null) {
			domains.add(domain);
		}
		return domains;
	}

	@Override
	public void deleteFlowDomainFlows(String domainIdentification) {
		final FlowDomain domain = domainRepository.findByIdentification(domainIdentification);
		// Delete all data from this Domain,
		// including flows, nodes and properties
		final List<Flow> flows = flowRepository.findByFlowDomain_Identification(domain.getIdentification());
		for (final Flow flow : flows) {
			final List<FlowNode> nodes = nodeRepository.findByFlow_NodeRedFlowId(flow.getNodeRedFlowId());
			for (final FlowNode node : nodes) {
				nodeRepository.delete(node);
			}
			flowRepository.delete(flow);
		}
	}

	@Override
	public void deleteFlowdomain(String domainIdentification) {
		deleteFlowDomainFlows(domainIdentification);
		domainRepository.deleteByIdentification(domainIdentification);
	}

	@Override
	public FlowDomain getFlowDomainByIdentification(String identification) {
		return domainRepository.findByIdentification(identification);
	}

	@Override
	public FlowDomain createFlowDomain(String identification, User user) {

		if (domainRepository.findByIdentification(identification) != null) {
			log.debug("Flow domain {} already exist.", identification);
			throw new FlowDomainServiceException("The requested flow domain already exists in CDB");
		}

		final FlowDomain domain = new FlowDomain();
		domain.setIdentification(identification);
		domain.setActive(true);
		domain.setState(State.STOP.name());
		domain.setUser(user);
		domain.setHome(homeBase + user.getUserId());
		// Check free domain ports
		final List<Integer> usedDomainPorts = domainRepository.findAllDomainPorts();
		Integer selectedPort = domainPortMin;
		boolean portFound = false;
		while (selectedPort <= domainPortMax && !portFound) {
			if (!usedDomainPorts.contains(selectedPort)) {
				portFound = true;
			} else {
				selectedPort++;
			}
		}
		if (!portFound) {
			log.error("No port available found for domain = {}.", identification);
			throw new FlowDomainServiceException("No port available found for domain " + identification);
		}
		domain.setPort(selectedPort);
		// Check free service ports
		final List<Integer> usedServicePorts = domainRepository.findAllServicePorts();
		Integer selectedServicePort = servicePortMin;
		boolean servicePortFound = false;
		while (selectedServicePort <= servicePortMax && !servicePortFound) {
			if (!usedServicePorts.contains(selectedServicePort)) {
				servicePortFound = true;
			} else {
				selectedServicePort++;
			}
		}
		if (!servicePortFound) {
			log.error("No service port available found for domain = {}.", identification);
			throw new FlowDomainServiceException("No service port available found for domain " + identification);
		}
		domain.setServicePort(selectedServicePort);
		domainRepository.save(domain);
		return domain;
	}

	@Override
	public boolean flowDomainExists(FlowDomain domain) {
		if (domainRepository.findByIdentification(domain.getIdentification()) == null) {
			return false;
		}
		return true;
	}

	@Override
	public void updateDomain(FlowDomain domain) {

		if (!flowDomainExists(domain)) {
			log.error("Domain not found for identification = {}.", domain.getIdentification());
			throw new FlowDomainServiceException("Domain " + domain.getIdentification() + " not found.");
		} else {
			domainRepository.save(domain);
		}
	}

	@Override
	public boolean domainExists(String domainIdentification) {
		if (domainRepository.findByIdentification(domainIdentification) != null) {
			return true;
		}
		return false;
	}

	@Override
	public FlowDomain getFlowDomainById(String id) {
		return domainRepository.findOne(id);
	}

	@Override
	public boolean hasUserManageAccess(String id, String userId) {
		final FlowDomain domain = domainRepository.findOne(id);
		if (domain.getUser().getUserId().equals(userId))
			return true;
		else if (userService.getUser(userId).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}
	}

	@Override
	public boolean hasUserViewAccess(String id, String userId) {
		final FlowDomain domain = domainRepository.findOne(id);
		if (domain.getUser().getUserId().equals(userId))
			return true;
		else if (userService.getUser(userId).getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}
	}
}
