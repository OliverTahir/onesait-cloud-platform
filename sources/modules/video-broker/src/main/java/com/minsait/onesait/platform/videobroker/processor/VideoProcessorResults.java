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
package com.minsait.onesait.platform.videobroker.processor;

import java.sql.Timestamp;

import org.opencv.core.Mat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
public abstract class VideoProcessorResults {

	@Getter
	@Setter
	private String ipCamera; // ip camera (used as ID)
	@Getter
	@Setter
	private String captureName;
	@Getter
	@Setter
	private Mat frame; // Frame when detected
	@Getter
	@Setter
	private long timestamp;
	@Getter
	@Setter
	private String result = "";
	@Getter
	@Setter
	private String processingType = "";
	@Getter
	@Setter
	private String extraInfo = "";
	@Getter
	@Setter
	private boolean isThereResults = false;
	@Getter
	@Setter
	private boolean isReady = true;
	@Getter
	@Setter
	private String ontology;

	public void setCurrentTime() {
		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		setTimestamp(timestamp.getTime());
	}

	public void generateResult() {
		setResult("");
	}

}
