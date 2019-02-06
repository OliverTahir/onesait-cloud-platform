package com.minsait.onesait.platform.config.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.base.AbstractReportAuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "REPORT_TYPE")
@Getter @Setter
public class ReportType implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 939458280316059171L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(name = "code")
	@NotNull
	private String code;
	
	@Column(name = "description")
	private String description;
}
