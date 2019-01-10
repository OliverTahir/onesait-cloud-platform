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
package com.minsait.onesait.platform.persistence.util;

import java.io.File;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;

public class JSONPersistenceUtilsElasticSearch {
	
	private static final String PROPERTIES_STR = "properties";
	
	public static boolean isJSONSchema(String schemaString) {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(schemaString);
			Object o = jsonObj.get("$schema");
			return (o!=null);
		} catch (JSONException e) {
			return false;
		}
		
		
	}
	
	public static void main(String args[]) throws JSONException {
		String jsonPath = "src/main/resources/testschema.json";
		String list = ESInsertService.readAllBytes(jsonPath);
		
		String kk = getElasticSearchSchemaFromJSONSchema("pepe",list);
		System.out.println(kk);
		
		 jsonPath = "src/main/resources/testschema2.json";
		 list = ESInsertService.readAllBytes(jsonPath);
		
		 kk = getElasticSearchSchemaFromJSONSchema("pepe",list);
		System.out.println(kk);
		
		 jsonPath = "src/main/resources/testchema3.json";
		 list = ESInsertService.readAllBytes(jsonPath);
		
		 kk = getElasticSearchSchemaFromJSONSchema("pepe",list);
		System.out.println(kk);
	}
	
	public static boolean hasDatos(String schemaString) {

			try {
				JSONObject jsonObj = new JSONObject(schemaString);
				JSONObject pre = jsonObj.getJSONObject("datos");
				
				if (pre!=null) return true;
				else return false;
				
			} catch (JSONException e) {
				return false;
			}
	}
	
	public static String getElasticSearchSchemaFromJSONSchema(String ontology,String schemaString) throws JSONException {
		if (hasDatos(schemaString)) return getElasticSearchSchemaFromJSONSchemaDatos(ontology,schemaString);
		else return getElasticSearchSchemaFromJSONSchemaDatosNoDatos(ontology,schemaString);
	}
	
	
	public static String getElasticSearchSchemaFromJSONSchemaDatos(String ontology,String schemaString) throws JSONException {
		JSONObject jsonObj = new JSONObject(schemaString);
		JSONObject props = new JSONObject();
		JSONObject elasticSearch = new JSONObject();
		JSONObject theObject = new JSONObject();
		
		JSONObject pre = jsonObj.getJSONObject("datos");
		JSONObject prepre =  pre.getJSONObject(PROPERTIES_STR);
		
		Iterator it = prepre.keys();
		while (it.hasNext())
		{
			String key = (String)it.next();
			JSONObject o = (JSONObject)prepre.get(key);
			Object type= o.get("type");
			if (type instanceof String) {
				if ("string".equalsIgnoreCase((String)type)) type = "text";
				else if ("number".equalsIgnoreCase((String)type)) type = "float";
				else if ("object".equalsIgnoreCase((String)type)) {
					try {
						JSONArray enume = o.getJSONObject(PROPERTIES_STR).getJSONObject("type").getJSONArray("enum");
						String point = enume.getString(0);
						if ("Point".equalsIgnoreCase(point)) {
							type = "geo_point";
						}
						else type = "geo_shape";
						
					} catch (Exception e) {}
					
					try {
						JSONObject enume = o.getJSONObject(PROPERTIES_STR).getJSONObject("$date");
						if (enume!=null)
						{
							type="date";
						}
						
					} catch (Exception e) {}
				}
				o = new JSONObject();
				o.put("type", type);
			}
			props.put(key, o);
		}
		
		elasticSearch.put(PROPERTIES_STR, props);
		theObject.put(ontology, elasticSearch);
		return theObject.toString();
	}
	
	public static String getElasticSearchSchemaFromJSONSchemaDatosNoDatos(String ontology,String schemaString) throws JSONException {
		JSONObject jsonObj = new JSONObject(schemaString);
		JSONObject props = new JSONObject();
		JSONObject elasticSearch = new JSONObject();
		JSONObject theObject = new JSONObject();

		JSONObject prepre =  jsonObj.getJSONObject(PROPERTIES_STR);
		
		Iterator it = prepre.keys();
		while (it.hasNext())
		{
			String key = (String)it.next();
			JSONObject o = (JSONObject)prepre.get(key);
			Object type= o.get("type");
			if (type instanceof String) {
				if ("string".equalsIgnoreCase((String)type)) type = "text";
				else if ("number".equalsIgnoreCase((String)type)) type = "float";
				else if ("date".equalsIgnoreCase((String)type)) type = "date";
				else if ("timestamp".equalsIgnoreCase((String)type)) type = "date";
				else if ("object".equalsIgnoreCase((String)type)) {
					try {
						JSONArray enume = o.getJSONObject(PROPERTIES_STR).getJSONObject("type").getJSONArray("enum");
						String point = enume.getString(0);
						if ("Point".equalsIgnoreCase(point)) {
							type = "geo_point";
						}
						else type = "geo_shape";
						
					} catch (Exception e) {}
					
					try {
						JSONObject enume = o.getJSONObject(PROPERTIES_STR).getJSONObject("$date");
						if (enume!=null)
						{
							type="date";
						}
						
					} catch (Exception e) {}
				}
				o = new JSONObject();
				o.put("type", type);
			}
			props.put(key, o);
		}
		
		elasticSearch.put(PROPERTIES_STR, props);
		theObject.put(ontology, elasticSearch);
		return theObject.toString();
	}
	
	
	
}
