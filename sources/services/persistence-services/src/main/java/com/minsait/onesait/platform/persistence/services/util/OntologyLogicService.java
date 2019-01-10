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
package com.minsait.onesait.platform.persistence.services.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OntologyLogicService {

	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;

	public void createOntology(Ontology ontology)
			throws OntologyLogicServiceException, OntologyDataJsonProblemException {

		try {

			log.debug("create ontology in db " + ontology.getRtdbDatasource());
			manageDBPersistenceServiceFacade.createTable4Ontology(ontology.getIdentification(),
					ontology.getJsonSchema());

		} catch (final Exception e) {

			throw new OntologyServiceException("Problems creating the ontology." + e.getMessage(), e);
		}

		log.debug("ontology created");
	}

}
