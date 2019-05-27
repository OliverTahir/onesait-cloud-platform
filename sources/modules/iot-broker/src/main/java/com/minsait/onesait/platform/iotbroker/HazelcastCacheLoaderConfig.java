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
package com.minsait.onesait.platform.iotbroker;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HazelcastCacheLoaderConfig {

	@Bean(name="globalCache")
	@Primary
	@Profile("default")
	public HazelcastInstance defaultHazelcastInstanceEmbedded() throws IOException {
		String configFile = "hazelcast-client.xml";
		ClientConfig config = new XmlClientConfigBuilder(configFile).build();
		log.info("Configured Local Cache with data: Name : "+configFile+" Instance Name: "+config.getInstanceName()+" Group Name: "+config.getGroupConfig().getName());
		return HazelcastClient.newHazelcastClient(config);
	}

	@Bean(name="globalCache")
	@Primary
	@Profile("docker")
	public HazelcastInstance dockerHazelcastInstanceEmbedded() throws IOException {
		String configFile = "hazelcast-client-docker.xml";
		ClientConfig config = new XmlClientConfigBuilder(configFile).build();
		log.info("Configured Local Cache with data: Name : "+configFile+" Instance Name: "+config.getInstanceName()+" Group Name: "+config.getGroupConfig().getName());
		return HazelcastClient.newHazelcastClient(config);
	}
}
