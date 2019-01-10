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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/

package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "MODEL")
public class Model extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@Column(name = "IDENTIFICATION", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "DESCRIPTION", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
	@Getter
	@Setter
	private User user;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "notebook", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Notebook notebook;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "dashboard", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Dashboard dashboard;

	@Column(name = "OUTPT_PARAGRAPH_ID", length = 50, nullable = true)
	@Getter
	@Setter
	private String outputParagraphId;

	@Column(name = "INPUT_PARAGRAPH_ID", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String inputParagraphId;

}
