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
package com.minsait.onesait.platform.audit.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventSenderImpl implements EventRouter {

	public final static String AUDIT_QUEUE_NAME = "audit";

	@Autowired
	@Qualifier("globalCache")
	private HazelcastInstance instance;
	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Override
	public void notify(String event) {
		log.info("Received Audit Event: " + event.toString());
		if (ignoreAudit()) {
			log.warn("You have configured to ignore Audit. Review it!");
			return;
		}
		if (instance != null) {
			instance.getQueue(AUDIT_QUEUE_NAME).offer(event);
		}

	}

	private boolean ignoreAudit() {
		boolean b = false;
		try {
			b = ((Boolean) resourcesServices.getGlobalConfiguration().getEnv().getAudit().get("ignore")).booleanValue();
		} catch (final RuntimeException e) {
			log.error("Could not find property ignore-audit, returning false as default");
		}
		return b;
	}
	/*
	 * @Override public void notify(Sofia2AuditEvent event) { log.
	 * info("EventSenderImpl :: thread '{}' handling '{}' Notify to Router The Event: "
	 * , Thread.currentThread(), event.getMessage());
	 * instance.getQueue("audit").offer(event.toJson());
	 *
	 * }
	 */

}
