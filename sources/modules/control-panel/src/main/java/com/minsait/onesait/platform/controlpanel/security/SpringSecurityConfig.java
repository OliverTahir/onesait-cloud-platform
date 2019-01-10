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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final static String LOGIN_STR = "/login";

	@Bean
	public FilterRegistrationBean corsFilterOauth() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	@Autowired
	private AccessDeniedHandler accessDeniedHandler;
	@Autowired
	private AuthenticationProvider authenticationProvider;

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;

	@Autowired
	private Securityhandler successHandler;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.headers().frameOptions().disable();
		http.csrf().disable().authorizeRequests().antMatchers("/", "/home", "/favicon.ico").permitAll()
				.antMatchers("/api/applications", "/api/applications/").permitAll().antMatchers("/users/register")
				.permitAll().antMatchers(HttpMethod.POST, "/users/reset-password").permitAll()
				.antMatchers(HttpMethod.PUT, "/users/update/**/**").permitAll()
				.antMatchers(HttpMethod.GET, "/users/update/**/**").permitAll()
				.antMatchers("/health/", "/info", "/metrics", "/trace", "/api", "/dashboards/**", "/viewers/view/**",
						"/gadgets/**", "/viewers/**", "/datasources/**", "/v2/api-docs/", "/v2/api-docs/**",
						"/swagger-resources/", "/swagger-resources/**", "/swagger-ui.html")
				.permitAll().antMatchers("/oauth/").permitAll().antMatchers("/api-ops", "/api-ops/**").permitAll()
				.antMatchers("/management", "/management/**").permitAll()
				.antMatchers("/notebook-ops", "/notebook-ops/**").permitAll().antMatchers(HttpMethod.GET, "/files/list")
				.authenticated().antMatchers(HttpMethod.GET, "/files/**").permitAll()
				.antMatchers("/binary-repository", "/binary-repository/**").permitAll().antMatchers("/admin")
				.hasAnyRole("ROLE_ADMINISTRATOR").antMatchers("/admin/**").hasAnyRole("ROLE_ADMINISTRATOR").anyRequest()
				.authenticated().and().formLogin().loginPage(LOGIN_STR).successHandler(successHandler).permitAll().and()
				.logout().logoutSuccessHandler(logoutSuccessHandler).permitAll().and().sessionManagement()
				.invalidSessionUrl(LOGIN_STR).maximumSessions(10).expiredUrl(LOGIN_STR).maxSessionsPreventsLogin(false)
				.sessionRegistry(sessionRegistry()).and().sessionFixation().none().and().exceptionHandling()
				.accessDeniedHandler(accessDeniedHandler);
	}

	@Bean
	public FilterRegistrationBean someFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				response.setHeader("X-Frame-Options", "SAMEORIGIN");
				filterChain.doFilter(request, response);
			}

			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) {
				String path = request.getServletPath();

				return path.startsWith("/dashboards/env/") || path.startsWith("/dashboards/model/")
						|| path.startsWith("/dashboards/editfulliframe/") || path.startsWith("/dashboards/viewiframe/")
						|| path.startsWith("/static/") || path.startsWith("/gadgets/getGadgetMeasuresByGadgetId")
						|| path.startsWith("/gadgets/updateiframe/") || path.startsWith("/gadgets/createiframe/")
						|| path.startsWith("/gadgets/getGadgetConfigById/")
						|| path.startsWith("/datasources/getDatasourceById/") || path.startsWith("/viewers/view/");

			}
		});
		registration.addUrlPatterns("/*");
		registration.setName("xFrameOptionsFilter");
		registration.setOrder(Ordered.LOWEST_PRECEDENCE);
		return registration;
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**");
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider);

	}
}
