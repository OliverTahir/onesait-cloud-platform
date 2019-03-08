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

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditError;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.Sofia2EventFactory;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.rtdbmaintainer.audit.bean.RtdbMaintainerAuditEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RtdbMaintainerAuditProcessor {

	private static final String METHOD_EXPORT = "performExport";
	private static final String METHOD_DELETE = "performDelete";

	public RtdbMaintainerAuditEvent getEvent(Ontology ontology, String method) {

		String message = "";
		if (method.toLowerCase().contains(METHOD_EXPORT.toLowerCase()))
			message = "Exporting instances for ontology " + ontology.getIdentification() + ": STARTED";
		else if (method.toLowerCase().contains(METHOD_DELETE.toLowerCase()))
			message = "Deleting instances for ontology " + ontology.getIdentification() + ": STARTED";

		final Date today = new Date();
		return RtdbMaintainerAuditEvent.builder().id(UUID.randomUUID().toString()).timeStamp(today.getTime())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).message(message)
				.ontology(ontology.getIdentification()).user(ontology.getUser().getUserId())
				.module(Module.RTDBMAINTAINER).type(EventType.BATCH).operationType(OperationType.EXPORT.name())
				.resultOperation(ResultOperationType.SUCCESS).build();

	}

	public Sofia2AuditError getErrorEvent(Ontology ontology, Exception e) {

		final String message = "Exception detected while exporting json from database for ontology "
				+ ontology.getIdentification();

		return Sofia2EventFactory.builder().build().createAuditEventError(message, Module.RTDBMAINTAINER, e);
	}

	public RtdbMaintainerAuditEvent completeEvent(Ontology ontology, String method) {

		String message = "";
		if (method.toLowerCase().contains(METHOD_EXPORT.toLowerCase()))
			message = "Exporting instances for ontology " + ontology.getIdentification() + ": COMPLETED";
		else if (method.toLowerCase().contains(METHOD_DELETE.toLowerCase()))
			message = "Deleting instances for ontology " + ontology.getIdentification() + ": COMPLETED";

		final Date today = new Date();

		return RtdbMaintainerAuditEvent.builder().id(UUID.randomUUID().toString()).timeStamp(today.getTime())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).message(message)
				.ontology(ontology.getIdentification()).user(ontology.getUser().getUserId())
				.module(Module.RTDBMAINTAINER).type(EventType.BATCH).operationType(OperationType.DELETE.name())
				.resultOperation(ResultOperationType.SUCCESS).build();
	}
}