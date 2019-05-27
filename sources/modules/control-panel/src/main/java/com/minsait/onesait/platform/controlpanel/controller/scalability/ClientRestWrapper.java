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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.comms.protocol.exception.SSAPConnectionException;

public class ClientRestWrapper implements Client {

	private RestClient client;
	private String token;
	private String clientPlatform;
	private String clientPlatformInstance;
	private boolean avoidSSLValidation;
	private int attempts = 0;
	private final static int MAX_ATTEMPTS = 100;
	private final static int ATTEMPTS_DELAY = 500;

	public ClientRestWrapper(String url) {
		createClient(url);
	}

	@Override
	public void createClient(String url) {
		final RestClient restClient = new RestClient(url);
		if (client != null) {
			client.disconnect();
		}
		client = restClient;
	}

	@Override
	public void connect(String token, String clientPlatform, String clientPlatformInstance, boolean avoidSSLValidation)
			throws SSAPConnectionException {
		this.token = token;
		this.clientPlatform = clientPlatform;
		this.clientPlatformInstance = clientPlatformInstance;
		this.avoidSSLValidation = avoidSSLValidation;
		client.connect(token, clientPlatform, clientPlatformInstance, avoidSSLValidation);
	}

	private void reconnect() throws InterruptedException, SSAPConnectionException {
		Thread.sleep(ATTEMPTS_DELAY);
		if (MAX_ATTEMPTS > attempts) {
			attempts++;
			try {
				connect(token, clientPlatform, clientPlatformInstance, avoidSSLValidation);
			} catch (final SSAPConnectionException e) {
				reconnect();
			}
		} else {
			throw new RuntimeException("Impossible to reconnect with the server");
		}
	}

	@Override
	public void insertInstance(String ontology, String instance) throws SSAPConnectionException {
		try {
			client.insert(ontology, instance);
		} catch (final SSAPConnectionException e) {
			try {
				reconnect();
			} catch (final InterruptedException e1) {
				throw new RuntimeException("Error sleeping task");
			}
		}
	}

	@Override
	public void disconnect() {
		client.disconnect();
	}

	@Override
	public String getProtocol() {
		return "rest";
	}

}
