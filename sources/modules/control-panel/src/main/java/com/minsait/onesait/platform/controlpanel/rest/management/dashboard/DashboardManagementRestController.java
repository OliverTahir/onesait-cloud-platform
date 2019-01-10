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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.controller.dashboardapi.dto.CommandDTO;
import com.minsait.onesait.platform.controlpanel.rest.ManagementRestServices;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "Dashboard Management", tags = { "Dashoard management service" })
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class DashboardManagementRestController extends ManagementRestServices {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subCategoryService;

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	@Autowired
	private AppWebUtils utils;

	@Value("${onesaitplatform.dashboardengine.url}")
	private String url;

	private final static String PATH = "/dashboard";

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO[].class))
	@ApiOperation(value = "Get dashboards")
	@GetMapping(PATH)
	public ResponseEntity<DashboardDTO[]> getAll() {
		List<Dashboard> dashboards = dashboardService.getByUserId(utils.getUserId());
		DashboardDTO[] dashboardsDTO = new DashboardDTO[dashboards.size()];
		int i = 0;
		for (Dashboard dashboard : dashboards) {
			CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
			String categoryIdentification = null;
			String subCategoryIdentification = null;
			if (categoryRelationship != null) {
				Category category = categoryService.getCategoryByIdentification(categoryRelationship.getCategory());
				Subcategory subcategory = subCategoryService.getSubcategoryById(categoryRelationship.getSubcategory());
				categoryIdentification = category.getIdentification();
				subCategoryIdentification = subcategory.getIdentification();
			}

			DashboardDTO dashboardDTO = DashboardDTO.builder().identification(dashboard.getIdentification())
					.user(dashboard.getUser().getUserId()).url(url + dashboard.getId()).category(categoryIdentification)
					.subcategory(subCategoryIdentification).createdAt(dashboard.getCreatedAt())
					.modifiedAt(dashboard.getUpdatedAt()).build();

			dashboardsDTO[i] = dashboardDTO;
			i++;
		}

		return new ResponseEntity<>(dashboardsDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Create new dashboard")
	@PostMapping(PATH)
	public ResponseEntity<?> create(
			@ApiParam(value = "CommandDTO", required = true) @Valid @RequestBody CommandDTO commandDTO, Errors errors) {
		try {

			DashboardCreateDTO dashboard = new DashboardCreateDTO();
			dashboard.setIdentification(commandDTO.getInformation().getDashboard());
			String description = "";
			if (commandDTO.getInformation().getDashboardDescription() != null) {
				description = commandDTO.getInformation().getDashboardDescription();
			}
			dashboard.setDescription(description);
			dashboard.setPublicAccess(Boolean.FALSE);
			List<DashboardConf> listStyles = dashboardConfRepository.findAll();
			String initialStyleId = null;
			String initialIdentification = null;
			if (commandDTO.getInformation().getDashboardStyle() == null) {
				initialIdentification = "notitle";
			} else {
				initialIdentification = commandDTO.getInformation().getDashboardStyle();
			}
			for (Iterator iterator = listStyles.iterator(); iterator.hasNext();) {
				DashboardConf dashboardCon = (DashboardConf) iterator.next();
				if (dashboardCon.getIdentification().equals(initialIdentification)) {
					initialStyleId = dashboardCon.getId();
					break;
				}
			}
			dashboard.setDashboardConfId(initialStyleId);
			final String dashboardId = dashboardService.createNewDashboard(dashboard, utils.getUserId());
			Dashboard dashboardCreated = dashboardService.getDashboardById(dashboardId, utils.getUserId());

			DashboardDTO dashboardDTO = DashboardDTO.builder().identification(dashboardCreated.getIdentification())
					.user(dashboardCreated.getUser().getUserId()).url(url + dashboardCreated.getId()).category(null)
					.subcategory(null).createdAt(dashboardCreated.getCreatedAt())
					.modifiedAt(dashboardCreated.getUpdatedAt()).build();
			return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);

		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Delete dashboard by id")
	@DeleteMapping(PATH + "/{id}")
	public ResponseEntity<?> delete(
			@ApiParam(value = "dashboard id", example = "developer", required = true) @PathVariable("id") String DashboardId) {
		try {
			Dashboard dashboard = dashboardService.getDashboardById(DashboardId, utils.getUserId());
			dashboardService.deleteDashboard(dashboard.getId(), utils.getUserId());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
