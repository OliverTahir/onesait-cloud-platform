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
package com.minsait.onesait.platform.config.services.ksql.resource;

import java.util.List;

import com.minsait.onesait.platform.config.model.KsqlResource;

public interface KsqlResourceService {

	public KsqlResource getKsqlResourceByIdentification(String identification);

	public KsqlResource getKsqlResourceById(String id);

	public List<KsqlResource> getKsqlResourceByKafkaTopic(String kafkaTopic);

	public void validateNewKsqlResource(KsqlResource ksqlResource);

	public void createKsqlResource(KsqlResource ksqlResource);

	public void updateKsqlResource(KsqlResource ksqlResource);

	public void deleteKsqlResource(KsqlResource ksqlResource);

}
