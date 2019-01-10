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
package com.minsait.onesait.platform.config.services.deletion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Device;
import com.minsait.onesait.platform.config.model.DeviceSimulation;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientConnectionRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DeviceRepository;
import com.minsait.onesait.platform.config.repository.DeviceSimulationRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.TwitterListeningRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class EntityDeletionServiceImpl implements EntityDeletionService {

	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private TwitterListeningRepository twitterListeningRepository;
	@Autowired
	private DeviceSimulationRepository deviceSimulationRepository;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ClientConnectionRepository clientConnectionRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private OntologyRestRepository ontologyRestRepository;
	@Autowired
	private OntologyRestSecurityRepository ontologyRestSecurityRepository;
	@Autowired
	private OntologyRestHeadersRepository ontologyRestHeaderRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private OPResourceRepository resourceRepository;

	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private GadgetRepository gadgetRepository;

	@Override
	@Transactional
	public void deleteOntology(String id, String userId) {

		try {
			final User user = userService.getUser(userId);
			final Ontology ontology = ontologyService.getOntologyById(id, userId);
			if (ontologyService.hasUserPermisionForChangeOntology(user, ontology)) {
				if (clientPlatformOntologyRepository.findByOntology(ontology) != null) {
					clientPlatformOntologyRepository.findByOntology(ontology).forEach(cpo -> {
						final ClientPlatform client = cpo.getClientPlatform();
						client.getClientPlatformOntologies().removeIf(r -> r.getOntology().equals(ontology));
						clientPlatformOntologyRepository.deleteById(cpo.getId());
					});

				}

				if (!apiRepository.findByOntology(ontology).isEmpty()) {
					apiRepository.findByOntology(ontology).forEach(a -> apiRepository.delete(a));
				}
				if (ontologyUserAccessRepository.findByOntology(ontology) != null) {
					ontologyUserAccessRepository.deleteByOntology(ontology);
				}
				if (twitterListeningRepository.findByOntology(ontology) != null) {
					twitterListeningRepository.deleteByOntology(ontology);
				}
				if (ontologyUserAccessRepository.findByOntology(ontology) != null) {
					ontologyUserAccessRepository.deleteByOntology(ontology);
				}
				if (!twitterListeningRepository.findByOntology(ontology).isEmpty()) {
					twitterListeningRepository.deleteByOntology(ontology);
				}
				if (!deviceSimulationRepository.findByOntology(ontology).isEmpty()) {
					deviceSimulationRepository.deleteByOntology(ontology);
				}

				if (ontologyRestRepository.findByOntologyId(ontology) != null) {
					ontologyRestHeaderRepository
							.delete(ontologyRestRepository.findByOntologyId(ontology).getHeaderId());
				}
				if (ontologyRestRepository.findByOntologyId(ontology) != null) {
					ontologyRestSecurityRepository
							.delete(ontologyRestRepository.findByOntologyId(ontology).getSecurityId());
				}

				ontologyRepository.deleteById(id);

			} else {
				throw new OntologyServiceException("Couldn't delete ontology");
			}
		} catch (final Exception e) {
			throw new OntologyServiceException("Couldn't delete ontology", e);
		}

	}

	@Override
	@Transactional
	public void deleteTwitterListening(TwitterListening twitterListening) {
		twitterListeningRepository.deleteById(twitterListening.getId());
	}

	@Override
	@Transactional
	public void deleteClient(String id) {
		try {

			final ClientPlatform client = clientPlatformRepository.findById(id);
			final List<ClientPlatformOntology> cpf = clientPlatformOntologyRepository.findByClientPlatform(client);
			if (cpf != null && cpf.size() > 0) {
				for (final Iterator iterator = cpf.iterator(); iterator.hasNext();) {
					final ClientPlatformOntology clientPlatformOntology = (ClientPlatformOntology) iterator.next();
					clientPlatformOntologyRepository.delete(clientPlatformOntology);
				}

			}
			final List<ClientConnection> cc = clientConnectionRepository.findByClientPlatform(client);
			if (cc != null && cc.size() > 0) {
				for (final Iterator<ClientConnection> iterator = cc.iterator(); iterator.hasNext();) {
					final ClientConnection clientConnection = iterator.next();
					clientConnectionRepository.delete(clientConnection);
				}
			}

			final List<Device> ld = deviceRepository.findByClientPlatform(client);
			if (ld != null && ld.size() > 0) {
				for (final Iterator<Device> iterator = ld.iterator(); iterator.hasNext();) {
					final Device device = iterator.next();
					deviceRepository.delete(device);
				}
			}
			final List<DeviceSimulation> lds = deviceSimulationRepository.findByClientPlatform(client);
			if (lds != null && lds.size() > 0) {
				for (final Iterator<DeviceSimulation> iterator = lds.iterator(); iterator.hasNext();) {
					final DeviceSimulation deviceSim = iterator.next();
					deviceSimulationRepository.delete(deviceSim);
				}
			}
			final Ontology ontoLog = ontologyRepository.findByIdentification("LOG_" + client.getIdentification());
			if (ontoLog != null) {
				deleteOntology(ontoLog.getId(), client.getUser().getUserId());
			}
			clientPlatformRepository.delete(client);

		} catch (final Exception e) {
			throw new OntologyServiceException("Couldn't delete ClientPlatform");
		}
	}

	@Override
	public void deleteToken(String id) {
		try {
			final Token token = tokenRepository.findById(id);
			tokenRepository.delete(token);
		} catch (final Exception e) {
			throw new OntologyServiceException("Couldn't delete Token");
		}

	}

	@Override
	@Transactional
	public void deleteDeviceSimulation(DeviceSimulation simulation) throws Exception {
		if (!simulation.isActive())
			deviceSimulationRepository.deleteById(simulation.getId());
		else
			throw new Exception("Simulation is currently running");

	}

	@Override
	@Transactional
	public void revokeAuthorizations(Ontology ontology) {
		try {
			ontologyUserAccessRepository.deleteByOntology(ontology);
		} catch (final Exception e) {
			throw new OntologyServiceException("Couldn't delete ontology's authorizations");
		}

	}

	@Override
	@Transactional
	public void deleteGadgetDataSource(String id, String userId) {
		try {
			if (gadgetDatasourceService.hasUserPermission(id, userId)) {
				// find measures with this datasource
				final List<GadgetMeasure> list = gadgetMeasureRepository.findByDatasource(id);
				if (list.size() > 0) {
					final HashSet<String> map = new HashSet<>();
					for (final GadgetMeasure gm : list) {
						map.add(gm.getGadget().getId());
					}
					for (final String gadgetId : map) {
						// Delete gadget for id
						gadgetRepository.delete(gadgetId);
					}
				}
				// delete datasource
				gadgetDatasourceRepository.delete(id);
			}
		} catch (final Exception e) {
			throw new OntologyServiceException("Couldn't delete gadgetDataSource");
		}

	}

	@Override
	public void deleteUser(String userId) {
		try {
			userRepository.deleteByUserId(userId);
		} catch (final Exception e) {
			try {
				final List<OPResource> resources = resourceRepository.findByUser(userRepository.findByUserId(userId));
				if (resources.size() == 1) {
					if (resources.get(0) instanceof Ontology
							&& resources.get(0).getIdentification().toLowerCase().contains("audit")) {
						resourceRepository.delete(resources.get(0).getId());
						userRepository.deleteByUserId(userId);
						return;
					}
				}
				throw new UserServiceException("Could not delete user, there are resources owned by " + userId);

			} catch (final Exception e2) {
				throw e2;
			}

		}

	}
}
