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
package com.minsait.onesait.platform.controlpanel.services.hadoop;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.persistence.common.DescribeColumnData;

import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HadoopService {

	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;

	/*@Autowired
	private JsonGeneratorFromHive jsonGenerator;*/

	public List<String> getHiveTables() {
		return manageDBPersistenceServiceFacade.getListOfTables(RtdbDatasource.KUDU);
	}

	public List<DescribeColumnData> describe(String name) {
		List<DescribeColumnData> columns = manageDBPersistenceServiceFacade.describeTable(RtdbDatasource.KUDU, name);
		return columns;
	}

	public String generateSchemaFromHive(String tablename) {
		throw new NotImplementedException();
		/*List<DescribeColumnData> columns = describe(tablename);
		JsonSchemaHive schema = jsonGenerator.parse(tablename, columns);
		return schema.build();*/
	}
}
