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
package com.minsait.onesait.platform.config.services.app.dto;

import java.util.List;

import com.minsait.onesait.platform.config.model.AppRole;

import lombok.Getter;
import lombok.Setter;

public class AppCreateDTO {

	@Getter
	@Setter
	private String appId;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String description;
	@Getter
	@Setter
	private String roles;	
	@Getter
	@Setter
	private String users;
	@Getter
	@Setter
	private String associations;
	@Getter
	@Setter
	private Integer tokenValiditySeconds;
	@Getter
	@Setter
	private String secret;
}
