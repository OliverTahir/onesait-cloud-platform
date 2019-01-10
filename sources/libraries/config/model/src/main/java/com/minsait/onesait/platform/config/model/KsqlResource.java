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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Entity
@Table(name = "KSQL_RESOURCE")
@Slf4j
public class KsqlResource extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public static enum KsqlResourceType {
		STREAM, TABLE;
	}

	public static enum FlowResourceType {
		ORIGIN, PROCESS, DESTINY;
	}

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 50, unique = true, nullable = false)
	private String identification;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@Getter
	@Setter
	private String description;

	@Column(name = "KSQL_TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private KsqlResourceType ksqlType;

	@Column(name = "RESOURCE_TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FlowResourceType resourceType;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Ontology ontology;

	@NotNull
	@Lob
	@Getter
	@Setter
	@Column(name = "STATEMENT_TEXT", nullable = false)
	@Type(type = "org.hibernate.type.TextType")
	private String statemetnText;

	@NotNull
	@Getter
	@Setter
	@Column(name = "KAFKA_TOPIC", length = 50, unique = false, nullable = false)
	private String kafkaTopic;

	public List<String> parseStatementTextAndGetDependencies() {

		Pattern creation = Pattern.compile(
				"(?i)\\s*CREATE\\s+(TABLE|STREAM)\\s+(\\w+)\\s+(\\(.+\\))\\s*(WITH\\s+\\((\\w+\\s*\\=\\s*'[\\w]+')?(\\s*,\\s*(\\w+\\s*\\=\\s*'[\\w]+'))*\\))?.*;");
		Pattern creationAs = Pattern.compile(
				"(?i)\\s*CREATE\\s+(TABLE|STREAM)\\s+(\\w+)\\s+AS\\s+.*FROM\\s+(.+)(\\s+WITH\\s*\\((\\w+\\s*\\=\\s*'[\\w]+')?(\\s*,\\s*(\\w+\\s*\\=\\s*'[\\w]+'))*\\))?.*;");
		Pattern fromClause = Pattern.compile("(?i)(\\w+)(\\s+(LEFT\\s+JOIN\\s+)(\\w+))?\\s*");
		Matcher matcher = creation.matcher(this.statemetnText);

		List<String> dependencies = new ArrayList<>();
		boolean isCreateAs = false;

		if (!matcher.matches()) {
			matcher = creationAs.matcher(this.statemetnText);
			isCreateAs = true;
		}

		if (matcher.matches()) {
			this.setKsqlType(KsqlResourceType.valueOf(matcher.group(1).toUpperCase()));
			this.setIdentification(matcher.group(2));
			String withClause = matcher.group(4);

			if (withClause != null) {
				String[] listOfPorperties = withClause.trim().replaceAll("^WITH\\s*\\(", "").replaceAll("\\s+\\).*", "")
						.split(",");
				for (String property : listOfPorperties) {
					String[] prpNameValue = property.split("=");
					if (prpNameValue[0].equalsIgnoreCase("KAFKA_TOPIC")) {
						this.setKafkaTopic(prpNameValue[1]);
					}
				}
			} else {
				this.setKafkaTopic(this.getIdentification());
			}
			// Dependencies
			if (isCreateAs) {
				Matcher from = fromClause.matcher(matcher.group(3));
				if (from.matches()) {
					dependencies.add(from.group(1)); // First element FROM
					if (from.group(2) != null) {
						dependencies.add(from.group(4));
					}
				} else {
					log.warn("Match nos possible...");
				}
			}
		} else {
			log.warn("Statement does not correspond to any type");
		}
		return dependencies;
	}
}
