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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rest.api.dto.TokenUserDTO;
import com.minsait.onesait.platform.api.rest.api.fiql.TokenUserFIQL;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;

@Component("tokenUserRestService")
public class TokenUserRestServiceImpl implements TokenUserRestService {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private TokenUserFIQL tokenUser;

	@Override
	public Response getTokenUser(String identificacion, String token) throws Exception {
		return Response.ok(tokenUser.toTokenUsuarioDTO(apiService.findTokenUserByIdentification(identificacion, token)))
				.build();
	}

	@Override
	public Response addTokenUser(String identificacion, String token) throws Exception {
		TokenUserDTO tokenUsuarioDTO = tokenUser.toTokenUsuarioDTO(apiService.addTokenUsuario(identificacion, token));
		return Response.ok(tokenUsuarioDTO.getToken()).build();
	}

	@Override
	public Response generateToken(String identificacion, String token) throws Exception {
		TokenUserDTO tokenUsuarioDTO = tokenUser
				.toTokenUsuarioDTO(apiService.generateTokenUsuario(identificacion, token));
		return Response.ok(tokenUsuarioDTO.getToken()).build();
	}

}
