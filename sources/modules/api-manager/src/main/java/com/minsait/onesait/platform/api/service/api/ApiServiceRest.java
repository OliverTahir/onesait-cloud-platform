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
package com.minsait.onesait.platform.api.service.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.rest.api.dto.ApiDTO;
import com.minsait.onesait.platform.api.rest.api.dto.ApiHeaderDTO;
import com.minsait.onesait.platform.api.rest.api.dto.ApiQueryParameterDTO;
import com.minsait.onesait.platform.api.rest.api.dto.AutenticacionAtribDTO;
import com.minsait.onesait.platform.api.rest.api.dto.AutenticacionDTO;
import com.minsait.onesait.platform.api.rest.api.dto.OperacionDTO;
import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.AuthenticationFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.HeaderFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.OperationFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.QueryParameterFIQL;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.ApiAuthentication;
import com.minsait.onesait.platform.config.model.ApiAuthenticationAttribute;
import com.minsait.onesait.platform.config.model.ApiAuthenticationParameter;
import com.minsait.onesait.platform.config.model.ApiHeader;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationAttributeRepository;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationRepository;
import com.minsait.onesait.platform.config.repository.ApiHeaderRepository;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiQueryParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;

@Service
public class ApiServiceRest {

	@Autowired
	private ApiFIQL apiFIQL;

	@Autowired
	private ApiRepository apiRepository;

	@Autowired
	private ApiOperationRepository apiOperationRepository;

	@Autowired
	private ApiHeaderRepository apiHeaderRepository;

	@Autowired
	private ApiQueryParameterRepository apiQueryParameterRepository;

	@Autowired
	private ApiAuthenticationRepository apiAuthenticationRepository;

	@Autowired
	private ApiAuthenticationParameterRepository apiAuthenticationParameterRepository;

	@Autowired
	private ApiAuthenticationAttributeRepository apiAuthenticationAttributeRepository;

	@Autowired
	private UserApiRepository userApiRepository;

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Autowired
	private UserTokenRepository userTokenRepository;

	private static final String NOT_ALLOWED_OPERATIONS = "com.indra.sofia2.web.api.services.NoPermisosOperacion";

	private static final String NO_API = "com.indra.sofia2.web.api.services.NoApi";

	private static final String NON_EXISTENT_SUSCRIPTION = "com.indra.sofia2.web.api.services.SuscripcionNoExiste";

	private static final String REQUIRED_ID_SCRIPT = "IdentificacionScriptRequerido";

	private static final String NOT_ALLOWED_USER_OPERATION = "NoPermisosOperacionUsuario";

	public List<Api> findApisByUser(String userId, String token) {
		List<Api> apis = null;
		apis = apiRepository.findByUser(apiSecurityService.getUser(userId));
		return apis;
	}

	public Api getApi(String identificacionApi) {
		Api api = null;
		List<Api> apis = apiRepository.findByIdentification(identificacionApi);
		for (Api apiAux : apis) {
			if (apiAux.getState().name().equalsIgnoreCase(Api.ApiStates.PUBLISHED.name())) {
				api = apiAux;
			}
		}
		if (api == null) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.ApiNoPublicada");
		}
		return api;
	}

	public Api getApiMaxVersion(String identificacionApi) {
		Api api = null;
		List<Api> apis = apiRepository.findByIdentification(identificacionApi);
		for (Api apiAux : apis) {
			if (api == null || api.getNumversion() < apiAux.getNumversion()) {
				api = apiAux;
			}
		}
		if (api == null) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.ApiNoExiste");
		}
		return api;
	}

	public Api findApi(String identificacion, String token) {
		Api api = getApiMaxVersion(identificacion);
		if (api != null) {
			if (apiSecurityService.authorized(api, token)) {
				return api;
			}
		} else {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.ApiNoExiste");
		}
		return null;
	}

	public List<Api> findApis(String identificacion, String estado, String usuario, String token) {
		List<Api> apis = null;
		apis = apiRepository.findByIdentification(identificacion);
		return apis;
	}

	public Api changeState(String indentifier, ApiStates api, String token) {

		User user = apiSecurityService.getUserByApiToken(token);
		if (apiSecurityService.isAdmin(user)) {
			List<Api> apis = apiRepository.findByIdentification(indentifier);
			if (apis != null) {
				Api theApi = apis.get(0);
				theApi.setState(api);
				apiRepository.saveAndFlush(theApi);
				return theApi;
			} else
				return null;
		}
		return null;
	}

	public void createApi(ApiDTO apiDTO, String token) {
		User user = apiSecurityService.getUserByApiToken(token);
		Api api = apiFIQL.copyProperties(apiDTO, user);

		Integer numVersion = 0;
		List<Api> apis = apiRepository.findByIdentification(api.getIdentification());
		for (Api apiBD : apis) {
			if (numVersion < apiBD.getNumversion()) {
				numVersion = apiBD.getNumversion();
			}
		}
		if (numVersion >= api.getNumversion()) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.wrongversionMin");
		}

		api.setUser(user);

		api.setState(Api.ApiStates.CREATED);
		apiRepository.saveAndFlush(api);
		createOperations(apiDTO.getOperations(), api);
		createAutenticacion(apiDTO.getAuthentication(), api);
	}

	public void updateApi(ApiDTO apiDTO, String token) {
		try {
			User user = apiSecurityService.getUserByApiToken(token);
			Api api = apiFIQL.copyProperties(apiDTO, user);

			Api apiUpdate = apiRepository
					.findByIdentificationAndNumversion(api.getIdentification(), api.getNumversion()).get(0);
			if (apiSecurityService.authorized(api, token)) {
				apiUpdate = apiFIQL.copyProperties(apiUpdate, api);
				apiRepository.saveAndFlush(apiUpdate);
				updateOperaciones(apiDTO.getOperations(), apiUpdate);
				updateAutenticacion(apiDTO.getAuthentication(), apiUpdate);

			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	public void removeApi(ApiDTO apiDTO, String token) {
		try {
			User user = apiSecurityService.getUserByApiToken(token);
			Api api = apiFIQL.copyProperties(apiDTO, user);
			Api apiDelete = apiRepository
					.findByIdentificationAndNumversion(api.getIdentification(), api.getNumversion()).get(0);
			if (apiSecurityService.authorized(apiDelete, token)) {
				removeOperations(apiDelete);
				removeAuthorization(apiDelete);
				apiRepository.delete(apiDelete);
			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	public void removeApiByIdentificacionNumversion(String identificacion, String numversion, String token) {
		Integer version = null;
		try {
			version = Integer.parseInt(numversion);
		} catch (Exception e) {
			throw new AuthorizationServiceException("com.indra.sofia2.web.api.services.wrongversion");
		}
		try {
			Api apiDelete = apiRepository.findByIdentificationAndNumversion(identificacion, version).get(0);
			if (apiSecurityService.authorized(apiDelete, token)) {
				removeOperations(apiDelete);
				removeAuthorization(apiDelete);
				apiRepository.delete(apiDelete);
			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	private void createOperations(ArrayList<OperacionDTO> operaciones, Api api) {
		for (OperacionDTO operacionDTO : operaciones) {
			ApiOperation operacion = OperationFIQL.copyProperties(operacionDTO);
			operacion.setIdentification(operacionDTO.getIdentification());
			// if (operacion.getIdentification()==null ||
			// "".equals(operacion.getIdentification()) ) {
			// String path = operacion.getPath();
			// if (path!=null && path.contains("?")) {
			// path = path.substring(0, path.indexOf("?"));
			// if ("".equals(path)==false)
			// operacion.setIdentification(api.getIdentification()+"_"+operacion.getOperation()+"_"+path.replace("/",
			// ""));
			// }
			//
			// else {
			// operacion.setIdentification(api.getIdentification()+"_"+operacion.getOperation()+"_"+path.replace("/",
			// "").replace("?", ""));
			// }
			// }
			operacion.setApi(api);
			apiOperationRepository.saveAndFlush(operacion);
			if (operacionDTO.getHeaders() != null)
				createHeaders(operacion, operacionDTO.getHeaders());
			if (operacionDTO.getQueryParams() != null)
				createQueryParams(operacion, operacionDTO.getQueryParams());
		}
	}

	private void updateOperaciones(ArrayList<OperacionDTO> operacionesDTO, Api api) {
		removeOperations(api);
		createOperations(operacionesDTO, api);
	}

	private void removeOperations(Api api) {
		List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);
		for (ApiOperation operacion : operaciones) {
			apiOperationRepository.delete(operacion);
		}
	}

	private void createHeaders(ApiOperation operacion, ArrayList<ApiHeaderDTO> headersDTO) {
		for (ApiHeaderDTO headerDTO : headersDTO) {
			ApiHeader apiHeader = HeaderFIQL.copyProperties(headerDTO);
			apiHeader.setApiOperation(operacion);
			apiHeaderRepository.saveAndFlush(apiHeader);
		}
	}

	private void createQueryParams(ApiOperation operacion, ArrayList<ApiQueryParameterDTO> queryParamsDTO) {
		for (ApiQueryParameterDTO queryParamDTO : queryParamsDTO) {
			ApiQueryParameter apiQueryParam = QueryParameterFIQL.copyProperties(queryParamDTO);
			apiQueryParam.setApiOperation(operacion);

			apiQueryParameterRepository.saveAndFlush(apiQueryParam);

		}
	}

	private void createAutenticacion(AutenticacionDTO autenticacionDTO, Api api) {
		if (autenticacionDTO != null) {
			ApiAuthentication authentication = AuthenticationFIQL.copyProperties(autenticacionDTO);
			authentication.setApi(api);
			apiAuthenticationRepository.saveAndFlush(authentication);

			for (ArrayList<AutenticacionAtribDTO> parametroDTO : autenticacionDTO.getAuthParameters()) {
				ApiAuthenticationParameter parameter = new ApiAuthenticationParameter();
				parameter.setApiAuthentication(authentication);
				apiAuthenticationParameterRepository.saveAndFlush(parameter);
				for (AutenticacionAtribDTO atribDTO : parametroDTO) {
					ApiAuthenticationAttribute autparametroatrib = new ApiAuthenticationAttribute();
					autparametroatrib.setName(atribDTO.getName());
					autparametroatrib.setValue(atribDTO.getValue());
					autparametroatrib.setApiAuthenticationParameter(parameter);
					apiAuthenticationAttributeRepository.saveAndFlush(autparametroatrib);

				}
			}
		}
	}

	private void updateAutenticacion(AutenticacionDTO autenticacionDTO, Api apiUpdate) {
		removeAuthorization(apiUpdate);
		createAutenticacion(autenticacionDTO, apiUpdate);

	}

	// FIXME: process the remove of authorizations
	private void removeAuthorization(Api apiDelete) {

	}

	public UserApi findApiSuscriptions(String identificacionApi, String tokenUsuario) {
		if (identificacionApi == null) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.IdentificacionApiRequerido");
		}
		if (tokenUsuario == null) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.TokenUsuarioApiRequerido");
		}

		Api api = findApi(identificacionApi, tokenUsuario);
		UserApi suscription = null;

		User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		suscription = userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId());

		return suscription;
	}

	public UserApi findApiSuscriptions(Api api, User user) {
		UserApi suscription = null;
		suscription = userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId());
		return suscription;
	}

	public List<UserApi> findApiSuscripcionesUser(String identificacionUsuario) {
		List<UserApi> suscriptions = null;

		if (identificacionUsuario == null) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.IdentificacionApiRequerido");
		}

		User suscriber = apiSecurityService.getUser(identificacionUsuario);
		suscriptions = userApiRepository.findByUser(suscriber);
		return suscriptions;
	}

	private boolean authorizedOrSuscriptor(Api api, String tokenUsuario, String suscriptor) {
		User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(api.getUser().getUserId())
				|| user.getUserId().equals(suscriptor)) {
			return true;
		} else {
			return false;
		}
	}

	public void createSuscripcion(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate == null) {
					userApiRepository.save(suscription);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		} else {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.NoAutorizado");
		}
	}

	public void updateSuscripcion(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate != null) {
					apiUpdate.setCreatedAt(suscription.getCreatedAt());
					apiUpdate.setUpdatedAt(suscription.getUpdatedAt());
					userApiRepository.save(apiUpdate);
				}

			} catch (Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		}
	}

	public void removeSuscripcionByUserAndAPI(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate != null) {
					userApiRepository.delete(apiUpdate);
				}

			} catch (Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		}
	}

	public UserToken findTokenUserByIdentification(String identificacion, String tokenUsuario) {

		UserToken token = null;
		if (identificacion == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identificacion)) {
			User userToTokenize = apiSecurityService.getUser(identificacion);
			token = apiSecurityService.getUserToken(userToTokenize, identificacion);
		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public UserToken addTokenUsuario(String identificacion, String tokenUsuario) {
		UserToken token = null;
		if (identificacion == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identificacion)) {

			User userToTokenize = apiSecurityService.getUser(identificacion);

			token = apiSecurityService.getUserToken(userToTokenize, tokenUsuario);
			if (token == null)
				token = init_Token(userToTokenize);
			else {
				token = init_Token(userToTokenize);
			}

		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public UserToken generateTokenUsuario(String identificacion, String tokenUsuario) {

		UserToken token = null;
		if (identificacion == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identificacion)) {

			User userToTokenize = apiSecurityService.getUser(identificacion);

			token = apiSecurityService.getUserToken(userToTokenize, tokenUsuario);

			token = init_Token(userToTokenize);

		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public String generateTokenUsuario() {
		String candidateToken = "";
		candidateToken = UUID.randomUUID().toString();
		return candidateToken;
	}

	public UserToken init_Token(User user) {

		UserToken userToken = new UserToken();

		userToken.setToken(generateTokenUsuario());
		userToken.setUser(user);
		userToken.setCreatedAt(Calendar.getInstance().getTime());

		userTokenRepository.save(userToken);
		return userToken;

	}

}