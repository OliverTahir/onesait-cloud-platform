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
package com.minsait.onesait.platform.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.minsait.onesait.platform.config.repository.OntologyRepository;

@Configuration
@ComponentScan(value="com.minsait.onesait.platform", excludeFilters = { @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
		"com.minsait.onesait.platform.config.services.*" }) })
//@ComponentScan(value="com.minsait.onesait.platform")
@EnableJpaRepositories(
		entityManagerFactoryRef = "entityManagerFactory", 
		transactionManagerRef = "transactionManager",
		basePackageClasses = OntologyRepository.class)
@EnableTransactionManagement
public class ConfigDbEntitiesStarterAutoConfig {
	
	@Bean
	@Primary
	@ConfigurationProperties("spring.jpa")
	public JpaProperties jpaProperties() {
		return new JpaProperties();
	}
	
	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties onesaitplatformDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	public DataSource defaultDataSource() {
		return onesaitplatformDataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Autowired
    private JpaVendorAdapter jpaVendorAdapter;
	
	@Bean(name="entityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
	    LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
	    lef.setDataSource(defaultDataSource());
	    lef.setJpaVendorAdapter(jpaVendorAdapter);
	    lef.setJpaPropertyMap(jpaProperties().getHibernateProperties(defaultDataSource()));
	    lef.setPackagesToScan("com.minsait.onesait.platform.config");
	    lef.setPersistenceUnitName("onesaitPlatform");
	    return lef;
	}
	
	@Bean(name = "transactionManager")
	@Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory().getObject());
        return tm;
    }
}
