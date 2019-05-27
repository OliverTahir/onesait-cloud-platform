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
package com.minsait.onesait.platform.persistence.external.api.rest.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface APIRestClient {

	public APIRestResponse invokeGet(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams);

	public APIRestResponse invokePut(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams);

	public APIRestResponse invokePost(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams);

	public APIRestResponse invokeDelete(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams);

}
