package com.minsait.onesait.examples.security.platform.spring;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.minsait.onesait.examples.security.platform.OAuthAuthorization;

public class SpringOAuthFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequestWrapper request = (HttpServletRequestWrapper) servletRequest;
		if (request.authenticate((HttpServletResponse) servletResponse)) {
			OAuthAuthorization auth = (OAuthAuthorization)request.getUserPrincipal();
			
			SecurityContextHolder.getContext().setAuthentication(new OauthInfoSpring(auth));
		}
		
		filterChain.doFilter(servletRequest, servletResponse);
	}

	private class OauthInfoSpring implements Authentication{

		/**
		 * 
		 */
		private static final long serialVersionUID = 8453329447099011271L;
		private OAuthAuthorization data;
		private boolean authenticated;

		public OauthInfoSpring (OAuthAuthorization data){
			this.data = data;
			this.authenticated = data != null && data.isAuthenticated();
		}
		
		public List<GrantedAuthority> getAuthorities() {
			return data.getAuthorities().stream()
					.map(role -> new SimpleGrantedAuthority(role))
					.collect(Collectors.toList());
		}

		public Object getCredentials() {
			// TODO Auto-generated method stub
			return data.getToken();
		}

		public Object getDetails() {
			return null;
		}

		public Object getPrincipal() {
			return data;
		}

		public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
			authenticated = arg0;
		}

		@Override
		public String getName() {
			return data.getName();
		}

		@Override
		public boolean isAuthenticated() {
			return authenticated;
		}
		
	}
	
}
