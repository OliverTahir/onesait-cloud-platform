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
package com.minsait.onesait.platform.controlpanel.controller.dashboard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.EditorDTO;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import groovy.util.logging.Slf4j;

@RequestMapping("/dashboards")
@Controller
@Slf4j

public class DashboardController {

	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private SubcategoryService subcategoryService;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	@Autowired(required = false)
	private JWTService jwtService;

	private final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	private static final String DASHBOARD_STR = "dashboard";
	private static final String DASHB_CREATE = "dashboards/create";
	private static final String REDIRECT_DASHB_CREATE = "redirect:/dashboards/create";
	private static final String CREDENTIALS_STR = "credentials";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null) {
			if (identification.equals(""))
				identification = null;
		}
		if (description != null) {
			if (description.equals(""))
				description = null;
		}

		final List<DashboardDTO> dashboard = dashboardService
				.findDashboardWithIdentificationAndDescription(identification, description, utils.getUserId());

		uiModel.addAttribute("dashboards", dashboard);

		return "dashboards/list";

	}

	@RequestMapping(value = "/viewerlist", produces = "text/html")
	public String viewerlist(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null) {
			if (identification.equals(""))
				identification = null;
		}
		if (description != null) {
			if (description.equals(""))
				description = null;
		}

		final List<DashboardDTO> dashboard = dashboardService
				.findDashboardWithIdentificationAndDescription(identification, description, utils.getUserId());
		uiModel.addAttribute("dashboards", dashboard);
		return "dashboards/viewerlist";

	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(DASHBOARD_STR, new DashboardCreateDTO());
		model.addAttribute("users", getUserListDTO());
		model.addAttribute("categories", categoryService.findAllCategories());
		model.addAttribute("schema", dashboardConfRepository.findAll());
		return DASHB_CREATE;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {

		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardEditById(id, utils.getUserId()));

		return DASHB_CREATE;

	}

	@PostMapping(value = { "/create" })
	public String createDashboard(Model model, @Valid DashboardCreateDTO dashboard, BindingResult bindingResult,
			HttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("dashboard.validation.error", redirect);
			return REDIRECT_DASHB_CREATE;
		}

		try {

			final String dashboardId = dashboardService.createNewDashboard(dashboard, utils.getUserId());
			return "redirect:/dashboards/editfull/" + dashboardId;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return REDIRECT_DASHB_CREATE;
		}
	}

	@PostMapping(value = { "/dashboardconf/{id}" })
	public String saveUpdateDashboard(@PathVariable("id") String id, DashboardCreateDTO dashboard,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("dashboard.validation.error", redirect);
			return REDIRECT_DASHB_CREATE;
		}

		try {
			if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
				dashboardService.cleanDashboardAccess(dashboard, utils.getUserId());
				dashboardService.saveUpdateAccess(dashboard, utils.getUserId());
				dashboardService.updatePublicDashboard(dashboard, utils.getUserId());
			} else {
				throw new DashboardServiceException(
						"Cannot update Dashboard that does not exist or don't have permission");
			}
			return "redirect:/dashboards/list/";

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/dashboards/dashboardconf/" + dashboard.getId();
		}
	}

	@GetMapping(value = "/dashboardconf/{id}", produces = "text/html")
	public String updateDashboard(Model model, @PathVariable("id") String id) {
		final Dashboard dashboard = dashboardService.getDashboardEditById(id, utils.getUserId());

		if (dashboard != null) {

			final DashboardCreateDTO dashBDTO = new DashboardCreateDTO();

			dashBDTO.setId(id);
			dashBDTO.setIdentification(dashboard.getIdentification());
			dashBDTO.setDescription(dashboard.getDescription());
			if (null != dashboard.getImage()) {
				dashBDTO.setHasImage(Boolean.TRUE);
			} else {
				dashBDTO.setHasImage(Boolean.FALSE);
			}
			// dashBDTO.setImage(dashboard.getImage().getBytes());
			dashBDTO.setPublicAccess(dashboard.isPublic());
			final List<DashboardUserAccess> userAccess = dashboardService.getDashboardUserAccesses(id);
			if (userAccess != null && userAccess.size() > 0) {
				final ArrayList<DashboardAccessDTO> list = new ArrayList<DashboardAccessDTO>();
				for (final Iterator<DashboardUserAccess> iterator = userAccess.iterator(); iterator.hasNext();) {
					final DashboardUserAccess dua = iterator.next();
					if (userIsActive(dua.getUser().getUserId())) {
						final DashboardAccessDTO daDTO = new DashboardAccessDTO();
						daDTO.setAccesstypes(dua.getDashboardUserAccessType().getName());
						daDTO.setUsers(dua.getUser().getUserId());
						list.add(daDTO);
					}
				}
				final ObjectMapper objectMapper = new ObjectMapper();
				try {
					dashBDTO.setAuthorizations(objectMapper.writeValueAsString(list));
				} catch (final JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			model.addAttribute(DASHBOARD_STR, dashBDTO);
			model.addAttribute("users", getUserListDTO());
			return DASHB_CREATE;
		} else {
			return "redirect:/dashboards/list";
		}
	}

	@GetMapping(value = "/editor/{id}", produces = "text/html")
	public String editorDashboard(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardById(id, utils.getUserId()));
		model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
		return "dashboards/editor";

	}

	@GetMapping(value = "/model/{id}", produces = "application/json")
	public @ResponseBody String getModelById(@PathVariable("id") String id) {
		return dashboardService.getDashboardById(id, utils.getUserId()).getModel();
	}

	@GetMapping(value = "/editfull/{id}", produces = "text/html")
	public String editFullDashboard(Model model, @PathVariable("id") String id) {
		if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
			model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardById(id, utils.getUserId()));
			model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
			model.addAttribute("edition", true);
			model.addAttribute("iframe", false);
			return "dashboards/view";
		} else
			return "error/403";

	}

	@GetMapping(value = "/view/{id}", produces = "text/html")
	public String viewerDashboard(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		if (dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardById(id, utils.getUserId()));
			model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
			model.addAttribute("edition", false);
			model.addAttribute("iframe", false);
			request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
			return "dashboards/view";
		} else {
			request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
			return "redirect:/403";
		}
	}

	@PutMapping(value = "/save/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboard(@PathVariable("id") String id,
			@RequestParam("data") Dashboard dashboard) {
		dashboardService.saveDashboard(id, dashboard, utils.getUserId());
		return "ok";
	}

	@PutMapping(value = "/savemodel/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboardModel(@PathVariable("id") String id, @RequestBody EditorDTO model) {
		dashboardService.saveDashboardModel(id, model.getModel(), utils.getUserId());
		return "{\"ok\":true}";
	}

	@PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String deleteDashboard(@PathVariable("id") String id) {

		try {
			dashboardService.deleteDashboard(id, utils.getUserId());
		} catch (final RuntimeException e) {
			return "{\"ok\":false, \"error\":\"" + e.getMessage() + "\"}";
		}
		return "{\"ok\":true}";
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		try {
			if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
				dashboardService.deleteDashboardAccess(id, utils.getUserId());
				dashboardService.deleteDashboard(id, utils.getUserId());
			}
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, ra);
		}
		return "redirect:/dashboards/list/";
	}

	@RequestMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = dashboardService.getImgBytes(id);
		if (buffer.length > 0) {
			OutputStream output = null;
			try {
				output = response.getOutputStream();
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
			} finally {
				try {
					output.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	@GetMapping(value = "/getSubcategories/{category}")
	public @ResponseBody List<String> getSubcategories(@PathVariable("category") String category,
			HttpServletResponse response) {
		return subcategoryService
				.findSubcategoriesNamesByCategory(categoryService.getCategoryByIdentification(category));
	}

	@GetMapping(value = "/editfulliframe/{id}", produces = "text/html")
	public String editFullDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam("oauthtoken") String userToken) {

		try {
			OAuth2Authentication info = null;
			if (userToken != null) {
				info = (OAuth2Authentication) jwtService.getAuthentication(userToken);

				if (dashboardService.hasUserEditPermission(id, (String) info.getUserAuthentication().getPrincipal())) {
					model.addAttribute("dashboard", dashboardService.getDashboardById(id,
							(String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute("credentials", dashboardService
							.getCredentialsString((String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute("edition", true);
					model.addAttribute("iframe", true);
					return "dashboards/view";
				} else
					return "error/403";
			}
		} catch (final Exception e) {
			return "error/403";
		}
		return "error/403";

	}

	@GetMapping(value = "/viewiframe/{id}", produces = "text/html")
	public String viewerDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam("oauthtoken") String userToken, HttpServletRequest request) {

		try {
			OAuth2Authentication info = null;
			if (userToken != null) {
				info = (OAuth2Authentication) jwtService.getAuthentication(userToken);
				if (dashboardService.hasUserViewPermission(id, (String) info.getUserAuthentication().getPrincipal())) {
					model.addAttribute("dashboard", dashboardService.getDashboardById(id,
							(String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute("credentials", dashboardService
							.getCredentialsString((String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute("edition", false);
					model.addAttribute("iframe", true);
					request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
					return "dashboards/view";
				} else {
					request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
					return "redirect:/403";
				}
			}
		} catch (final Exception e) {
			return "redirect:/403";
		}
		return "redirect:/403";
	}

	private ArrayList<UserDTO> getUserListDTO() {
		final List<User> users = userService.getAllActiveUsers();
		final ArrayList<UserDTO> userList = new ArrayList<UserDTO>();
		if (users != null && users.size() > 0) {
			for (final Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
				final User user = iterator.next();
				final UserDTO uDTO = new UserDTO();
				uDTO.setUserId(user.getUserId());
				userList.add(uDTO);
			}
		}
		return userList;
	}

	private boolean userIsActive(String userId) {
		final User user = userService.getUser(userId);
		return user.isActive();
	}

}
