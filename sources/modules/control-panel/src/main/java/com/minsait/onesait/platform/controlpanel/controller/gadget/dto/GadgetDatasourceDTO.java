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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/

package com.minsait.onesait.platform.controlpanel.controller.gadget.dto;

import lombok.Getter;
import lombok.Setter;

public class GadgetDatasourceDTO {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String mode;

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private String dbtype;

	/*
	 * @Getter
	 * 
	 * @Setter private User user;
	 */

	@Getter
	@Setter
	private OntologyDTO ontology;

	@Getter
	@Setter
	private Integer refresh;

	@Getter
	@Setter
	private Integer maxvalues;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String config;
}