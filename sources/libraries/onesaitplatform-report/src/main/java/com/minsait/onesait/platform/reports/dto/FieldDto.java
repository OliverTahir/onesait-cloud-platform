package com.minsait.onesait.platform.reports.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@ToString
public class FieldDto extends BaseFieldDto implements Serializable {

	private static final long serialVersionUID = -3994588409066460836L;

	public static class Builder {
		private String name;
		private String value;
		private Class<?> type;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder value(String value) {
			this.value = value;
			return this;
		}

		public Builder type(Class<?> type) {
			this.type = type;
			return this;
		}

		public FieldDto build() {
			FieldDto fieldDto = new FieldDto();
			fieldDto.name = name;
			fieldDto.value = value;
			fieldDto.type = type;
			return fieldDto;
		}
	}
}
