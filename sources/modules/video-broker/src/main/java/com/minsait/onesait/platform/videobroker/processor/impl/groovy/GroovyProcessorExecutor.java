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
package com.minsait.onesait.platform.videobroker.processor.impl.groovy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;
import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroovyProcessorExecutor extends FrameProcessor {

	@Getter
	@Setter
	private String groovyScript;
	@Getter
	@Setter
	private File groovyFile;
	@Getter
	@Setter
	private ProcessType processingType = ProcessType.STATS;
	@Getter
	@Setter
	private Binding binding;
	@Getter
	@Setter
	private GroovyShell groovyShell;
	@Getter
	@Setter
	private Class<?> groovyClass;
	@Getter
	@Setter
	private Object groovyInstance;

	private Method groovyProcess;
	@Getter
	@Setter
	private boolean isSetupped = false;
	@Getter
	private static final Map<String, String> groovyScripts;
	static {
		Map<String, String> aMap = new HashMap<>();
		aMap.put("stats",
				"src/main/java/com/minsait/onesait/platform/videobroker/processor/impl/groovy/BasicStatsProcessor.groovy");
		groovyScripts = Collections.unmodifiableMap(aMap);
	};

	public GroovyProcessorExecutor(String groovyScriptOption) {
		// TODO Auto-generated constructor stub
		setGroovyScript(groovyScripts.get(groovyScriptOption));
		setGroovyScript(getGroovyScript());
		setGroovyFile(new File(getGroovyScript()));
		setUp();
	}

	public void setUp() {
		try {
			binding = new Binding();
			groovyShell = new GroovyShell(getBinding());
			groovyClass = new GroovyClassLoader().parseClass(getGroovyFile());
			groovyInstance = groovyClass.newInstance();
			groovyProcess = groovyClass.getDeclaredMethod("process", new Class[] { Object.class });
			setSetupped(true);
			log.info("Started {} processor", getProcessingType().toString());

		} catch (CompilationFailedException | IOException | InstantiationException | IllegalAccessException
				| SecurityException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			log.error("Not possible to setup GroovyProcessorExecutor {}", e.toString());
		}

	}

	@Override
	public VideoProcessorResults process(Mat frame) {
		// TODO Auto-generated method stub
		String result = "";

		if (isSetupped()) {

			try {

				double[][][] frameMatrix3D = VideoUtils.MatToDouble3D(frame);
				result = (String) groovyProcess.invoke(groovyInstance, new Object[] { frameMatrix3D });

			} catch (CompilationFailedException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("Not possible to process frame in Groovy{}", e.toString());
			}

		}

		final GroovyProcessorResults dr = new GroovyProcessorResults(frame, result);
		dr.setProcessingType(getProcessingType().toString());
		dr.generateResult();

		setLastProcessedFrame(frame);
		setLastProcessedResults(dr);

		// System.out.println(dr);
		return dr;
	}

	@Override
	public ProcessType getProcessType() {
		// TODO Auto-generated method stub
		return ProcessType.STATS;
	}

	public static void main(String[] args) {

		OpenCvLoader.loadOpenCV();
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		GroovyProcessorExecutor gpe = new GroovyProcessorExecutor("stats");
		gpe.process(mat);

	}

}
