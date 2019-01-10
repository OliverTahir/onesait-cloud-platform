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
package authentication;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import authentication.server.PlainSaslServerProvider;

public class PlainLoginModule implements LoginModule {

	private static final String USERNAME_CONFIG = "username";
	private static final String PASSWORD_CONFIG = "password";

	public static String URL = "";

	static {
		PlainSaslServerProvider.initialize();
	}

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {

		System.out.println("--------------------------->PlainModule Initialize");
		System.out.println(options.toString());
		String url = (String) options.get("url");
		if (url != null) {
			System.out.println(url);
			PlainLoginModule.URL = url;
		}

		String username = (String) options.get(USERNAME_CONFIG);
		if (username != null)
			subject.getPublicCredentials().add(username);
		String password = (String) options.get(PASSWORD_CONFIG);
		if (password != null)
			subject.getPrivateCredentials().add(password);
	}

	@Override
	public boolean login() throws LoginException {
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		return true;
	}

	@Override
	public boolean commit() throws LoginException {
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return false;
	}
}