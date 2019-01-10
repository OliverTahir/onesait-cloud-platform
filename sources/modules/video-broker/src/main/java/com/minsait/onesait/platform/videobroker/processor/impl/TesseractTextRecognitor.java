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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.Mat;

import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Slf4j
public class TesseractTextRecognitor {

	public static String getImgText(Mat mat) {
		final BufferedImage img = VideoUtils.MatToBufferedImage(mat);
		return getImgText(img);
	}

	public static String cleanedSimbolString(String inputString) {
		return cleanedSimbolString(inputString, null);
	}

	public static String cleanedSimbolString(String inputString, String validCharacters) {
		String outputString = "";
		// String validCharacters_default = ",;'-
		// %$ABCDEFGHIJKLMNÑOPQRSTUWVXYZabcdefghijklmnñopqrstuvwxyz01234567890";
		final String validCharacters_default = " ABCDEFGHIJKLMNOPQRSTUWVXYZ01234567890";
		final Pattern matriculaPattern = Pattern.compile("^.*(\\d{4}[A-Z]{3}).*$");// ("((\\d{4})([A-Z]{3}))");

		if (validCharacters == null || validCharacters == "") {
			validCharacters = validCharacters_default;
		}

		inputString = inputString.replaceAll(" ", "");

		for (int i = 0; i < inputString.length(); i++) {
			final String ch = inputString.substring(i, i + 1);
			if (validCharacters.contains(ch)) {
				outputString += ch;
			}

		}

		final Matcher matcher = matriculaPattern.matcher(outputString);
		if (!matcher.matches()) {
			outputString = "";
		} else {
			outputString = matcher.group(1);
		}

		return outputString;
	}

	public static String getImgText(BufferedImage img) {

		final ITesseract instance = new Tesseract();
		instance.setLanguage("eng");
		try {
			final String imgText = instance.doOCR(img);
			return cleanedSimbolString(imgText);
		} catch (final TesseractException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Error while ocr image";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		 * System.out.println("Loading OpenCV..."); OpenCvLoader.loadOpenCV();
		 *
		 * String testImagePath =
		 * "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/matricula_004.jpg";
		 *
		 * Mat img = Imgcodecs.imread(testImagePath); BufferedImage imgImage =
		 * VideoUtils.MatToBufferedImage(img);
		 *
		 *
		 * Mat img_gray = new Mat(), img_sobel=new Mat(), img_threshold=new Mat(),
		 * img_element=new Mat(), element=new Mat(); Imgproc.cvtColor(img, img_gray,
		 * Imgproc.COLOR_RGB2GRAY); BufferedImage imgGrayImage =
		 * VideoUtils.MatToBufferedImage(img_gray); Imgproc.Sobel(img_gray, img_sobel,
		 * CvType.CV_8U, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT); BufferedImage
		 * imgSobelImage = VideoUtils.MatToBufferedImage(img_sobel);
		 *
		 * TesseractTextRecognitor recognitor = new TesseractTextRecognitor();
		 * TesseractTextRecognitor.getImgText(img_gray);
		 */

		final String st = "12345\n67890";
		System.out.println("Input String: " + st);
		final String st_formated = TesseractTextRecognitor.cleanedSimbolString(st);
		System.out.println("Output String: " + st_formated);
	}
}
