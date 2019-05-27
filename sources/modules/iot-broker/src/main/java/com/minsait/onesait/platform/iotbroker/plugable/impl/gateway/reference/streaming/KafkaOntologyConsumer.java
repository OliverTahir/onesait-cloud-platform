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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.streaming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
@Slf4j
@Component
public class KafkaOntologyConsumer {

	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	OntologyDataService ontologyDataService;

	@Autowired
	RouterService routerService;

	ObjectMapper mapper = new ObjectMapper();

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.prefix:ontology_}")
	private String ontologyPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.router.topic:router}")
	private String topicRouter;

	@Autowired
	private KafkaTemplate<String, NotificationModel> kafkaTemplate;

	@KafkaListener(topicPattern = "${onesaitplatform.iotbroker.plugable.gateway.kafka.topic.pattern}", containerFactory = "kafkaListenerContainerFactoryBatch")
	public void listenToPartitionBatch(List<String> data,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.RECEIVED_TOPIC) List<String> receivedTopicList,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		try {
			log.info("listenToPartitionBatch: Start consuming batch messages size: " + data.size());
			
			Map<String, StringBuilder> batchsToInsert = new HashMap<>();

			String comma = "";
			for (int i = 0; i < data.size(); i++) {
				// log.info("received message='{}' with partition-offset='{}'", data.get(i),
				// partitions.get(i) + "-" + offsets.get(i));
								
				String receivedTopic = receivedTopicList.get(i);
				String ontologyId = receivedTopic.replace(ontologyPrefix, "");
				
				if (!batchsToInsert.containsKey(ontologyId)) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("[");
					batchsToInsert.put(ontologyId, stringBuilder);
				}
				
				StringBuilder sb = batchsToInsert.get(ontologyId);
				
				String message = data.get(i);		
				sb.append(comma);
				sb.append(message);
				comma = ",";
			}
			
			Set<String> ontologies = batchsToInsert.keySet();
			for (String ontology : ontologies) {
				String batch = batchsToInsert.get(ontology).append("]").toString();
				
				OperationType operationType = OperationType.INSERT;
				OperationModel model = OperationModel.builder(ontology, OperationType.valueOf(operationType.name()),
						null, OperationModel.Source.KAFKA).body(batch).deviceTemplate("").cacheable(false).build();
				NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);
				try {
					routerService.insert(modelNotification);
				} catch (Exception e) {
					log.error("listenToPartitionBatch:Message ignored by error:", e);
				}
			}
		} catch (Exception e) {
			log.error("listenToPartitionBatch:Error:", e);
		}

	}

	public void sendMessage(NotificationModel message) {
		ListenableFuture<SendResult<String, NotificationModel>> pp = kafkaTemplate.send(topicRouter, message);
	}

}
