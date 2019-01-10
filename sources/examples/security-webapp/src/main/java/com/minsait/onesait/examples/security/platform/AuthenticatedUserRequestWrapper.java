package com.minsait.onesait.examples.security.platform;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class AuthenticatedUserRequestWrapper extends HttpServletRequestWrapper {

	private OAuthAuthorization oauthInfo;

	public AuthenticatedUserRequestWrapper(HttpServletRequest request, OAuthAuthorization user) {
		super(request);
		
		this.oauthInfo = user;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return oauthInfo != null && oauthInfo.isAuthenticated();
	}

	@Override
	public String getAuthType() {
		return "OAuth2";
	}

	@Override
	public Principal getUserPrincipal() {
		return oauthInfo;
	}

	@Override
	public boolean isUserInRole(String role) {
		return oauthInfo.getAuthorities().contains(role);
	}
	
	

}
