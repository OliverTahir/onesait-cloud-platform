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
package com.minsait.onesait.platform.router.service.app.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.client.RouterClient;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import lombok.extern.slf4j.Slf4j;

//@Service("routerDigitalTwinServiceImpl")
@Slf4j
public class RouterDigitalTwinServiceImpl
		implements RouterDigitalTwinService, RouterClient<DigitalTwinCompositeModel, OperationResultModel> {

	@Value("${onesaitplatform.router.alternativeURL:http://localhost:20000/router/router/")
	private String routerURLAlternative4;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private String routerStandaloneURL;

	@PostConstruct
	public void postConstruct() {
		routerStandaloneURL = resourcesService.getUrl(Module.routerStandAlone, ServiceUrl.router);
		if ("RESOURCE_URL_NOT_FOUND".equals(routerStandaloneURL)) {
			routerStandaloneURL = routerURLAlternative4;
		}
		try {
			SSLUtil.turnOffSslChecking();
		} catch (KeyManagementException e) {
			log.info(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			log.info(e.getMessage());
		}

	}

	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationResultModel execute(DigitalTwinCompositeModel input) {
		// TODO Auto-generated method stub
		return null;
	}

}
