package com.minsait.onesait.examples.security.platform;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class OAuthAuthorization implements Principal{

	private String principal;
	private List<String> roles;
	private String token;
	
	public OAuthAuthorization() {
		roles = new ArrayList<>();
		
	}
	public OAuthAuthorization(JsonObject oauthInfo, String clientId) {
		
		principal = oauthInfo.getString("principal");
		token = oauthInfo.getString("access_token");
		JsonArray aux;
		
		if (clientId.equals(oauthInfo.getString("clientId")))
			aux = oauthInfo.getJsonArray("authorities");
		else {
			JsonObject app = oauthInfo.getJsonObject("apps");
			aux = app != null ? app.getJsonArray(clientId) : null;
		}
		
		if (aux != null)
			roles = aux.stream().map(JsonValue::toString).collect(Collectors.toList());
	}

	public String getName() {
		return this.principal;
	}

	public List<String> getAuthorities() {
		return this.roles;
	}
	
	public String getToken() {
		return token;
	}

	public boolean isAuthenticated() {
		return principal != null;
	}
	
	@Override
	public String toString() {
		if (isAuthenticated())
			return principal.toString() + ":" + roles.toString();
		return "Not Authenticated";
	}


}
