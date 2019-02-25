package com.minsait.onesait.platform.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.converters.ReportParameterTypeConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "REPORT_PARAMETER") 
public class ReportParameter implements Serializable //extends AbstractReportAuditableEntity 
{
	private static final long serialVersionUID = 394574298423983486L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(name = "name")
	@NotNull
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "type")
	@Convert(converter = ReportParameterTypeConverter.class)
	private ReportParameterType type;
	
	@Column(name = "value")
	private String value;
}
