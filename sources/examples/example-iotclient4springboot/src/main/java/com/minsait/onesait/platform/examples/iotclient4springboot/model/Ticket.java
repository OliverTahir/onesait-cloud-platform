package com.minsait.onesait.platform.examples.iotclient4springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Ticket {

	/*
	 * "required": [ "identification", "status", "email", "name" ],
	 * 
	 */
	private String identification;

	private String status;

	private String email;

	private String name;

	@JsonIgnore
	private String response_via;

	@JsonIgnore
	private JsonNode coordinates;

	@JsonIgnore
	private String type;

	@JsonIgnore
	private String description;

	@JsonIgnore
	private JsonNode file;

}
