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
package com.minsait.onesait.platform.config.services.client;

import java.util.List;

import org.json.JSONException;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;

public interface ClientPlatformService {

	Token createClientAndToken(List<Ontology> ontologies, ClientPlatform clientPlatform);

	ClientPlatform getByIdentification(String identification);

	public List<ClientPlatform> getAllClientPlatforms();

	public List<ClientPlatform> getclientPlatformsByUser(User user);

	List<ClientPlatform> getAllClientPlatformByCriteria(String userId, String identification, String[] ontologies);

	List<AccessType> getClientPlatformOntologyAccessLevel();

	ClientPlatform createClientPlatform(DeviceCreateDTO device, String userId, Boolean isUpdate) throws JSONException;

	void updateDevice(DeviceCreateDTO clientPlatform, String userI) throws JSONException;

	void createOntologyRelation(Ontology ontology, ClientPlatform clientPlatform);

	public Ontology createDeviceLogOntology(String clientIdentification);

	Ontology getDeviceLogOntology(ClientPlatform client);

	public List<Token> getTokensByClientPlatformId(String clientPlatformId);

	public List<Ontology> getOntologiesByClientPlatform(String clientPlatformId);

	public ClientPlatform getById(String id);

	public boolean hasUserManageAccess(String id, String userId);

	public boolean hasUserViewAccess(String id, String userId);

}
