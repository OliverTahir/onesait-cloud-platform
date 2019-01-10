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
package com.minsait.onesait.platform.streamsets.connection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseRest {
	private int resCode;
	private String  responseText;
	
	public ResponseRest(int resCode, String responseText) {
		this.resCode = resCode;
		this.responseText = responseText; 
	}
}
