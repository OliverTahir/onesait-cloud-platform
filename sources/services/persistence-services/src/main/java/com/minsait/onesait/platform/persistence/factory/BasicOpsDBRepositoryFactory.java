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
package com.minsait.onesait.platform.persistence.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.api.rest.ExternalApiRestOpsDBRepository;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;

@Component
public class BasicOpsDBRepositoryFactory {

	@Autowired
	private ElasticSearchBasicOpsDBRepository elasticBasicOps;

	@Autowired
	private MongoBasicOpsDBRepository mongoBasicOps;

	@Autowired
	private ExternalApiRestOpsDBRepository externalApiRest;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private VirtualOntologyDBRepository virtualRepository;

	public BasicOpsDBRepository getInstance(String ontologyId) throws DBPersistenceException {
		Ontology ds = ontologyRepository.findByIdentification(ontologyId);
		RtdbDatasource dataSource = ds.getRtdbDatasource();
		return getInstance(dataSource);
	}

	public BasicOpsDBRepository getInstance(RtdbDatasource dataSource) throws DBPersistenceException {
		if (RtdbDatasource.MONGO.equals(dataSource)) {
			return mongoBasicOps;
		} else if (RtdbDatasource.ELASTIC_SEARCH.equals(dataSource)) {
			return elasticBasicOps;
		} else if (RtdbDatasource.API_REST.equals(dataSource)) {
			return externalApiRest;
		} else if (RtdbDatasource.VIRTUAL.equals(dataSource)) {
			return virtualRepository;
		} else {
			return mongoBasicOps;
		}
	}

}
