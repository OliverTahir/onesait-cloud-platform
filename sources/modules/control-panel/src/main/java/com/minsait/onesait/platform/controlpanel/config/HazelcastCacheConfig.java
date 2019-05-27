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
package com.minsait.onesait.platform.controlpanel.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.minsait.onesait.platform.controlpanel.config.hazelcast.ClusterMembershipListener;
import com.minsait.onesait.platform.controlpanel.config.hazelcast.HzDistributedObjectListener;
import com.minsait.onesait.platform.controlpanel.config.hazelcast.NodeLifecycleListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching(mode = AdviceMode.ASPECTJ)
@Slf4j
public class HazelcastCacheConfig {

	@Autowired
	private HazelcastInstance hazelcastInstance;
	@Value("${onesaitplatform.videobroker.hazelcast.queue}")
	private String videoQueueName;

	@Bean(name = "videoQueue")
	public IQueue<String> hazelcastVideoQueue() {
		final IQueue<String> videoQueue = hazelcastInstance.getQueue(videoQueueName);
		return videoQueue;
	}

	@Bean
	CacheManager cacheManager() {
		if (hazelcastInstance != null) {
			hazelcastInstance.getCluster().addMembershipListener(new ClusterMembershipListener());
			final HzDistributedObjectListener sample = new HzDistributedObjectListener();
			hazelcastInstance.addDistributedObjectListener(sample);
			hazelcastInstance.getLifecycleService().addLifecycleListener(new NodeLifecycleListener());
			final CacheManager manager = new HazelcastCacheManager(hazelcastInstance);
			log.info("Configured Local Cache Manager: Name : " + manager.toString());
			return manager;
		} else
			return new NoOpCacheManager();
	}
}