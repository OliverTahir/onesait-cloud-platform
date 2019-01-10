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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.virtual.QueryAsTextVirtualDBImpl;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.interfaces.QueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.services.QueryAsTextMongoDBImpl;

@Component
public class QueryAsTextDBRepositoryFactory {

	@Autowired
	private QueryAsTextMongoDBImpl queryMongo;

	@Autowired
	private ElasticSearchQueryAsTextDBRepository queryElasticSearch;

	@Autowired
	@Qualifier(NameBeanConst.KUDU_QUERY_REPO_BEAN_NAME)
	private QueryAsTextDBRepository kuduQueryAsTextDBRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private QueryAsTextVirtualDBImpl queryVirtual;

	public QueryAsTextDBRepository getInstance(String ontologyId, String sessionUserId) throws DBPersistenceException {
		Ontology ds = ontologyService.getOntologyByIdentification(ontologyId, sessionUserId);
		RtdbDatasource dataSource = ds.getRtdbDatasource();
		return getInstance(dataSource);
	}

	public QueryAsTextDBRepository getInstanceClientPlatform(String ontologyId, String clientP)
			throws DBPersistenceException {
		ClientPlatform cp = clientPlatformService.getByIdentification(clientP);

		List<Ontology> ds = ontologyService.getOntologiesByClientPlatform(cp);

		Ontology result1 = ds.stream().filter(x -> ontologyId.equals(x.getIdentification())).findAny().orElse(null);

		if (result1 != null) {
			RtdbDatasource dataSource = result1.getRtdbDatasource();
			return getInstance(dataSource);
		} else
			return queryMongo;
	}

	public QueryAsTextDBRepository getInstance(RtdbDatasource dataSource) {
		if (dataSource.equals(RtdbDatasource.MONGO))
			return queryMongo;
		else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH))
			return queryElasticSearch;
		else if (dataSource.equals(RtdbDatasource.KUDU))
			return kuduQueryAsTextDBRepository;
		else if (dataSource.equals(RtdbDatasource.VIRTUAL))
			return queryVirtual;
		else
			return queryMongo;
	}
}
