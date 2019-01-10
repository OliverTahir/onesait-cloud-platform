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
package com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

@RestController
@EnableAutoConfiguration
public class LayersRestImpl implements LayersRest {

	final private static String FEATURE = "Feature";
	final private static String FEATURE_COLLECTION = "FeatureCollection";

	@Autowired
	private UserService userService;

	@Autowired
	private LayerService layerService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private AppWebUtils utils;

	@Override
	public ResponseEntity<?> getByUserAndIdentification(
			@RequestParam(name = "layer", required = true) String identification) {
		try {

			String userId = utils.getUserId();
			if (userId != null) {
				User user = userService.getUser(userId);
				FeatureCollection featureCollection = new FeatureCollection();

				Layer layer = layerService.getLayerByIdentification(identification, user);

				if (layer != null && (layer.getUser().equals(user)
						|| layer.getUser().getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))) {

					Symbology symbology = buildSymbology(layer, identification);

					String root = getRootField(layer);

					String features = runQuery(userId, layer.getOntology().getIdentification());
					HeatMap heatMap = new HeatMap();
					try {

						JSONArray jsonArray = new JSONArray(features);
						List<Feature> featureList = new ArrayList<Feature>();
						if (jsonArray != null) {
							for (Integer i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								JSONObject rootObject = null;
								Feature feature = new Feature();

								if (root != null) {
									rootObject = obj.getJSONObject("0").getJSONObject(root);
									feature.setOid(obj.getJSONObject("0").getString("_id"));
								} else {
									rootObject = new JSONObject();
								}

								Map<String, String> mapProperties = new HashMap<String, String>();
								Geometry geometry = new Geometry();

								if (!layer.isHeatMap()) {
									JSONArray properties = new JSONArray(layer.getInfoBox());
									for (int x = 0; x < properties.length(); x++) {
										JSONObject json = properties.getJSONObject(x);

										String[] split = json.getString("field").split("\\.");
										JSONObject objAux = rootObject;
										for (int j = 0; j < split.length - 1; j++) {
											objAux = objAux.getJSONObject(split[j]);
										}
										Object value = objAux.get(split[split.length - 1]);
										mapProperties.put(json.getString("attribute"), value.toString());

									}
								} else {
									JSONObject objAux = rootObject;
									String value = objAux.getString(layer.getWeightField());
									mapProperties.put("value", value);

								}

								String[] split = layer.getGeometryField().split("\\.");
								JSONObject objAux = rootObject;
								for (int j = 0; j < split.length - 1; j++) {
									objAux = objAux.getJSONObject(split[j]);
								}

								JSONArray geo = objAux.getJSONObject(split[split.length - 1])
										.getJSONArray("coordinates");

								ArrayList<Object> listGeo = new ArrayList<Object>();
								for (int y = 0; y < geo.length(); y++) {
									try {
										JSONArray geoAux = geo.getJSONArray(y);
										ArrayList<Object> listGeoAux = new ArrayList<Object>();
										for (int z = 0; z < geoAux.length(); z++) {
											listGeoAux.add(geoAux.get(z));
										}
										listGeo.add(listGeoAux);
									} catch (JSONException e) {
										listGeo.add(geo.get(y));
									}
								}
								if (layer.getGeometryType().equals("Polygon")) {
									ArrayList<Object> geoFinal = new ArrayList<Object>();
									geoFinal.add(listGeo);
									geometry.setCoordinates(geoFinal);
								} else {
									geometry.setCoordinates(listGeo);
								}

								geometry.setType(layer.getGeometryType());

								feature.setProperties(mapProperties);
								feature.setType(FEATURE);
								feature.setGeometry(geometry);

								featureList.add(feature);

							}

							if (layer.isHeatMap()) {
								heatMap.setRadius(layer.getHeatMapRadius());
								heatMap.setMax(layer.getHeatMapMax());
								heatMap.setMin(layer.getHeatMapMin());
							}
						}
						featureCollection.setHeatMap(heatMap);
						featureCollection.setName(layer.getIdentification());
						featureCollection.setSymbology(symbology);
						featureCollection.setType(FEATURE_COLLECTION);
						if (layer.getGeometryType().equals("LineString")) {
							featureCollection.setTypeGeometry("Polyline");
						} else {
							featureCollection.setTypeGeometry(layer.getGeometryType());
						}

						featureCollection.setFeatures(featureList);

					} catch (JSONException e) {
						return new ResponseEntity<>("User has not permission over the ontology of the layer.",
								HttpStatus.UNAUTHORIZED);
					}
					return new ResponseEntity<>(featureCollection, HttpStatus.OK);
				} else {
					return new ResponseEntity<>("Layer not found for this user.", HttpStatus.NOT_FOUND);
				}

			} else {

				return new ResponseEntity<>("User is not found.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private Symbology buildSymbology(Layer layer, String identification) {
		try {
			String innerColor = layer.getInnerColor();
			ColorRGB innerColorRGB = new ColorRGB();
			String innerColorHex = null;
			String innerColorAlpha = null;
			if (innerColor.startsWith("#")) {
				innerColorHex = innerColor;
				Color color = Color.decode(innerColor);

				innerColorRGB.setBlue(color.getBlue());
				innerColorRGB.setRed(color.getRed());
				innerColorRGB.setGreen(color.getGreen());
				innerColorAlpha = "0.99";
			} else if (innerColor.startsWith("rgb")) {
				innerColor = innerColor.substring(innerColor.indexOf("(") + 1, innerColor.indexOf(")"));
				String[] split = innerColor.split(",");
				innerColorHex = String.format("#%02x%02x%02x", Integer.parseInt(split[0]), Integer.parseInt(split[1]),
						Integer.parseInt(split[2]));
				innerColorAlpha = split[3];

				Color color = Color.decode(innerColorHex);

				innerColorRGB.setBlue(color.getBlue());
				innerColorRGB.setRed(color.getRed());
				innerColorRGB.setGreen(color.getGreen());

			}

			String outerColor = layer.getOuterColor();
			ColorRGB outerColorRGB = new ColorRGB();
			String outerColorHex = null;
			String outerColorAlpha = null;
			if (outerColor.startsWith("#")) {
				outerColorHex = outerColor;
				Color color = Color.decode(outerColor);

				outerColorRGB.setBlue(color.getBlue());
				outerColorRGB.setRed(color.getRed());
				outerColorRGB.setGreen(color.getGreen());
				outerColorAlpha = "0.99";
			} else if (outerColor.startsWith("rgb")) {
				outerColor = outerColor.substring(outerColor.indexOf("("), outerColor.indexOf(")"));
				String[] split = outerColor.split(",");
				outerColorHex = String.format("#%02x%02x%02x", Integer.parseInt(split[0]), Integer.parseInt(split[1]),
						Integer.parseInt(split[2]));
				outerColorAlpha = split[3];

				Color color = Color.decode(outerColorHex);

				outerColorRGB.setBlue(color.getBlue());
				outerColorRGB.setRed(color.getRed());
				outerColorRGB.setGreen(color.getGreen());

			}

			Symbology symbology = new Symbology();

			if (layer.getGeometryType().equals("Polygon")) {
				symbology.setName("simbPolygonBasic");
			} else if (layer.getGeometryType().equals("LineString")) {
				symbology.setName("simbPolylineBasic");
			} else if (layer.getGeometryType().equals("Point")) {
				symbology.setName("simbPointBasic");
			}

			symbology.setPixelSize(layer.getSize());
			symbology.setInnerColorAlpha(innerColorAlpha);
			symbology.setInnerColorHEX(innerColorHex);
			symbology.setInnerColorRGB(innerColorRGB);
			symbology.setOutlineColorHEX(outerColorHex);
			symbology.setOutlineColorRGB(outerColorRGB);
			symbology.setOuterColorAlpha(outerColorAlpha);
			symbology.setPixelSize(layer.getSize());
			symbology.setOutlineWidth(layer.getOuterThin());

			return symbology;
		} catch (Exception e) {
			return null;
		}
	}

	private String getRootField(Layer layer) {
		try {
			String schema = layer.getOntology().getJsonSchema();
			JSONObject jsonschema = new JSONObject(schema);
			Iterator<String> iterator = jsonschema.keys();
			String root = null;
			while (iterator.hasNext()) {
				String prop = iterator.next();
				try {
					Iterator<String> iteratorAux = jsonschema.getJSONObject(prop).keys();
					while (iteratorAux.hasNext()) {
						String p = iteratorAux.next();
						if (jsonschema.getJSONObject(prop).getJSONObject(p).has("$ref")) {
							root = p;
							break;
						}
					}
				} catch (Exception e) {
				}
			}

			return root;
		} catch (JSONException e) {
			return null;
		}
	}

	private String runQuery(String userId, String ontologyIdentification) {

		if (ontologyService.hasUserPermissionForQuery(userId, ontologyIdentification)) {
			final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
			if (manageDB.getListOfTables4Ontology(ontologyIdentification).size() == 0) {
				manageDB.createTable4Ontology(ontologyIdentification, "{}");
			}

			String queryResult = queryToolService.querySQLAsJson(userId, ontologyIdentification,
					"select c, c._id from " + ontologyIdentification + " as c", 0);

			return queryResult;

		} else
			return utils.getMessage("querytool.ontology.access.denied.json",
					"You don't have permissions for this ontology");
	}

}
