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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.api.ApiSecurityService;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

@Component
@Rule
public class SecurityOntologyRule extends DefaultRuleBase {

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Priority
	public int getPriority() {
		return 5;
	}

	@Condition
	@SuppressWarnings("unchecked")
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = (HttpServletRequest) facts.get(RuleManager.REQUEST);
		final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);
		final Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);

		if ((request != null && ontology != null) && canExecuteRule(facts))
			return true;
		else
			return false;
	}

	@Action
	@SuppressWarnings("unchecked")
	public void setFirstDerivedData(Facts facts) {
		final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);

		final User user = (User) data.get(ApiServiceInterface.USER);
		final Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);
		final boolean insert = !((String) data.get(ApiServiceInterface.METHOD)).equals(ApiOperation.Type.GET.name());

		final boolean ontologyPermission = apiSecurityService.checkRole(user, ontology, insert);

		if (!ontologyPermission) {
			stopAllNextRules(facts, "User has no permission to use Ontology " + ontology.getIdentification(),
					DefaultRuleBase.ReasonType.SECURITY);
		}

	}

}