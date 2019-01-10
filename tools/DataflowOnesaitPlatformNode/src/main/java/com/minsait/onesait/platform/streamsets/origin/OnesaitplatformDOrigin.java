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

import com.minsait.onesait.platform.streamsets.GroupsOnesaitplatform;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Source;
import com.streamsets.pipeline.api.StageDef;

@StageDef(
	version = 1, 
	label = "OnesaitPlatform Origin", 
	description = "Obtain data from Onesait Platform",
	execution= ExecutionMode.STANDALONE,
	icon = "onesaitplatform_black.png", 
	recordsByRef = true, 
	resetOffset = true, 
	onlineHelpRefUrl = "")
@ConfigGroups(value = GroupsOnesaitplatform.class)
@GenerateResourceBundle
public class OnesaitplatformDOrigin extends DSource {
	
	@ConfigDef(
			required = true, 
			type = ConfigDef.Type.STRING, 
			label = "Protocol", 
			defaultValue = "http", 
			description = "Protocol to listen on",
			group = "ONESAITPLATFORM",
			displayPosition = 10
	)
	public String protocol;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING, 
		label = "Host", 
		defaultValue = "localhost", 
		description = "Host to listen on",
		group = "ONESAITPLATFORM",
		displayPosition = 10
	)
	public String host;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.NUMBER,
		label = "Port",
		defaultValue = "8080", 
		description = "Port to listen on",
		group = "ONESAITPLATFORM",
		displayPosition = 12
	)
	public Integer port;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING, 
		label = "Token",
		defaultValue = "token", 
		description = "Token used by Device",
		group = "ONESAITPLATFORM",
		displayPosition = 14
	)
	public String token;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING,
		label = "DeviceId",
		defaultValue = "DevideId", 
		description = "DeviceId to connect",
		group = "ONESAITPLATFORM",
		displayPosition = 16 
	)
	public String device;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING,
		label = "Ontology",
		defaultValue = "ontology", 
		description = "Ontology from obtain data",
		group = "ONESAITPLATFORM",
		displayPosition = 18
	)
	public String ontology;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING,
		label = "Query",
		defaultValue = "select * from ontology", 
		description = "Query use to obtain data",
		group = "ONESAITPLATFORMO",
		displayPosition = 10	
	)
	public String query;

	@ConfigDef(
		required = true, 
		type = ConfigDef.Type.STRING,
		label = "Query type", 
		defaultValue = "SQL", 
		description = "Query type SQL or NATIVE",
		group = "ONESAITPLATFORMO",
		displayPosition = 12
	)
	public String queryType;

	@Override
	protected Source createSource() {
		return new OnesaitplatformOrigin(protocol, host, port, token, device, ontology, query, queryType);
	}

}
