package com.indracompany.sofia2.persistence.hadoop.kudu;

import com.minsait.onesait.platform.persistence.ContextData;

import lombok.Getter;
import lombok.Setter;

public class TestInstance {

	@Getter
	@Setter
	private String field1;

	@Getter
	@Setter
	private Long field2;

	@Getter
	@Setter
	private Boolean field3;

	@Getter
	@Setter
	private Integer field4;

	@Getter
	@Setter
	private ContextData contextdata;
}
