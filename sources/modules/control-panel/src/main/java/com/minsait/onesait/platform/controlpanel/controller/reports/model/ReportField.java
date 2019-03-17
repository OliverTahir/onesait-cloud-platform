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
public class FieldDto<T> implements Serializable {

	private static final long serialVersionUID = -3994588409066460836L;

	protected String name;
	
	protected String description;
	
	protected T value;
	
	protected Class<?> type;
}
