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
package com.minsait.onesait.platform.flowengine.api.rest.service;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.exception.NotAuthorizedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;

public interface FlowEngineValidationNodeService {

	User validateUserCredentials(String userId, String credentials)
			throws ResourceNotFoundException, NotAuthorizedException;

	User validateUser(String userId) throws ResourceNotFoundException;

	DecodedAuthentication decodeAuth(String authentication) throws IllegalArgumentException;
}
