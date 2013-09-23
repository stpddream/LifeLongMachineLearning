package edu.eatonlab.imageprofessor.imageproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


/**
 * Image Proc contains static util methods for image processing
 */
public class ImageProc {


    /**
     * Resize image to desired width and height.
     * @param thisImage The original image
     * @param width
     * @param height
     * @return the resized image.
     */
	public static BufferedImage resize(BufferedImage thisImage, int width, int height) {
		return ImageUtil.detype(
                thisImage.getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT));
	}


    /**
     * Convert BufferedImage type to opencv Matrix type
     * @param thisImage
     * @return 3/4 dimensional (with or without alpha channels Matrix of ARGB values of the original image
     */
    public static Mat bufToMat(BufferedImage thisImage) {
        //byte[] imagePixels = ((DataBufferByte)thisImage.getRaster().getDataBuffer()).getData();
        Mat resMat = new Mat(thisImage.getWidth(), thisImage.getHeight(), CvType.CV_8UC3);

        /**** This approach does not work ****/
        /*
        if((thisImage.getRGB(0, 0) & 0xFF000000) == 0xFF000000) {
            //If image has alpha channel
            System.out.println("has alpha channel");
            Mat thisMat = new Mat(thisImage.getWidth(), thisImage.getHeight(), CvType.CV_8UC4);
            thisMat.put(0, 0, imagePixels);
            List<Mat> newList = new ArrayList<Mat>();
            Core.split(thisMat, newList);
            newList.remove(0);
            Core.merge(newList,resMat);
        }

        //If the image does not have alpha channel
        else resMat.put(0, 0, imagePixels);
          */

        //TODO: Need optimize. Could be extremely slow. Couldnt find other ways working
        int size = thisImage.getHeight()*thisImage.getWidth();
        double[] imgRGB = new double[size*3];
        int color = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for(int i = 0; i < thisImage.getHeight(); i++) {
            for(int j = 0;j < thisImage.getWidth(); j++) {
                color = thisImage.getRGB(j, i);
                r = (color & 0x00ff0000) >> 16;
                g = (color & 0x0000ff00) >> 8;
                b = color & 0x000000ff;

                int pos = i*thisImage.getWidth()*3 + j*3;
                imgRGB[pos] = b;
                imgRGB[pos + 1] = g;
                imgRGB[pos + 2] = r;
            }
        }

        resMat.put(0, 0, imgRGB);
        return resMat;
    }


    /**
     * Convert BufferedImage to Matrix and resize the image
     * @param thisImage
     * @param width
     * @param height
     * @return 3/4 dimensional (with or without alpha channels Matrix of ARGB values of the resized image
     */
    public static Mat bufToMat(BufferedImage thisImage, int width, int height) {
        BufferedImage resizedImage = ImageProc.resize(thisImage, width, height);   //Resize
        return ImageProc.bufToMat(resizedImage);
    }


    /**
     * Calculate the BGR color feature vector of the image. For details ask Eric.
     * @param thisImage
     * @return
     */
	public static Mat calBGRFeature(Mat thisImage, int numBins) {

		ArrayList<Mat> bgr = new ArrayList<Mat>();

		Core.split(thisImage, bgr);               //Split image into 3 channels

		MatOfInt ch = new MatOfInt(0);

		ArrayList<Mat> lstPlane_b = new ArrayList<Mat>();
		lstPlane_b.add(bgr.get(0));

		ArrayList<Mat> lstPlane_g = new ArrayList<Mat>();
		lstPlane_g.add(bgr.get(1));

		ArrayList<Mat> lstPlane_r  = new ArrayList<Mat>();
		lstPlane_r.add(bgr.get(2));

		Mat mHist_b = new Mat();
		Mat mHist_g = new Mat();
		Mat mHist_r = new Mat();

		MatOfFloat range = new MatOfFloat(0, 256);


        //Calculate histograms for BGR
		Imgproc.calcHist(lstPlane_b, ch,  new Mat(), mHist_b, new MatOfInt(numBins), range);
		Imgproc.calcHist(lstPlane_g, ch,  new Mat(), mHist_g, new MatOfInt(numBins), range);
		Imgproc.calcHist(lstPlane_r, ch, new Mat(), mHist_r, new MatOfInt(numBins), range);

		Mat featureVector = new Mat(
                1,
                mHist_b.rows() +
				mHist_g.rows() +
				mHist_r.rows(), CvType.CV_32F);

		float[] blueHistArr = new float[(int) mHist_b.total()]; 
		float[] greenHistArr = new float[(int) mHist_g.total()];
		float[] redHistArr = new float[(int) mHist_r.total()];

		mHist_b.get(0, 0, blueHistArr);
		mHist_g.get(0, 0, greenHistArr);
		mHist_r.get(0, 0, redHistArr);

        //Combine BGR feature vectors to a single vector
		featureVector.put(0, 0, blueHistArr);
		featureVector.put(0, blueHistArr.length, greenHistArr);
		featureVector.put(0, blueHistArr.length*2, redHistArr);

        return featureVector;
	}


    /**
     * Calculate the Sift feature of the provided image
     * @param thisImage
     * @return Sift Feature vector
     */
    public static Mat calSiftFeature(Mat thisImage, MatOfInt numKeys) {

        //Detect keypoints
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(thisImage, keypoints);

        //Extract descriptors
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        Mat descriptors = new Mat();
        extractor.compute(thisImage, keypoints, descriptors);


        numKeys.put(0, 0, new int[] {descriptors.rows()});
        return descriptors;

    }



    /**
     * Deprecated
     * Calculate the feature vector of patches of image based on centers
     * @param thisImage the image to be calculated
     * @return
     */

    @Deprecated
    public static Mat calFeatureVector(List<Mat> patches, Mat centers) {

        double curDis = -1.0;
        double minDis = 999999.0;
        int bestCenter = -1;

        Mat bestLabelsMat = new Mat(1, patches.size(), CvType.CV_32F);
        Mat resHist = new Mat();

        double[] bestLabels = new double[patches.size()];
        for(int i = 0; i < patches.size(); i++) {
            float[] curPatch = new float[patches.get(0).cols()];
            //System.out.println("Type" + patches.get(i).type());
            patches.get(i).get(0, 0,  curPatch);
            bestCenter = -1;
            minDis = 999999.0;

            for(int j = 0; j < centers.rows(); j++) {
                float[] curCenter = new float[centers.cols()];

                centers.get(j, 0, curCenter);
                curDis = ImageProc.dis(curPatch, curCenter);
                if(curDis < minDis) { minDis = curDis; bestCenter = j; }

            }
            bestLabels[i] = bestCenter;
        }

        bestLabelsMat.put(0, 0, bestLabels);
        ArrayList<Mat> labelsList = new ArrayList<Mat>();
        labelsList.add(bestLabelsMat);

        MatOfFloat bestLabelRange = new MatOfFloat(0, centers.rows());
        Imgproc.calcHist(labelsList, new MatOfInt(0),  new Mat(),
                resHist, new MatOfInt((int) centers.rows()), bestLabelRange);

        return resHist;
    }



    /**
     * Compute the distance between two vectors
     * @param vec1
     * @param vec2
     * @return
     */
	public static double dis(float[] vec1, float[] vec2) {
		if(vec1.length != vec2.length) return -1;
		int sum = 0; 
		for(int i = 0; i < vec1.length; i++) {
			sum += (vec1[i] - vec2[i])*(vec1[i] - vec2[i]);
		}

		return Math.sqrt(sum);
	}

    /**
     * Compute the distance between 2 vectors
     * @param vec1
     * @param vec2
     * @return
     */
    public static double dis(double[] vec1, double[] vec2) {
        if(vec1.length != vec2.length) return -1;
        int sum = 0;
        for(int i = 0; i < vec1.length; i++) {
            sum += (vec1[i] - vec2[i])*(vec1[i] - vec2[i]);
        }

        return Math.sqrt(sum);
    }


	/**
	 * Divide image into random patches with fixed size 
	 * @param thisImage
	 * @return
	 */
	public static List<Mat> sampleImage(Mat thisImage, int x, int y, int n) {

		ArrayList<Mat> samples = new ArrayList<Mat>();

		int startX = 0;
		int startY = 0;
		int boundX = thisImage.cols() - x;
		int boundY = thisImage.rows() - y;
		Random gen  = new Random();

		Mat thisMat;
        int cnt = 0;
		for(int i = 0; i < n; i ++) {

			startX = gen.nextInt(boundX);
			startY = gen.nextInt(boundY);
			thisMat = new Mat(thisImage, new Range(startY, startY + y),
					new Range(startX, startX + x));
			samples.add(thisMat);

			Highgui.imwrite("base/test/" + (cnt++) + ".jpg", thisMat);

		}
		return samples;
	}

}
