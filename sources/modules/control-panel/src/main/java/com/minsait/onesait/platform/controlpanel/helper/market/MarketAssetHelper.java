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
package com.minsait.onesait.platform.controlpanel.helper.market;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserComment;
import com.minsait.onesait.platform.config.model.UserRatings;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.UserCommentRepository;
import com.minsait.onesait.platform.config.repository.UserRatingsRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.multipart.MarketAssetMultipart;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MarketAssetHelper {

	@Autowired
	ApiRepository apiRepository;

	@Autowired
	WebProjectRepository webProjectRepository;

	@Autowired
	private MarketAssetRepository marketAssetRepository;

	@Autowired
	UserRatingsRepository userRatingsRepository;

	@Autowired
	UserCommentRepository userCommentRepository;

	@Autowired
	IntegrationResourcesService resourcesService;

	@Autowired
	UserService userService;

	@Autowired
	AppWebUtils utils;

	@Value("${apimanager.services.api:/api-manager/services}")
	private String apiServices;

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private String rootWWW = "";

	private static final String MARKET_ASSET_TYPES_STR = "marketassettypes";
	private static final String MARKET_ASSET_MODES_STR = "marketassetmodes";
	private static final String TECHNOLOGIES_STR = "technologies";
	private static final String MARKET_ASSET_STR = "marketasset";
	private static final String DESCRIPTION_STR = "description";

	public void populateMarketAssetListForm(Model model) {
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute("marketassetstates", MarketAsset.MarketAssetState.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());
		model.addAttribute(TECHNOLOGIES_STR, getTechnologies());
	}

	public void populateMarketAssetCreateForm(Model model) {
		model.addAttribute(MARKET_ASSET_STR, new MarketAsset());
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());
	}

	public void populateMarketAssetUpdateForm(Model model, String id) throws Exception {
		User user = this.userService.getUser(utils.getUserId());

		MarketAsset marketAsset = marketAssetRepository.findById(id);

		// If the user is not the owner nor Admin an exception is launch to redirect to
		// show view
		if (!marketAsset.getUser().equals(user)
				&& !user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			throw new Exception();
		}

		model.addAttribute(MARKET_ASSET_STR, marketAsset);
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());

		populateMarketAssetFragment(model, marketAsset.getMarketAssetType().toString());
	}

	public void populateMarketAssetShowForm(Model model, String id) {
		// Asset to show
		MarketAsset marketAsset = marketAssetRepository.findById(id);

		model.addAttribute(MARKET_ASSET_STR, marketAsset);
		model.addAttribute("json_desc", marketAsset.getJsonDesc());

		// User Asset Market Rating
		List<UserRatings> userRatings = userRatingsRepository.findByMarketAssetAndUser(id, utils.getUserId());
		if (userRatings != null && userRatings.size() > 0) {
			model.addAttribute("userRating", userRatings.get(0).getValue());
		}

		// Asset Market Rating
		Double ratingMarketAsset = 0.0;
		List<UserRatings> usersRatingsMarketAssets = userRatingsRepository.findByMarketAsset(id);

		for (UserRatings usersRatingsMarketAsset : usersRatingsMarketAssets) {
			ratingMarketAsset = ratingMarketAsset + usersRatingsMarketAsset.getValue();
		}

		if (usersRatingsMarketAssets.size() > 0) {
			ratingMarketAsset = ratingMarketAsset / usersRatingsMarketAssets.size();
		} else {
			ratingMarketAsset = 5.0;
		}
		model.addAttribute("marketassetRating", ratingMarketAsset.intValue());

		// Asset Comments
		List<UserComment> usersComments = userCommentRepository.findByMarketAsset(id);
		model.addAttribute("commentsList", usersComments);
		model.addAttribute("commentsNumber", usersComments.size());

		// Technologies
		model.addAttribute(TECHNOLOGIES_STR, getTechnologies());

		// Five Assets
		List<MarketAsset> assets = marketAssetRepository.findByUser(utils.getUserId());
		List<MarketAssetDTO> fiveAssets = toMarketAssetBean(assets.subList(0, Math.min(assets.size(), 5)));

		// Technologies
		model.addAttribute("fiveAssets", fiveAssets);
	}

	public void populateMarketAssetFragment(Model model, String type) {

		User user = this.userService.getUser(utils.getUserId());

		if (type.equals(MarketAsset.MarketAssetType.API.toString())) {

			List<Api> apiList = null;

			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				apiList = apiRepository.findAll();
			} else {
				apiList = apiRepository.findByUser(user);
			}

			List<Api> apis = getApisIds(apiList);

			model.addAttribute("apis", apis);

		} else if (type.equals(MarketAsset.MarketAssetType.WEBPROJECT.toString())) {

			List<WebProject> webProjectList = null;
			List<WebProject> webProjectListFiltered = null;

			webProjectList = webProjectRepository.findAll();

			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				webProjectListFiltered = webProjectList;
			} else {
				webProjectListFiltered = new ArrayList<WebProject>();
				for (WebProject webProject : webProjectList) {
					if (user.getUserId().equals(webProject.getUser().getUserId())) {
						webProjectListFiltered.add(webProject);
					}
				}
			}
			model.addAttribute("webProjects", webProjectListFiltered);
		} else if (type.equals(MarketAsset.MarketAssetType.DOCUMENT.toString())) {
		} else if (type.equals(MarketAsset.MarketAssetType.APPLICATION.toString())) {
		} else if (type.equals(MarketAsset.MarketAssetType.URLAPPLICATION.toString())) {
		}

	}

	private List<Api> getApisIds(List<Api> apiList) {
		List<Api> apis = new ArrayList<Api>();
		for (Api api : apiList) {
			if (api.getState().equals(Api.ApiStates.DEVELOPMENT) || api.getState().equals(Api.ApiStates.PUBLISHED)) {
				apis.add(api);
			}
		}
		return apis;
	}

	public void populateApiVersions(Model model, String identification) {
		User user = this.userService.getUser(utils.getUserId());

		List<Api> apiList = null;

		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			apiList = apiRepository.findByIdentification(identification);
		} else {
			apiList = apiRepository.findByIdentificationAndUser(identification, user);
		}

		List<Api> apis = getApisIds(apiList);

		model.addAttribute("apis", apis);
	}

	public String getApiDescription(String apiData) {
		List<Api> apis = null;
		try {
			Map<String, String> obj = new ObjectMapper().readValue(apiData, new TypeReference<Map<String, String>>() {
			});

			String identification = obj.get("identification");
			String version = obj.get("version");

			apis = apiRepository.findByIdentificationAndNumversion(identification, Integer.parseInt(version));

		} catch (JsonParseException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		} catch (JsonMappingException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		} catch (IOException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		}

		String srcSwagger = resourcesService.getUrl(Module.apiManager, ServiceUrl.base) + "swagger-ui.html?url="
				+ apiServices + "/management/swagger/" + apis.get(0).getIdentification() + "/swagger.json";

		JSONObject returnObject = new JSONObject();

		try {
			returnObject.put(DESCRIPTION_STR, apis.get(0).getDescription());
			returnObject.put("srcSwagger", srcSwagger);
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
		return (returnObject.toString());
	}

	public String validateId(String marketAssetId) {
		try {
			Map<String, String> obj = new ObjectMapper().readValue(marketAssetId,
					new TypeReference<Map<String, String>>() {
					});

			String identification = obj.get("identification");

			MarketAsset marketAsset = marketAssetRepository.findByIdentification(identification);

			if (marketAsset != null) {
				return "ERROR";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public MarketAsset marketAssetMultipartMap(MarketAssetMultipart marketAssetMultipart) {

		MarketAsset marketAsset = new MarketAsset();

		marketAsset.setIdentification(marketAssetMultipart.getIdentification());

		marketAsset.setUser(this.userService.getUser(utils.getUserId()));

		marketAsset.setPublic(marketAssetMultipart.isPublic());
		marketAsset.setState(marketAssetMultipart.getState());
		marketAsset.setMarketAssetType(marketAssetMultipart.getMarketAssetType());
		marketAsset.setPaymentMode(marketAssetMultipart.getPaymentMode());

		marketAsset.setJsonDesc(marketAssetMultipart.getJsonDesc().toString());

		try {
			if (marketAssetMultipart.getContentId() != null && !"".equals(marketAssetMultipart.getContentId())) {
				marketAsset.setContent(marketAssetMultipart.getContent().getBytes());
				marketAsset.setContentId(marketAssetMultipart.getContentId());
			}
			if (marketAssetMultipart.getImage() != null) {
				marketAsset.setImage(marketAssetMultipart.getImage().getBytes());
				marketAsset.setImageType(marketAssetMultipart.getImageType());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		marketAsset.setCreatedAt(marketAssetMultipart.getCreatedAt());
		marketAsset.setUpdatedAt(marketAssetMultipart.getUpdatedAt());

		return marketAsset;
	}

	public List<MarketAssetDTO> toMarketAssetBean(List<MarketAsset> marketAssetList) {
		List<MarketAssetDTO> marketAssetDTOList = new ArrayList<MarketAssetDTO>();

		for (MarketAsset marketAsset : marketAssetList) {

			MarketAssetDTO marketAssetDTO = new MarketAssetDTO();

			marketAssetDTO.setId(marketAsset.getId());
			marketAssetDTO.setIdentification(marketAsset.getIdentification());

			marketAssetDTO.setUser(marketAsset.getUser());

			marketAssetDTO.setPublic(marketAsset.isPublic());
			marketAssetDTO.setState(marketAsset.getState());
			marketAssetDTO.setRejectionReason(marketAsset.getRejectionReason());
			marketAssetDTO.setMarketAssetType(marketAsset.getMarketAssetType());
			marketAssetDTO.setPaymentMode(marketAsset.getPaymentMode());
			marketAssetDTO.setJsonDesc(marketAsset.getJsonDesc().toString());

			if (marketAsset.getImage() != null) {
				marketAssetDTO.setImage(marketAsset.getImage());
			}

			if (marketAsset.getJsonDesc().toString() != null && !"".equals(marketAsset.getJsonDesc().toString())) {

				Map<String, String> obj;
				try {
					obj = new ObjectMapper().readValue(marketAsset.getJsonDesc().toString(),
							new TypeReference<Map<String, String>>() {
							});

					marketAssetDTO.setTitle(obj.get("title"));
					marketAssetDTO.setDescription(obj.get(DESCRIPTION_STR));
					marketAssetDTO.setTechnologies(obj.get(TECHNOLOGIES_STR));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			marketAssetDTO.setCreatedAt(marketAsset.getCreatedAt());
			marketAssetDTO.setUpdatedAt(marketAsset.getUpdatedAt());

			marketAssetDTOList.add(marketAssetDTO);
		}
		return marketAssetDTOList;
	}

	private Collection<String> getTechnologies() {
		List<String> jsonDescArray = marketAssetRepository.findJsonDescs();
		HashMap<String, String> technologiesMap = new HashMap<String, String>();

		Map<String, String> obj;
		for (String jsonDesc : jsonDescArray) {
			try {
				obj = new ObjectMapper().readValue(jsonDesc, new TypeReference<Map<String, String>>() {
				});

				String technologies = obj.get(TECHNOLOGIES_STR).trim();
				List<String> technologiesList = new ArrayList<String>(Arrays.asList(technologies.split(",")));

				for (String technology : technologiesList) {
					technologiesMap.put(technology.toUpperCase(), technology.toUpperCase());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return (technologiesMap.values());
	}

	public String getUrlWebProjectData(String webProjectData) {

		Map<String, String> obj;
		String id = "";

		try {
			obj = new ObjectMapper().readValue(webProjectData, new TypeReference<Map<String, String>>() {
			});

			id = obj.get("webprojectId");
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		WebProject webProject = webProjectRepository.findById(id);

		String webProjectUrl = rootWWW + webProject.getIdentification() + "/" + webProject.getMainFile();

		JSONObject returnObject = new JSONObject();

		try {
			returnObject.put(DESCRIPTION_STR, webProject.getDescription());
			returnObject.put("webProjectUrl", webProjectUrl);
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
		return (returnObject.toString());
	}

}
