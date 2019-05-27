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
package com.minsait.onesait.platform.controlpanel.config;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private static final String INFO_VERSION = "";
	private static final String INFO_TITLE = "onesait Platform";
	private static final String INFO_DESCRIPTION = "onesait Platform Control Panel Management";

	private static final String LICENSE_NAME = "Apache2 License";
	private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";

	private static final String CONTACT_NAME = "onesait Platform Team";
	private static final String CONTACT_URL = "https://www.sofia4cities.com";
	private static final String CONTACT_EMAIL = "select4citiesminsait@gmail.com";

	private static final String HEADER_STR = "header";
	private static final String STRING_STR = "string";
	private static final String AUTH_STR = "Authorization";
	private static final String APP_JSON = "application/json";
	private static final String TEXT_PL = "text/plain";
	private static final String APP_YAML = "application/yaml";

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(INFO_TITLE).description(INFO_DESCRIPTION).termsOfServiceUrl(CONTACT_URL)
				.contact(new Contact(CONTACT_NAME, CONTACT_URL, CONTACT_EMAIL)).license(INFO_VERSION)
				.licenseUrl(LICENSE_URL).version(LICENSE_NAME).build();
	}

	List<Parameter> addRestParameters(ParameterBuilder aParameterBuilder, List<Parameter> aParameters) {
		return aParameters;
	}

	@Bean
	public Docket ManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("management").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorManagement() {
		return or(regex("/management.*"));
	}

	@Bean
	public Docket ApiOpsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		return new Docket(DocumentationType.SWAGGER_2).groupName("api-ops").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorApiOps()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorApiOps() {
		return or(regex("/api-ops.*"));
	}

	@Bean
	public Docket LoginOpsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		return new Docket(DocumentationType.SWAGGER_2).groupName("login").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorApiOpsLogin()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorApiOpsLogin() {
		return or(regex("/api-ops/login.*"));
	}

	@Bean
	public Docket NotebookOpsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("notebook-ops").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorNotebookOps()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorNotebookOps() {
		return or(regex("/notebook-ops.*"));
	}

	@Bean
	public Docket UserManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("users").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorUserManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorUserManagement() {
		return or(regex("/management/users.*"));
	}

	@Bean
	public Docket RealmManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("realms").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorRealmManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorRealmManagement() {
		return or(regex("/management/realms.*"));
	}

	@Bean
	public Docket DeploymentAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("deployment").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDeploymentManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDeploymentManagement() {
		return or(regex("/deployment.*"));
	}

	@Bean
	public Docket DashboardsAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<String>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("dashboard").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorDashoardsManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDashoardsManagement() {
		return or(regex("/dashboard.*"));
	}

	@Bean
	public Docket LayerssAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name("Authorization").modelRef(new ModelRef("string")).parameterType("header").required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<String>(
				Arrays.asList("application/json", "application/yaml", "text/plain"));
		return new Docket(DocumentationType.SWAGGER_2).groupName("layer").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorLayersManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorLayersManagement() {
		return or(regex("/layer.*"));
	}

	@Bean
	public Docket ModelssAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<String>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("model").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorModelsManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorModelsManagement() {
		return or(regex("/model.*"));
	}

	@Bean
	public Docket ConfigurationAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		final Set<String> produces = new HashSet<String>(Arrays.asList(APP_JSON, APP_YAML, TEXT_PL));
		return new Docket(DocumentationType.SWAGGER_2).groupName("configurations").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorConfigurationManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters)).produces(produces);
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorConfigurationManagement() {
		return or(regex("/management/configurations.*"));
	}

	@Bean
	public Docket DeviceAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).groupName("devices").select().apis(RequestHandlerSelectors.any())
				.paths(buildPathSelectorDeviceManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorDeviceManagement() {
		return or(regex("/management/device.*"));
	}

	@Bean
	public Docket VideobrokerAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).groupName("videobroker").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorVideobrokerManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorVideobrokerManagement() {
		return or(regex("/management/videobroker.*"));
	}

	@Bean
	public Docket OntologyManagementAPI() {

		// Adding Header
		final ParameterBuilder aParameterBuilder = new ParameterBuilder();
		final List<Parameter> aParameters = new ArrayList<Parameter>();

		aParameterBuilder.name(AUTH_STR).modelRef(new ModelRef(STRING_STR)).parameterType(HEADER_STR).required(true)
				.build();
		aParameters.add(aParameterBuilder.build());

		return new Docket(DocumentationType.SWAGGER_2).groupName("ontologies").select()
				.apis(RequestHandlerSelectors.any()).paths(buildPathSelectorOntologyManagement()).build()
				.globalOperationParameters(addRestParameters(aParameterBuilder, aParameters));
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> buildPathSelectorOntologyManagement() {
		return or(regex("/management/ontologies.*"));
	}

}