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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.digitaltwin.type.DigitalTwinTypeService;
import com.minsait.onesait.platform.config.services.flow.FlowService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.flownode.FlowNodeService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DeployRequestRecord;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinDeviceDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinTypeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.UserDomainValidationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.audit.aop.FlowEngineAuditable;
import com.minsait.onesait.platform.flowengine.exception.NodeRedAdminServiceException;
import com.minsait.onesait.platform.flowengine.exception.NotAllowedException;
import com.minsait.onesait.platform.flowengine.exception.NotAuthorizedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowEngineNodeServiceImpl implements FlowEngineNodeService {

	/*
	 * @Autowired
	 *
	 * @Qualifier("MongoBasicOpsDBRepository") private BasicOpsDBRepository
	 * basicRDBRepository;
	 *
	 * @Autowired private QueryToolService queryToolService;
	 */
	
	private static final String ADMINISTRATOR_STR = "ROLE_ADMINISTRATOR";

	@Autowired(required = false)
	private RouterService routerService;

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private FlowService flowService;

	@Autowired
	private FlowNodeService nodeService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;

	@Autowired
	private DigitalTwinTypeService digitalTwinTypeService;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;
	@Autowired
	private OPResourceService resourceService;

	@Override
	public ResponseEntity<String> deploymentNotification(String json) {
		final ObjectMapper mapper = new ObjectMapper();
		FlowDomain domain = null;
		List<DeployRequestRecord> deployRecords = new ArrayList<>();

		try {
			deployRecords = mapper.readValue(json, new TypeReference<List<DeployRequestRecord>>() {
			});
			for (final DeployRequestRecord record : deployRecords) {
				if (record != null) {
					if (record.getDomain() != null) {

						log.info("Deployment info from domain = {}", record.getDomain());
						domain = domainService.getFlowDomainByIdentification(record.getDomain());
						domainService.deleteFlowDomainFlows(record.getDomain());
					} else {
						log.debug("Deployment record = {}", record.toString());
						if (record.getType() != null) {
							if (record.getType().equals("tab")) {
								// it is a FLOW
								final Flow newFlow = new Flow();
								newFlow.setIdentification(record.getLabel());
								newFlow.setNodeRedFlowId(record.getId());
								newFlow.setActive(true);
								newFlow.setFlowDomain(domain);
								flowService.createFlow(newFlow);
							} else {
								// It is a node
								if (record.getType().equals(FlowNode.Type.HTTP_NOTIFIER.getName())) {
									final FlowNode node = new FlowNode();
									final Flow flow = flowService.getFlowByNodeRedFlowId(record.getZ());
									node.setIdentification(record.getName());
									node.setNodeRedNodeId(record.getId());
									node.setFlow(flow);
									node.setFlowNodeType(FlowNode.Type.HTTP_NOTIFIER);
									node.setMessageType(MessageType.valueOf(record.getMeassageType()));
									node.setOntology(ontologyService.getOntologyByIdentification(record.getOntology(),
											domain.getUser().getUserId()));
									node.setPartialUrl(record.getUrl());
									try {
										nodeService.createFlowNode(node);
									} catch (final Exception e) {
										final String msg = "Notification node '" + node.getIdentification()
												+ "' has an invalid Ontology selected: '" + node.getOntology() + "'.";
										return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
									}
								}
							}
						} else {
							log.warn("Undefined type for NodeRed element. Record will be skipped : {}",
									record.toString());
						}
					}
				}
			}
		} catch (final IOException e) {
			log.error("Unable to save deployment info from NodeRed into CDB. Cause = {}, message = {}", e.getCause(),
					e.getMessage());
			return new ResponseEntity<>("Unable to save deployment info from NodeRed into CDB.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	@Override
	public Set<String> getOntologyByUser(String authentication)
			throws ResourceNotFoundException, NotAuthorizedException {

		final Set<String> response = new TreeSet<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		switch (sofia2User.getRole().getId()) {
		case ADMINISTRATOR_STR:
			response.addAll(ontologyService.getAllOntologies(sofia2User.getUserId()).stream()
					.map(o -> o.getIdentification()).collect(Collectors.toSet()));
			break;
		default:
			response.addAll(ontologyService.getOntologiesByUserId(sofia2User.getUserId()).stream()
					.map(o -> o.getIdentification()).collect(Collectors.toSet()));
			response.addAll(resourceService.getResourcesForUserAndType(sofia2User, Ontology.class.getSimpleName())
					.stream().map(o -> o.getIdentification()).collect(Collectors.toSet()));
			break;
		}

		return response;
	}

	@Override
	public List<String> getClientPlatformByUser(String authentication)
			throws ResourceNotFoundException, NotAuthorizedException {

		final List<String> response = new ArrayList<>();

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		List<ClientPlatform> clientPlatforms = null;
		switch (sofia2User.getRole().getId()) {
		case ADMINISTRATOR_STR:
			clientPlatforms = clientPlatformService.getAllClientPlatforms();
			break;
		default:
			clientPlatforms = clientPlatformService.getclientPlatformsByUser(sofia2User);
			break;
		}
		for (final ClientPlatform clientPlatform : clientPlatforms) {
			response.add(clientPlatform.getIdentification());
		}
		Collections.sort(response);
		return response;
	}

	@Override
	public String validateUserDomain(UserDomainValidationRequest request)
			throws ResourceNotFoundException, NotAuthorizedException, NotAllowedException, IllegalArgumentException {

		String response = null;
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService
				.decodeAuth(request.getAuthentication());
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		if (request.getDomainId() == null) {
			throw new IllegalArgumentException("DomainId must be specified.");
		}

		final FlowDomain domain = domainService.getFlowDomainByIdentification(request.getDomainId());

		if (domain == null) {
			throw new ResourceNotFoundException(
					"Domain with identification " + request.getDomainId() + " could not be found.");
		}

		switch (sofia2User.getRole().getName()) {
		case ADMINISTRATOR_STR:
			response = "OK"; // Has permission over all domains
			break;
		default:
			if (!domain.getUser().getUserId().equals(sofia2User.getUserId())) {
				throw new NotAllowedException("User " + decodedAuth.getUserId()
						+ " has no permissions over specified domain " + request.getDomainId());
			}
			response = "OK";
			break;
		}
		return response;
	}

	@Override
	@FlowEngineAuditable
	public String submitQuery(String ontology, String queryType, String query, String authentication)
			throws ResourceNotFoundException, NotAuthorizedException, NotFoundException, JsonProcessingException,
			DBPersistenceException {

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		OperationType operationType = OperationType.QUERY;
		QueryType type;

		if ("sql".equalsIgnoreCase(queryType)) {
			type = QueryType.SQLLIKE;
			// NOT ALLOWED YET
			/*
			 * if (query.trim().toUpperCase().startsWith("DELETE ")) { operationType =
			 * OperationType.DELETE; }
			 */
		} else if ("native".equalsIgnoreCase(queryType)) {
			type = QueryType.NATIVE;
			if (query.trim().startsWith("db." + ontology + ".remove"))
				operationType = OperationType.DELETE;
		} else {
			log.error("Invalid value {} for queryType. Possible values are: SQL, NATIVE.", queryType);
			throw new IllegalArgumentException(
					"Invalid value " + queryType + " for queryType. Possible values are: SQL, NATIVE.");
		}

		final OperationModel operationModel = OperationModel
				.builder(ontology, operationType, sofia2User.getUserId(), OperationModel.Source.FLOWENGINE).body(query)
				.queryType(type).build();

		OperationResultModel result = null;
		try {
			final NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.query(notificationModel);
		} catch (final Exception e) {

			log.error("Error executing query. Ontology={}, QueryType ={}, Query = {}. Cause = {}, Message = {}.",
					ontology, queryType, query, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException(
					"Error executing query. Ontology=" + ontology + ", QueryType =" + queryType + ", Query = " + query
							+ ". Cause = " + e.getCause() + ", Message = " + e.getMessage() + ".");
		}
		return result.getResult();
	}

	@Override
	@FlowEngineAuditable
	public String submitInsert(String ontology, String data, String authentication)
			throws ResourceNotFoundException, NotAuthorizedException, JsonProcessingException, NotFoundException {

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		final OperationModel operationModel = OperationModel
				.builder(ontology, OperationType.INSERT, sofia2User.getUserId(), OperationModel.Source.FLOWENGINE)
				.body(data).build();

		OperationResultModel result = null;

		try {
			final NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.insert(notificationModel);
		} catch (final Exception e) {
			log.error("Error inserting data. Ontology={}, Data = {}. Cause = {}, Message = {}.", ontology, data,
					e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Error executing query. Ontology=" + ontology + ", Data = " + data
					+ ". Cause = " + e.getCause() + ", Message = " + e.getMessage() + ".");
		}
		return result.getResult();
	}

	@Override
	public List<DigitalTwinTypeDTO> getDigitalTwinTypes(String authentication)
			throws ResourceNotFoundException, NotAuthorizedException {

		final List<DigitalTwinTypeDTO> response = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		final List<DigitalTwinType> digitalTwinTypes = digitalTwinTypeService.getAll();

		for (final DigitalTwinType digitalTwinType : digitalTwinTypes) {
			final DigitalTwinTypeDTO type = new DigitalTwinTypeDTO();
			type.setName(digitalTwinType.getName());
			type.setJson(digitalTwinType.getJson());
			final List<DigitalTwinDevice> devices = digitalTwinDeviceService
					.getAllDigitalTwinDevicesByTypeId(digitalTwinType.getName());
			final List<DigitalTwinDeviceDTO> devicesDTO = new ArrayList<>();
			if (devices != null) {
				for (final DigitalTwinDevice device : devices) {
					final DigitalTwinDeviceDTO deviceDTO = new DigitalTwinDeviceDTO();
					deviceDTO.setDevice(device.getIdentification());
					deviceDTO.setDigitalKey(device.getDigitalKey());
					devicesDTO.add(deviceDTO);
				}
			}
			type.setDevices(devicesDTO);
			response.add(type);
		}
		return response;

	}
}
