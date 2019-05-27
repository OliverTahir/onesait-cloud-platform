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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthorizationException;
import com.minsait.onesait.platform.iotbroker.common.exception.BaseException;
import com.minsait.onesait.platform.iotbroker.common.exception.OntologySchemaException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.persistence.util.MultiDocumentOperationResult;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InsertProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;

	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	SecurityPluginManager securityPluginManager;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message)
			throws BaseException {
		@SuppressWarnings("unchecked")
		final SSAPMessage<SSAPBodyInsertMessage> insertMessage = (SSAPMessage<SSAPBodyInsertMessage>) message;
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();

		final Optional<IoTSession> session = securityPluginManager.getSession(insertMessage.getSessionKey());

		String user = null;
		String deviceTemplate = null;
		String device = null;
		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
			device = session.get().getDevice();
		}

		final OperationModel model = OperationModel
				.builder(insertMessage.getBody().getOntology(), OperationType.POST, user, Source.IOTBROKER)
				.body(insertMessage.getBody().getData().toString()).queryType(QueryType.NATIVE)
				.deviceTemplate(deviceTemplate).device(device).clientSession(insertMessage.getSessionKey())
				.clientConnection("").build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);

		String repositoryResponse = "";
		try {
			final OperationResultModel result = routerService.insert(modelNotification);
			if (!result.getResult().equals("ERROR")) {
				repositoryResponse = result.getResult();

				responseMessage.setDirection(SSAPMessageDirection.RESPONSE);
				responseMessage.setMessageId(insertMessage.getMessageId());
				responseMessage.setMessageType(insertMessage.getMessageType());
				// responseMessage.setOntology(insertMessage.getOntology());
				responseMessage.setSessionKey(insertMessage.getSessionKey());
				responseMessage.setBody(new SSAPBodyReturnMessage());
				responseMessage.getBody().setOk(true);

				MultiDocumentOperationResult multidocument = MultiDocumentOperationResult
						.fromString(repositoryResponse);
				long totalInserted = multidocument.getCount();
				if (totalInserted == 1) {
					responseMessage.getBody()
							.setData(objectMapper.readTree("{\"id\":\"" + multidocument.getIds().get(0) + "\"}"));
				} else if (totalInserted > 1) {
					String bulkResponse = String.format("{\"nInserted\":%s, \"inserted\":%s}", multidocument.getCount(),
							multidocument.getStrIds());

					responseMessage.getBody().setData(objectMapper.readTree(bulkResponse));
				}

			} else {
				throw new SSAPProcessorException(result.getMessage());
			}

		} catch (final Exception e1) {
			log.error("Error processing Insert", e1);
			throw new SSAPProcessorException("Response from repository on insert is not JSON compliant, cause : " + e1);
		}

		return responseMessage;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.INSERT);
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message)
			throws AuthorizationException, OntologySchemaException, SSAPProcessorException {

		final SSAPMessage<SSAPBodyInsertMessage> insertMessage = (SSAPMessage<SSAPBodyInsertMessage>) message;

		if (insertMessage.getBody().getData() == null || insertMessage.getBody().getData().isNull()) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "data",
					insertMessage.getMessageType().name()));
		}
		return true;
	}
}
