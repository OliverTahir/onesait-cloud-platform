package com.minsait.onesait.platform.reports.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ParameterDto<T> implements Serializable {

	private static final long serialVersionUID = -4973268497792626848L;

	protected Long id;
	
	protected String name;
	
	protected String description;
	
	protected String value;
	
	protected String type;
}
