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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2014  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.streamsets.origin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minsait.onesait.platform.streamsets.Errors;
import com.minsait.onesait.platform.streamsets.GroupsOnesaitplatform;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperations;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperationsREST;
import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseSource;

/**
 * This target is an example and does not actually write to any destination.
 */
public class OnesaitplatformOrigin extends BaseSource {
	
	private static final Logger logger = LoggerFactory.getLogger(OnesaitplatformOrigin.class);

	public boolean noFinish=true;
	public String protocol;
	public String host;
	public Integer port;
	public String token;
	public String device;
	public String ontology;
	public String query;
	public String queryType;

	public OnesaitplatformOrigin(
			  String protocol,
			  String host,
		      Integer port,
		      String token,
		      String device,
		      String ontology,
		      String query,
		      String queryType
		  ){
		this.protocol = protocol;
		this.host=host;
		this.port=port;
		this.token=token;
		this.device=device;
		this.ontology=ontology;
		this.query=query;
		this.queryType=queryType;
		
	}
	
	private DeviceOperations deviceOperations;

	/** {@inheritDoc} */
	@Override
	protected List<ConfigIssue> init() {
		// Validate configuration values and open any required resources.
		List<ConfigIssue> issues = super.init();
		
		if (protocol.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Protocol required"));
		}

		if (host.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Host required"));
		}
		if (port.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Port required"));
		}
		if (token.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Token required"));
		}
		if (device.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Device required"));
		}
		if (ontology.equals("invalidValue")) {
			issues.add(getContext().createConfigIssue(GroupsOnesaitplatform.ONESAITPLATFORMO.name(), "config", Errors.ERROR_00,
					"Ontology required"));
		}
		// If issues is not empty, the UI will inform the user of each
		// configuration issue in the list.
		try {
			this.deviceOperations = new DeviceOperationsREST(protocol, host, port, token, device, null, null, null);
		} catch (Exception e) {
			logger.error("Error init rest operation ", e);
		}
		return issues;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		// Clean up any open resources.
		super.destroy();
	}

	/** {@inheritDoc} */
	@Override
	public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
		long nextSourceOffset = 0;
		if (noFinish){
			try{
				// Offsets can vary depending on the data source. Here we use an integer
				// as an example only.
				if (lastSourceOffset != null) {
					nextSourceOffset = Long.parseLong(lastSourceOffset);
				}
				
				String message = deviceOperations.query(ontology, query, queryType);
				//logger.info(message);
				JSONParser parser = new JSONParser();
				try {
					
					Object obj = parser.parse(message);
			        //JSONObject messageObj = (JSONObject) obj;
					
					JSONArray jsnarray = (JSONArray) obj;
					
					for (Object  jsnobject : jsnarray){
						Record record = getContext().createRecord(ontology + nextSourceOffset);
						//logger.info(ontology + nextSourceOffset);
						Map<String, Field> rootmap = new HashMap<>();
						
						for (Object key :((JSONObject)jsnobject).keySet()){
							//logger.info("key: " + key.toString());
							//logger.info("JSONString: " + ((JSONObject)jsnobject).toJSONString());
							//logger.info("JSONValue: " + ((JSONObject)jsnobject).get(key));
							
							if (!"_id".equals(key.toString()) && !"contextData".equals(key.toString())) {
								Object o = ((JSONObject)jsnobject).get(key);
								
								if (o instanceof Map){
									rootmap.put(key.toString(), Field.create(evaluateMap (o)));
								} else{
									
									Field campo = evaluateSingle(o);
									rootmap.put(key.toString(), campo);
								}
								
								/*
								Map<String, Field> map = evaluateMap(jsnonElement);
								rootmap.put(key.toString(), Field.create(map));
								*/
							}	
						}
						
						record.set(Field.create(rootmap));
						batchMaker.addRecord(record);
						++nextSourceOffset;

					}
				} catch (ParseException e) {
					logger.error("parsing query object ", e);
				}
				
			}catch(Exception e){
				logger.error("error produce ", e);
			} finally{
				try {
					this.deviceOperations.leave();
				} catch (Exception e) {
					logger.error("Error leave", e);
				}
			}
		}
		noFinish=false;
		return String.valueOf(nextSourceOffset);
	}
	
	private Map<String, Field> evaluateMap(Object jsnonElement){
		
		Map<String, Field> map= new HashMap<>();
		for (Object key : ((JSONObject)jsnonElement).keySet()){
	
			if (((JSONObject)jsnonElement).get(key) instanceof Map){
				map.put(key.toString(), Field.create(evaluateMap (((JSONObject)jsnonElement).get(key))));
			} else{
				
				Field campo = evaluateSingle(((JSONObject)jsnonElement).get(key));
				map.put(key.toString(), campo);
			}
		}
		return map;
	}
	
	private Field evaluateSingle(Object objeto){
		if (objeto instanceof Integer){
			return Field.create((int)objeto);
		}else if (objeto instanceof Float){
			return Field.create((Float)objeto);
		}else if (objeto instanceof Long){
			return Field.create((Long)objeto);
		}else if (objeto instanceof Boolean){
			return Field.create((boolean)objeto);
		}else {
			return Field.create((String)objeto);
		}
	}
}
