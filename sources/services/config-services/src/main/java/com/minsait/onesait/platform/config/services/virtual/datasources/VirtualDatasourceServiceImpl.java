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
package com.minsait.onesait.platform.config.services.virtual.datasources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.persistence.external.exception.SGDBNotSupportedException;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDataSourceDescriptor;
import com.minsait.onesait.platform.persistence.external.virtual.helper.VirtualOntologyHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VirtualDatasourceServiceImpl implements VirtualDatasourceService {

	@Autowired
	OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	@Qualifier("OracleVirtualOntologyHelper")
	private VirtualOntologyHelper oracleVirtualOntologyHelper;

	@Override
	public List<String> getAllIdentifications() {
		final List<OntologyVirtualDatasource> datasources = ontologyVirtualDatasourceRepository
				.findAllByOrderByDatasourceNameAsc();
		final List<String> identifications = new ArrayList<String>();
		for (final OntologyVirtualDatasource datasource : datasources) {
			identifications.add(datasource.getDatasourceName());

		}
		return identifications;
	}

	@Override
	public List<OntologyVirtualDatasource> getAllDatasources() {
		return ontologyVirtualDatasourceRepository.findAll();
	}

	@Override
	public void createDatasource(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.save(datasource);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceById(String id) {
		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository.findById(id);

		if (datasource != null) {
			return datasource;
		} else {
			return null;
		}

	}

	@Override
	public void updateOntology(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.save(datasource);
	}

	@Override
	public void deleteDatasource(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.delete(datasource);
	}

	@Override
	public Boolean checkConnection(String datasourceName, String user, String credentials, String sgdb, String url,
			String queryLimit) {
		VirtualDataSourceDescriptor datasource = new VirtualDataSourceDescriptor();

		String driverClassName;
		switch (VirtualDatasourceType.valueOf(sgdb)) {
		case ORACLE:
			driverClassName = oracle.jdbc.driver.OracleDriver.class.getName();
			datasource.setVirtualDatasourceType(VirtualDatasourceType.ORACLE);
			break;
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + sgdb);

		}

		DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
		driverManagerDatasource.setDriverClassName(driverClassName);
		driverManagerDatasource.setUrl(url);
		driverManagerDatasource.setUsername(user);
		driverManagerDatasource.setPassword(credentials);

		datasource.setQueryLimit(Integer.valueOf(queryLimit));
		datasource.setDriverManagerDataSource(driverManagerDatasource);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource.getDriverManagerDataSource());

		VirtualOntologyHelper helper = getOntologyHelper(datasource.getVirtualDatasourceType());

		try {
			jdbcTemplate.queryForList(helper.getAllTablesStatement(), String.class);
			return true;
		} catch (Exception e) {
			log.error("Error checking connection to datasource", e);
			return false;
		}
	}

	private VirtualOntologyHelper getOntologyHelper(VirtualDatasourceType type) {
		switch (type) {
		case ORACLE:
			return oracleVirtualOntologyHelper;

		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type);
		}

	}

}
