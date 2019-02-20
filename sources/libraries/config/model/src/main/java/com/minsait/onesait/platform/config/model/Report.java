package com.minsait.onesait.platform.config.model;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.model.base.AbstractReportAuditableEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@Entity
@Table(name = "REPORT")
@NamedEntityGraphs(
		@NamedEntityGraph(name = "findByIdFetchFileAndParams", 
			attributeNodes = { 
					@NamedAttributeNode("file"),
					@NamedAttributeNode("parameters")
			}
		)
)
public class Report extends AbstractReportAuditableEntity {
	
	private static final long serialVersionUID = -3383279797731473231L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(name = "name")
	@NotNull
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "public", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	private Boolean isPublic;

	@Column(name = "active", nullable = false, columnDefinition = "BIT default 1")
	@NotNull
	private Boolean active;
	
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "file", columnDefinition = "LONGBLOB" )
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] file;
	
	// -- Unidireccional ManyToOne -- //
	@ManyToOne(fetch = FetchType.LAZY) // , cascade = CascadeType.ALL
	@JoinColumn(name = "report_type_id")
	private ReportType reportType;
	
	// -- Unidireccional ManyToOne -- //
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	// -- Unidireccional OneToMany -- //
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "report_id")
	private List<ReportParameter> parameters;
}
