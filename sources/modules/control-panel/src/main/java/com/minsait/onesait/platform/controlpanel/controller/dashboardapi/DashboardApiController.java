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
package com.minsait.onesait.platform.controlpanel.controller.dashboardapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboardapi.dto.CommandDTO;
import com.minsait.onesait.platform.controlpanel.controller.dashboardapi.dto.MeasureDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboardapi")
@Controller
@Slf4j
public class DashboardApiController {

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private UserService userService;

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private OntologyService ontologyService;

	private final String SELECT_FROM = "select * from ";
	private final String RTDB = "RTDB";
	private final String QUERY = "query";
	private final String TREND = "trend";
	private final String PIETIMESSERIES = "pieTimesSeries";
	private final String MAP = "map";
	private final int MAXVALUES = 1000;

	@PostMapping(value = { "/createGadget" }, produces = "application/json")
	public @ResponseBody String createGadget(String json) {

		ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot create gadget: command Malformed");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getOntology() == null
					|| commandDTO.getInformation().getOntology().trim().length() == 0) {
				log.error("Cannot create gadget: ontology is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getDashboard() == null
					|| commandDTO.getInformation().getDashboard().trim().length() == 0) {
				log.error("Cannot create gadget: dashboard is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetName() == null) {
				log.error("Cannot create gadget: gadgetName is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getGadgetType() == null
					|| commandDTO.getInformation().getGadgetType().trim().length() == 0) {
				log.error("Cannot create gadget: gadgetType is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetType().equals(TREND)
					|| commandDTO.getInformation().getGadgetType().equals(PIETIMESSERIES)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAxes() == null) {
					log.error("Cannot create gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getAxes().getMeasuresX().size() == 0
						|| commandDTO.getInformation().getAxes().getMeasuresY().size() == 0) {
					log.error("Cannot create gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAssetsID() == null
						|| commandDTO.getInformation().getAssetsID().length == 0) {
					log.error("Cannot create gadget: At least one AssetsID is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
				}
			}

			if (commandDTO.getInformation().getRefresh() == null
					|| commandDTO.getInformation().getRefresh().trim().length() == 0) {
				log.error("Cannot create gadget: refresh is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}
			Gadget gadget = null;
			if (commandDTO.getInformation().getGadgetType().equals(TREND)) {
				gadget = createTrend(commandDTO);
			} else if (commandDTO.getInformation().getGadgetType().equals(PIETIMESSERIES)) {
				gadget = createPieTimeSeries(commandDTO);
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				gadget = createMap(commandDTO);
			}
			return "{\"requestcode\":\"newGadget\",\"status\":\"OK\", \"message\":\"properly created gadget\",\"data\":{\"id\":\""
					+ gadget.getId() + "\",\"type\":\"" + gadget.getType() + "\"}}";

		} catch (IOException e1) {
			log.error("Cannot create gadget", e1);
			return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot create gadget", e);
			return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		}

	}

	private Gadget createTrend(CommandDTO commandDTO) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String query = SELECT_FROM + ontologyIdentification;
		// String identificationDashboard = commandDTO.getInformation().getDashboard();
		String gadgetType = commandDTO.getInformation().getGadgetType();
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());

		User user = this.userService.getUser(this.utils.getUserId());
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);

		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(
				ontologyService.getOntologyByIdentification(ontologyIdentification, this.utils.getUserId()));
		datasource.setUser(user);

		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);

		// Creation gadget

		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		String configGadget = "";
		List<GadgetMeasure> measures = createGadgetAxes(commandDTO, gadgetType, user, gadget, configGadget);
		gadget = gadgetService.createGadget(gadget, datasource, measures);

		return gadget;
	}

	private Gadget updateTrend(CommandDTO commandDTO) {
		User user = this.userService.getUser(this.utils.getUserId());
		List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(this.utils.getUserId(),
				commandDTO.getInformation().getGadgetId());
		String idDataSource = "";
		for (Iterator iterator = listMeasures.iterator(); iterator.hasNext();) {
			GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
			idDataSource = gadgetMeasure.getDatasource().getId();
			break;
		}
		Gadget gadget = gadgetService.getGadgetById(this.utils.getUserId(), commandDTO.getInformation().getGadgetId());
		List<GadgetMeasure> measures = updateGadgetAxes(commandDTO, user);
		gadgetService.updateGadget(gadget, idDataSource, measures);

		return gadget;
	}

	private Gadget updatePieTimeSeries(CommandDTO commandDTO) {
		User user = this.userService.getUser(this.utils.getUserId());
		List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(this.utils.getUserId(),
				commandDTO.getInformation().getGadgetId());
		String idDataSource = "";
		for (Iterator iterator = listMeasures.iterator(); iterator.hasNext();) {
			GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
			idDataSource = gadgetMeasure.getDatasource().getId();
			break;
		}
		Gadget gadget = gadgetService.getGadgetById(this.utils.getUserId(), commandDTO.getInformation().getGadgetId());
		List<GadgetMeasure> measures = updateGadgetAxes(commandDTO, user);
		gadgetService.updateGadget(gadget, idDataSource, measures);
		return gadget;
	}

	private Gadget updateMap(CommandDTO commandDTO) {
		List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(this.utils.getUserId(),
				commandDTO.getInformation().getGadgetId());
		String idDataSource = "";
		for (Iterator iterator = listMeasures.iterator(); iterator.hasNext();) {
			GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
			idDataSource = gadgetMeasure.getDatasource().getId();
			break;
		}
		GadgetDatasource datasource = this.gadgetDatasourceService.getGadgetDatasourceById(idDataSource);
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String assetsList = getAssetsList(commandDTO);
		String query = "select c.asset.id as identifier,c.asset.name as name, c.asset.location.coordinates[0] as latitude , c.asset.location.coordinates[1] as longitude from "
				+ ontologyIdentification + " as c where c.asset.id in(" + assetsList + ")";
		datasource.setQuery(query);
		this.gadgetDatasourceService.updateGadgetDatasource(datasource);
		Gadget gadget = gadgetService.getGadgetById(this.utils.getUserId(), commandDTO.getInformation().getGadgetId());
		List<GadgetMeasure> measures = updateGadgetCoordinates(commandDTO);
		gadgetService.updateGadget(gadget, idDataSource, measures);
		return gadget;
	}

	private Gadget createPieTimeSeries(CommandDTO commandDTO) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String query = SELECT_FROM + ontologyIdentification;
		// String identificationDashboard = commandDTO.getInformation().getDashboard();
		String gadgetType = commandDTO.getInformation().getGadgetType();
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		User user = this.userService.getUser(this.utils.getUserId());
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(
				ontologyService.getOntologyByIdentification(ontologyIdentification, this.utils.getUserId()));
		datasource.setUser(user);
		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);
		// Creation gadget
		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		String configGadget = "";
		List<GadgetMeasure> measures = createGadgetAxes(commandDTO, gadgetType, user, gadget, configGadget);
		gadget = gadgetService.createGadget(gadget, datasource, measures);
		return gadget;
	}

	private Gadget createMap(CommandDTO commandDTO) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String assetsList = getAssetsList(commandDTO);
		String query = "select c.asset.id as identifier,c.asset.name as name, c.asset.location.coordinates[0] as latitude , c.asset.location.coordinates[1] as longitude from "
				+ ontologyIdentification + " as c where c.asset.id in(" + assetsList + ")";
		String gadgetType = commandDTO.getInformation().getGadgetType();
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());

		User user = this.userService.getUser(this.utils.getUserId());
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(
				ontologyService.getOntologyByIdentification(ontologyIdentification, this.utils.getUserId()));
		datasource.setUser(user);
		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);

		// Creation gadget

		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		String configGadget = "";
		List<GadgetMeasure> measures = createGadgetCoordinates(commandDTO, gadgetType, user, gadget, configGadget);
		gadget = gadgetService.createGadget(gadget, datasource, measures);

		return gadget;
	}

	private String getAssetsList(CommandDTO commandDTO) {
		String result[] = commandDTO.getInformation().getAssetsID().clone();
		for (int i = 0; i < result.length; i++) {
			result[i] = "\"" + result[i] + "\"";
		}

		return String.join(",", result);
	}

	private List<GadgetMeasure> createGadgetAxes(CommandDTO commandDTO, String gadgetType, User user, Gadget gadget,
			String configGadget) {
		// String axisLabelX = "";
		// String axisLabelY = "";
		if (gadgetType != null && gadgetType.equals(TREND)) {
			configGadget = "{" + "  \"chart\": {" + "    \"type\": \"lineWithFocusChart\"," + "    \"height\": 450,"
					+ "    \"margin\": {" + "      \"top\": 20," + "      \"right\": 20," + "      \"bottom\": 60,"
					+ "      \"left\": 40" + "    }," + "    \"duration\": 500,"
					+ "    \"useInteractiveGuideline\": true," + "    \"xAxis\": { }," + "    \"x2Axis\": {},"
					+ "    \"yAxis\": {\"rotateYLabel\": false" + "    }," + "    \"y2Axis\": {}" + "  }" + "}";
		} else if (gadgetType != null && gadgetType.equals(PIETIMESSERIES)) {
			configGadget = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"top\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":1}},\"elements\":{\"arc\":{\"borderWidth\":1,\"borderColor\":\"#fff\"}},\"maintainAspectRatio\":false,\"responsive\":true,\"responsiveAnimationDuration\":500,\"circumference\":\"6.283185307179586\",\"rotation\":\"6.283185307179586\",\"charType\":\"pie\"}";
		}
		// " + " \"axisLabel\": \"X Axis\" , " + " \"axisLabel\": \"Y Axis\",
		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();

		for (Iterator iterator = commandDTO.getInformation().getAxes().getMeasuresY().iterator(); iterator.hasNext();) {
			MeasureDTO measureDTOY = (MeasureDTO) iterator.next();
			for (Iterator iterator2 = commandDTO.getInformation().getAxes().getMeasuresX().iterator(); iterator2
					.hasNext();) {
				MeasureDTO measureDTOX = (MeasureDTO) iterator2.next();
				GadgetMeasure measure = new GadgetMeasure();
				String config = "{\"fields\": [\"" + measureDTOX.getPath() + "\",\"" + measureDTOY.getPath()
						+ "\"],\"name\":\"" + measureDTOY.getName() + "\",\"config\": {}}";
				measure.setConfig(config);
				measures.add(measure);
			}

		}
		return measures;
	}

	private List<GadgetMeasure> updateGadgetAxes(CommandDTO commandDTO, User user) {
		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();

		for (Iterator iterator = commandDTO.getInformation().getAxes().getMeasuresY().iterator(); iterator.hasNext();) {
			MeasureDTO measureDTOY = (MeasureDTO) iterator.next();
			for (Iterator iterator2 = commandDTO.getInformation().getAxes().getMeasuresX().iterator(); iterator2
					.hasNext();) {
				MeasureDTO measureDTOX = (MeasureDTO) iterator2.next();
				GadgetMeasure measure = new GadgetMeasure();
				String config = "{\"fields\": [\"" + measureDTOX.getPath() + "\",\"" + measureDTOY.getPath()
						+ "\"],\"name\":\"" + measureDTOY.getName() + "\",\"config\": {}}";
				measure.setConfig(config);
				measures.add(measure);
			}

		}
		return measures;
	}

	private List<GadgetMeasure> createGadgetCoordinates(CommandDTO commandDTO, String gadgetType, User user,
			Gadget gadget, String configGadget) {

		configGadget = "{\"center\":{\"lat\":31.952162238024975,\"lng\":5.625,\"zoom\":2},\"markersFilter\":\"identifier\",\"jsonMarkers\":\"\"}";
		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		GadgetMeasure measure = new GadgetMeasure();
		String config = "{\"fields\":[\"latitude\",\"longitude\",\"identifier\",\"name\"],\"name\":\"\",\"config\":{}}";
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

	private List<GadgetMeasure> updateGadgetCoordinates(CommandDTO commandDTO) {
		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		GadgetMeasure measure = new GadgetMeasure();
		String config = "{\"fields\":[\"latitude\",\"longitude\",\"identifier\",\"name\"],\"name\":\"\",\"config\":{}}";
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

	@PostMapping(value = { "/deleteGadget" }, produces = "application/json")
	public @ResponseBody String deleteGadget(String json) {
		return "";
	}

	@PostMapping(value = { "/updateGadget" }, produces = "application/json")
	public @ResponseBody String updateGadget(String json) {

		ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot update gadget: command Malformed");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getOntology() == null
					|| commandDTO.getInformation().getOntology().trim().length() == 0) {
				log.error("Cannot update gadget: ontology is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getDashboard() == null
					|| commandDTO.getInformation().getDashboard().trim().length() == 0) {
				log.error("Cannot update gadget: dashboard is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetName() == null) {
				log.error("Cannot update gadget: gadgetName is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getGadgetType() == null
					|| commandDTO.getInformation().getGadgetType().trim().length() == 0) {
				log.error("Cannot update gadget: gadgetType is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetType().equals(TREND)
					|| commandDTO.getInformation().getGadgetType().equals(PIETIMESSERIES)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAxes() == null) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getAxes().getMeasuresX().size() == 0
						|| commandDTO.getInformation().getAxes().getMeasuresY().size() == 0) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAssetsID() == null
						|| commandDTO.getInformation().getAssetsID().length == 0) {
					log.error("Cannot update gadget: At least one AssetsID is necessary ");
					return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
				}
			}

			if (commandDTO.getInformation().getRefresh() == null
					|| commandDTO.getInformation().getRefresh().trim().length() == 0) {
				log.error("Cannot update gadget: refresh is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getGadgetId() == null) {
				log.error("Cannot update gadget: id is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			Gadget gadget = null;
			if (commandDTO.getInformation().getGadgetType().equals(TREND)) {
				gadget = updateTrend(commandDTO);
			} else if (commandDTO.getInformation().getGadgetType().equals(PIETIMESSERIES)) {
				gadget = updatePieTimeSeries(commandDTO);
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				gadget = updateMap(commandDTO);
			}
			return "{\"requestcode\":\"updateGadget\",\"status\":\"OK\", \"message\":\"properly created gadget\",\"data\":{\"id\":\""
					+ gadget.getId() + "\",\"type\":\"" + gadget.getType() + "\"}}";

		} catch (IOException e1) {
			log.error("Cannot create gadget", e1);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot create gadget", e);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		}
	}

	@PutMapping(value = "/savemodel/{id}", produces = "application/json")
	public @ResponseBody String updateDashboardModel(@PathVariable("id") String id, String json) {

		dashboardService.saveDashboardModel(id, json, utils.getUserId());
		return "{\"ok\":true}";
	}

}
