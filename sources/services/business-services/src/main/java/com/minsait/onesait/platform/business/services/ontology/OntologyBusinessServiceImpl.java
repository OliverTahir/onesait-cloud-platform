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
package com.minsait.onesait.platform.business.services.ontology;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.kafka.KafkaService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicService;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@Service
public class OntologyBusinessServiceImpl implements OntologyBusinessService {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyLogicService ontologyLogicService;

	@Autowired(required = false)
	private KafkaService kafkaService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private OntologyKPIRepository ontologyKPIRepository;
	
	@Autowired
	private TaskService taskService;
	
	static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";

	@Override
	public void createOntology(Ontology ontology, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException {

		if (!ontologyService.isIdValid(ontology.getIdentification())) {
			// log.debug("The ontology name is not valid");
			// utils.addRedirectMessage("ontology.validation.error", redirect);
			// return "redirect:/ontologies/create";
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT,
					"Ontology identification is not valid");
		}

		final String ontologyName = ontology.getIdentification();
		boolean topicCreated = false;

		final User user = userService.getUser(userId);
		ontology.setUser(user);

		try {
			ontologyService.checkOntologySchema(ontology.getJsonSchema());
		} catch (final OntologyDataJsonProblemException e) {
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.NO_VALID_SCHEMA,
					"The provided json schema is not valid", e);
		}

		if (ontology.isAllowsCreateTopic()) {
			if (kafkaService != null) {
				topicCreated = kafkaService.createTopicForOntology(ontologyName);
			}

			if (topicCreated == true) {
				ontology.setTopic(kafkaService.getTopicName(ontologyName));
			} else {
				throw new OntologyBusinessServiceException(
						OntologyBusinessServiceException.Error.KAFKA_TOPIC_CREATION_ERROR,
						"Error creationg kafka topic");
			}
		}
		if (ontology.isRtdbToHdb())
			ontology.setRtdbClean(true);
		else
			ontology.setRtdbToHdbStorage(null);

		if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
			ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_DAY);
		}

		try {
			ontologyService.createOntology(ontology, config);
		} catch (final Exception e) {
			try {
				if (topicCreated) {
					kafkaService.deleteTopic(ontologyName);
				}
			} catch (final Exception e2) {
				throw new OntologyBusinessServiceException(
						OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR_UNCLEAN,
						"Error creating the ontology, it was not possible to undo the kafka topic creation", e);
			}
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.CONFIG_CREATION_ERROR,
					"Error creating the ontology configuration: " + e.getMessage(), e);
		}

		try {
			ontologyLogicService.createOntology(ontology);
		} catch (final Exception e) {
			try {
				if (topicCreated) {
					kafkaService.deleteTopic(ontologyName);
				}
				ontologyService.delete(ontology);
			} catch (final Exception e2) {
				throw new OntologyBusinessServiceException(
						OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR_UNCLEAN,
						"Error creating the persistence infrastructure for ontology, it was not possible to undo the ontology configuration and/or the kafka topic creation");
			}
			throw new OntologyBusinessServiceException(
					OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR,
					"Error creating the persistence infrastructure for ontology: " + e.getMessage(), e);
		}
	}
	
	   @Override
	    public void scheduleKpi(OntologyKPI oKPI) {
	        if (!oKPI.isActive()) {
	            final TaskInfo task = new TaskInfo();
	            task.setSchedulerType(SchedulerType.OKPI);

	            final Map<String, Object> jobContext = new HashMap<String, Object>();
	            jobContext.put("id", oKPI.getId());
	            jobContext.put("ontology", oKPI.getOntology().getIdentification());
	            jobContext.put("query", oKPI.getQuery());
	            jobContext.put("userId", oKPI.getUser().getUserId());
	            task.setJobName("Ontology KPI");
	            task.setData(jobContext);
	            if (oKPI.getDateFrom() != null) {
	                task.setStartAt(oKPI.getDateFrom());
	            }
	            if (oKPI.getDateTo() != null) {
	                task.setEndAt(oKPI.getDateTo());
	            }
	            task.setSingleton(false);
	            task.setCronExpression(oKPI.getCron());
	            task.setUsername(oKPI.getUser().getUserId());
	            final ScheduleResponseInfo response = taskService.addJob(task);
	            oKPI.setActive(true);
	            oKPI.setJobName(response.getJobName());
	            ontologyKPIRepository.save(oKPI);

	        }

	    }

	    @Override
	    public void unscheduleKpi(OntologyKPI oKPI) {
	        final String jobName = oKPI.getJobName();
	        if (jobName != null && oKPI.isActive()) {
	            final TaskOperation operation = new TaskOperation();
	            operation.setJobName(jobName);
	            taskService.unscheduled(operation);
	            oKPI.setActive(false);
	            oKPI.setJobName(null);
	            ontologyKPIRepository.save(oKPI);
	        }

	    }

	    @Override
	    public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
	        final JsonNode schemaSubTree = organizeRootNodeIfExist(schema);
	        ((ObjectNode) schemaSubTree).put("type", "object");
	        ((ObjectNode) schemaSubTree).put("description", "Info " + identification);

	        ((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
	        ((ObjectNode) schemaSubTree).put("title", identification);

	        ((ObjectNode) schemaSubTree).put("additionalProperties", true);
	        return schemaSubTree;
	    }

	    @Override
	    public JsonNode organizeRootNodeIfExist(String schema) throws IOException {

	        final ObjectMapper mapper = new ObjectMapper();
	        final JsonNode schemaSubTree = mapper.readTree(schema);
	        boolean find = Boolean.FALSE;
	        for (final Iterator<Entry<String, JsonNode>> elements = schemaSubTree.fields(); elements.hasNext();) {
	            final Entry<String, JsonNode> e = elements.next();
	            if (e.getKey().equals("properties")) {
	                e.getValue().fields();
	                for (final Iterator<Entry<String, JsonNode>> properties = e.getValue().fields(); properties
	                        .hasNext();) {
	                    final Entry<String, JsonNode> prop = properties.next();
	                    final String field = prop.getKey();
	                    if (!field.toUpperCase().equals(field) && Character.isUpperCase(field.charAt(0))) {
	                        ((ObjectNode) schemaSubTree).set("datos", prop.getValue());
	                        // Add required
	                        ArrayNode required = ((ObjectNode) schemaSubTree).putArray("required");
	                        required.add(prop.getKey());
	                        final String newString = "{\"type\": \"string\",\"$ref\": \"#/datos\"}";
	                        final JsonNode newNode = mapper.readTree(newString);
	                        prop.setValue(newNode);
	                        find = Boolean.TRUE;
	                        break;
	                    }
	                    if (find) {
	                        break;
	                    }
	                }
	            }
	        }
	        return schemaSubTree;
	    }

}
