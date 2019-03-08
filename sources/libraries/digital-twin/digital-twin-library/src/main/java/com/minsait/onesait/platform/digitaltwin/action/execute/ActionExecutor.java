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
package com.minsait.onesait.platform.digitaltwin.action.execute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.logic.LogicManager;
import com.minsait.onesait.platform.digitaltwin.status.IDigitalTwinStatus;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActionExecutor {

	@Autowired
	private IDigitalTwinStatus digitalTwinStatus;

	@Autowired
	private LogicManager logicManager;

	@Autowired(required = false)
	private ActionJavaListener actionJavaListener;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public void executeAction(String name, String data) {

		if (null != actionJavaListener) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						actionJavaListener.executeAction(name, data);
					} catch (Exception e) {
						log.error("Error executing Java Action", e);
					}
				}
			});
		}

		try {
			log.info("Invoques Javascript function");
			this.logicManager.invokeFunction("onAction" + name.substring(0, 1).toUpperCase() + name.substring(1), data);

		} catch (ScriptException e1) {
			log.error("Execution logic for action " + name + " failed", e1);
		} catch (NoSuchMethodException e2) {
			log.error("Action " + name + " not found", e2);
		}

	}

}