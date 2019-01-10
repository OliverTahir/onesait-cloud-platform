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
package com.minsait.onesait.platform.videobroker.processor.impl.groovy;

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
public class GroovyProcessorResults extends VideoProcessorResults{


	@Getter
	@Setter
	private String resultFromGroovy = ""; 	// string with text extraction
	
	public GroovyProcessorResults() {
		
		super();
		
	}
	
	/**
	 * 
	 */
	public GroovyProcessorResults(Mat frame, String resultFromGroovy) {
		// TODO Auto-generated constructor stub
	
		super();
		setFrame(frame);
		setResultFromGroovy(resultFromGroovy);
		setCurrentTime();
	
	}
	
	@Override
	public void generateResult() {
		if (getResultFromGroovy() != "") {
			setThereResults(true);
		}
		else {
			setThereResults(false);
		}
		setResult(getResultFromGroovy());
	}
	
	
}
