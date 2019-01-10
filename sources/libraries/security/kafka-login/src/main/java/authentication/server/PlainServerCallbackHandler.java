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
package authentication.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.kafka.common.KafkaException;

import authentication.AuthenticateCallbackHandler;
import authentication.PlainAuthenticateCallback;
import authentication.PlainLoginModule;

public class PlainServerCallbackHandler implements AuthenticateCallbackHandler {

	private final String USER_AGENT = "Mozilla/5.0";

	private static String BASE = "http://localhost:18000/controlpanel/api-ops/validate/";
	private static String ZOOKEPER = "zookeeper";
	private static String SCHEMA_REGISTRY = "schema-registry";

	@Override
	public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
		System.out.println("--------------------------->PlainServerCallbackHandler Initialize");
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		String username = null;
		for (Callback callback : callbacks) {
			if (callback instanceof NameCallback)
				username = ((NameCallback) callback).getDefaultName();
			else if (callback instanceof PlainAuthenticateCallback) {
				PlainAuthenticateCallback plainCallback = (PlainAuthenticateCallback) callback;
				boolean authenticated = authenticate(username, plainCallback.password());
				plainCallback.authenticated(authenticated);
			} else
				throw new UnsupportedCallbackException(callback);
		}
	}

	protected boolean authenticate(String username, char[] password) throws IOException {
		if (username == null) {
			return false;
		} else {

			System.out.println("GET OPERATION " + username + ":" + new String(password));

			boolean ret = false;
			if (username.equals("admin")) {
				String expectedPassword = "admin-secret";
				ret = expectedPassword.equals(new String(password));
			} else if (username.equals(ZOOKEPER)) {
				String expectedPassword = "zookeeper";
				ret = expectedPassword.equals(new String(password));
			} else if (username.equals(SCHEMA_REGISTRY)) {
				String expectedPassword = "schema-registry";
				ret = expectedPassword.equals(new String(password));
			}

			else {
				try {
					ret = sendGet(username, new String(password));
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			System.out.println("RETURN GET OPERATION " + username + ":" + new String(password) + " RET " + ret);
			return ret;
		}
	}

	// HTTP GET request
	private boolean sendGet(String username, String password) throws IOException {

		String device = "/device/" + username;
		String token = "/token/" + password;

		String url = getBaseURL() + device + token;

		System.out.println("CALLING URL" + url);

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		String res = response.toString();
		if ("VALID".equals(res))
			return true;
		else
			return false;

	}

	@Override
	public void close() throws KafkaException {
	}

	private String getBaseURL() {
		String myEnv = PlainLoginModule.URL;
		if (myEnv == null || "".equals(myEnv))
			return BASE;
		else
			return myEnv;
	}

}
