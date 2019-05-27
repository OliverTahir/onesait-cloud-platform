/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.security.ri;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConfigDBDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		final User user = userRepository.findByUserId(username);

		if (user == null) {
			log.info("LoadUserByUserName: User not found by name: " + username);
			throw new UsernameNotFoundException("User not found by name: " + username);
		}

		return toUserDetails(user);
	}

	private UserDetails toUserDetails(User userObject) {
		return org.springframework.security.core.userdetails.User.withUsername(userObject.getUserId())
				.password(userObject.getPassword())
				.authorities(new SimpleGrantedAuthority(userObject.getRole().getId())).build();
	}

}
