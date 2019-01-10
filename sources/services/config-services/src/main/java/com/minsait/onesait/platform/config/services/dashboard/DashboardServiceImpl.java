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
package com.minsait.onesait.platform.config.services.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;
	@Autowired
	private DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private SubcategoryRepository subcategoryRepository;
	@Autowired
	private CategoryRelationRepository categoryRelationRepository;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	// private static final String INITIAL_MODEL = "{\"header\":{\"title\":\"My new
	// onesait platform
	// Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New
	// Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
	private static final String ANONYMOUSUSER = "anonymousUser";
	private static final String AUTH_PARSE_EXCEPT = "Authorizations parse Exception";
	private static final String DASH_NOT_EXIST = "Dashboard does not exist in the database";

	@Override
	public List<DashboardDTO> findDashboardWithIdentificationAndDescription(String identification, String description,
			String userId) {
		List<Dashboard> dashboards;
		final User sessionUser = userRepository.findByUserId(userId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			dashboards = dashboardRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
		} else {
			dashboards = dashboardRepository
					.findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(sessionUser,
							identification, description);
		}

		final List<DashboardDTO> dashboardsDTO = dashboards.stream().map(temp -> {
			final DashboardDTO obj = new DashboardDTO();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			if (null != temp.getImage()) {
				obj.setHasImage(Boolean.TRUE);
			} else {
				obj.setHasImage(Boolean.FALSE);
			}
			obj.setPublic(temp.isPublic());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			obj.setUserAccessType(getUserTypePermissionForDashboard(temp, sessionUser));
			return obj;
		}).collect(Collectors.toList());

		return dashboardsDTO;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<Dashboard> dashboards = dashboardRepository.findAllByOrderByIdentificationAsc();
		final List<String> identifications = new ArrayList<String>();
		for (final Dashboard dashboard : dashboards) {
			identifications.add(dashboard.getIdentification());

		}
		return identifications;
	}

	@Transactional
	@Override
	public void deleteDashboard(String dashboardId, String userId) {
		final Dashboard dashboard = dashboardRepository.findById(dashboardId);
		if (dashboard != null) {
			if (resourceService.isResourceSharedInAnyProject(dashboard))
				throw new OPResourceServiceException(
						"This Dashboard is shared within a Project, revoke access from project prior to deleting");
			final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(dashboard.getId());
			if (categoryRelation != null) {

				categoryRelationRepository.delete(categoryRelation);
			}

			dashboardRepository.delete(dashboard);
		} else
			throw new DashboardServiceException("Cannot delete dashboard that does not exist");

	}

	@Transactional
	@Override
	public String deleteDashboardAccess(String dashboardId, String userId) {

		final Dashboard d = dashboardRepository.findById(dashboardId);
		if (resourceService.isResourceSharedInAnyProject(d))
			throw new OPResourceServiceException(
					"This Dashboard is shared within a Project, revoke access from project prior to deleting");
		dashboardUserAccessRepository.deleteByDashboard(d);
		return d.getId();

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			return dashboardRepository.findById(id).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			final boolean propietary = dashboardRepository.findById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository
					.findByDashboardAndUser(dashboardRepository.findById(id), user);

			if (userAuthorization != null) {
				switch (DashboardUserAccessType.Type
						.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
				case EDIT:
					return true;
				case VIEW:
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
			}

		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);

		if (dashboardRepository.findById(id).isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return dashboardRepository.findById(id).isPublic();
		} else if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			final boolean propietary = dashboardRepository.findById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository
					.findByDashboardAndUser(dashboardRepository.findById(id), user);

			if (userAuthorization != null) {
				switch (DashboardUserAccessType.Type
						.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
				case EDIT:
					return true;
				case VIEW:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
			}

		}
	}

	public String getUserTypePermissionForDashboard(Dashboard dashboard, User user) {

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return DashboardUserAccessType.Type.EDIT.toString();
		} else {

			if (dashboard.getUser().getUserId().equals(user.getUserId())) {
				return DashboardUserAccessType.Type.EDIT.toString();
			}
			final DashboardUserAccess userAuthorization = dashboardUserAccessRepository
					.findByDashboardAndUser(dashboard, user);

			if (userAuthorization != null) {
				switch (DashboardUserAccessType.Type
						.valueOf(userAuthorization.getDashboardUserAccessType().getName())) {
				case EDIT:
					return DashboardUserAccessType.Type.EDIT.toString();
				case VIEW:
					return DashboardUserAccessType.Type.VIEW.toString();
				default:
					return DashboardUserAccessType.Type.VIEW.toString();
				}
			} else {
				if (resourceService.getResourceAccess(user.getUserId(), dashboard.getId()) != null) {
					switch (resourceService.getResourceAccess(user.getUserId(), dashboard.getId())) {
					case MANAGE:
						return DashboardUserAccessType.Type.EDIT.toString();
					case VIEW:
					default:
						return DashboardUserAccessType.Type.VIEW.toString();
					}
				} else
					return DashboardUserAccessType.Type.VIEW.toString();

			}

		}
	}

	@Override
	public void saveDashboard(String id, Dashboard dashboard, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final Dashboard dashboardEnt = dashboardRepository.findById(dashboard.getId());
			dashboardEnt.setCustomcss(dashboard.getCustomcss());
			dashboardEnt.setCustomjs(dashboard.getCustomjs());
			dashboardEnt.setDescription(dashboard.getDescription());
			dashboardEnt.setJsoni18n(dashboard.getJsoni18n());
			dashboardEnt.setModel(dashboard.getModel());
			dashboardEnt.setPublic(dashboard.isPublic());
			dashboardRepository.save(dashboardEnt);
		} else
			throw new DashboardServiceException("Cannot update Dashboard that does not exist or don't have permission");
	}

	@Override
	public void saveDashboardModel(String id, String model, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final Dashboard dashboardEnt = dashboardRepository.findById(id);
			dashboardEnt.setModel(model);

			dashboardRepository.save(dashboardEnt);
		} else
			throw new DashboardServiceException("Cannot update Dashboard that does not exist or don't have permission");
	}

	@Override
	public Dashboard getDashboardById(String id, String userId) {
		return dashboardRepository.findById(id);
	}

	@Override
	public Dashboard getDashboardByIdentification(String identification, String userId) {
		return dashboardRepository.findByIdentification(identification).get(0);
	}

	@Override
	public Dashboard getDashboardEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return dashboardRepository.findById(id);
		}
		throw new DashboardServiceException("Cannot view Dashboard that does not exist or don't have permission");
	}

	@Override
	public String getCredentialsString(String userId) {
		final User user = userRepository.findByUserId(userId);
		return userId;
	}

	@Override
	public boolean dashboardExists(String identification) {
		if (dashboardRepository.findByIdentification(identification).size() != 0)
			return true;
		else
			return false;
	}

	@Override
	public String createNewDashboard(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExists(dashboard.getIdentification())) {

			log.debug("Dashboard no exist, creating...");
			final Dashboard d = new Dashboard();
			d.setCustomcss("");
			d.setCustomjs("");
			d.setJsoni18n("");
			try {
				if (null != dashboard.getImage() && !dashboard.getImage().isEmpty()) {
					d.setImage(dashboard.getImage().getBytes());
				} else {
					d.setImage(null);
				}
			} catch (final IOException e) {

				e.printStackTrace();

			}
			d.setDescription(dashboard.getDescription());
			d.setIdentification(dashboard.getIdentification());
			d.setPublic(dashboard.getPublicAccess());
			d.setUser(userRepository.findByUserId(userId));

			String model = null;
			if (dashboard.getDashboardConfId() == null) {
				List<DashboardConf> dashConfList = dashboardConfRepository.findByIdentification("default");
				for (Iterator<DashboardConf> iterator = dashConfList.iterator(); iterator.hasNext();) {
					DashboardConf dashConf = iterator.next();
					model = dashConf.getModel();
					break;
				}
			} else {
				DashboardConf dashConf = dashboardConfRepository.findById(dashboard.getDashboardConfId());
				model = dashConf.getModel();
			}
			d.setModel(model);

			final Dashboard dAux = dashboardRepository.save(d);

			if (dashboard.getCategory() != null && dashboard.getSubcategory() != null
					&& !dashboard.getCategory().isEmpty() && !dashboard.getSubcategory().isEmpty()) {

				final CategoryRelation categoryRelation = new CategoryRelation();
				categoryRelation
						.setCategory(categoryRepository.findByIdentification(dashboard.getCategory()).get(0).getId());
				categoryRelation.setSubcategory(
						subcategoryRepository.findByIdentification(dashboard.getSubcategory()).get(0).getId());
				categoryRelation.setType(CategoryRelation.Type.DASHBOARD);
				categoryRelation.setTypeId(dAux.getId());

				categoryRelationRepository.save(categoryRelation);
			}

			final ObjectMapper objectMapper = new ObjectMapper();

			try {
				if (dashboard.getAuthorizations() != null) {
					final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
							objectMapper.getTypeFactory().constructCollectionType(List.class,
									DashboardAccessDTO.class));
					for (final Iterator<DashboardAccessDTO> iterator = access.iterator(); iterator.hasNext();) {
						final DashboardAccessDTO dashboardAccessDTO = iterator.next();
						final DashboardUserAccess dua = new DashboardUserAccess();
						dua.setDashboard(d);
						final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
								.findByName(dashboardAccessDTO.getAccesstypes());
						final DashboardUserAccessType managedType = managedTypes != null && managedTypes.size() > 0
								? managedTypes.get(0)
								: null;
						dua.setDashboardUserAccessType(managedType);
						dua.setUser(userRepository.findByUserId(dashboardAccessDTO.getUsers()));
						dashboardUserAccessRepository.save(dua);
					}
				}

			} catch (final JsonParseException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			} catch (final JsonMappingException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			} catch (final IOException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			}

			return d.getId();
		} else
			throw new DashboardServiceException("Dashboard already exists in Database");
	}

	@Override
	public List<DashboardUserAccess> getDashboardUserAccesses(String dashboardId) {
		final Dashboard dashboard = dashboardRepository.findById(dashboardId);
		final List<DashboardUserAccess> authorizations = dashboardUserAccessRepository.findByDashboard(dashboard);
		return authorizations;
	}

	@Transactional
	@Override
	public String cleanDashboardAccess(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExists(dashboard.getIdentification())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			final Dashboard d = dashboardRepository.findById(dashboard.getId());
			dashboardUserAccessRepository.deleteByDashboard(d);
			return d.getId();

		}
	}

	@Transactional
	@Override
	public String saveUpdateAccess(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExists(dashboard.getIdentification())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {

			final Dashboard d = dashboardRepository.findById(dashboard.getId());
			final ObjectMapper objectMapper = new ObjectMapper();

			try {
				if (dashboard.getAuthorizations() != null) {
					final List<DashboardAccessDTO> access = objectMapper.readValue(dashboard.getAuthorizations(),
							objectMapper.getTypeFactory().constructCollectionType(List.class,
									DashboardAccessDTO.class));
					for (final Iterator iterator = access.iterator(); iterator.hasNext();) {
						final DashboardAccessDTO dashboardAccessDTO = (DashboardAccessDTO) iterator.next();
						final DashboardUserAccess dua = new DashboardUserAccess();
						dua.setDashboard(dashboardRepository.findById(dashboard.getId()));
						final List<DashboardUserAccessType> managedTypes = dashboardUserAccessTypeRepository
								.findByName(dashboardAccessDTO.getAccesstypes());
						final DashboardUserAccessType managedType = managedTypes != null && managedTypes.size() > 0
								? managedTypes.get(0)
								: null;
						dua.setDashboardUserAccessType(managedType);
						dua.setUser(userRepository.findByUserId(dashboardAccessDTO.getUsers()));
						dashboardUserAccessRepository.save(dua);
					}
				}
				return d.getId();

			} catch (final JsonParseException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			} catch (final JsonMappingException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			} catch (final IOException e) {
				throw new DashboardServiceException(AUTH_PARSE_EXCEPT);
			}

		}
	}

	@Transactional
	@Override
	public String updatePublicDashboard(DashboardCreateDTO dashboard, String userId) {
		if (!dashboardExists(dashboard.getIdentification())) {
			throw new DashboardServiceException(DASH_NOT_EXIST);
		} else {
			final Dashboard d = dashboardRepository.findById(dashboard.getId());
			d.setPublic(dashboard.getPublicAccess());
			d.setDescription(dashboard.getDescription());
			try {
				if (dashboard.getImage() != null && !dashboard.getImage().isEmpty()) {
					d.setImage(dashboard.getImage().getBytes());
				} else {
					d.setImage(null);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			dashboardRepository.save(d);
			return d.getId();
		}
	}

	@Override
	public byte[] getImgBytes(String id) {
		final Dashboard d = dashboardRepository.findById(id);

		final byte[] buffer = d.getImage();

		return buffer;
	}

	@Override
	public List<Dashboard> getByUserId(String userId) {
		final User sessionUser = userRepository.findByUserId(userId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return dashboardRepository.findAllByOrderByIdentificationAsc();
		} else {
			return dashboardRepository.findByUser(sessionUser);
		}
	}

}