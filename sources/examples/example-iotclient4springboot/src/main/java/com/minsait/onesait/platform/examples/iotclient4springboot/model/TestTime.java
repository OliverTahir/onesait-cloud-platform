package com.minsait.onesait.platform.examples.iotclient4springboot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.client.springboot.fromjson.TimeStamp;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class TestTime {

	private TimeStamp timestamp;
	private String time;
}
