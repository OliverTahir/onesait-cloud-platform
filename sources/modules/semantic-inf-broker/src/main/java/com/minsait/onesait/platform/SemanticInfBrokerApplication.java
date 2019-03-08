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
package com.minsait.onesait.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

// @SpringBootApplication(scanBasePackages =
// "com.minsait.onesait.platform.router.config")
// @EnableAutoConfiuration(exclude = { CamelAutoConfiguration.class })

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.minsait.onesait.platform.config.repository")
@EnableMongoRepositories(basePackages = "com.minsait.onesait.platform.persistence.mongodb")
@ComponentScan(basePackages = { "com.minsait.onesait.platform.router" })
@EnableAsync
public class SemanticInfBrokerApplication {

	@Configuration
	@Profile("default")
	@ComponentScan(basePackages = { "com.minsait.onesait.platform.router" }, lazyInit = true)
	static class LocalConfig {
	}

	public static void main(String[] args) {
		SpringApplication.run(SemanticInfBrokerApplication.class, args);
	}

}