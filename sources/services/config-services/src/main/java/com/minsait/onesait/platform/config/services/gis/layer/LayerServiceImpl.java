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
package com.minsait.onesait.platform.config.services.gis.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.exceptions.LayerServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class LayerServiceImpl implements LayerService {

	@Autowired
	private UserService userService;

	@Autowired
	private LayerRepository layerRepository;

	@Autowired
	OntologyRepository ontologyRepository;

	@Override
	public List<Layer> findAllLayers(String userId) {
		List<Layer> layers = new ArrayList<Layer>();
		final User sessionUser = userService.getUser(userId);

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			layers = layerRepository.findAll();
		} else {
			layers = layerRepository.findByUserOrIsPublicTrue(sessionUser);
		}

		return layers;
	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		List<Layer> layers = new ArrayList<Layer>();
		final User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			layers = layerRepository.findAllByOrderByIdentificationAsc();
		} else {
			layers = layerRepository.findByUserOrderByIdentificationAsc(user);
		}

		final List<String> identifications = new ArrayList<String>();
		for (final Layer layer : layers) {
			identifications.add(layer.getIdentification());

		}
		return identifications;
	}

	@Override
	public Ontology getOntologyByIdentification(String identification, String sessionUserId) {
		final Ontology ontology = ontologyRepository.findByIdentification(identification);
		return ontology;

	}

	@Override
	public void create(Layer layer) {
		layerRepository.save(layer);
	}

	@Override
	public Layer findById(String id, String userId) {
		User user = userService.getUser(userId);
		Layer layer = layerRepository.findById(id);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || layer.getUser().equals(user)
				|| layer.isPublic()) {
			return layer;
		} else {
			throw new LayerServiceException("The user is not authorized");
		}
	}

	@Override
	public void deleteLayer(Layer layer, String userId) {
		User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || layer.getUser().equals(user)
				|| layer.isPublic()) {
			layerRepository.delete(layer);
		} else {
			throw new LayerServiceException("The user is not authorized");
		}
	}

	@Override
	public Map<String, String> getOntologyGeometryFields(String identification, String sessionUserId)
			throws JsonProcessingException, IOException {
		Map<String, String> fields = new TreeMap<String, String>();
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		if (ontology != null) {
			final ObjectMapper mapper = new ObjectMapper();

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'"))
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
			}

			// Predefine Path to data properties
			if (!jsonNode.path("datos").path("properties").isMissingNode()) {

				jsonNode = jsonNode.path("datos").path("properties");

			} else
				jsonNode = jsonNode.path("properties");

			final Iterator<String> iterator = jsonNode.fieldNames();
			String property;
			Boolean hasCoordinates = false;
			Boolean hasType = false;
			while (iterator.hasNext()) {
				property = iterator.next();
				if (jsonNode.path(property).get("type").asText().equals("object")) {

					JsonNode jsonNodeAux = jsonNode.path(property).path("properties");

					if (!jsonNodeAux.path("coordinates").isMissingNode()
							&& jsonNodeAux.path("coordinates").get("type").asText().equals("array")
							&& jsonNodeAux.path("coordinates").path("items").size() == 2) {
						hasCoordinates = true;
					}
					if (!jsonNodeAux.path("type").isMissingNode()
							&& !jsonNodeAux.path("type").path("enum").isMissingNode()
							&& jsonNodeAux.path("type").get("enum").isArray()) {
						hasType = true;
					}
					if (hasCoordinates && hasType) {
						fields.put(property, jsonNodeAux.path("type").get("enum").get(0).asText());
					}

				}
			}
		}

		return fields;
	}

	@Override
	public Layer getLayerByIdentification(String identification, User user) {
		List<Layer> layers = layerRepository.findByIdentification(identification);
		if (!layers.isEmpty() && (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| layers.get(0).getUser().equals(user))) {
			return layers.get(0);
		}
		throw new LayerServiceException("The user is not authorized");
	}

	@Override
	public Boolean isLayerInUse(String layerId) {
		Layer layer = layerRepository.findById(layerId);
		if (layer.getViewers().isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public Layer findByIdentification(String layerIdentification) {
		return layerRepository.findByIdentification(layerIdentification).get(0);
	}

	@Override
	public Map<String, String> getLayersTypes(String userId) {
		Map<String, String> map = new HashMap<String, String>();
		List<Layer> layers = new ArrayList<Layer>();
		final User sessionUser = userService.getUser(userId);

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			layers = layerRepository.findAll();
		} else {
			layers = layerRepository.findByUserOrIsPublicTrue(sessionUser);
		}

		for (Layer layer : layers) {
			if (layer.getOntology() != null && !layer.isHeatMap()) {
				map.put(layer.getIdentification(), "iot");
			} else if (layer.getOntology() != null && layer.isHeatMap()) {
				map.put(layer.getIdentification(), "heat");
			} else if (layer.getExternalType().equalsIgnoreCase("wms")) {
				map.put(layer.getIdentification(), "wms");
			} else if (layer.getExternalType().equalsIgnoreCase("kml")) {
				map.put(layer.getIdentification(), "kml");
			}
		}

		return map;
	}

	@Override
	public String getLayerWms(String layerIdentification) {
		Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);
		return "{\"url\":\"" + layer.getUrl() + "\",\"layerWms\":\"" + layer.getLayerTypeWms() + "\"}";
	}

	@Override
	public String getLayerKml(String layerIdentification) {
		Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);
		return "{\"url\":\"" + layer.getUrl() + "\"}";
	}

}
