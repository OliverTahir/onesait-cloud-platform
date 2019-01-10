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
package com.minsait.onesait.platform.api.init;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.api.rest.api.dto.ApiDTO;
import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplateImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoadHelsinkiSampleData implements ApplicationRunner {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;

	@Autowired
	MongoDbTemplateImpl connect;

	@Autowired
	MongoBasicOpsDBRepository repository;

	@Autowired
	MongoTemplate nativeTemplate;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	UserRepository userCDBRepository;

	static final String ONT_NAME = "HelsinkiPopulation";
	static final String APINAME = "HelsinkiPopulationAPI";
	static final String DATABASE = "sofia2_s4c";
	static User userCollaborator = null;
	static User userAdministrator = null;

	String refOid = "";

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void run(ApplicationArguments arg0) {
		try {
			createAPI();
		} catch (Exception e) {
		}

	}

	private User getUserDeveloper() {
		if (userCollaborator == null)
			userCollaborator = this.userCDBRepository.findByUserId("developer");
		return userCollaborator;
	}


	public void createAPI() throws Exception {
		String token = "acbca01b-da32-469e-945d-05bb6cd1552e";
		try {
			Api theApi = apiService.findApi(APINAME, token);

			List<Ontology> ontologies = this.ontologyRepository.findByIdentificationIgnoreCase(ONT_NAME);
			if (!ontologies.isEmpty()) {
				theApi.setOntology(ontologies.get(0));
			}

		} catch (Exception e) {
			File in = new ClassPathResource("data/helsinki.json").getFile();

			ApiDTO api = mapper.readValue(in, ApiDTO.class);
			List<Ontology> ontologies = this.ontologyRepository.findByIdentificationIgnoreCase(ONT_NAME);
			if (!ontologies.isEmpty()) {
				api.setOntologyId(ontologies.get(0).getId());
				api.setIdentification(APINAME);

			}
			apiService.createApi(api, token);
			ApiDTO out = apiFIQL.toApiDTO(apiService.findApi(APINAME, token));

			log.info(out.toString());
		}

	}

	public void init_Ontology() {

		List<Ontology> ontologies = this.ontologyRepository.findByIdentificationIgnoreCase(ONT_NAME);
		if (ontologies.isEmpty()) {

			Ontology ontology = new Ontology();
			ontology.setJsonSchema("{}");
			ontology.setIdentification(ONT_NAME);
			ontology.setDescription(ONT_NAME);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontologyRepository.save(ontology);

		}

	}

}