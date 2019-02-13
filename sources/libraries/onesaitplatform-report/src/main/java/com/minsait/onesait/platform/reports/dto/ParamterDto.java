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
public class ParamterDto extends BaseFieldDto implements Serializable {

	private static final long serialVersionUID = -4973268497792626848L;

	public static class Builder {
		private String name;
		private String description;
		private String value;
		private Class<?> type;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
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

		public ParamterDto build() {
			ParamterDto paramterDto = new ParamterDto();
			paramterDto.name = name;
			paramterDto.description = description;
			paramterDto.value = value;
			paramterDto.type = type;
			return paramterDto;
		}
	}
}
