package com.minsait.onesait.platform.config.repository;

import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
@Ignore("Pendiente resolver")
public class OntologyVirtualDatasourceIntegrationTest {

	@Autowired
	private OntologyVirtualDatasourceRepository repository;

	@Test
	@Transactional
	public void addDatasource() {
		OntologyVirtualDatasource data = new OntologyVirtualDatasource();
		data.setCreatedAt(new Date());
		data.setUpdatedAt(new Date());
		data.setUser("sys as sysdba");
		data.setCredentials("indra2013");
		data.setPoolSize("10");
		data.setDatasourceName("oracle");
		data.setSgdb(VirtualDatasourceType.ORACLE);
		data.setQueryLimit(100);
		data.setConnectionString("jdbc:oracle:thin:@10.0.0.6:1521:XE");

		this.repository.saveAndFlush(data);

	}

}
