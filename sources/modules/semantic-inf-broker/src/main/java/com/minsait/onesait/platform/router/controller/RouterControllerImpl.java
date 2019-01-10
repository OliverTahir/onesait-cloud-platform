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
package com.minsait.onesait.platform.router.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.audit.bean.Sofia2AuditEvent;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.router.audit.aop.Auditable;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceService;
import com.minsait.onesait.platform.router.service.app.service.RouterDigitalTwinService;
import com.minsait.onesait.platform.router.service.app.service.RouterService;
import com.minsait.onesait.platform.router.service.app.service.RouterSuscriptionService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@EnableAutoConfiguration
@CrossOrigin(origins = "*")
@RequestMapping("router")
@Slf4j
public class RouterControllerImpl implements RouterControllerInterface, RouterService, RouterSuscriptionService,
		AdviceService, RouterDigitalTwinService {

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterService routerService;

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterSuscriptionService routerSuscriptionService;

	@Autowired
	@Qualifier("routerDigitalTwinServiceImpl")
	private RouterDigitalTwinService routerDigitalTwinService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Autowired
	private EventProducer eventProducer;

	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	@ApiOperation(value = "insert")
	public OperationResultModel insert(@RequestBody NotificationModel model) throws Exception {
		log.info("insert:");
		try {
			return routerService.insert(model);
		} catch (Exception e) {
			log.error("Error in insert", e);
			throw e;
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ApiOperation(value = "update")
	public OperationResultModel update(@RequestBody NotificationModel model) throws Exception {
		log.info("update:");
		try {
			return routerService.update(model);
		} catch (Exception e) {
			log.error("Error in update", e);
			throw e;
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ApiOperation(value = "delete")
	public OperationResultModel delete(@RequestBody NotificationModel model) throws Exception {
		log.info("delete:");
		try {
			return routerService.delete(model);
		} catch (Exception e) {
			log.error("Error in delete", e);
			throw e;
		}
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ApiOperation(value = "query")
	public OperationResultModel query(@RequestBody NotificationModel model) throws Exception {
		log.info("query:");
		try {
			return routerService.query(model);
		} catch (Exception e) {
			log.error("Error in query", e);
			throw e;
		}
	}

	@RequestMapping(value = "/suscribe", method = RequestMethod.POST)
	@ApiOperation(value = "subscribe")
	public OperationResultModel suscribe(@RequestBody SuscriptionModel model) throws Exception {
		log.info("suscribe:");
		try {
			return routerSuscriptionService.suscribe(model);
		} catch (Exception e) {
			log.error("Error in suscribe", e);
			throw e;
		}
	}

	@RequestMapping(value = "/unsuscribe", method = RequestMethod.POST)
	@ApiOperation(value = "subscribe")
	public OperationResultModel unSuscribe(@RequestBody SuscriptionModel model) throws Exception {
		log.info("unSuscribe:");
		try {
			return routerSuscriptionService.unSuscribe(model);
		} catch (Exception e) {
			log.error("Error in unSuscribe", e);
			throw e;
		}
	}

	@RequestMapping(value = "/advice", method = RequestMethod.POST)
	@ApiOperation(value = "advice")
	@Override
	public OperationResultModel advicePostProcessing(@RequestBody NotificationCompositeModel input) {
		log.info("advicePostProcessing:");
		try {
			OperationResultModel output = new OperationResultModel();
			output.setErrorCode("NOUS");
			output.setMessage("ALL IS OK");
			output.setOperation("ADVICE");
			output.setResult("OK");
			return output;
		} catch (Exception e) {
			log.error("Error in advicePostProcessing", e);
			throw e;
		}
	}

	@RequestMapping(value = "/token", method = RequestMethod.POST)
	@ApiOperation(value = "token")
	public String tokenPostProcessing(@RequestBody String input) {
		log.info("tokenPostProcessing:");
		return jwtService.extractToken(input);
	}

	@RequestMapping(value = "/event", method = RequestMethod.POST)
	@ApiOperation(value = "event")
	@Auditable
	public String eventProcessing(@RequestBody String input) {
		log.info("eventProcessing:");
		Sofia2AuditEvent event = new Sofia2AuditEvent();
		event.setMessage(input);
		eventProducer.publish(event);
		return input;
	}

	@RequestMapping(value = "/insertEvent", method = RequestMethod.POST)
	@ApiOperation(value = "insertEvent")
	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		return routerDigitalTwinService.insertEvent(compositeModel);
	}

	@RequestMapping(value = "/insertLog", method = RequestMethod.POST)
	@ApiOperation(value = "insertLog")
	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		return routerDigitalTwinService.insertLog(compositeModel);
	}

	@RequestMapping(value = "/updateShadow", method = RequestMethod.POST)
	@ApiOperation(value = "updateShadow")
	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		return routerDigitalTwinService.updateShadow(compositeModel);
	}

	@Override
	@RequestMapping(value = "/insertAction", method = RequestMethod.POST)
	@ApiOperation(value = "insertAction")
	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		return routerDigitalTwinService.insertAction(compositeModel);
	}

}
