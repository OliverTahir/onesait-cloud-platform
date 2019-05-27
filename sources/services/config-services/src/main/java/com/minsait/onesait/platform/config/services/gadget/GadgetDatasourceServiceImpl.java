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
package com.minsait.onesait.platform.config.services.gadget;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetDatasourceServiceImpl implements GadgetDatasourceService {

	private static final String ELASTIC_DATASOURCE_TYPE = "ElasticSearch";

	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private ObjectMapper mapper;
	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Override
	public List<GadgetDatasource> findAllDatasources() {
		final List<GadgetDatasource> datasources = gadgetDatasourceRepository.findAll();

		return datasources;
	}

	@Override
	public List<GadgetDatasource> findGadgetDatasourceWithIdentificationAndDescription(String identification,
			String description, String userId) {
		List<GadgetDatasource> datasources;
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(GadgetServiceImpl.ADMINISTRATOR)) {
			if (description != null && identification != null) {

				datasources = gadgetDatasourceRepository
						.findByIdentificationContainingAndDescriptionContaining(identification, description);

			} else if (description == null && identification != null) {

				datasources = gadgetDatasourceRepository.findByIdentificationContaining(identification);

			} else if (description != null && identification == null) {

				datasources = gadgetDatasourceRepository.findByDescriptionContaining(description);

			} else {

				datasources = gadgetDatasourceRepository.findAll();
			}
		} else {
			if (description != null && identification != null) {

				datasources = gadgetDatasourceRepository.findByUserAndIdentificationContainingAndDescriptionContaining(
						user, identification, description);

			} else if (description == null && identification != null) {

				datasources = gadgetDatasourceRepository.findByUserAndIdentificationContaining(user, identification);

			} else if (description != null && identification == null) {

				datasources = gadgetDatasourceRepository.findByUserAndDescriptionContaining(user, description);

			} else {

				datasources = gadgetDatasourceRepository.findByUser(user);
			}
		}
		return datasources;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<GadgetDatasource> datasources = gadgetDatasourceRepository.findAllByOrderByIdentificationAsc();
		final List<String> names = new ArrayList<String>();
		for (final GadgetDatasource datasource : datasources) {
			names.add(datasource.getIdentification());

		}
		return names;
	}

	@Override
	public GadgetDatasource getGadgetDatasourceById(String id) {
		return gadgetDatasourceRepository.findById(id);
	}

	@Override
	public GadgetDatasource createGadgetDatasource(GadgetDatasource gadgetDatasource) {
		if (!gadgetDatasourceExists(gadgetDatasource)) {
			log.debug("Gadget datasource no exist, creating...");
			return gadgetDatasourceRepository.save(gadgetDatasource);
		} else {
			throw new GadgetDatasourceServiceException("Gadget Datasource already exists in Database");
		}
	}

	@Override
	public boolean gadgetDatasourceExists(GadgetDatasource gadgetDatasource) {
		if (gadgetDatasourceRepository.findByIdentification(gadgetDatasource.getIdentification()) != null)
			return true;
		else
			return false;
	}

	@Override
	public void updateGadgetDatasource(GadgetDatasource gadgetDatasource) {
		if (gadgetDatasourceExists(gadgetDatasource)) {
			final GadgetDatasource gadgetDatasourceDB = gadgetDatasourceRepository.findById(gadgetDatasource.getId());
			gadgetDatasourceDB.setConfig(gadgetDatasource.getConfig());
			gadgetDatasourceDB.setDbtype(gadgetDatasource.getDbtype());
			gadgetDatasourceDB.setDescription(gadgetDatasource.getDescription());
			gadgetDatasourceDB.setMaxvalues(gadgetDatasource.getMaxvalues());
			gadgetDatasourceDB.setMode(gadgetDatasource.getMode());
			gadgetDatasourceDB.setOntology(gadgetDatasource.getOntology());
			gadgetDatasourceDB.setQuery(gadgetDatasource.getQuery());
			gadgetDatasourceDB.setRefresh(gadgetDatasource.getRefresh());
			gadgetDatasourceRepository.save(gadgetDatasourceDB);
		} else
			throw new GadgetDatasourceServiceException("Cannot update GadgetDatasource that does not exist");
	}

	@Override
	public void deleteGadgetDatasource(String gadgetDatasourceId, String userId) {
		if (hasUserPermission(gadgetDatasourceId, userId)) {
			final GadgetDatasource gadgetDatasource = gadgetDatasourceRepository.findById(gadgetDatasourceId);
			if (gadgetDatasource != null) {
				gadgetDatasourceRepository.delete(gadgetDatasource);
			} else
				throw new GadgetDatasourceServiceException("Cannot delete gadget datasource that does not exist");
		}

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			return true;
		} else {
			return gadgetDatasourceRepository.findById(id).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public List<GadgetDatasource> getUserGadgetDatasources(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			return gadgetDatasourceRepository.findAll();
		} else {
			return gadgetDatasourceRepository.findByUser(user);
		}
	}

	@Override
	public String getSampleQueryGadgetDatasourceById(String datasourceId, String ontology, String user) {
		final Ontology ont = ontologyService.getOntologyByIdentification(ontology, user);
		final String query = gadgetDatasourceRepository.findById(datasourceId).getQuery();

		final int i = query.toLowerCase().lastIndexOf("limit ");
		if (i == -1) {// Add limit add the end
			return query + " limit 1";
		} else {
			return query.substring(0, i) + " limit 1";
		}
	}

	@Override
	public GadgetDatasource getDatasourceByIdentification(String dsIdentification) {
		return gadgetDatasourceRepository.findByIdentification(dsIdentification);
	}

}
