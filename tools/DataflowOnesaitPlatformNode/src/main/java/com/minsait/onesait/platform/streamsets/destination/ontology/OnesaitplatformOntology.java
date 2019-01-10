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
package com.minsait.onesait.platform.streamsets.destination.ontology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.lang.Float.NaN;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minsait.onesait.platform.streamsets.destination.OnesaitplatformTarget;
import com.minsait.onesait.platform.streamsets.destination.beans.OntologyProcessInstance;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesConfig;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;

import _ss_org.apache.commons.lang3.reflect.FieldUtils;

public class OnesaitplatformOntology {
	
	private static final Logger logger = LoggerFactory.getLogger(OnesaitplatformTarget.class);
	
	private static String setValueSecondTemplate = "\"%1$s.%2$s.%3$d.%2$s.%4$d.%2$s.%5$d\":%6$f";
	private static String setValueMinuteTemplate = "\"%1$s.%2$s.%3$d.%2$s.%4$d\":%5$f";
	private static String setCountMinuteTemplate = "\"%1$s.%2$s.%3$d.%2$s.%4$d.%5$s\":%6$d";
	private static String setCountHourTemplate = "\"%1$s.%2$s.%3$d.%4$s\":%5$d";
	private static String setCountDayTemplate = "\"%1$s.%2$s\":%3$d";
	private static String setSumMinuteTemplate = "\"%1$s.%2$s.%3$d.%2$s.%4$d.%5$s\":%6$f";
	private static String setSumHourTemplate = "\"%1$s.%2$s.%3$d.%4$s\":%5$f";
	private static String setSumDayTemplate = "\"%1$s.%2$s\":%3$f";
	
	public static String constructOntologyInstance(Record record, OntologyProcessInstance ontologyProcessInstance, String ontology, String customRootNode) throws Exception {
		
		JSONObject json=null;
		
		switch(ontologyProcessInstance) {
			case NOROOTNODE:
				json = (JSONObject) constructBody(record.get());
				break;
			case CUSTOMNAME:
				json = new JSONObject();
				json.put(customRootNode, constructBody(record.get()));
				break;
			case ONTOLOGYNAME:
				json = new JSONObject();
				json.put(ontology, constructBody(record.get()));
				break;
		}
		return json.toJSONString();
	}
	
	private static void constructUpdateUntilSet(StringBuilder stb, String rootNode, String ontology, String customRootNode, TimeseriesConfig tsConfig, Map<String, Field> fieldvalue, DateTime dt) {
		stb.append("db.");
		stb.append(ontology);
		stb.append(".update({");
		
		for (String updField : tsConfig.getUpdateFields()) {
			stb.append("\"" + rootNode + updField + "\": " + constructBodyFieldQuery(fieldvalue.get(updField)) + ",");
		}
		stb.append("\"" + rootNode + tsConfig.getTsTimeField() + "\":{\"$date\":\"" + formatToIsoDateDay(dt) + "\"}");
		stb.append("},");
	}
	
	private static void constructUpdateSetAndPrecalcs(StringBuilder stb, LinkedList<Record> records, String fixedValuePath, int secondStepRef, TimeseriesConfig tsConfig, Map<Integer,Float> precalcSum, Map<Integer,Integer> precalcCount) {
		//Day precalcs
		if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
			precalcSum.put(10000, 0f);
		}
		if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
			precalcCount.put(10000, 0);
		}
		if(secondStepRef != -1) {
			Iterator<Record> it = records.iterator();
			while(it.hasNext()) {
				Map<String, Field> fieldvalue = (Map<String, Field>) it.next().get().getValue();
				
				Field timestamp = fieldvalue.get(tsConfig.getTsTimeField());
				DateTime dt = new DateTime(timestamp.getValueAsDatetime());
				
				int hour = dt.getHourOfDay();
				int minute = dt.getMinuteOfHour();
				int second = dt.getSecondOfMinute();
				int minuteStep = minute/tsConfig.getMinuteStep();
				int secondStep = -1;
				
				if(secondStepRef!=-1) {
					secondStep = second/secondStepRef;
				}
				Float vnumber = fieldvalue.get(tsConfig.getOriginTimeseriesValueField()).getValueAsFloat();
				if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
					int hourKey = hour*100;
					int minuteKey = hour*100 + minuteStep*minute + 1;
					Float vsh = precalcSum.get(hourKey);
					Float vsm = precalcSum.get(minuteKey);
					if(vsh!=null) {
						precalcSum.put(hourKey, vsh + vnumber);
					}
					else {
						precalcSum.put(hourKey, vnumber);
					}
					if(vsm!=null) {
						precalcSum.put(minuteKey, vsm + vnumber);
					}
					else {
						precalcSum.put(minuteKey, vnumber);
					}
					precalcSum.put(10000,precalcSum.get(10000) + vnumber);
				}
				if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
					int hourKey = hour*100;
					int minuteKey = hour*100 + minuteStep*minute + 1;
					Integer vch = precalcCount.get(hourKey);
					Integer vcm = precalcCount.get(minuteKey);
					if(vch!=null) {
						precalcCount.put(hourKey, vch+1);
					}
					else {
						precalcCount.put(hourKey, 1);
					}
					if(vcm!=null) {
						precalcCount.put(minuteKey, vcm+1);
					}
					else {
						precalcCount.put(minuteKey, 1);
					}
					precalcCount.put(10000,precalcCount.get(10000) + 1);
				}
				
				stb.append(String.format(setValueSecondTemplate, fixedValuePath, tsConfig.getValueTimeseriesField(), hour, minuteStep, secondStep, vnumber));
				if(it.hasNext()) {
					stb.append(",");
				}
			}
		}
		else {
			Iterator<Record> it = records.iterator();
			while(it.hasNext()) {
				Map<String, Field> fieldvalue = (Map<String, Field>) it.next().get().getValue();
				
				Field timestamp = fieldvalue.get(tsConfig.getTsTimeField());
				DateTime dt = new DateTime(timestamp.getValueAsDatetime());
				
				int hour = dt.getHourOfDay();
				int minute = dt.getMinuteOfHour();
				int minuteStep = minute/tsConfig.getMinuteStep();				
				
				Float vnumber = fieldvalue.get(tsConfig.getOriginTimeseriesValueField()).getValueAsFloat();
				if(tsConfig.getPrecalcSumTimeseriesField() !=null) {					
					int hourKey = hour*100;
					Float vsh = precalcSum.get(hourKey);
					if(vsh!=null) {
						precalcSum.put(hourKey, vsh + vnumber);
					}
					else {
						precalcSum.put(hourKey, vnumber);
					}
					precalcSum.put(10000,precalcSum.get(10000) + vnumber);
				}
				if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
					int hourKey = hour*100;
					Integer vch = precalcCount.get(hourKey);
					if(vch!=null) {
						precalcCount.put(hourKey, vch+1);
					}
					else {
						precalcCount.put(hourKey, 1);
					}
					precalcCount.put(10000,precalcCount.get(10000) + 1);
				}
				stb.append(String.format(setValueMinuteTemplate, fixedValuePath, tsConfig.getValueTimeseriesField(), hour, minuteStep, vnumber));
				if(it.hasNext()) {
					stb.append(",");
				}
			}
		}
	}
	
	private static void constructUpdateIncWithPrecals(StringBuilder stb, String fixedValuePath, TimeseriesConfig tsConfig, Map<Integer,Float> precalcSum, Map<Integer,Integer> precalcCount) {
		if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
			Iterator<Integer> ipc = precalcCount.keySet().iterator();
			while(ipc.hasNext()) {
				Integer pck = ipc.next();
				Integer pcv = precalcCount.get(pck);
				int partHour = pck/100;
				int partMinute = (pck % 100 == 0 ? -1 : (pck%100-1));
				//hour-minute $inc
				
				if(partMinute!=-1) {
					stb.append(String.format(setCountMinuteTemplate,fixedValuePath,tsConfig.getValueTimeseriesField(),partHour,partMinute,tsConfig.getPrecalcCountTimeseriesField(),pcv));
				}
				else if(pck == 10000) {//Day inc
					stb.append(String.format(setCountDayTemplate,fixedValuePath,tsConfig.getPrecalcCountTimeseriesField(),pcv));
				}//hour $inc
				else{
					stb.append(String.format(setCountHourTemplate,fixedValuePath,tsConfig.getValueTimeseriesField(),partHour,tsConfig.getPrecalcCountTimeseriesField(),pcv));
				}
				if(ipc.hasNext() || tsConfig.getPrecalcSumTimeseriesField() !=null) {
					stb.append(",");
				}
			}
		}
		if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
			Iterator<Integer> ipc = precalcSum.keySet().iterator();
			while(ipc.hasNext()) {
				Integer psk = ipc.next();
				Float psv = precalcSum.get(psk);
				int partHour = psk/100;
				int partMinute = (psk % 100 == 0 ? -1 : (psk%100-1));
				//hour-minute $inc
				if(partMinute!=-1) {
					stb.append(String.format(setSumMinuteTemplate,fixedValuePath,tsConfig.getValueTimeseriesField(),partHour,partMinute,tsConfig.getPrecalcSumTimeseriesField(),psv));
				}
				else if(psk == 10000) {//Day inc
					stb.append(String.format(setSumDayTemplate,fixedValuePath,tsConfig.getPrecalcSumTimeseriesField(),psv));
				}//hour $inc
				else {
					stb.append(String.format(setSumHourTemplate,fixedValuePath,tsConfig.getValueTimeseriesField(),partHour,tsConfig.getPrecalcSumTimeseriesField(),psv));
				}
				if(ipc.hasNext()) {
					stb.append(",");
				}
			}
		}
	}
	
	public static String constructMultiUpdate(LinkedList<Record> records, OntologyProcessInstance ontologyProcessInstance, String ontology, String customRootNode, TimeseriesConfig tsConfig) {
		Map<String, Field> fieldvalueRef = (Map<String, Field>) records.element().get().getValue();
		Field timestamp = fieldvalueRef.get(tsConfig.getTsTimeField());
		DateTime dt = new DateTime(timestamp.getValueAsDatetime());
		
		int hour = dt.getHourOfDay();
		int minute = dt.getMinuteOfHour();
		int second = dt.getSecondOfMinute();
		int minuteStep = minute/tsConfig.getMinuteStep();
		int secondStepRef = tsConfig.getSecondStep();
		int secondStep = -1;
		
		if(secondStepRef!=-1) {
			secondStep = second/secondStepRef;
		}
		
		StringBuilder stb = new StringBuilder();
		String rootNode;
		
		switch(ontologyProcessInstance) {
			case NOROOTNODE:
				rootNode="";
				break;
			case CUSTOMNAME:
				rootNode=customRootNode + ".";
				break;
			case ONTOLOGYNAME:
				rootNode=ontology + ".";
				break;
			default:
				rootNode="";
		}
		
		constructUpdateUntilSet(stb, rootNode, ontology, rootNode, tsConfig, fieldvalueRef, dt);
		
		//precalcSum, precalcCount to accumulate partials sums, counts in different levels of timeseries, the key is hour*100 or hour*100 + minutes + 1 in order to difference hour 0 sum, count a hour 0 min 0 sum 
		Map<Integer,Float> precalcSum = new HashMap<Integer,Float>();
		Map<Integer,Integer> precalcCount = new HashMap<Integer,Integer>();
		String fixedValuePath = rootNode + tsConfig.getDestinationTimeseriesValueField();
		
		stb.append("{$set:{");
		
		constructUpdateSetAndPrecalcs(stb, records, fixedValuePath, secondStepRef, tsConfig, precalcSum, precalcCount);
		
		stb.append("}");
		
		if(tsConfig.getPrecalcCountTimeseriesField() != null || tsConfig.getPrecalcSumTimeseriesField() != null) {
			stb.append(",$inc:{");
			
			constructUpdateIncWithPrecals(stb, fixedValuePath, tsConfig, precalcSum, precalcCount);
			
			stb.append("}");
		}
		stb.append("})");
		return stb.toString();
	}
	
	public static String constructUpdate(Record record, OntologyProcessInstance ontologyProcessInstance, String ontology, String customRootNode, TimeseriesConfig tsConfig) {
		Map<String, Field> fieldvalue = (Map<String, Field>) record.get().getValue();
		Field timestamp = fieldvalue.get(tsConfig.getTsTimeField());
		DateTime dt = new DateTime(timestamp.getValueAsDatetime());
		
		int hour = dt.getHourOfDay();
		int minute = dt.getMinuteOfHour();
		int second = dt.getSecondOfMinute();
		int minuteStep = minute/tsConfig.getMinuteStep();
		int secondStepRef = tsConfig.getSecondStep();
		int secondStep = -1;
		
		if(secondStepRef!=-1) {
			secondStep = second/secondStepRef;
		}
		
		StringBuilder stb = new StringBuilder();
		String rootNode;
		
		switch(ontologyProcessInstance) {
			case NOROOTNODE:
				rootNode="";
				break;
			case CUSTOMNAME:
				rootNode=customRootNode + ".";
				break;
			case ONTOLOGYNAME:
				rootNode=ontology + ".";
				break;
			default:
				rootNode="";
		}
		
		constructUpdateUntilSet(stb, rootNode, ontology, rootNode, tsConfig, fieldvalue, dt);
		
		if(secondStepRef != -1) {
			stb.append(rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getValueTimeseriesField() + minuteStep + "." + tsConfig.getValueTimeseriesField() + "." + secondStep + "\":" + constructBodyFieldQuery(fieldvalue.get(tsConfig.getOriginTimeseriesValueField())) + "}");
		}
		else {
			stb.append(rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getValueTimeseriesField() + "." + minuteStep + "\":" + constructBodyFieldQuery(fieldvalue.get(tsConfig.getOriginTimeseriesValueField())) + "}");
		}
		if(tsConfig.getPrecalcCountTimeseriesField() != null || tsConfig.getPrecalcSumTimeseriesField() != null) {
			stb.append(",$inc:{");
			if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
				//stb.append("\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + tsConfig.getPrecalcCountTimeseriesField() + "\":1,");
				stb.append("\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getPrecalcCountTimeseriesField() + "\":1");
				if(secondStepRef != -1) {
					stb.append(",\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getValueTimeseriesField() + "." + minute + "." + tsConfig.getValueTimeseriesField() + "." + tsConfig.getPrecalcCountTimeseriesField() + "\":1");
				}
				if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
					stb.append(",");
				}
			}
			if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
				//stb.append("\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + tsConfig.getPrecalcSumTimeseriesField() + "\": " + constructBodyFieldQuery(fieldvalue.get(tsConfig.getOriginTimeseriesValueField())) + ",");
				stb.append("\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getPrecalcSumTimeseriesField() + "\": " + constructBodyFieldQuery(fieldvalue.get(tsConfig.getOriginTimeseriesValueField())));
				if(secondStepRef != -1) {
					stb.append(",\"" + rootNode + tsConfig.getDestinationTimeseriesValueField() + "." + tsConfig.getValueTimeseriesField() + "." + hour + "." + tsConfig.getValueTimeseriesField() + "." + minute + "." + tsConfig.getValueTimeseriesField() + "." + tsConfig.getPrecalcSumTimeseriesField() + "\":" + constructBodyFieldQuery(fieldvalue.get(tsConfig.getOriginTimeseriesValueField())));
				}
			}
			stb.append("}");
		}
		stb.append("})");
		return stb.toString();
		
        /*String nativeUpdate = "db.Signals.update(" + 
                "  { " + 
                "    \"signal.timestamp\": ISODate(\""+stringDay+"\")," +
                "    \"signal.assetId\": \""+asset+"\"," +
                "    \"signal.signalId\": \""+signal+"\" " +
                "  }," + 
                "  {"  +
                "      $set: {\"signal.values.v."+hour+".v."+minute+".v."+second+"\": "+stringValue+" }, " + 
                "      $inc: {" +
                            "\"signal.values.c\": 1, " +
                            "\"signal.values.v."+hour+".c\": 1, " +
                            "\"signal.values.v."+hour+".v."+minute+".c\": 1, " +
                            "\"signal.values.s\": "+stringValue+", " +
                            "\"signal.values.v."+hour+".s\": "+stringValue+", " +
                            "\"signal.values.v."+hour+".v."+minute+".s\": "+stringValue+" " +
                "      }" +
                "  }" +
                ")";
		*/
	}

	private static Object constructBody(Map<String, Field> subcampos) throws Exception{
		JSONObject json=new JSONObject();
		for (String subcampoName : subcampos.keySet()){
			Field subcampo = subcampos.get(subcampoName);
			if (subcampo.getType()!=Field.Type.MAP && subcampo.getType()!=Field.Type.LIST_MAP && subcampo.getType()!=Field.Type.LIST){
				//si es 'primitivo'
				json.put(subcampoName,constructBodyField(subcampo));
			}else {
				json.put(subcampoName, constructBody(subcampos.get(subcampoName)));
			}
		}	
		return json;
	}
	
	private static Object constructBody(Field campo) throws Exception{
		
		JSONObject json=new JSONObject();
		JSONArray jsonarray=new JSONArray();
		if (campo.getType()==Field.Type.MAP || campo.getType()==Field.Type.LIST_MAP){
			
			Map<String, Field>subcampos = (Map<String, Field>)campo.getValue();
			for (String subcampoName : subcampos.keySet()){
				Field subcampo = subcampos.get(subcampoName);
				if (subcampo.getType()!=Field.Type.MAP && subcampo.getType()!=Field.Type.LIST_MAP && subcampo.getType()!=Field.Type.LIST){
					//si es 'primitivo'
					json.put(subcampoName,constructBodyField(subcampo));
				}else {
					json.put(subcampoName, constructBody(subcampos.get(subcampoName)));
				}
			}	
			return json;
		}else if (campo.getType()==Field.Type.LIST){
			List<Field> subcampos = (List)campo.getValue();
			for (Field field : subcampos){
				if (field.getType()!=Field.Type.MAP && field.getType()!=Field.Type.LIST_MAP && field.getType()!=Field.Type.LIST){
					//si es 'primitivo'
					jsonarray.add(constructBodyField(field));
				}else {
					jsonarray.add(constructBody(field));
				}
			}	
			return jsonarray;
		}else {
			return constructBodyField(campo);
		}
		
	}
	
	private static Object constructBodyFieldQuery(Field field){
		
		switch (field.getType()) {
			case INTEGER:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case BOOLEAN:
			case DATE:
			case DATETIME:
				return constructBodyField(field);
			default:
				return "\"" + field.getValueAsString() + "\"";
		}
	}

	private static Object constructBodyField(Field field){
		
		switch (field.getType()) {
			case INTEGER:
				return field.getValueAsInteger();
			case LONG:
				return field.getValueAsLong();
			case FLOAT:
				return field.getValueAsFloat();
			case DOUBLE:
				return field.getValueAsDouble();
			case BOOLEAN:
				return field.getValueAsBoolean();
			case DATE:
			case DATETIME:
				JSONObject json=new JSONObject();
				json.put("$date", formatToIsoDate(new DateTime(field.getValueAsDate())));
				return json;
			default:
				return (field.getValueAsString());
		}
	}
	
	public static String instanceToBaseInsert(Record record, String ontology, String customRootNode, TimeseriesConfig tsConfig, OntologyProcessInstance ontologyProcessInstance)  throws Exception {
		JSONObject json=null;
		Field baseRecord = record.get();
		Map<String, Field> mapBaseRecord = (Map<String, Field>) baseRecord.getValue();
		
		Field dateField = mapBaseRecord.get(tsConfig.getTsTimeField());
		
		Map<String, Field> mapBaseRecordInsert = new HashMap<String, Field>();
		for(String field : mapBaseRecord.keySet()) {
			if(!field.equals(tsConfig.getOriginTimeseriesValueField()) && !field.equals(tsConfig.getTsTimeField())) {
				mapBaseRecordInsert.put(field, mapBaseRecord.get(field));
			}
		}
		
		JSONObject jobj = (JSONObject) constructBody(mapBaseRecordInsert);
		
		JSONObject jsonDate=new JSONObject();
		
		jsonDate.put("$date", formatToIsoDateDay(new DateTime(dateField.getValueAsDate())));
		
		jobj.put(tsConfig.getTsTimeField(), jsonDate);
		
		generateTimeseriesValuesArrays(jobj,tsConfig);
		
		if(customRootNode != null) {
			json = new JSONObject();
			json.put(customRootNode, jobj);
		}
		else {
			json = (JSONObject) jobj;
		}
		return json.toJSONString();
	}
	
	private static void generateTimeseriesValuesArrays(JSONObject jobj, TimeseriesConfig tsConfig) {
		JSONObject dayValue = new JSONObject();
        JSONArray hoursValueArray = new JSONArray();
        for (int i = 0; i < 24; i++) {
        	JSONObject hourValue = new JSONObject();
            int hourSum = 0;
            int hourCount = 0;
            JSONArray minutesValueArray = new JSONArray();
            
            int minutesSteps = 60 / tsConfig.getMinuteStep();
            
            for (int j = 0; j < minutesSteps; j++) {
               
                if(tsConfig.getSecondStep()!=-1) {
                	JSONObject minuteJSONValue = new JSONObject();
                	int secondsSteps = 60 / tsConfig.getSecondStep();
                	
	                JSONArray secondsValueArray = new JSONArray();
	                for (int k = 0; k < secondsSteps; k++) {
	                    //int value = parse"";
	                	float value = NaN;
	                    secondsValueArray.add(value);
	                }
	                minuteJSONValue.put(tsConfig.getValueTimeseriesField(), secondsValueArray);
                
                
	                if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
	                	minuteJSONValue.put(tsConfig.getPrecalcCountTimeseriesField(), 0);
	                }
	                if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
	                	minuteJSONValue.put(tsConfig.getPrecalcSumTimeseriesField(), 0);
	                }
	                minutesValueArray.add(minuteJSONValue);
                }
                
                else {
                	minutesValueArray.add(NaN);
                }

            }
            
            hourValue.put(tsConfig.getValueTimeseriesField(), minutesValueArray);
            if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
            	hourValue.put(tsConfig.getPrecalcCountTimeseriesField(), 0);
            }
            if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
            	hourValue.put(tsConfig.getPrecalcSumTimeseriesField(), 0);
            }
            hoursValueArray.add(hourValue);
        }
        
        dayValue.put(tsConfig.getValueTimeseriesField(), hoursValueArray);
        if(tsConfig.getPrecalcCountTimeseriesField() !=null) {
        	dayValue.put(tsConfig.getPrecalcCountTimeseriesField(), 0);
        }
        if(tsConfig.getPrecalcSumTimeseriesField() !=null) {
        	dayValue.put(tsConfig.getPrecalcSumTimeseriesField(), 0);
        }
        jobj.put(tsConfig.getDestinationTimeseriesValueField(), dayValue);
	}
	
	private static DateTimeFormatter isoDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static DateTimeFormatter isoDateDayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'00:00:00.000'Z'");

	private static String formatToIsoDate(DateTime date) {
		return isoDateFormatter.print(date);
	}
	
	private static String formatToIsoDateDay(DateTime date) {
		return isoDateDayFormatter.print(date);
	}
	
}
