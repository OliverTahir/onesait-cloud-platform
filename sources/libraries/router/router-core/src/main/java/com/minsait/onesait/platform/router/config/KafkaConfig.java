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
package com.minsait.onesait.platform.router.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.minsait.onesait.platform.router.camel.CamelContextHandler;

import lombok.extern.slf4j.Slf4j;

//@ConditionalOnProperty(prefix = "sofia2.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
//@Configuration
@Slf4j
public class KafkaConfig {

	@Autowired
	CamelContextHandler camelContextHandler;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.host:localhost}")
	private String kafkaHost;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.port:9092}")
	private String kafkaPort;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.partitions:1}")
	int partitions;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.replication:1}")
	short replication;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.router.topic:router}")
	private String topicRouter;

	@Bean
	public KafkaComponent kafkaComponent() {
		KafkaComponent kafka = new KafkaComponent();
		kafka.setBrokers(kafkaHost + ":" + kafkaPort);
		camelContextHandler.getDefaultCamelContext().addComponent("kafka", kafka);
		return kafka;
	}

	@PostConstruct
	public void postKafka() {
		Properties config = new Properties();
		config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		
		try(AdminClient adminAcl = AdminClient.create(config))
		{
			//AdminClient adminAcl = AdminClient.create(config);
	
			NewTopic t = new NewTopic(topicRouter, partitions, replication);
			adminAcl.createTopics(Arrays.asList(t));
		}
		catch (final Exception e) {
			log.error("Error in postKafka function, message: {}", e.getMessage());
		}

	}
}