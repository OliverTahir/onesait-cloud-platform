package com.minsait.onesait.platform.reports.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public abstract class BaseFieldDto<T> implements Serializable {

	private static final long serialVersionUID = -7180474757055816942L;

	protected String name;
	
	protected String description;
	
	protected T value;
	
	protected Class<T> type;
}
