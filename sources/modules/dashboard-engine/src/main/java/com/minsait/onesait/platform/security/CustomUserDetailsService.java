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
package com.minsait.onesait.platform.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.bean.Role;
import com.minsait.onesait.platform.bean.User;
import com.minsait.onesait.platform.security.user.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

@Service()
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	public CustomUserDetailsService() {
		super();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			User user = new User();
			user.setUser_id(1L);
			user.setUsername("user");
			user.setPassword("passsword");
			List<Role> lRoles = new ArrayList<Role>();
			lRoles.add(new Role(1L, "USER"));
			user.setRoles(lRoles);
			if (user != null)
				return new CustomUserDetails(user, getAuthorities(user));
		} catch (Exception ex) {
			log.error("Exception in CustomUserDetailsService: " + ex);
		}
		return null;
	}

	private Collection<GrantedAuthority> getAuthorities(User user) {
		Collection<GrantedAuthority> authorities = new HashSet<>();
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("USER");
		authorities.add(grantedAuthority);

		return authorities;
	}
}