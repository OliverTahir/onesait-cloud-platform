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
package com.minsait.onesait.platform.streamsets.connection;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.streamsets.connection.DeviceOperations;
import com.minsait.onesait.platform.streamsets.destination.InstancesStt;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesConfig;
import com.minsait.onesait.platform.streamsets.destination.ontology.OnesaitplatformOntology;
import com.minsait.onesait.platform.streamsets.destination.beans.OntologyProcessInstance;
import com.streamsets.pipeline.api.Record;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DeviceOperationsREST  implements DeviceOperations {

    private static final Logger log = LoggerFactory.getLogger(DeviceOperationsREST.class);
	
	private String protocol;
	private String host;
	private String token;
	private int port;
	
    private String path;
    private String clientPlatform;
    private boolean connected = false;
    private String sessionKey;
    private String clientPlatformId;
    
    private String rootNode;
    OntologyProcessInstance ontologyProcessInstance;
    private TimeseriesConfig tsConfig;

    private static final String joinTemplate = "/rest/client/join?token=%s&clientPlatform=%s&clientPlatformId=%s";
    private static final String leaveTemplate = "/rest/client/leave";
    private static final String insertTemplate = "/rest/ontology/%s";
    private static final String queryTemplate = "/rest/ontology/%s?query=%s&queryType=%s";
	private static final String updateTemplate = "/rest/ontology/%s/update?ids=false";
	
	//String Response when 0 updated records and 200
	private static final String responseNoUpdate = "{\"nModified\":0}";
	
	public DeviceOperationsREST(String protocol, String host, Integer port, String token, String instance, TimeseriesConfig tsConfig, String rootNode, OntologyProcessInstance ontologyProcessInstance){
		this.protocol = protocol;
		this.host=host;
		this.port=port;
		this.clientPlatformId = instance;
		this.token=token;
		this.path = getUrl();
		this.tsConfig = tsConfig;
		this.rootNode = rootNode;
		this.ontologyProcessInstance = ontologyProcessInstance;
		this.doJoin(clientPlatformId, token);
	}
	
	private String getUrl () {
		return protocol + "://"+host+":"+port+"/iot-broker";
	}
	
	@Override
	public void leave() throws Exception {
				
		this.doLeave();	
		
	}
	
	@Override
	public String query(String ontology, String query, String queryType) throws Exception{
		log.info("ontology= " + ontology + " query= " + query + " queryType= " + queryType);
		String result = this.doQuery(generateURLQuery(ontology, query, queryType));
		return result;	
	}
	
	@Override
	public LinkedList<ErrorResponseOriginalRecord> insert(InstancesStt message, String ontology){
		LinkedList<ErrorResponseOriginalRecord> leror;
		if(tsConfig != null) {
			leror = new LinkedList<ErrorResponseOriginalRecord>();
			for(int i=0;i<message.getInsertableRest().size();i++) {
				leror.addAll(this.doUpdate(ontology, message.getInsertableRest().get(i), message.getOriginalValues().get(i)));
			}
		}
		else {
			leror = this.doInsert(ontology, message.getInsertableRest().toString(),message.getOriginalValues());
		}
		return leror;
		
	}
	
	
    
    public boolean isConnected() {
    	return this.connected;
    }
    
    public boolean doJoin(String client, String token) {
        try{
        	this.token = token;
        	this.clientPlatform = client;
            log.info("Doing Join");
            String join = String.format(joinTemplate, this.token , this.clientPlatform, this.clientPlatform + ":" + UUID.randomUUID().toString());
            log.info("Join sentence: " + join);
            String resultStr = callRestAPI(join, "GET");
            log.info("Join response: " + resultStr);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonTree = jsonParser.parse(resultStr).getAsJsonObject();
            
            this.sessionKey = jsonTree.get("sessionKey").getAsString();
            log.info("SessionKey is " + this.sessionKey);
            log.info("Join Ok");
            this.connected = true;
        }
        catch(Exception e){
            log.error("Error en Join: " + e.getMessage());
            return false;
        }
        return true;
    }
    public boolean doLeave() {
        try{
            log.info("Doing Leave");
            String leave = String.format(leaveTemplate);
            log.info("Leave sentence: " + leave);
            callRestAPI(leave, "GET");
            log.info("Leave Ok");
        }
        catch(Exception e){
            log.error("Error en Leave: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public String generateURLQuery(String ontology, String query, String queryType) throws UnsupportedEncodingException {
        log.info("Generating Query");
        query = String.format(queryTemplate, ontology, URLEncoder.encode(query, "UTF-8"), queryType);
        log.info("Query sentence: " + query);
        return query;
    }
    public String doQuery(String query){
        try{
            log.info("Doing Query: " + query);
            String resultResponse = callRestAPI(query, "GET");
            log.info("Response: " + resultResponse.length());
            return resultResponse;
        }
        catch(Exception e){
            log.error("Error en query: " + e.getMessage());
            return null;
        }
        
    }
    
    public void singleInsert(String ontology, String instances) throws Exception{
    	log.info("Doing Insert in " + ontology );
    	ResponseRest result = callRestAPI(generateURLInsert(ontology), "POST", instances);
    	if(result.getResCode()/100!=2) {
    		throw new Exception("Error code not 2xx, " + result.getResCode() + ", " + result.getResponseText());
    	}
    	else {
    		log.info("Update TS Ok");
    	}
    }
    
    public void singleUpdate(String ontology, String query, List<Record> originalValues) throws Exception{
        ResponseRest result = callRestAPI(generateURLUpdate(ontology), "PUT", query);
        //log.info("Response: " + result.getResponseText());
        if(responseNoUpdate.equals(result.getResponseText())) {
        	log.info("Creating base instance");
        	insertBaseTSInstance(ontology, originalValues.get(0));
        	result = callRestAPI(generateURLUpdate(ontology), "PUT", query);
        }
        else {
        	if(result.getResCode()/100!=2) {
        		throw new Exception("Error code not 2xx, " + result.getResCode() + ", " + result.getResponseText());
        	}
        	else {
        		log.info("Update TS Ok");
        	}        	
        }
    }
    
    private void insertBaseTSInstance(String ontology, Record originalValue) throws Exception{
    	singleInsert(ontology, OnesaitplatformOntology.instanceToBaseInsert(originalValue, ontology, this.rootNode, tsConfig, ontologyProcessInstance));
    } 
    
    public LinkedList<ErrorResponseOriginalRecord> doUpdate(String ontology, String instances, List<Record> originalValues) {
    	LinkedList<ErrorResponseOriginalRecord> leror = new LinkedList<ErrorResponseOriginalRecord>();
    	try{    
            singleUpdate(ontology, instances, originalValues);
        }
        catch(Exception e){
            log.error("Error in update: " + e.getMessage());
            log.info("Retry update with new sessionKey");
            doLeave();
            if(doJoin(this.clientPlatform, this.token)) {
            	try{    
                    singleUpdate(ontology, instances, originalValues);
                }
            	catch(Exception e2){
            		log.error("Error in retry update: " + e2.getMessage());
            		for(Record record : originalValues) {
            			leror.add(new ErrorResponseOriginalRecord(record, e2.getMessage()));
            		}
            	}
            }else {
            	log.error("Error in try join again to platform");
            	for(Record record : originalValues) {
        			leror.add(new ErrorResponseOriginalRecord(record, "Error join to platform after error in update: " + e));
        		}
            }
        }
    	return leror;
    }
    
    public LinkedList<ErrorResponseOriginalRecord> doInsert(String ontology, String instances, LinkedList<LinkedList<Record>> lrds){
    	LinkedList<ErrorResponseOriginalRecord> leror = new LinkedList<ErrorResponseOriginalRecord>();
    	try{    
            singleInsert(ontology, instances);
        }
        catch(Exception e){
            log.error("Error in insert: " + e);
            log.info("Retry insert with new sessionKey");
            doLeave();
            if(doJoin(this.clientPlatform, this.token)) {
            	try{    
                    singleInsert(ontology, instances);
                }
            	catch(Exception e2){
            		log.error("Error in retry insert: " + e2.getMessage());
            		for(List<Record> lrd : lrds) {
            			leror.add(new ErrorResponseOriginalRecord(lrd.get(0), e2.getMessage()));
            		}
            	}
            }
            else {
            	log.error("Error in try join again to platform");
            	for(List<Record> lrd : lrds) {
        			leror.add(new ErrorResponseOriginalRecord(lrd.get(0), "Error join to platform after error in insert: " + e));
        		}
            }
        }
    	return leror;
    }
    
    private String generateURLInsert(String ontology){
        return String.format(insertTemplate, ontology);
    }
    
    private String generateURLUpdate(String ontology){
        return String.format(updateTemplate, ontology);
    }
    
    private String callRestAPI(String targetURL, String method) throws Exception {
        return callRestAPI(targetURL, method, "").getResponseText();
    }
    
    private void addAuthorizationHeader(HttpRequest http) {
    	http.addHeader("Authorization",this.sessionKey);
    }
    
    private ResponseRest callRestAPI(String targetURL, String method, String jsonData) throws ClientProtocolException, IOException  {
        log.info("Call rest api in {}, method: {}, jsonData: {}", targetURL, method, jsonData);
        
        RequestConfig requestConfig = RequestConfig.custom().build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        String result = null;
        HttpResponse httpResponse = null;
        try {
            HttpRequest http = null;
            HttpHost httpHost = new HttpHost(this.host, this.port);
            StringEntity entity = new StringEntity(jsonData);
            
            if("".equals(targetURL)){
                targetURL = this.path;
            }
            else{
                targetURL = this.path + targetURL;
            }
            switch (method) {
            case "GET":
                http = new HttpGet(targetURL);
                break;
            case "POST":
                http = new HttpPost(targetURL);
                ((HttpPost) http).setEntity(entity);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");
                break;
            case "PUT":
                http = new HttpPut(targetURL);
                ((HttpPut) http).setEntity(entity);
                http.setHeader("Accept", "application/json");
                http.setHeader("Content-type", "application/json");
                break;
            case "DELETE":
                http = new HttpDelete(targetURL);
                break;
            }
            addAuthorizationHeader(http);
            // Execute HTTP request
            httpResponse = httpClient.execute(httpHost, http);
            if(httpResponse.getStatusLine().getStatusCode()/100==2) {
            	log.info("----------------------------------------");
            	log.info(httpResponse.getStatusLine().toString());
            	log.info("----------------------------------------");
            }
            else {
            	log.error("----------------------------------------");
            	log.error(httpResponse.getStatusLine().toString());
            	log.error("----------------------------------------");
            }
            
            // Get hold of the response entity
            HttpEntity entityResponse = httpResponse.getEntity();
            result = EntityUtils.toString(entityResponse, "UTF-8");
            
        } catch (ClientProtocolException e) {
        	httpClient.close();
        	throw new ClientProtocolException(e);
        } catch (IOException e) {
        	httpClient.close();
        	throw new IOException(e);
        } finally {
			httpClient.close();            
        }
        log.info("Response of: " + result.length() + " chars");
        return new ResponseRest(httpResponse.getStatusLine().getStatusCode(), result);
    }
}
