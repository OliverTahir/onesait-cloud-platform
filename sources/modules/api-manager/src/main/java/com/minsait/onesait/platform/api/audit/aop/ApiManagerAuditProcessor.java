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
package com.minsait.onesait.platform.api.audit.aop;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.audit.bean.ApiManagerAuditEvent;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditError;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.Sofia2EventFactory;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApiManagerAuditProcessor {

	public ApiManagerAuditEvent getStoppedEvent(Exchange exchange) {
		ApiManagerAuditEvent event = null;
		try {
			if ("STOP".equals(exchange.getIn().getHeader(ApiServiceInterface.STATUS))) {
				final String reason = (String) exchange.getIn().getHeader(ApiServiceInterface.REASON);

				final String remoteAddress = (String) exchange.getIn().getHeader(ApiServiceInterface.REMOTE_ADDRESS);
				final Ontology ontology = (Ontology) exchange.getIn().getHeader(ApiServiceInterface.ONTOLOGY);
				final String method = (String) exchange.getIn().getHeader(ApiServiceInterface.METHOD);

				final String query = (String) exchange.getIn().getHeader(ApiServiceInterface.QUERY);
				final String body = (String) exchange.getIn().getHeader(ApiServiceInterface.BODY);

				final OperationType operationType = getAuditOperationFromMethod(method);

				final User user = (User) exchange.getIn().getHeader(ApiServiceInterface.USER);

				final String userId = getUserId(user);
				final String ontologyId = getOntologyId(ontology);
				final String operation = getOperation(operationType);

				final Date today = new Date();

				event = ApiManagerAuditEvent.builder().id(UUID.randomUUID().toString()).module(Module.APIMANAGER)
						.type(EventType.APIMANAGER).operationType(operation).resultOperation(ResultOperationType.ERROR)
						.remoteAddress(remoteAddress).message(reason).data(body).ontology(ontologyId).query(query)
						.timeStamp(today.getTime()).user(userId)
						.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).build();
			}

		} catch (final Exception e) {
			log.error("error processing stopped event ", e);
		}
		return event;
	}

	public ApiManagerAuditEvent getEvent(Map<String, Object> data) {
		final String remoteAddress = (String) data.get(ApiServiceInterface.REMOTE_ADDRESS);
		final Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		final String query = (String) data.get(ApiServiceInterface.QUERY);
		final String body = (String) data.get(ApiServiceInterface.BODY);
		final User user = (User) data.get(ApiServiceInterface.USER);
		final Api api = (Api) data.get(ApiServiceInterface.API);
		final OperationType operationType = getAuditOperationFromMethod(method);
		final String operation = getOperation(operationType);

		final Date today = new Date();

		final String userId = getUserId(user);
		final String ontologyId = getOntologyId(ontology);

		final String message = operation + " on ontology " + ontologyId + " by user " + userId;

		final ApiManagerAuditEvent event = ApiManagerAuditEvent.builder().id(UUID.randomUUID().toString())
				.module(Module.APIMANAGER).type(EventType.APIMANAGER).operationType(operation)
				.resultOperation(ResultOperationType.SUCCESS).remoteAddress(remoteAddress).message(message).data(body)
				.ontology(ontologyId).query(query).timeStamp(today.getTime()).user(userId).api(api.getIdentification())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).build();

		return event;
	}

	public Sofia2AuditError getErrorEvent(Map<String, Object> data, Exchange exchange, Exception ex) {
		final String remoteAddress = (String) data.get(ApiServiceInterface.REMOTE_ADDRESS);
		final Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		final User user = (User) data.get(ApiServiceInterface.USER);

		final OperationType operationType = getAuditOperationFromMethod(method);
		final String operation = getOperation(operationType);

		final String userId = getUserId(user);
		final String ontologyId = getOntologyId(ontology);

		final String messageOperation = "Exception Detected while operation : " + ontologyId + " Type : " + operation;

		final Sofia2AuditError event = Sofia2EventFactory.builder().build().createAuditEventError(userId,
				messageOperation, remoteAddress, Module.APIMANAGER, ex);

		return event;
	}

	public ApiManagerAuditEvent completeEvent(Map<String, Object> retVal, ApiManagerAuditEvent event) {
		if (event != null && (event.getUser() == null || "".equals(event.getUser()))) {
			log.debug("the user is null so set user to anonymous");
			event.setUser(AuditConst.ANONYMOUS_USER);
		}
		return event;
	}

	public OperationType getAuditOperationFromMethod(String method) {
		OperationType operationType = null;
		if (method != null) {
			if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
				operationType = OperationType.QUERY;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
				operationType = OperationType.INSERT;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
				operationType = OperationType.UPDATE;
			} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
				operationType = OperationType.DELETE;
			}
		}
		log.debug("the audit operation from method " + method + " is " + operationType);
		return operationType;

	}

	public String getUserId(User user) {
		final String userId = (user != null) ? user.getUserId() : AuditConst.ANONYMOUS_USER;
		return userId;
	}

	public String getOntologyId(Ontology ontology) {
		final String ontologyId = (ontology != null) ? ontology.getIdentification() : null;
		return ontologyId;
	}

	public String getOperation(OperationType operationType) {
		final String operation = (operationType != null) ? operationType.name() : "";
		return operation;
	}

}
