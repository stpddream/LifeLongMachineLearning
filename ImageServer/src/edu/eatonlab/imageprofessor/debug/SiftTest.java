package edu.eatonlab.imageprofessor.debug;

import edu.eatonlab.imageprofessor.imageproc.ImageProc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;


public class SiftTest {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat image = Highgui.imread("base/Red/1.jpg");


        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);

        MatOfKeyPoint keypoints = new MatOfKeyPoint();

        detector.detect(image, keypoints);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);

        Mat descriptors = new Mat();

        extractor.compute(image, keypoints, descriptors);

        MatOfInt numKeys = new MatOfInt(0);
        Mat dscriptors = ImageProc.calSiftFeature(image, numKeys);


        Mat image2 = Highgui.imread("base/Red/images.jpg");
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

        detector.detect(image2, keypoints2);

        Mat descriptor2 = new Mat();

        extractor.compute(image2, keypoints2, descriptor2);

        System.out.println(descriptors);
        System.out.println(descriptor2);
        descriptors.push_back(descriptor2);

        System.out.println(descriptors.dump());
        System.out.println(descriptors);

       // System.out.println(dscriptors.dump());
        //System.out.println(numKeys.dump());



        /*
        System.out.println(keypoints.dump());
        System.out.println(keypoints.total());
        */
        //System.out.println(image.dump());


    }

}
