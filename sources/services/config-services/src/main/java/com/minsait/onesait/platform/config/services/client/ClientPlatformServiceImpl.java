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
package com.minsait.onesait.platform.config.services.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.exceptions.TokenServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class ClientPlatformServiceImpl implements ClientPlatformService {
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private OPResourceService resourceService;

	private static final String LOG_ONTOLOGY_PREFIX = "LOG_";
	private static final String LOG_DEVICE_DATA_MODEL = "DeviceLog";
	private static final String ACCESS_STR = "access";

	@Override
	public Token createClientAndToken(List<Ontology> ontologies, ClientPlatform clientPlatform)
			throws TokenServiceException {
		if (clientPlatformRepository.findByIdentification(clientPlatform.getIdentification()) == null) {
			final String encryptionKey = UUID.randomUUID().toString();
			clientPlatform.setEncryptionKey(encryptionKey);
			clientPlatform = clientPlatformRepository.save(clientPlatform);

			for (final Ontology ontology : ontologies) {
				final ClientPlatformOntology relation = new ClientPlatformOntology();
				relation.setClientPlatform(clientPlatform);
				relation.setAccess(AccessType.ALL.name());
				relation.setOntology(ontology);
				// If relation does not exist then create
				if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(ontology.getIdentification(),
						clientPlatform.getIdentification()) == null) {
					clientPlatformOntologyRepository.save(relation);
				}
			}

			final Token token = tokenService.generateTokenForClient(clientPlatform);
			return token;
		} else {
			throw new ClientPlatformServiceException("Platform Client already exists");
		}
	}

	@Override
	public ClientPlatform getByIdentification(String identification) {
		return clientPlatformRepository.findByIdentification(identification);
	}

	@Override
	public List<ClientPlatform> getAllClientPlatforms() {
		return clientPlatformRepository.findAll();
	}

	@Override
	public List<ClientPlatform> getclientPlatformsByUser(User user) {
		return clientPlatformRepository.findByUser(user);
	}

	@Override
	public List<ClientPlatform> getAllClientPlatformByCriteria(String userId, String identification,
			String[] ontologies) {
		List<ClientPlatform> clients = new ArrayList<ClientPlatform>();

		final User user = userService.getUser(userId);

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			if (identification != null) {

				clients.add(clientPlatformRepository.findByIdentification(identification));

			} else {

				clients = clientPlatformRepository.findAll();
			}
			final List<ClientPlatform> clientPlatformAdd = new ArrayList<ClientPlatform>();
			if (ontologies != null && ontologies.length > 0) {
				for (final ClientPlatform k : clients) {
					for (int i = 0; i < ontologies.length; i++) {
						final Ontology o = ontologyRepository.findByIdentification(ontologies[i]);
						if (o != null) {
							final ClientPlatformOntology clpo = clientPlatformOntologyRepository
									.findByOntologyAndClientPlatform(o.getIdentification(), k.getIdentification());
							if (clpo != null) {
								if (!clientPlatformAdd.contains(k)) {
									clientPlatformAdd.add(k);
								}
							}
						}

					}

				}
				return clientPlatformAdd;
			}
			return clients;
		} else {
			if (identification != null) {

				clients.add(clientPlatformRepository.findByUserAndIdentification(user, identification));

			} else {

				clients = clientPlatformRepository.findByUser(user);
			}
			final List<ClientPlatform> clientPlatformAdd = new ArrayList<ClientPlatform>();
			if (ontologies != null && ontologies.length > 0) {
				for (final ClientPlatform k : clients) {
					for (int i = 0; i < ontologies.length; i++) {
						final Ontology o = ontologyRepository.findByIdentification(ontologies[i]);
						if (o != null) {
							final ClientPlatformOntology clpo = clientPlatformOntologyRepository
									.findByOntologyAndClientPlatform(o.getIdentification(), k.getIdentification());
							if (clpo != null) {
								if (!clientPlatformAdd.contains(k)) {
									clientPlatformAdd.add(k);
								}
							}
						}

					}

				}
				return clientPlatformAdd;
			}
			return clients;
		}
	}

	@Override
	public List<AccessType> getClientPlatformOntologyAccessLevel() {
		final List<AccessType> list = new ArrayList<AccessType>();
		list.add(ClientPlatformOntology.AccessType.ALL);
		list.add(ClientPlatformOntology.AccessType.INSERT);
		list.add(ClientPlatformOntology.AccessType.QUERY);
		return list;
	}

	@Override
	@Transactional
	public ClientPlatform createClientPlatform(DeviceCreateDTO device, String userId, Boolean isUpdate)
			throws JSONException {

		if (clientPlatformRepository.findByIdentification(device.getIdentification()) != null) {
			throw new ClientPlatformServiceException(
					"Device with identification:" + device.getIdentification() + " exists");
		}

		final ClientPlatform ndevice = new ClientPlatform();
		ndevice.setIdentification(device.getIdentification());
		ndevice.setMetadata(device.getMetadata());
		ndevice.setDescription(device.getDescription());

		final JSONArray ontologies = new JSONArray(device.getClientPlatformOntologies());
		final Set<ClientPlatformOntology> clientsPlatformOntologies = new HashSet<ClientPlatformOntology>();
		for (int i = 0; i < ontologies.length(); i++) {
			final JSONObject ontology = ontologies.getJSONObject(i);
			final ClientPlatformOntology clientPlatformOntology = new ClientPlatformOntology();
			clientPlatformOntology.setAccess(ontology.getString(ACCESS_STR));
			clientPlatformOntology.setOntology(ontologyRepository.findByIdentification(ontology.getString("id")));
			clientsPlatformOntologies.add(clientPlatformOntology);
		}

		ndevice.setUser(userService.getUser(userId));

		final String encryptionKey = UUID.randomUUID().toString();
		ndevice.setEncryptionKey(encryptionKey);

		final ClientPlatform cli = clientPlatformRepository.save(ndevice);

		final JSONArray tokensArray = new JSONArray(device.getTokens());
		for (int i = 0; i < tokensArray.length(); i++) {
			final JSONObject token = tokensArray.getJSONObject(i);
			final Token tokn = new Token();
			tokn.setClientPlatform(cli);
			tokn.setToken(token.getString("token"));
			tokn.setActive(token.getBoolean("active"));
			tokenRepository.save(tokn);

		}

		for (final ClientPlatformOntology cpoNew : clientsPlatformOntologies) {
			cpoNew.setClientPlatform(cli);
			clientPlatformOntologyRepository.save(cpoNew);
		}

		return ndevice;

	}

	@Override
	@Transactional
	public void updateDevice(DeviceCreateDTO device, String userId) throws JSONException {
		final ClientPlatform ndevice = clientPlatformRepository.findByIdentification(device.getIdentification());
		ndevice.setMetadata(device.getMetadata());
		ndevice.setDescription(device.getDescription());
		ndevice.setUser(userService.getUser(userId));
		final JSONArray ontologies = new JSONArray(device.getClientPlatformOntologies());
		final Set<ClientPlatformOntology> clientsPlatformOntologies = new HashSet<ClientPlatformOntology>();
		for (final Iterator iterator = ndevice.getClientPlatformOntologies().iterator(); iterator.hasNext();) {
			final ClientPlatformOntology clientPlatformOntology = (ClientPlatformOntology) iterator.next();
			boolean find = false;
			for (int i = 0; i < ontologies.length(); i++) {
				final JSONObject ontology = ontologies.getJSONObject(i);
				if (ontology.getString("id").equals(clientPlatformOntology.getOntology().getIdentification())) {
					clientPlatformOntology.setAccess(ontology.getString(ACCESS_STR));
					find = true;
					break;
				}
			}
			if (!find) {
				clientPlatformOntologyRepository.delete(clientPlatformOntology);
				iterator.remove();
			}
		}
		for (int i = 0; i < ontologies.length(); i++) {
			final JSONObject ontology = ontologies.getJSONObject(i);
			boolean find = false;
			for (final Iterator iterator = ndevice.getClientPlatformOntologies().iterator(); iterator.hasNext();) {
				final ClientPlatformOntology clientPlatformOntology = (ClientPlatformOntology) iterator.next();
				if (ontology.getString("id").equals(clientPlatformOntology.getOntology().getIdentification())) {
					find = true;
					break;
				}
			}
			if (!find) {
				final ClientPlatformOntology clientPlatformOntology = new ClientPlatformOntology();
				clientPlatformOntology.setAccess(ontology.getString(ACCESS_STR));
				clientPlatformOntology.setOntology(ontologyRepository.findByIdentification(ontology.getString("id")));
				clientPlatformOntology.setClientPlatform(ndevice);
				ndevice.getClientPlatformOntologies().add(clientPlatformOntology);
			}

		}

		final Set<Token> tokens = new HashSet<Token>();
		final JSONArray tokensArray = new JSONArray(device.getTokens());
		for (int i = 0; i < tokensArray.length(); i++) {
			final JSONObject token = tokensArray.getJSONObject(i);
			final Token tokn = new Token();
			tokn.setClientPlatform(ndevice);
			tokn.setToken(token.getString("token"));
			tokn.setActive(token.getBoolean("active"));
			tokens.add(tokn);
		}
		ndevice.setTokens(tokens);

		final ClientPlatform cli = clientPlatformRepository.save(ndevice);

	}

	@Override
	public void createOntologyRelation(Ontology ontology, ClientPlatform clientPlatform) {

		final ClientPlatformOntology relation = new ClientPlatformOntology();
		relation.setClientPlatform(clientPlatform);
		relation.setAccess(AccessType.ALL.name());
		relation.setOntology(ontology);
		// If relation does not exist then create
		if (clientPlatformOntologyRepository.findByOntologyAndClientPlatform(ontology.getIdentification(),
				clientPlatform.getIdentification()) == null) {
			clientPlatformOntologyRepository.save(relation);
		}

	}

	@Override
	public Ontology createDeviceLogOntology(String clientIdentification) {
		final ClientPlatform client = clientPlatformRepository.findByIdentification(clientIdentification);
		final Ontology logOntology = new Ontology();
		logOntology.setDataModel(dataModelService.getDataModelByName(LOG_DEVICE_DATA_MODEL));
		logOntology.setIdentification(LOG_ONTOLOGY_PREFIX + clientIdentification.replaceAll(" ", ""));
		logOntology.setActive(true);
		logOntology.setUser(client.getUser());
		logOntology.setDescription("System Ontology. Centralized Log for devices of type " + clientIdentification);
		logOntology.setJsonSchema(dataModelService.getDataModelByName(LOG_DEVICE_DATA_MODEL).getJsonSchema());
		logOntology.setPublic(false);
		logOntology.setRtdbClean(true);
		logOntology.setRtdbDatasource(RtdbDatasource.MONGO);
		logOntology.setRtdbCleanLapse(RtdbCleanLapse.SIX_MONTHS);
		return logOntology;

	}

	@Override
	public Ontology getDeviceLogOntology(ClientPlatform client) {
		return ontologyRepository
				.findByIdentification((LOG_ONTOLOGY_PREFIX + client.getIdentification()).replaceAll(" ", ""));

	}

	@Override
	public List<Token> getTokensByClientPlatformId(String clientPlatformId) {
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(clientPlatformId);
		return tokenService.getTokens(clientPlatform);
	}

	@Override
	public List<Ontology> getOntologiesByClientPlatform(String clientPlatformIdentification) {
		final ClientPlatform client = clientPlatformRepository.findByIdentification(clientPlatformIdentification);
		if (client != null) {
			final List<Ontology> ontologies = clientPlatformOntologyRepository.findByClientPlatform(client).stream()
					.map(ClientPlatformOntology::getOntology).collect(Collectors.toList());
			return ontologies;
		}
		return new ArrayList<>();
	}

	@Override
	public ClientPlatform getById(String id) {
		return clientPlatformRepository.findOne(id);
	}

	@Override
	public boolean hasUserManageAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(id);
		if (user.equals(clientPlatform.getUser())
				|| user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}

	}

	@Override
	public boolean hasUserViewAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final ClientPlatform clientPlatform = clientPlatformRepository.findById(id);
		if (user.equals(clientPlatform.getUser())
				|| user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			return true;
		else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}

	}

}
