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
package com.minsait.onesait.platform.persistence.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.mongodb.client.MongoIterable;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UtilMongoDB {
	
	private static final String PROPERTIES_STR = "properties";
	private static final String INSERT_STR = "insert";
	private static final String D_FIND = ".find";
	private static final String D_COUNT = ".count";
	private static final String D_AGGREGATE = ".aggregate";
	private static final String D_DISTINCT = ".distinct";
	private static final String D_INSERT = ".insert";
	private static final String D_UPDATE = ".update";
	private static final String D_REMOVE = "insert";

	public String getQueryContent(String query) {
		if (query.contains("db.")) {
			String collection = getCollectionName(query);
			String auxName = query.replace("db." + collection + ".", "");
			auxName = auxName.substring(auxName.indexOf("(") + 1, auxName.indexOf(")"));
			if (auxName.equals(""))
				auxName = "{}";
			return auxName;
		} else {
			throw new DBPersistenceException(new Exception("No found db. in sentence"));
		}
	}

	public String getCollectionName(String query) {
		if (query.contains("db.")) {
			String auxName = query.replace("db.", "");
			auxName = auxName.substring(0, auxName.indexOf("."));
			return auxName;
		} else {
			throw new DBPersistenceException(new Exception("No found db. in sentence"));
		}
	}

	public String getCollectionName(String query, String pattern) {
		if (query.contains("db.")) {
			String auxName = query.replace("db.", "");
			auxName = auxName.substring(0, auxName.indexOf("." + pattern));
			return auxName;
		} else {
			throw new DBPersistenceException(new Exception("No found collection in query"));
		}
	}

	public String prepareQuotes(String query) {
		String result = query.trim();
		// newChar)
		if (!result.startsWith("{")) {
			StringBuffer resultObj = new StringBuffer(result);
			resultObj.insert(0, "{");
			resultObj.append("}");
			result = resultObj.toString();
		}
		return result;
	}

	public String getParentProperties(Map<String, Object> elements, Map<String, Object> schema) {
		for (Entry<String, Object> jsonElement : elements.entrySet()) {
			if (jsonElement.getValue() instanceof LinkedHashMap) {
				Map<String, Object> elementMap = (Map<String, Object>) jsonElement.getValue();
				Object ref = elementMap.get("$ref");
				if (ref != null) {
					String refScript = ((String) ref).replace("#/", "");
					// Es una referencia la recupero del raiz
					Object refObjet = schema.get(refScript);
					if (refObjet != null) {
						Map<String, Object> refObjectMap = (Map<String, Object>) refObjet;
						Map<String, Object> properties = (Map<String, Object>) refObjectMap.get(PROPERTIES_STR);
						if (null != properties && properties.containsKey("geometry")) {
							Map<String, Object> geometry = (Map<String, Object>) properties.get("geometry");
							if (geometry.containsKey(PROPERTIES_STR)) {
								Map<String, Object> propertiesGeometry = (Map<String, Object>) geometry
										.get(PROPERTIES_STR);
								if (propertiesGeometry.containsKey("type")
										&& propertiesGeometry.containsKey("coordinates")) {
									Map<String, Object> type = (Map<String, Object>) propertiesGeometry.get("type");
									Map<String, Object> coordinates = (Map<String, Object>) propertiesGeometry
											.get("coordinates");
									if (type.containsKey("enum") && coordinates.containsKey("type")) {
										log.debug("DEBUG.END", "getParentProperties");
										return jsonElement.getKey();
									}
								}
							} else if (geometry.containsKey("oneOf")) {
								geometry.get("oneOf");
								log.debug("DEBUG.END", "getParentProperties");
								return jsonElement.getKey();
							}
						}
					}
				}
			}
		}
		return "";
	}

	public String prepareEsquema(String esquemajson) {
		String esquemajsonAux = esquemajson;
		if (esquemajsonAux != null && esquemajsonAux.length() > 0) {
			esquemajsonAux = esquemajsonAux.replaceAll("&nbsp;", "");
			esquemajsonAux = esquemajsonAux.replaceAll("&amp;", "");
			esquemajsonAux = esquemajsonAux.replaceAll("&quot;", "\"");
		}
		if (esquemajsonAux != null) {
			esquemajsonAux = esquemajsonAux.replace("\t", "");
			esquemajsonAux = esquemajsonAux.replace("\r", "");
			esquemajsonAux = esquemajsonAux.replace("\n", "");
		}
		return esquemajsonAux;
	}

	public String getOntologyFromNativeQuery(String query) {
		String ontology = "";
		// .find or .count or .distinct or .aggregate
		if (query.toLowerCase().contains(D_FIND)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_FIND));
		} else if (query.toLowerCase().contains(D_COUNT)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_COUNT));
		} else if (query.toLowerCase().contains(D_AGGREGATE)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_AGGREGATE));
		} else if (query.toLowerCase().contains(D_DISTINCT)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_DISTINCT));
		} else if (query.toLowerCase().contains(D_INSERT)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_INSERT));
		} else if (query.toLowerCase().contains(D_UPDATE)) {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_UPDATE));
		} else {
			ontology = query.substring(query.indexOf('.') + 1, query.toLowerCase().indexOf(D_REMOVE));
		}
		return ontology;
	}

	public boolean isNativeQuery(String query) {
		boolean isNative = true;
		if ((query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_FIND) == -1
				|| (query.indexOf('.') > query.toLowerCase().indexOf(D_FIND)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_COUNT) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_COUNT)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_DISTINCT) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_DISTINCT)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_AGGREGATE) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_AGGREGATE)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_INSERT) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_INSERT)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_UPDATE) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_UPDATE)))
				&& (query.indexOf('.') == -1 || query.toLowerCase().indexOf(D_REMOVE) == -1
						|| (query.indexOf('.') > query.toLowerCase().indexOf(D_REMOVE)))) {
			isNative = false;
		}
		return isNative;
	}

	public String processInsert(String insert) {
		String pInsert = "";
		String insertAux = insert;
		try {
			String ontology = getCollectionName(insertAux, INSERT_STR);
			if (insertAux.contains("db." + ontology)) {
				insertAux = insertAux.replace("db." + ontology, "");
			}

			insertAux = insertAux.trim();
			if (insertAux.contains(INSERT_STR)) {
				insertAux = insertAux.substring(insertAux.indexOf(INSERT_STR) + 6).trim();
				if (insertAux.charAt(0) == '(') {
					insertAux = insertAux.substring(1).trim();
					if (insertAux.charAt(0) == '{') {
						pInsert = insertAux.substring(0, insertAux.lastIndexOf('}') + 1);
					}
				}
			}

		} catch (Exception e) {
			log.error("ERROR", e);
			throw new DBPersistenceException(e.getMessage());
		}
		return pInsert;
	}

//	public ContextData buildMinimalContextData() {
//		ContextData contextData = new ContextData();
//		contextData.setTimezoneId(CalendarAdapter.getServerTimezoneId());
//		return contextData;
//	}

	public <T> Collection<T> toJavaCollection(MongoIterable<T> iterable) {
		return toJavaList(iterable);
	}

	public <T> List<T> toJavaList(MongoIterable<T> iterable) {
		List<T> result = new ArrayList<T>();
		iterable.into(result);
		return result;
	}

	public <T> Map<String, T> toJavaMap(Document document, Class<T> valueType) {
		Map<String, T> result = new HashMap<String, T>();
		for (String key : document.keySet()) {
			result.put(key, document.get(key, valueType));
		}
		return result;
	}

	public String getObjectIdAsJson(ObjectId id) {
		if (id == null)
			return null;
		return "{\"_id\":{ \"$oid\":\"" + id.toString() + "\"}}";
	}

}
