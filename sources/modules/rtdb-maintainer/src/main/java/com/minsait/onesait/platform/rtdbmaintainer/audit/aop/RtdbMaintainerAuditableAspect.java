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
package com.minsait.onesait.platform.rtdbmaintainer.audit.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.aop.BaseAspect;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditError;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.rtdbmaintainer.audit.bean.RtdbMaintainerAuditEvent;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order
@Component
@Slf4j
public class RtdbMaintainerAuditableAspect extends BaseAspect {

	@Autowired
	RtdbMaintainerAuditProcessor auditProcessor;

	@Around("@annotation(auditable) && args(ontology,query,..)")
	public Object exportToJsonAndDeleteAudit(ProceedingJoinPoint joinPoint, Ontology ontology,
			RtdbMaintainerAuditable auditable, String query) throws Throwable {
		String file = null;
		log.debug("execute rtdb maintainer aspect method export to json");
		RtdbMaintainerAuditEvent event = auditProcessor.getEvent(ontology, joinPoint.getSignature().getName());
		eventProducer.publish(event);
		try {

			file = (String) joinPoint.proceed();
			event = auditProcessor.completeEvent(ontology, joinPoint.getSignature().getName());
		} catch (final Throwable e) {
			log.error("Exception while auditing method exportToJsonAndDeleteAudit");
			throw e;
		}

		eventProducer.publish(event);
		return file;
	}

	@AfterThrowing(pointcut = "@annotation(auditable) && args(ontology,..)", throwing = "ex")
	public void doRecoveryActions(JoinPoint joinPoint, RtdbMaintainerAuditable auditable, Ontology ontology,
			Exception ex) {
		log.debug("Processing Rtdb maintainer auditable recovery actions");
		final Sofia2AuditError event = auditProcessor.getErrorEvent(ontology, ex);
		eventProducer.publish(event);
	}

}
