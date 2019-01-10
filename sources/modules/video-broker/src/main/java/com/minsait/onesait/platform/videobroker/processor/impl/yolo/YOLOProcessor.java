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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import com.minsait.onesait.platform.videobroker.config.ProcessorPathConfig;
import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YOLOProcessor extends FrameProcessor {

	private final ProcessType processingType = ProcessType.YOLO;
	@Getter
	private final int backend = Dnn.DNN_BACKEND_OPENCV;
	@Getter
	private final int target = Dnn.DNN_TARGET_CPU;
	@Getter
	@Setter
	private String weightsFile;
	@Getter
	@Setter
	private String configFile;
	@Getter
	@Setter
	private String namesFile;
	@Getter
	private final int[] inputSize = { 416, 416 };
	@Getter
	@Setter
	private double confThreshold = 0.5;
	@Getter
	@Setter
	private double nmsThreshold = 0.4;
	@Getter
	@Setter
	private List<String> classes = new ArrayList<String>();
	@Getter
	@Setter
	private List<double[]> COLORS = new ArrayList<double[]>();
	@Getter
	@Setter
	private Net net;
	@Setter
	@Getter
	private List<String> outputLayers = new ArrayList<String>();
	@Getter
	@Setter
	private double blobScale = 0.00392;
	@Getter
	@Setter
	private Mat blobs;
	@Getter
	@Setter
	private int imgWidth;
	@Getter
	@Setter
	private int imgHeight;
	@Getter
	@Setter
	private MatOfInt matOfclassIds = new MatOfInt();
	@Getter
	@Setter
	private MatOfFloat matOfconfidences = new MatOfFloat();
	@Getter
	@Setter
	private MatOfRect matOfboxes = new MatOfRect();
	@Getter
	@Setter
	private List<Integer> detectedIndexes = new ArrayList<Integer>();
	@Getter
	@Setter
	private List<Integer> detectedClasses = new ArrayList<Integer>();
	@Getter
	@Setter
	private List<String> detectedNamesClasses = new ArrayList<String>();
	@Getter
	@Setter
	private List<Double> detectedConfidences = new ArrayList<Double>();
	@Getter
	@Setter
	private List<Rect> detectedBoxes = new ArrayList<Rect>();
	@Getter
	@Setter
	private Mat detectedImage;

	public YOLOProcessor() {
		setProcessingType(processingType);
		try {
			startNet();
			log.info("Started {} processor", getProcessingType().toString());
		} catch (final URISyntaxException e) {
			// TODO Auto-generated catch block
			log.error("Not possible to read yolo config files: ", e.toString());
		}
	}

	public YOLOProcessor(double conf_threshold, double nms_threshold) {
		// TODO Auto-generated constructor stub
		setProcessingType(processingType);
		setConfThreshold(conf_threshold);
		setConfThreshold(nms_threshold);
		try {
			startNet();
			log.info("Started {} processor", getProcessingType().toString());
		} catch (final URISyntaxException e) {
			// TODO Auto-generated catch block
			log.error("Not possible to read yolo config files: ", e.toString());
		}
	}

	@Override
	public VideoProcessorResults process(Mat frame) {
		// TODO Auto-generated method stub
		compute(frame);
		final YOLOProcessorResults dr = new YOLOProcessorResults(frame, getDetectedIndexes(), getDetectedNamesClasses(),
				getDetectedConfidences(), getDetectedBoxes(), getDetectedImage());
		dr.generateResult();
		dr.setProcessingType(getProcessingType().toString());

		setLastProcessedFrame(frame);
		setLastProcessedResults(dr);

		return dr;
	}

	@Override
	public ProcessType getProcessType() {
		return processingType;
	}

	public void loadNetFiles() {

		try {
			log.info(ProcessorPathConfig.getPathResources().concat(YoloConfig.getWeights()));

			setWeightsFile(Paths.get(getClass().getClassLoader().getResource(YoloConfig.getWeights()).toURI())
					.toAbsolutePath().toString());
			setConfigFile(Paths.get(getClass().getClassLoader().getResource(YoloConfig.getConfig()).toURI())
					.toAbsolutePath().toString());
			setNamesFile(Paths.get(getClass().getClassLoader().getResource(YoloConfig.getNames()).toURI())
					.toAbsolutePath().toString());
		} catch (final Exception e) {
			// failed trying to load from /usr/local/app/resources
			try {
				setWeightsFile(ProcessorPathConfig.getPathResources().concat(YoloConfig.getWeights()));
				setConfigFile(ProcessorPathConfig.getPathResources().concat(YoloConfig.getConfig()));
				setNamesFile(ProcessorPathConfig.getPathResources().concat(YoloConfig.getNames()));
			} catch (final Exception e2) {
				log.error("Could not load net");
			}

		}

	}

	public void loadClasses() {

		if (getClasses().size() != 0) {
			setClasses(new ArrayList<String>()); // restart variable
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(getNamesFile()))) {
			// reader = new BufferedReader(new FileReader(getNamesFile()));
			String line = reader.readLine();
			while (line != null) {
				getClasses().add(line);
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void generateCOLORS() {
		final boolean sameColor = false;
		final Random generator = new Random(0);

		if (getCOLORS().size() != 0) {
			setCOLORS(new ArrayList<double[]>()); // restart variable
		}

		for (int i = 0; i < getClasses().size(); i++) {
			double b = 0;
			double g = 0;
			double r = 255;
			if (!sameColor) {
				b = (int) (generator.nextDouble() * 255);
				g = (int) (generator.nextDouble() * 255);
				r = (int) (generator.nextDouble() * 255);
			}
			final double[] color = { b, g, r };
			getCOLORS().add(color);
		}
	}

	public void createNet() {
		setNet(Dnn.readNetFromDarknet(getConfigFile(), getWeightsFile()));
		log.debug("setting backend");
		getNet().setPreferableBackend(getBackend());
		log.debug("setting target");
		getNet().setPreferableTarget(getTarget());
	}

	public void startNet() throws URISyntaxException {
		log.debug("loading net");
		loadNetFiles();
		log.debug("loading classes");
		loadClasses();
		log.debug("generating colours");
		generateCOLORS();
		log.debug("creating net");
		createNet();

	}

	public void generateBlobs(Mat image, double blobScale) {
		setBlobScale(blobScale);
		generateBlobs(image);
	}

	public void generateBlobs(Mat image) {
		setImgWidth(image.width());
		setImgHeight(image.height());
		setBlobs(Dnn.blobFromImage(image, getBlobScale(), new Size(inputSize[0], inputSize[1]), new Scalar(0, 0, 0),
				true, false));

	}

	public void setInputBlob() {
		getNet().setInput(getBlobs());

	};

	public void generateOutputLayers() {
		List<String> layerNames;
		final List<String> outLayerNames = new ArrayList<String>();
		layerNames = getNet().getLayerNames();

		final MatOfInt unconnected = getNet().getUnconnectedOutLayers();
		for (int i = 0; i < unconnected.rows(); i++) {
			final double idx = (int) unconnected.get(i, 0)[0] - 1;
			final String outLayer = layerNames.get((int) (idx));
			outLayerNames.add(outLayer);

		}

		setOutputLayers(outLayerNames);
	}

	public void runInference() {
		runInference(getConfThreshold(), getNmsThreshold());
	}

	public void runInference(double confThreshold, double nmsThreshold) {

		final List<Integer> class_ids = new ArrayList<Integer>();
		final List<Float> confidences = new ArrayList<Float>();
		final List<Rect> boxes = new ArrayList<Rect>();
		setMatOfclassIds(new MatOfInt());
		setMatOfconfidences(new MatOfFloat());
		setMatOfboxes(new MatOfRect());

		// run inference through the network
		// and gather predictions from each output layers

		for (final String lyr : getOutputLayers()) { // I dont know a way to forward all out layers at same time

			final Mat outs = getNet().forward(lyr);

			// for each detetion from each output layer
			// get the confidence, class id, bounding box params
			// and ignore weak detections (confidence < 0.5)
			for (int i = 0; i < outs.rows(); i++) {
				final Mat box = outs.submat(i, i + 1, 0, 4);
				final Mat scores = outs.submat(i, i + 1, 5, 85);
				int class_id = 0;
				for (int c = 0; c < scores.cols(); c++) {
					class_id = scores.get(0, c)[0] > scores.get(0, class_id)[0] ? c : class_id;
				}
				final float confidence = (float) scores.get(0, class_id)[0];

				if (confidence > 0.5) {

					final int centerX = (int) (box.get(0, 0)[0] * getImgWidth());
					final int centerY = (int) (box.get(0, 1)[0] * getImgHeight());
					final int w = (int) (box.get(0, 2)[0] * getImgWidth());
					final int h = (int) (box.get(0, 3)[0] * getImgHeight());
					final int x = centerX - w / 2;
					final int y = centerY - h / 2;
					final Rect bBox = new Rect(x, y, w, h);

					class_ids.add(class_id);
					confidences.add(confidence);
					boxes.add(bBox);
					// System.out.println("Detection - class " + class_id
					// + ", confidence: " + confidence
					// + ", position: [" + x + ", " + y + " - " + w + "x" + h + "]");
				}
			}
		}
		getMatOfclassIds().fromList(class_ids);
		getMatOfconfidences().fromList(confidences);
		getMatOfboxes().fromList(boxes);

	}

	public void nonMaxSupression(Mat image) {
		nonMaxSupression(image, getConfThreshold(), getNmsThreshold());
	}

	public void nonMaxSupression(Mat image, double confThreshold, double nmsThreshold) {
		// apply non-max suppression
		final MatOfInt matOfIndices = new MatOfInt();
		final List<Integer> detectedIndexes = new ArrayList<Integer>(); // matOfIndices.toList();
		final List<Integer> detectedClasses = new ArrayList<Integer>();
		final List<String> detectedNamesClasses = new ArrayList<String>();
		final List<Double> detectedConfidences = new ArrayList<Double>();
		final List<Rect> detectedBoxes = new ArrayList<Rect>();

		Dnn.NMSBoxes(getMatOfboxes(), getMatOfconfidences(), (float) confThreshold, (float) nmsThreshold, matOfIndices);

		final List<Rect> listOfBoxes = getMatOfboxes().toList(); // problems getting from MatOfRect
		for (int i = 0; i < matOfIndices.height(); i++) {
			final int class_id = (int) getMatOfclassIds().get(i, 0)[0];
			final double confid = getMatOfconfidences().get(i, 0)[0];
			final Rect box = listOfBoxes.get(i);
			drawBoundingBox(image, class_id, (float) confid, box);

			detectedClasses.add(class_id);
			detectedNamesClasses.add(getClasses().get(class_id));
			detectedConfidences.add(confid);
			detectedBoxes.add(box);
		}

		setDetectedIndexes(detectedIndexes);
		setDetectedClasses(detectedClasses);
		setDetectedNamesClasses(detectedNamesClasses);
		setDetectedConfidences(detectedConfidences);
		setDetectedBoxes(detectedBoxes);
		setDetectedImage(image);

	}

	public void drawBoundingBox(Mat img, int class_id, float confidence, Rect rect) {
		final double[] color = getCOLORS().get(class_id);
		final String className = getClasses().get(class_id);
		final String text = className + ": " + Float.toString(Math.round(confidence * 100)) + "%";

		Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
				new Scalar(color[0], color[1], color[2]), 1);
		Imgproc.putText(img, text, new Point(rect.x, rect.y - 10), Core.FONT_HERSHEY_SIMPLEX, 0.4,
				new Scalar(color[0], color[1], color[2]), 1);
	}

	public void compute(Mat image) {
		final Mat image_detected = image.clone();
		generateBlobs(image_detected);
		setInputBlob();
		generateOutputLayers();
		runInference();
		nonMaxSupression(image_detected);

	}

}
