/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/

package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_REST_OPERATION")
public class OntologyRestOperation extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum OperationType {
		GET, POST, PUT, DELETE
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_REST_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private OntologyRest ontologyRestId;

	@Column(name = "NAME", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "ORIGIN", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String origin;

	@Column(name = "TYPE", length = 512, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private OperationType type;

}
