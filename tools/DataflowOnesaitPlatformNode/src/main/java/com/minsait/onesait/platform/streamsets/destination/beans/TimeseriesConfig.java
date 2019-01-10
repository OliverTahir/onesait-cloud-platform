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
package com.minsait.onesait.platform.streamsets.destination.beans;

import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeseriesConfig {
	private TimeseriesTime tsTime;
	private String tsTimeField;
	private String valueTimeseriesField;
	private String precalcSumTimeseriesField;
	private String precalcCountTimeseriesField;
	private LinkedList<String> updateFields;
	private String originTimeseriesValueField;
	private String destinationTimeseriesValueField;
	
	public TimeseriesConfig(TimeseriesTime tsTime, String tsTimeField, String valueTimeseriesField, String precalcSumTimeseriesField, String precalcCountTimeseriesField, LinkedList<String> updateFields, String originTimeseriesValueField, String destinationTimeseriesValueField) {
		this.tsTime = tsTime;
		this.tsTimeField = tsTimeField;
		this.valueTimeseriesField = valueTimeseriesField;
		this.precalcSumTimeseriesField = precalcSumTimeseriesField;
		this.precalcCountTimeseriesField = precalcCountTimeseriesField;
		this.updateFields = updateFields;
		this.originTimeseriesValueField = originTimeseriesValueField;
		this.destinationTimeseriesValueField = destinationTimeseriesValueField;
	}
	
	public int getMinuteStep() {
		switch(tsTime) {
			case TENMINUTES:
				return 10;
			case FIVEMINUTES:
				return 5;
			case MINUTES:
			case THIRTYSECONDS:
			case TENSECONDS:
			case FIVESECONDS:
			case FOURSECONDS:
			case ONESECOND:
				return 1;
		}
		return 1;
	}
	
	public int getSecondStep() {
		switch(tsTime) {
			case THIRTYSECONDS:
				return 30;
			case TENSECONDS:
				return 10;
			case FIVESECONDS:
				return 5;
			case FOURSECONDS:
				return 4;
			case ONESECOND:
				return 1;
			default:
				return -1;
		}
	}
}
