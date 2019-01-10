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
package com.minsait.onesait.platform.controlpanel.rest.management.app.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Realm extends RealmCreate {
	@Getter
	@Setter
	private Set<RealmUser> users;
	@Getter
	@Setter
	private Set<RealmAssociation> associations;

	public Realm(App app, List<AppRole> allRoles) {
		super(app);
		users = new HashSet<>();
		app.getAppRoles().forEach(r -> {
			users.addAll(r.getAppUsers().stream()
					.map(u -> RealmUser.builder().avatar(u.getUser().getAvatar())
							.extraFields(u.getUser().getExtraFields()).fullName(u.getUser().getFullName())
							.mail(u.getUser().getEmail()).role(u.getRole().getName()).username(u.getUser().getUserId())
							.build())
					.collect(Collectors.toSet()));
		});
		associations = new HashSet<>();
		app.getAppRoles().forEach(r -> {
			r.getChildRoles()
					.forEach(cr -> associations.add(new RealmAssociation(r.getName().concat(":").concat(cr.getName()),
							realmId, r.getName(), cr.getApp().getAppId(), cr.getName())));
		});

		app.getAppRoles().forEach(r -> {
			allRoles.forEach(role -> {
				if (role.getChildRoles() != null && role.getChildRoles().contains(r)) {
					associations.add(new RealmAssociation(role.getName().concat(":").concat(r.getName()),
							role.getApp().getAppId(), role.getName(), realmId, r.getName()));
				}
			});
		});

	}

}
