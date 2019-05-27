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
package com.minsait.onesait.platform.router.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.advice.AdviceServiceImpl;

public class Application {


	public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException {

		OperationResultModel input = new OperationResultModel();
		NotificationCompositeModel model = new NotificationCompositeModel();

		RouterClient<NotificationCompositeModel, OperationResultModel> routerClient = new AdviceServiceImpl();
		RouterClientGateway<NotificationCompositeModel, OperationResultModel> gateway = new RouterClientGateway<NotificationCompositeModel, OperationResultModel>(
				RouterClientGateway.setupDefault("PEPE", "PEPE"), routerClient);
		gateway.setFallback(input);
		input = gateway.execute(model);

	}

}