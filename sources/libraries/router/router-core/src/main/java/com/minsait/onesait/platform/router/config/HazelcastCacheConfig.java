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
package com.minsait.onesait.platform.router.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.minsait.onesait.platform.audit.notify.EventSenderImpl;
import com.minsait.onesait.platform.router.camel.CamelContextHandler;
import com.minsait.onesait.platform.router.config.hazelcast.ClusterMembershipListener;
import com.minsait.onesait.platform.router.config.hazelcast.HzDistributedObjectListener;
import com.minsait.onesait.platform.router.config.hazelcast.NodeLifecycleListener;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching(mode = AdviceMode.ASPECTJ)
@Slf4j
public class HazelcastCacheConfig {

	@Autowired
	CamelContextHandler camelContextHandler;

	// @Bean
	// public HazelcastQueueComponent hazelcastQueueComponent() {
	// // setup camel hazelcast
	// final HazelcastQueueComponent hazelcast = new HazelcastQueueComponent();
	// hazelcast.setHazelcastInstance(hazelcastInstance);
	// camelContextHandler.getDefaultCamelContext().addComponent("hazelcast-queue",
	// hazelcast);
	// return hazelcast;
	// }

	// @Bean
	/*
	 * public HazelcastInstance hazelcastInstanceEmbedded() { return
	 * Hazelcast.newHazelcastInstance(); }
	 */

	@Bean(name = "auditQueue")
	public IQueue<String> hazelcastAuditQueue() {
		IQueue<String> auditQueue = hazelcastInstance.getQueue(EventSenderImpl.AUDIT_QUEUE_NAME);
		return auditQueue;
	}

	@Autowired
	HazelcastInstance hazelcastInstance;

	@Bean
	CacheManager cacheManager() {
		if (hazelcastInstance != null) {
			hazelcastInstance.getCluster().addMembershipListener(new ClusterMembershipListener());
			final HzDistributedObjectListener sample = new HzDistributedObjectListener();
			hazelcastInstance.addDistributedObjectListener(sample);
			hazelcastInstance.getLifecycleService().addLifecycleListener(new NodeLifecycleListener());
			final CacheManager manager = new HazelcastCacheManager(hazelcastInstance);
			log.info("Configured Global Cache Manager: Name : " + manager.toString());
			return manager;
		} else {
			log.info("NO Op Cache will be configured");
			return new NoOpCacheManager();
		}
	}

}