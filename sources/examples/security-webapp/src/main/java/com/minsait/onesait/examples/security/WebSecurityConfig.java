package com.minsait.onesait.examples.security;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.minsait.onesait.examples.security.platform.OAuthFilter;
import com.minsait.onesait.examples.security.platform.spring.SpringOAuthFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(new OAuthFilter(), BasicAuthenticationFilter.class)
        	.addFilterAfter(new SpringOAuthFilter(), OAuthFilter.class)
            .authorizeRequests()
	            .antMatchers("/", "/home").permitAll()
                .antMatchers("/employeeTask").hasAnyAuthority("operations")
                .antMatchers("/managerTask").hasAnyAuthority("admin", "operations")
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

	@Bean
    public FilterRegistrationBean<Filter> oauthFilter() {
    	
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(createOauthFilter());
        registration.addUrlPatterns("/*");
        registration.setName("OauthFilter");
        registration.setOrder(10);
        registration.addInitParameter("applicationId", "child1");
        return registration;
    }
    
    private Filter createOauthFilter() {
        return new OAuthFilter();
    }

}
