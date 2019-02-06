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
package com.minsait.onesait.platform.config.services.user;

import java.util.List;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;

public interface UserService {

	boolean isUserAdministrator(User user);

	boolean isUserDeveloper(User user);

	Token getToken(String token);

	UserToken getUserToken(String token);

	User getUser(UserToken token);

	User getUserByToken(String token);

	User getUserByEmail(String email);

	User getUserByIdentification(String identification);

	User getUser(String userId);

	List<Role> getAllRoles();

	List<UserToken> getUserToken(User userId);

	List<User> getAllUsers();

	List<User> getAllUsersByCriteria(String userId, String fullName, String email, String roleType, Boolean active);

	void createUser(User user);

	boolean userExists(User user) throws UserServiceException;

	void updateUser(User user);

	void updatePassword(User user);

	Role getUserRole(String role);

	Role getUserRoleById(String roleId);

	void deleteUser(String userId);
	
	void deleteUser(List<String> userIds);

	void hardDeleteUser(String userId);

	void registerRoleDeveloper(User user);

	void registerRoleUser(User user);

	List<ClientPlatform> getClientsForUser(User user);

	boolean emailExists(User user);

	UserToken getUserToken(String user, String token);

	User saveExistingUser(User user);

	List<User> getAllActiveUsers();

	List<User> getAllActiveUsersWithoutRoles(Type... role);

}
