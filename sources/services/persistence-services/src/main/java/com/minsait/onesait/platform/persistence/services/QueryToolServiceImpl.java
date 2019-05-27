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
package com.minsait.onesait.platform.persistence.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.templates.PlatformQuery;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.QueryAsTextDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.services.util.QueryParsers;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QueryToolServiceImpl implements QueryToolService {

	@Autowired
	private OntologyService ontologyService;
	
	@Autowired
	private QueryTemplateService queryTemplateService;

	@Autowired
	private QueryAsTextDBRepositoryFactory queryAsTextDBRepositoryFactory;

	private void hasUserPermission(String user, String ontology, String query) throws DBPersistenceException {
		if (!ontologyService.hasUserPermissionForQuery(user, ontology)) {
			throw new DBPersistenceException("User:" + user + " has nos permission to query ontology " + ontology);
		}
		if (query.toLowerCase().indexOf("update") != -1 || query.toLowerCase().indexOf("update") != -1
				|| query.toLowerCase().indexOf("remove") != -1 || query.toLowerCase().indexOf("delete") != -1
				|| query.toLowerCase().indexOf("createindex") != -1

		) {
			if (!ontologyService.hasUserPermissionForInsert(user, ontology)) {
				throw new DBPersistenceException(
						"User:" + user + " has nos permission to update,insert or remove on ontology " + ontology);
			}
		}
	}

	private void hasClientPlatformPermisionForQuery(String clientPlatform, String ontology)
			throws DBPersistenceException {
		if (!ontologyService.hasClientPlatformPermisionForQuery(clientPlatform, ontology)) {
			throw new DBPersistenceException(
					"Client Platform:" + clientPlatform + " has nos permission to query ontology " + ontology);
		}
	}

	@Override
	public String queryNativeAsJson(String user, String ontology, String query, int offset, int limit)
			throws DBPersistenceException {
		try {
			hasUserPermission(user, ontology, query);

			return queryAsTextDBRepositoryFactory.getInstance(ontology, user).queryNativeAsJson(ontology, query, offset,
					limit);
		} catch (final Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String queryNativeAsJson(String user, String ontology, String query) throws DBPersistenceException {
		try {
			hasUserPermission(user, ontology, query);
			return queryAsTextDBRepositoryFactory.getInstance(ontology, user).queryNativeAsJson(ontology, query);
		} catch (final Exception e) {
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}
	
	private String querySQLAsJson(String user, String ontology, String query, int offset, boolean checkTemplates) throws DBPersistenceException {
	    try {
    	    hasUserPermission(user, ontology, query);
    	    
    	    if (checkTemplates) {
    	        PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
    	        if (newQuery != null) {
    	            
    	            switch (newQuery.getType()) {
                    case SQL:
                        return querySQLAsJson(user, ontology, newQuery.getQuery(), offset, false);
                    case NATIVE:
                        return queryNativeAsJson(user, ontology, newQuery.getQuery());
                    default:
                        throw new IllegalStateException("Only SQL or NATIVE queries are supported");
                    }
                } 
    	    } 
    	    
	        query = QueryParsers.parseFunctionNow(query);
            return queryAsTextDBRepositoryFactory.getInstance(ontology, user).querySQLAsJson(ontology, query, offset);    	   
  
    	} catch (final Exception e) {
                log.error("Error querySQLAsJson:" + e.getMessage());
                throw new DBPersistenceException(e);
    	}
	}

	@Override
	public String querySQLAsJson(String user, String ontology, String query, int offset) throws DBPersistenceException {
		return querySQLAsJson(user, ontology, query, offset, true);
	}

	@Override
	public String queryNativeAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset,
			int limit) throws DBPersistenceException {

		try {
			hasClientPlatformPermisionForQuery(clientPlatform, ontology);
			return queryAsTextDBRepositoryFactory.getInstanceClientPlatform(ontology, clientPlatform)
					.queryNativeAsJson(ontology, query, offset, limit);
		} catch (final Exception e) {
			log.error("Error queryNativeAsJsonForPlatformClient:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}
	
	
	private String querySQLAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset, boolean checkTemplates) {
	    try {
            hasClientPlatformPermisionForQuery(clientPlatform, ontology);
            if (checkTemplates) {
                PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
                if (newQuery != null) {
                    
                    switch (newQuery.getType()) {
                    case SQL:
                        return querySQLAsJsonForPlatformClient(clientPlatform, ontology, newQuery.getQuery(), offset, false);
                    case NATIVE:
                        return queryNativeAsJsonForPlatformClient(clientPlatform, ontology, newQuery.getQuery(), offset, 0);
                    default:
                        throw new IllegalStateException("Only SQL or NATIVE queries are supported");
                    }
                } 
            } 
            
            query = QueryParsers.parseFunctionNow(query);
            return queryAsTextDBRepositoryFactory.getInstanceClientPlatform(ontology, clientPlatform)
                    .querySQLAsJson(ontology, query, offset);
        } catch (final Exception e) {
            log.error("Error querySQLAsJsonForPlatformClient:" + e.getMessage());
            throw new DBPersistenceException(e);
        }
	}

	@Override
	public String querySQLAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset)
			throws DBPersistenceException {
		return querySQLAsJsonForPlatformClient(clientPlatform, ontology, query, offset, true);
	}

}
