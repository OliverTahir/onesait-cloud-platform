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
package com.minsait.onesait.platform.videobroker.processor.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;
import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

public class LicensePlateRecognition extends FrameProcessor {

	private ProcessType processingType = ProcessType.PLATES;

	public LicensePlateRecognition() {
		// TODO Auto-generated constructor stub
		super();
		setProcessingType(processingType);
	}

	public static void main(String[] args) {

		System.out.println("Loading OpenCV...");
		OpenCvLoader.loadOpenCV();

		final String testImagePath = "./src/main/resources/processor/licenseplaterecognition/licenseplateimages/matricula_010.jpg";
		final Mat img = Imgcodecs.imread(testImagePath);

		final BufferedImage imgImage = VideoUtils.MatToBufferedImage(img);
		displayImage(imgImage, "1 - Original image");

		final List<Rect> boundRect = detectLetters1(img);
		final Mat tagged = addContoursToMat(img, boundRect);
		final BufferedImage imgTaggedImage = VideoUtils.MatToBufferedImage(tagged);
		displayImage(imgTaggedImage, "Contours image");
		System.out.println("Bounding boxes result: " + boundRect);

		// findLicensePlate();

	}

	public static Mat addContoursToMat(Mat mat, List<Rect> rectList) {

		final ArrayList<Double> subWeightList = new ArrayList<Double>();
		final ArrayList<Rect> subRectList = new ArrayList<Rect>();

		final Point rectPoint1 = new Point();
		final Point rectPoint2 = new Point();
		final Point fontPoint = new Point();
		final Scalar rectColor = new Scalar(0, 255, 0);

		int index = 0;
		for (final Rect rect : rectList) {

			rectPoint1.x = rect.x;
			rectPoint1.y = rect.y;
			rectPoint2.x = rect.x + rect.width;
			rectPoint2.y = rect.y + rect.height;
			// Draw rectangle around fond object
			Imgproc.rectangle(mat, rectPoint1, rectPoint2, rectColor, 8);
			fontPoint.x = rect.x;
			// illustration
			fontPoint.y = rect.y - 4;
			// Print weight
			// illustration

			subRectList.add(rect);

			index++;
		}

		return mat;

	}

	public static List<Rect> getPossibleContours(Mat mat, double minRatioWH, double minWith) {

		final List<Rect> boundRect = new ArrayList<>();
		final Mat hierarchy = new Mat();
		final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(mat, contours, hierarchy, 0, 1);

		for (int i = 0; i < contours.size(); i++) {

			final MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
			final MatOfPoint2f mMOP2f2 = new MatOfPoint2f();

			contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
			Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 2, true);
			mMOP2f2.convertTo(contours.get(i), CvType.CV_32S);

			final Rect appRect = Imgproc.boundingRect(contours.get(i));
			final double width = appRect.width;
			final double height = appRect.height;
			final double ratioWithHeight = width / height;

			if (ratioWithHeight > minRatioWH && ratioWithHeight > minRatioWH && width > minWith) {
				System.out.println("dimensions: " + width + ", " + height);
				boundRect.add(appRect);

			}
		}
		return boundRect;
	}

	public static void detectPlate(Mat mat) {
		final String pathXMLPlates = "./src/main/resources/processor.licenseplaterecognition/haarCascade_license_plate.xml";
		final CascadeClassifier faceDetector = new CascadeClassifier(pathXMLPlates);

		final Mat img_gray = new Mat(), img_sobel = new Mat(), img_threshold = new Mat(), img_element = new Mat(),
				element = new Mat();
		Imgproc.cvtColor(mat, img_gray, Imgproc.COLOR_RGB2GRAY);
		final BufferedImage imgGrayImage = VideoUtils.MatToBufferedImage(img_gray);

		final Mat image = img_gray;

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		final MatOfRect faceDetections = new MatOfRect();
		System.out.println(image);
		System.out.println(faceDetector);
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

		// Draw a bounding box around each face.
		for (final Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

		displayImage(image, "---");

	}

	public static void findLicensePlate() {
		final String templatePlate = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/template.jpg";
		final String testImagePath = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/matricula_001.jpg";
		final String outFile = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/outFile.jpg";
		final Mat img = Imgcodecs.imread(testImagePath);
		final Mat templ = Imgcodecs.imread(templatePlate);

		displayImage(img, "find");
		displayImage(templ, "find");

		final int result_cols = img.cols() - templ.cols() + 1;
		final int result_rows = img.rows() - templ.rows() + 1;
		final Mat result = new Mat(); // (result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
		System.out.println(result.get(10, 10));

		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		saveImage(result, outFile);

		displayImage(result, "find");

	}

	public static List<Rect> detectLetters1(Mat imgg) {
		List<Rect> boundRect = new ArrayList<>();

		final Mat img_gray = new Mat(), img_sobel = new Mat(), img_threshold = new Mat(), img_element = new Mat();
		Mat element = new Mat();
		Imgproc.cvtColor(imgg, img_gray, Imgproc.COLOR_RGB2GRAY);
		final BufferedImage imgGrayImage = VideoUtils.MatToBufferedImage(img_gray);
		displayImage(imgGrayImage, "2 - Gray scale image");

		Imgproc.Sobel(img_gray, img_sobel, CvType.CV_8U, 1, 0, 1, 3, 0, Core.BORDER_DEFAULT);
		final BufferedImage imgSobelImage = VideoUtils.MatToBufferedImage(img_sobel);
		displayImage(imgSobelImage, "3 - Sobel image");

		Imgproc.threshold(img_sobel, img_threshold, 0, 255, 8);
		final BufferedImage imgSThresImage = VideoUtils.MatToBufferedImage(img_threshold);
		displayImage(imgSThresImage, "4 - Threshold image");

		displayImage(img_threshold, "4 - morphologyEx");
		element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(75, 10));
		Imgproc.morphologyEx(img_threshold, img_element, Imgproc.MORPH_CLOSE, element);
		displayImage(img_element, "4 - morphologyEx");

		boundRect = getPossibleContours(img_element, 2, 10);

		return boundRect;

	}

	public static List<Rect> detectLetters2(Mat img) {
		List<Rect> contours;

		final Mat imgGray = new Mat(), imgGaussBlur = new Mat(), imgadaptThres = new Mat(), imgContours = new Mat();
		System.out.println("2 - Gray scale...");
		Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
		final BufferedImage imgGrayImage = VideoUtils.MatToBufferedImage(imgGray);
		displayImage(imgGrayImage, "Gray scale image");

		System.out.println("3 - GaussianBlur...");
		Imgproc.GaussianBlur(imgGray, imgGaussBlur, new Size(5, 5), 0);
		final BufferedImage imgGaussBlurImage = VideoUtils.MatToBufferedImage(imgGaussBlur);
		displayImage(imgGaussBlurImage, "GaussianBlur image");

		final Mat img_sobel = new Mat();
		Imgproc.Sobel(imgGaussBlur, img_sobel, CvType.CV_8U, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
		final BufferedImage imgSobelImage = VideoUtils.MatToBufferedImage(img_sobel);
		displayImage(imgSobelImage, "3 - Sobel image");

		System.out.println("4 - Adaptative Threshold...");
		Imgproc.adaptiveThreshold(imgGaussBlur, imgadaptThres, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY, 15, 2);
		final BufferedImage imgimgadaptThresImage = VideoUtils.MatToBufferedImage(imgGray);
		displayImage(imgimgadaptThresImage, "Adaptative Threshold");

		System.out.println("5 - Contours...");
		contours = getPossibleContours(imgContours, 1, 3);

		return contours;
	}

	public static List<Rect> detectLetters3(Mat img) {
		List<Rect> contours;

		Mat imgGray = img;
		final Mat imgGaussBlur = img, imgadaptThres = img, imgContours = img;

		final Mat imgHSV = img, imgHue = img, imgSaturation = img, imgValue = img;
		final List<Mat> HSV = new ArrayList<>(Arrays.asList(imgHue, imgSaturation, imgValue));
		Imgproc.cvtColor(img, imgHSV, Imgproc.COLOR_BGR2HSV);
		Core.split(imgHSV, HSV);

		System.out.println("2 - Gray scale...");
		// Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
		imgGray = HSV.get(2);
		final BufferedImage imgGrayImage = VideoUtils.MatToBufferedImage(imgGray);
		displayImage(imgGrayImage, "Gray scale image");

		System.out.println("3 - Maximize contrast scale...");
		final Mat imgTopHat = img, imgBlackHat = img;

		final Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 5));

		Imgproc.morphologyEx(imgGray, imgTopHat, Imgproc.MORPH_TOPHAT, structuringElement);
		Imgproc.morphologyEx(imgGray, imgBlackHat, Imgproc.MORPH_BLACKHAT, structuringElement);

		final Mat imgGrayscalePlusTopHat = new Mat(), imgGrayscalePlusTopHatMinusBlackHat = new Mat();
		Core.add(imgGray, imgTopHat, imgGrayscalePlusTopHat);
		Core.subtract(imgGrayscalePlusTopHat, imgBlackHat, imgGrayscalePlusTopHatMinusBlackHat);
		final BufferedImage imgGrayscalePlusTopHatMinusBlackHatImage = VideoUtils
				.MatToBufferedImage(imgGrayscalePlusTopHatMinusBlackHat);
		displayImage(imgGrayscalePlusTopHatMinusBlackHatImage, "Maximize contrast scale");

		System.out.println("3 - GaussianBlur...");
		Imgproc.GaussianBlur(imgGray, imgGaussBlur, new Size(15, 15), 0);
		final BufferedImage imgGaussBlurImage = VideoUtils.MatToBufferedImage(imgGray);
		displayImage(imgGaussBlurImage, "GaussianBlur image");

		System.out.println("4 - Adaptative Threshold...");
		Imgproc.adaptiveThreshold(imgGaussBlur, imgadaptThres, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY, 15, 2);
		final BufferedImage imgimgadaptThresImage = VideoUtils.MatToBufferedImage(imgGray);
		displayImage(imgimgadaptThresImage, "Adaptative Threshold");

		System.out.println("5 - Contours...");
		contours = getPossibleContours(imgContours, 1, 3);

		return contours;
	}

	@Override
	public VideoProcessorResults process(Mat frame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessType getProcessType() {
		return ProcessType.PLATES;
	}

}
