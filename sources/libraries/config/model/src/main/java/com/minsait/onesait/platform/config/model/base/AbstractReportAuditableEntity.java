package com.minsait.onesait.platform.config.model.base;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@ToString
@Getter @Setter 
public abstract class AbstractReportAuditableEntity implements Persistable<Long> {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "CREATED_AT",nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	//@NotNull
	private Date createdAt;

	@Column(name = "UPDATED_AT",nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	private Date updatedAt;
    
	@Column(name = "CREATED_BY", length = 31)
	@CreatedBy
	//@NotNull
	protected String createdBy;

	@Column(name = "UPDATED_BY", length = 31)
	@LastModifiedBy
	protected String lastModifiedBy;
	
	@Version
    @Column(name = "VERSION")
	protected Integer version;
	
	public boolean isNew() {

		return null == getId();
	}
}
