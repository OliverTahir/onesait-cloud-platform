/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.config.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "REPORT")
public class Report extends AuditableEntityWithUUID {
	public enum ReportExtension {
		JRXML, JASPER;
	}

	private static final long serialVersionUID = -3383279797731473231L;

	@Column(name = "NAME")
	@NotNull
	private String name;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	private Boolean isPublic;

	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT default 1")
	@NotNull
	private Boolean active;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "FILE", columnDefinition = "LONGBLOB")
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	private byte[] file;

	@Column(name = "EXTENSION")
	@Enumerated(EnumType.STRING)
	@NotNull
	private ReportExtension extension;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	private User user;

}
