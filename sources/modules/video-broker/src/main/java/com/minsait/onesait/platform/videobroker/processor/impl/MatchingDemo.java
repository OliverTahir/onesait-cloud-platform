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

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;

class MatchingDemo {
    public void run(String inFile, String templateFile, String outFile,
            int match_method) {
        System.out.println("\nRunning Template Matching");

        Mat img = Imgcodecs.imread(inFile);
        Mat templ = Imgcodecs.imread(templateFile);

        // / Create the result matrix
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        //Imgcodecs.imwrite("out2.png", result);

        // / Localizing the best match with minMaxLoc
        MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF
                || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
            System.out.println(mmr.minVal);
        } else {
            matchLoc = mmr.maxLoc;
            System.out.println(mmr.maxVal);
        }

        // / Show me what you got
        Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        System.out.println("Writing " + outFile);
        Imgcodecs.imwrite(outFile, img);

    }

    public static void main(String[] args) {
    	System.out.println("Loading OpenCV...");
		OpenCvLoader.loadOpenCV();
		String templatePlate = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/template4.jpg";
		String testImagePath = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/matricula_001.jpg";
		String outFile = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/outFile.jpg";
        new MatchingDemo().run(testImagePath, templatePlate, outFile, Imgproc.TM_CCOEFF);
    }
}