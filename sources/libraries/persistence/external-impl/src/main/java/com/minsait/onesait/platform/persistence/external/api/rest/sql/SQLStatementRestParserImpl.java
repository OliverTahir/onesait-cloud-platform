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
package com.minsait.onesait.platform.persistence.external.api.rest.sql;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("SQLStatementRestParserImpl")
@Lazy
@Slf4j
public class SQLStatementRestParserImpl implements SQLStatementRestParser {

	@Override
	public Optional<String> getSelectOperation(String statement) {
		// TODO
		return Optional.empty();
	}

	@Override
	public Optional<List<String>> getSelectPathParameters(String statement) {
		// TODO
		return Optional.empty();
	}

	@Override
	public Optional<Map<String, Object>> getSelectQueryParameters(String statement) {
		// TODO
		return Optional.empty();
	}

}
