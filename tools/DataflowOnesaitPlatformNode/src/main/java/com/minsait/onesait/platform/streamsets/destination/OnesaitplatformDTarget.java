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
package com.minsait.onesait.platform.streamsets.destination;

import java.util.LinkedList;
import java.util.List;

import com.minsait.onesait.platform.streamsets.GroupsOnesaitplatform;
import com.minsait.onesait.platform.streamsets.destination.beans.OntologyProcessInstance;
import com.minsait.onesait.platform.streamsets.destination.beans.OntologyProcessInstanceChooserValues;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesTime;
import com.minsait.onesait.platform.streamsets.destination.beans.TimeseriesTimeChooserValues;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.FieldSelectorModel;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooserModel;

@StageDef(
		version = 1, 
		label = "OnesaitPlatform Destination", 
		description = "Insert data as a OnesaitPlatform Ontology Instance", 
		icon = "onesaitplatform_black.png", 
		recordsByRef = true, 
		onlineHelpRefUrl = "")
@ConfigGroups(value = GroupsOnesaitplatform.class)
@GenerateResourceBundle
public class OnesaitplatformDTarget extends OnesaitplatformTarget {
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "http", 
			label = "IoT Broker Protocol",
			displayPosition = 1, 
			group = "ONESAITPLATFORM"
	)
	public String protocol;

	@Override
	public String getProtocol() {
		return protocol;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "localhost", 
			label = "IoT Broker Host", 
			displayPosition = 2, 
			group = "ONESAITPLATFORM"
	)
	public String host;

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return host;
	}

	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.NUMBER, 
			defaultValue = "8080", 
			label = "IoT Broker Port", 
			displayPosition = 3, 
			group = "ONESAITPLATFORM"
	)
	public Integer port;

	/** {@inheritDoc} */
	@Override
	public Integer getPort() {
		return port;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "token", 
			label = "Access Token of device", 
			displayPosition = 4, 
			group = "ONESAITPLATFORM"
	)
	public String token;

	/** {@inheritDoc} */
	@Override
	public String getToken() {
		return token;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "DeviceId", 
			label = "Access Device",
			displayPosition = 5, 
			group = "ONESAITPLATFORM"
	)
	public String device;

	/** {@inheritDoc} */
	@Override
	public String getDevice() {
		return device;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "ontology", 
			label = "Ontology", 
			displayPosition = 6, 
			group = "ONESAITPLATFORM"
	)
	public String ontology;

	/** {@inheritDoc} */
	@Override
	public String getOntology() {
		return ontology;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.MODEL, 
			defaultValue = "NOROOTNODE", 
			label = "Root node type", 
			displayPosition = 7, 
			group = "ONESAITPLATFORM"
	)
	@ValueChooserModel(OntologyProcessInstanceChooserValues.class)
	public OntologyProcessInstance ontologyProcessInstance;

	/** {@inheritDoc} */
	@Override
	public OntologyProcessInstance getOntologyProcessInstance() {
		return ontologyProcessInstance;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "", 
			label = "Custom root node name", 
			displayPosition = 8, 
			group = "ONESAITPLATFORM",
			dependsOn = "ontologyProcessInstance",
		    triggeredByValue = {"CUSTOMNAME"}
		    
	)
	public String customRootNode;

	/** {@inheritDoc} */
	@Override
	public String getCustomRootNode() {
		return customRootNode;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.NUMBER, 
			defaultValue = "2", label = "Bulk", 
			displayPosition = 1, 
			group = "ONESAITPLATFORMD"
	)
	public Integer bulk;

	/** {@inheritDoc} */
	@Override
	public Integer getBulk() {
		return bulk;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.NUMBER, 
			defaultValue = "1", 
			label = "ThreadPool", 
			displayPosition = 2, 
			group = "ONESAITPLATFORMD"
	)
	public Integer thread;

	/** {@inheritDoc} */
	@Override
	public Integer getThread() {
		return thread;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.BOOLEAN, 
			defaultValue = "FALSE", 
			label = "Timeseries ontology",
			description = "Check it when your destination ontology is onesait platform standard mongo timeseries type, update-insert base instance will be use instead bulk insert",
			displayPosition = 3, 
			group = "ONESAITPLATFORMD"
	)
	public Boolean timeseriesOntology;

	/** {@inheritDoc} */
	@Override
	public Boolean getTimeseriesOntology() {
		return timeseriesOntology;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.BOOLEAN, 
			defaultValue = "TRUE", 
			label = "Timeseries Multiupdate",
			description = "Check it when you want to activate timeseries multiupdate. This operation group your data by your update fields in order to reduce the number of updates with many $set and $inc operations",
			displayPosition = 4, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	public Boolean timeseriesMultiupdate;

	/** {@inheritDoc} */
	@Override
	public Boolean getTimeseriesMultiupdate() {
		return timeseriesMultiupdate;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.MODEL, 
			defaultValue = "TENMINUTES", 
			label = "Type ontology",
			description = "Time type interval of timeseries ontology",
			displayPosition = 5, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	@ValueChooserModel(TimeseriesTimeChooserValues.class)
	public TimeseriesTime timeseriesTimeOntology;

	/** {@inheritDoc} */
	@Override
	public TimeseriesTime getTimeseriesTimeOntology() {
		return timeseriesTimeOntology;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "timestamp", 
			label = "Timeseries time field of ontology",
			description = "This field must be of timestamp json date format: yyyy-MM-dd'T'HH:mm:ss.SSS",
			displayPosition = 6, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	
	public String timeseriesFieldOntology;

	/** {@inheritDoc} */
	@Override
	public String getTimeseriesFieldOntology() {
		return timeseriesFieldOntology;
	}
	
	@ConfigDef(
			required = false, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "v", 
			label = "Value timeseries estructure field",
			displayPosition = 7, 
			group = "ONESAITPLATFORMD",
			dependsOn = "precalcCountTimeseries",
		    triggeredByValue = "true"
	)
	public String valueTimeseriesField;

	/** {@inheritDoc} */
	@Override
	public String getValueTimeseriesField() {
		return valueTimeseriesField;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.BOOLEAN, 
			defaultValue = "FALSE", 
			label = "Enable sumatory pre-calc", 
			displayPosition = 8, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	public Boolean precalcSumTimeseries;

	/** {@inheritDoc} */
	@Override
	public Boolean getPrecalcSumTimeseries() {
		return precalcSumTimeseries;
	}
	
	@ConfigDef(
			required = false, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "s", 
			label = "Sumatory pre-calc field", 
			description = "This field must be of numeric type",
			displayPosition = 9, 
			group = "ONESAITPLATFORMD",
			dependsOn = "precalcSumTimeseries",
		    triggeredByValue = "true"
	)
	public String precalcSumTimeseriesField;

	/** {@inheritDoc} */
	@Override
	public String getPrecalcSumTimeseriesField() {
		return precalcSumTimeseriesField;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.BOOLEAN, 
			defaultValue = "FALSE", 
			label = "Enable count pre-calc", 
			displayPosition = 10, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	public Boolean precalcCountTimeseries;

	/** {@inheritDoc} */
	@Override
	public Boolean getPrecalcCountTimeseries() {
		return precalcCountTimeseries;
	}
	
	@ConfigDef(
			required = false, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "c", 
			label = "Count pre-calc field", 
			displayPosition = 11, 
			group = "ONESAITPLATFORMD",
			dependsOn = "precalcCountTimeseries",
		    triggeredByValue = "true"
	)
	public String precalcCountTimeseriesField;

	/** {@inheritDoc} */
	@Override
	public String getPrecalcCountTimeseriesField() {
		return precalcCountTimeseriesField;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.MODEL, 
			defaultValue = "", 
			label = "Update fields list",
			description = "Fields used to perform the filter of update operation plus timestamp field. This fields must be unique index of ontology with timestamp field in order to speed up the process and avoid posible duplicates",
			displayPosition = 12, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	@FieldSelectorModel
	public List<String> updateFields;

	/** {@inheritDoc} */
	@Override
	public LinkedList<String> getUpdateFields() {
		LinkedList<String> l = new LinkedList<String>();
		for(String field: updateFields) {
			if(field.startsWith("/")) {
				l.add(field.substring(1));
			}
			else {
				l.add(field);
			}
		}
		return l;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "value", 
			label = "Origin timeseries value field", 
			displayPosition = 13, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	
	public String originTimeseriesValueField;

	/** {@inheritDoc} */
	@Override
	public String getOriginTimeseriesValueField() {
		return originTimeseriesValueField;
	}
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			defaultValue = "values", 
			label = "Destination ontology timeseries value field", 
			displayPosition = 14, 
			group = "ONESAITPLATFORMD",
			dependsOn = "timeseriesOntology",
		    triggeredByValue = "true"
	)
	public String destinationTimeseriesValueField;

	/** {@inheritDoc} */
	@Override
	public String getDestinationTimeseriesValueField() {
		return destinationTimeseriesValueField;
	}
}
