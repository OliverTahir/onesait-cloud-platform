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
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.User;

@Component
@Rule
public class SecurityRule extends DefaultRuleBase {

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Priority
	public int getPriority() {
		return 3;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		HttpServletRequest request = (HttpServletRequest) facts.get(RuleManager.REQUEST);
		if ((request != null) && canExecuteRule(facts))
			return true;
		else
			return false;
	}

	@Action
	@SuppressWarnings("unchecked")
	public void setFirstDerivedData(Facts facts) {
		Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);

		User user = (User) data.get(ApiServiceInterface.USER);
		Api api = (Api) data.get(ApiServiceInterface.API);

		boolean published = false;
		
		boolean available  = apiSecurityService.checkApiAvailable(api, user);
		boolean checkUser  = apiSecurityService.checkUserApiPermission(api, user);
		published = apiSecurityService.checkApiIsPublic(api);
		
		if (!available) {
			stopAllNextRules(facts, "API is not Available",DefaultRuleBase.ReasonType.SECURITY);
		}
		if (!checkUser) {
			if (!published)
				stopAllNextRules(facts, "User has no permission to use API",DefaultRuleBase.ReasonType.SECURITY);
		}
		

	}

}