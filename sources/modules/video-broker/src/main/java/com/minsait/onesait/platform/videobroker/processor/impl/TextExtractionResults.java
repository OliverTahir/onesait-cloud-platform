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
/**
 * 
 */
package com.minsait.onesait.platform.videobroker.processor.impl;

import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;

import lombok.Getter;
import lombok.Setter;


/**
 * @author ebustos
 *
 */
public class TextExtractionResults extends VideoProcessorResults{


	@Getter
	@Setter
	private String textExtracted = ""; 	// string with text extraction
	
	public TextExtractionResults() {
		
		super();
		
	}
	
	/**
	 * 
	 */
	public TextExtractionResults(Mat frame, String textExtracted) {
		// TODO Auto-generated constructor stub
	
		super();
		setFrame(frame);
		setTextExtracted(textExtracted);
		setCurrentTime();
	
	}


	/*@Override
	public String toString() {
		return "DetectionResults [ipCamera=" + getIpCamera() + ", numDetections=" + getNumDetections() + ", time=" + getTime()
				+ ", textExtracted=" + gettextExtracted() + ", detectionType=" + getDetectionType() + "]";
	}*/
	
	@Override
	public void generateResult() {
		if (getTextExtracted() != "") {
			setThereResults(true);
		}
		else {
			setThereResults(false);
		}
		String res = getTextExtracted();
		
		setResult(res);
	}
	
	
}
