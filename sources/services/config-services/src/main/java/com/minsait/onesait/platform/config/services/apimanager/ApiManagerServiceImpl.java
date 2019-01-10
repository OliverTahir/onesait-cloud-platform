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
package com.minsait.onesait.platform.config.services.apimanager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiAuthentication;
import com.minsait.onesait.platform.config.model.ApiAuthenticationAttribute;
import com.minsait.onesait.platform.config.model.ApiAuthenticationParameter;
import com.minsait.onesait.platform.config.model.ApiHeader;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationAttributeRepository;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationRepository;
import com.minsait.onesait.platform.config.repository.ApiHeaderRepository;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiQueryParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.apimanager.authentication.AuthenticationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.HeaderJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.QueryStringJson;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiManagerServiceImpl implements ApiManagerService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private UserApiRepository userApiRepository;
	@Autowired
	private ApiOperationRepository apiOperationRepository;
	@Autowired
	private ApiAuthenticationRepository apiAuthenticationRepository;
	@Autowired
	private ApiAuthenticationParameterRepository apiAuthenticationParameterRepository;
	@Autowired
	private ApiAuthenticationAttributeRepository apiAuthenticationAttributeRepository;
	@Autowired
	private ApiQueryParameterRepository apiQueryParameterRepository;
	@Autowired
	private ApiHeaderRepository apiHeaderRepository;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private ServiceUtils serviceUtils;
	@Autowired
	private OPResourceService resourceService;

	private final static String EXCEPTION_REACHED = "Exception reached ";
	private final static String AMPERSAND = "&amp;";
	private final static String PARSING_ERROR = "Error parsing operations: {} ";

	@Override
	public List<Api> loadAPISByFilter(String apiId, String state, String userId, String loggeduser) {
		List<Api> apis = null;
		// Gets context User
		final User user = userService.getUser(loggeduser);

		// Clean the filter
		if ((apiId == null || "".equals(apiId))) {
			apiId = "";
		}

		if ((userId == null || "".equals(userId))) {
			userId = "";
		}

		if ((state == null || "".equals(state))) {
			apis = apiRepository.findApisByIdentificationOrUserForAdminOrOwnerOrPublicOrPermission(user.getUserId(),
					user.getRole().getId(), apiId, userId);
		} else {
			apis = apiRepository.findApisByIdentificationOrStateOrUserForAdminOrOwnerOrPublicOrPermission(
					user.getUserId(), user.getRole().getId(), apiId, Api.ApiStates.valueOf(state), userId);
		}

		return apis;
	}

	@Override
	public Integer calculateNumVersion(String numversionData) {
		List<Api> apis = null;
		Integer version = 0;
		String identification;
		ApiType apiType;
		Map<String, String> obj;
		try {
			obj = new ObjectMapper().readValue(numversionData, new TypeReference<Map<String, String>>() {
			});
			identification = obj.get("identification");
			apiType = ApiType.valueOf(obj.get("apiType"));

			if (StringUtils.isEmpty(apiType) || apiType.equals("null")) {
				apiType = null;
			}

			apis = apiRepository.findByIdentificationAndApiType(identification, apiType);
			for (final Api api : apis) {
				if (api.getNumversion() > version) {
					version = api.getNumversion();
				}
			}
		} catch (final JsonParseException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		} catch (final JsonMappingException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		} catch (final IOException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		}
		return (version + 1);
	}

	@Override
	public String createApi(Api api, String operationsObject, String authenticationObject) {

		final String numversionData = "{\"identification\":\"" + api.getIdentification() + "\",\"apiType\":\""
				+ api.getApiType() + "\"}";
		api.setNumversion(calculateNumVersion(numversionData));

		final ObjectMapper objectMapper = new ObjectMapper();

		List<OperationJson> operationsJson = null;

		if (operationsObject != null && !operationsObject.equals("")) {
			try {
				operationsJson = objectMapper.readValue(operationsObject, new TypeReference<List<OperationJson>>() {
				});
			} catch (final JsonParseException e) {
				log.error(EXCEPTION_REACHED + e.getMessage(), e);
			} catch (final JsonMappingException e) {
				log.error(EXCEPTION_REACHED + e.getMessage(), e);
			} catch (final IOException e) {
				log.error(EXCEPTION_REACHED + e.getMessage(), e);
			}

		}

		final AuthenticationJson authenticationJson = null;
		if (authenticationObject != null && !authenticationObject.equals("")) {
		}

		apiRepository.save(api);

		createAuthentication(api, authenticationJson);
		if (operationsJson != null)
			createOperations(api, operationsJson);

		return api.getId();
	}

	private void createAuthentication(Api api, AuthenticationJson authenticationJson) {
		if (authenticationJson != null) {
			final ApiAuthentication authentication = new ApiAuthentication();
			authentication.setType(authenticationJson.getType());
			authentication.setDescription(authenticationJson.getDescription());
			authentication.setApi(api);

			apiAuthenticationRepository.save(authentication);

			createHeaderParams(authentication, authenticationJson.getParams());
		}
	}

	private void createHeaderParams(ApiAuthentication authentication, List<List<Map<String, String>>> authParameters) {
		for (final List<Map<String, String>> authParameterJson : authParameters) {
			final ApiAuthenticationParameter authParam = new ApiAuthenticationParameter();
			authParam.setApiAuthentication(authentication);

			apiAuthenticationParameterRepository.save(authParam);

			createHeaderParamAtribs(authParam, authParameterJson);
		}
	}

	private void createHeaderParamAtribs(ApiAuthenticationParameter authparam,
			List<Map<String, String>> autHeaderParametersJson) {
		for (final Map<String, String> autHeaderParameterJson : autHeaderParametersJson) {
			final ApiAuthenticationAttribute autParameterAtrib = new ApiAuthenticationAttribute();
			autParameterAtrib.setName(autHeaderParameterJson.get("key"));
			autParameterAtrib.setValue(autHeaderParameterJson.get("value"));
			autParameterAtrib.setApiAuthenticationParameter(authparam);

			apiAuthenticationAttributeRepository.save(autParameterAtrib);
		}
	}

	private void createOperations(Api api, List<OperationJson> operationsJson) {
		for (final OperationJson operationJson : operationsJson) {
			final ApiOperation operation = new ApiOperation();
			operation.setApi(api);
			operation.setIdentification(operationJson.getIdentification());
			operation.setDescription(operationJson.getDescription());
			operation.setOperation(ApiOperation.Type.valueOf(operationJson.getOperation()));
			if (operationJson.getBasepath() != null && !operationJson.getBasepath().equals("")) {
				final String basepath = operationJson.getBasepath().replace(AMPERSAND, "&");
				operation.setBasePath(basepath);
			}
			if (operationJson.getEndpoint() != null && !operationJson.getEndpoint().equals("")) {
				final String endpoint = operationJson.getEndpoint().replace(AMPERSAND, "&");
				operation.setEndpoint(endpoint);
			}
			final String path = operationJson.getPath().replace(AMPERSAND, "&");
			operation.setPath(path);

			operation.setPostProcess(operationJson.getPostprocess());

			apiOperationRepository.save(operation);

			if (operationJson.getQuerystrings() != null && operationJson.getQuerystrings().size() > 0) {
				createQueryStrings(operation, operationJson.getQuerystrings());
			}
			if (operationJson.getHeaders() != null && operationJson.getHeaders().size() > 0) {
				createHeaders(operation, operationJson.getHeaders());
			}
		}
	}

	private void createQueryStrings(ApiOperation operation, List<QueryStringJson> querystrings) {
		for (final QueryStringJson queryStringJson : querystrings) {
			final ApiQueryParameter apiQueryParameter = new ApiQueryParameter();
			apiQueryParameter.setApiOperation(operation);
			apiQueryParameter.setName(queryStringJson.getName());
			apiQueryParameter.setDescription(queryStringJson.getDescription());
			apiQueryParameter.setDataType(ApiQueryParameter.DataType.valueOf(queryStringJson.getDataType()));
			apiQueryParameter.setHeaderType(ApiQueryParameter.HeaderType.valueOf(queryStringJson.getHeaderType()));
			apiQueryParameter.setValue(queryStringJson.getValue());
			apiQueryParameter.setCondition(queryStringJson.getCondition());

			apiQueryParameterRepository.save(apiQueryParameter);
		}
	}

	private void createHeaders(ApiOperation operation, List<HeaderJson> headers) {
		for (final HeaderJson headerJson : headers) {
			final ApiHeader header = new ApiHeader();
			header.setApiOperation(operation);
			header.setName(headerJson.getName());
			header.setHeader_description(headerJson.getDescription());
			header.setHeader_type(headerJson.getType());
			header.setHeader_value(headerJson.getValue());
			header.setHeader_condition(headerJson.getCondition());

			apiHeaderRepository.save(header);
		}
	}

	@Override
	public void updateApi(Api api, String deprecateApis, String operationsObject, String authenticationObject) {

		if (deprecateApis != null && !deprecateApis.equals("")) {
			deprecateApis(api.getIdentification());
		}

		final Api apimemory = apiRepository.findById(api.getId());

		final byte[] imagenOriginal = apimemory.getImage();

		apimemory.setIdentification(api.getIdentification());
		apimemory.setPublic(api.isPublic());
		apimemory.setSsl_certificate(api.isSsl_certificate());
		apimemory.setDescription(api.getDescription());
		apimemory.setCategory(api.getCategory());
		apimemory.setApiType(api.getApiType());
		apimemory.setEndpoint(api.getEndpoint());
		apimemory.setEndpointExt(api.getEndpointExt());
		apimemory.setMetaInf(api.getMetaInf());

		if (api.getCachetimeout() != null) {
			apimemory.setCachetimeout(api.getCachetimeout());
		} else {
			apimemory.setCachetimeout(null);
		}
		if (api.getApilimit() != null) {
			Integer apiLimit = api.getApilimit();
			if (apiLimit < 5) {
				apiLimit = 5;
			}
			if (apiLimit > 100) {
				apiLimit = 100;
			}
			apimemory.setApilimit(apiLimit);
		} else {
			apimemory.setApilimit(null);
		}

		apimemory.setState(api.getState());

		if (api.getImage() != null && api.getImage().length > 0) {
			apimemory.setImage(api.getImage());
		} else {
			apimemory.setImage(imagenOriginal);
		}

		// Si el tipo es vacio o no es igual a un tipo conocido, se asigna null para ser
		// retrocompatible
		// if ((apiMultipart.getTipoapi()!=null &&
		// apiMultipart.getTipoapi().trim().length()==0) ||
		// (apiMultipart.getTipoapi()!=null &&
		// !apiMultipart.getTipoapi().equalsIgnoreCase("webservice") &&
		// !apiMultipart.getTipoapi().equalsIgnoreCase("odata4"))){
		// apimemory.setTipoapi(null);
		// } else {
		// apimemory.setTipoapi(apiMultipart.getTipoapi());
		// }

		apiRepository.save(apimemory);

		final ObjectMapper objectMapper = new ObjectMapper();

		List<OperationJson> operationsJson = null;

		try {
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				operationsJson = objectMapper.readValue(reformat(operationsObject),
						new TypeReference<List<OperationJson>>() {
						});
				updateOperations(apimemory, operationsJson);
			}
		} catch (final JsonParseException e) {
			log.error(PARSING_ERROR, e);
		} catch (final JsonMappingException e) {
			log.error(PARSING_ERROR, e);
		} catch (final IOException e) {
			log.error(PARSING_ERROR, e);
		}

		// AuthenticationJson authenticationJson = null;
		//
		// if (authenticationObject!=null && !authenticationObject.equals("")){
		//
		// if (autenticacion!=null && !autenticacion.equals("")){
		// JSONDeserializer<AutenticationJson> JSONDeserializer = new
		// JSONDeserializer<AutenticationJson>().use(null,
		// AutenticationJson.class).use("parametros.values", ArrayList.class);
		// AutenticationJson autenticacionJson =
		// JSONDeserializer.deserialize(autenticacion);
		//
		// updateAutenticacion(apimemory, autenticacionJson);
		// }
		// }

	}

	private String reformat(String operationsObject) {
		if (operationsObject.indexOf(",") == 0) {
			operationsObject = operationsObject.substring(1);
		}
		return operationsObject;
	}

	// private void updateAuthentication(Api apimemory, AuthenticationJson
	// authenticationJson) {
	// List<ApiAuthentication> apiAutenticationlist =
	// apiAuthenticationRepository.findAllByApi(apimemory);
	// for (ApiAuthentication apiAuthentication : apiAutenticationlist) {
	// apiAuthenticationRepository.delete(apiAuthentication);
	// }
	// createAuthentication(apimemory, authenticationJson);
	// }

	private void updateOperations(Api api, List<OperationJson> operationsJson) {
		final List<ApiOperation> apiOperations = apiOperationRepository.findAllByApi(api);
		for (final ApiOperation apiOperation : apiOperations) {
			for (final ApiHeader apiHeader : apiOperation.getApiheaders()) {
				apiHeaderRepository.delete(apiHeader);
			}
			for (final ApiQueryParameter apiQueryParameter : apiOperation.getApiqueryparameters()) {
				apiQueryParameterRepository.delete(apiQueryParameter);
			}
			apiOperationRepository.delete(apiOperation);
		}
		createOperations(api, operationsJson);
	}

	@Override
	public void removeAPI(String id) throws Exception {

		final Api apiremove = apiRepository.findById(id);
		if (resourceService.isResourceSharedInAnyProject(apiremove))
			throw new OPResourceServiceException(
					"This Api is shared within a Project, revoke access from project prior to deleting");
		final List<ApiOperation> apiOperationList = apiOperationRepository.findByApiIdOrderByOperationDesc(id);

		apiOperationRepository.delete(apiOperationList);
		log.debug("API's operation deleted");

		final List<UserApi> userApiList = userApiRepository.findByApiId(id);

		userApiRepository.delete(userApiList);
		log.debug("API's authorizations deleted");

		apiRepository.delete(apiremove);
		log.debug("API deleted");
	}

	private void deprecateApis(String apiId) {
		List<Api> apis = null;
		try {
			apis = apiRepository.findByIdentification(apiId);
			for (final Api api : apis) {
				if (api.getState().equals(Api.ApiStates.PUBLISHED)) {
					api.setState(Api.ApiStates.DEPRECATED);
					apiRepository.save(api);
				}
			}
		} catch (final Exception e) {
		}
	}

	@Override
	public UserApi updateAuthorization(String apiId, String userId) {
		UserApi userApi = userApiRepository.findByApiIdAndUser(apiId, userId);

		if (userApi == null) {
			final Api api = apiRepository.findById(apiId);
			final User user = userRepository.findByUserId(userId);

			userApi = new UserApi();
			userApi.setApi(api);
			userApi.setUser(user);

			userApiRepository.save(userApi);
		}
		return userApi;
	}

	@Override
	public void removeAuthorizationById(String id) {
		final UserApi userApi = userApiRepository.findById(id);
		userApiRepository.delete(userApi);
	}

	@Override
	public byte[] getImgBytes(String id) {
		final Api api = apiRepository.findById(id);

		final byte[] buffer = api.getImage();

		return buffer;
	}

	@Override
	public void updateState(String id, String state) {
		final Api api = apiRepository.findById(id);
		api.setState(Api.ApiStates.valueOf(state));
		apiRepository.save(api);
	}

	@Override
	public void generateToken(String userId) throws Exception {
		final User user = userService.getUser(userId);

		userTokenService.generateToken(user);
	}

	@Override
	public void removeToken(String userId, String tokenJson) throws Exception {
		String token;
		Map<String, String> obj;

		obj = new ObjectMapper().readValue(tokenJson, new TypeReference<Map<String, String>>() {
		});
		token = obj.get("token");

		final User user = userService.getUser(userId);

		userTokenService.removeToken(user, token);
	}

	@Override
	public void removeAuthorizationByApiAndUser(String apiId, String userId) {
		final UserApi userApi = userApiRepository.findByApiIdAndUser(apiId, userId);
		if (userApi != null) {
			userApiRepository.delete(userApi);
		}

	}

	@Override
	public boolean hasUserEditAccess(String apiId, String userId) {
		final User user = userService.getUser(userId);
		final Api api = apiRepository.findById(apiId);
		if (user.equals(api.getUser()) || user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		else
			return resourceService.hasAccess(userId, apiId, ResourceAccessType.MANAGE);

	}

	@Override
	public boolean hasUserAccess(String apiId, String userId) {
		final User user = userService.getUser(userId);
		final Api api = apiRepository.findById(apiId);
		if (user.equals(api.getUser()) || user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return true;
		if ((api.isPublic() || userApiRepository.findByApiIdAndUser(apiId, userId) != null)
				&& (!api.getState().toString().equals("CREATED") || !api.getState().toString().equals("DELETED")))
			return true;
		return false;
	}

	@Override
	public void updateApiPostProcess(String apiId, String postProcessFx) {
		final Api api = apiRepository.findById(apiId);
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			// Only one operation should exist for this type of apis
			ApiOperation operation = apiOperationRepository.findAllByApi(api).stream().findFirst().orElse(null);
			if (operation != null) {
				operation.setPostProcess(postProcessFx);
			} else {
				operation = new ApiOperation();
				operation.setIdentification(api.getIdentification());
				operation.setApi(api);
				operation.setPostProcess(postProcessFx);
				operation.setOperation(Type.GET);
				operation.setDescription("Post process operation");

			}
			apiOperationRepository.save(operation);

		}

	}

	@Override
	public boolean postProcess(Api api) {

		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final ApiOperation operation = apiOperationRepository.findAllByApi(api).stream().findFirst().orElse(null);
			if (operation != null)
				return true;
		}
		return false;
	}

	@Override
	public String getPostProccess(Api api) {
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final ApiOperation operation = apiOperationRepository.findAllByApi(api).stream().findFirst().orElse(null);
			if (operation != null)
				return operation.getPostProcess();
		}
		return new String();
	}

	@Override
	public List<ApiOperation> getOperations(Api api) {
		return apiOperationRepository.findByApiOrderByOperationDesc(api);

	}

	@Override
	public List<ApiOperation> getOperationsByMethod(Api api, Type method) {
		return apiOperationRepository.findByApiAndOperation(api, method);
	}

}
