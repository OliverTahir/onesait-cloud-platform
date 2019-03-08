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
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Rule
@Slf4j
public class UserAndAPIRule extends DefaultRuleBase {

	@Autowired
	private ApiManagerService apiManagerService;

	@Autowired
	private UserService userService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Priority
	public int getPriority() {
		return 2;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = (HttpServletRequest) facts.get(RuleManager.REQUEST);
		if ((request != null) && canExecuteRule(facts))
			return true;
		else
			return false;
	}

	@Action
	@SuppressWarnings("unchecked")
	public void setFirstDerivedData(Facts facts) {

		final Map<String, Object> data = (Map<String, Object>) facts.get(RuleManager.FACTS);

		final String PATH_INFO = (String) data.get(ApiServiceInterface.PATH_INFO);
		final String TOKEN = (String) data.get(ApiServiceInterface.AUTHENTICATION_HEADER);
		final String JWT_TOKEN = (String) data.get(ApiServiceInterface.JWT_TOKEN);
		User user = null;
		try {
			user = userService.getUserByToken(TOKEN);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}

		Api api = null;
		if (user == null) {
			if (JWT_TOKEN.length() > 0 && jwtService != null) {

				final String userid = jwtService.extractToken(JWT_TOKEN);

				if (userid != null)
					user = userService.getUser(userid);
			}
		}
		if (user == null) {

			stopAllNextRules(facts, "Token " + TOKEN + " not recognized for user ",
					DefaultRuleBase.ReasonType.SECURITY);

		}

		else {
			api = apiManagerService.getApi(PATH_INFO, user);
			if (api == null)

				stopAllNextRules(facts, "API not found with Token :" + TOKEN + " and Path Info" + PATH_INFO,

						DefaultRuleBase.ReasonType.SECURITY);
		}

		data.put(ApiServiceInterface.USER, user);
		data.put(ApiServiceInterface.API, api);
	}

}