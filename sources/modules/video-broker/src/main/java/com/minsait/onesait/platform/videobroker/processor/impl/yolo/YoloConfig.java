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
package com.minsait.onesait.platform.videobroker.processor.impl.yolo;

import org.opencv.dnn.Dnn;

import lombok.Getter;

public class YoloConfig {

	static @Getter private String weights = "processor/yolo/yolov3.weights";
	static @Getter private String config = "processor/yolo/yolov3.cfg";
	static @Getter private String names = "processor/yolo/coco.names";
	static @Getter private int backend = Dnn.DNN_BACKEND_OPENCV;
	static @Getter private int target = Dnn.DNN_TARGET_CPU;

	static @Getter private String testImage01 = "./src/main/resources/processor/yolo/street13.jpg";

	static @Getter private double scoreThreshold = 0.4;
	static @Getter private double nmsThreshold = 0.2;

}
