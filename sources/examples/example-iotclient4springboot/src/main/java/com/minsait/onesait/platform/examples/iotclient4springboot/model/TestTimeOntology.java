package com.minsait.onesait.platform.examples.iotclient4springboot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.client.springboot.fromjson.ContextData;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class TestTimeOntology {

	// only needed for native queries
	private JsonNode _id;

	/**
	 * Options: You can ignore contextData with:@JsonIgnore You can get as a
	 * JsonNode: private JsonNode contextData;
	 */
	private ContextData contextData;

	@JsonProperty("TestTime")
	private TestTime testTime;
}
