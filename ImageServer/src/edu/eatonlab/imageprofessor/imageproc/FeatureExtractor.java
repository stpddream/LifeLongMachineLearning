package edu.eatonlab.imageprofessor.imageproc;

import org.opencv.core.*;

import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureExtractor {

    private int imgWidth;
    private int imgHeight;
    private int numPatch;
    private int patchWidth;
    private int patchHeight;

    private int numBins;

    /* K means parameters */
    private int k;
    private double eplison;


    /* Feature Vector Type */

    private int featureType;

    //Type Contants
    private static final int TYPE_RGB = 0;
    private static final int TYPE_SIFT = 1;



    /* Feature */
    private Feature features;


    /**
     * Default constructor with all values set to default
     */
    public FeatureExtractor() {
        this.imgWidth = 200;
        this.imgHeight = 200;
        this.numPatch = 2000;
        this.patchWidth = 40;
        this.patchHeight = 40;
        this.k = 200;
        this.numBins = 8;
        this.eplison = Math.pow(10, -5);
        this.featureType = TYPE_SIFT;

    }

    public FeatureExtractor(int k, int numPatch) {
        this();
        this.k = k;
        this.numPatch = numPatch;
    }



    public void setK(int k) {
        this.k = k;
    }


    /**
     * Core method that produces feature dictionaries from learned images
     * @param images
     * @return status of feature status, 0 for success
     */
    public int buildFeatureDic(List<BufferedImage> images) throws IllegalStateException {

        ArrayList<Mat> sources = new ArrayList<Mat>();
        Iterator<BufferedImage> imgIter = images.iterator();

        //Resize and convert images to Mat
        while(imgIter.hasNext()) {
            if(featureType == TYPE_RGB)
                sources.add(ImageProc.bufToMat(imgIter.next(), imgWidth, imgHeight));
            else if(featureType == TYPE_SIFT)
                sources.add(ImageProc.bufToMat(imgIter.next()));
        }

        Iterator<Mat> imgMatIter = sources.iterator();
        Mat thisImage;
        Mat featureMat = new Mat();
        List<Mat> imagePatches = null;
        Iterator<Mat> patchIter = null;
        ArrayList<MatOfInt> numKeyList = new ArrayList<MatOfInt>();

        if(featureType == TYPE_RGB) {

            //Loop over images and randomly sample patches
            while(imgMatIter.hasNext()) {

                thisImage = imgMatIter.next();

                //Randomly Sample Patches
                imagePatches = ImageProc.sampleImage(thisImage, patchWidth, patchHeight, numPatch);
                patchIter = imagePatches.iterator();

                //Create color feature vector for each patch
                while(patchIter.hasNext()) {
                    featureMat.push_back(ImageProc.calBGRFeature(patchIter.next(), numBins));
                }

            }

        }
        else if(featureType == TYPE_SIFT) {
            while(imgMatIter.hasNext()) {
                MatOfInt numKeys = new MatOfInt(0);
                thisImage = imgMatIter.next();
                featureMat.push_back(ImageProc.calSiftFeature(thisImage, numKeys));
                numKeyList.add(numKeys);

            }
        }

        //Run k means on feature vectors of all patches
        Mat centers = new Mat();
        Mat bestLabels = new Mat();
        Core.kmeans(featureMat, k, bestLabels,
                new TermCriteria(TermCriteria.EPS, 0, eplison),
                0, Core.KMEANS_RANDOM_CENTERS, centers);


        /// Print line ////
        //System.out.println(centers.dump());

        MatOfFloat bestLabelRange = new MatOfFloat(0, k);

        ArrayList<Mat> centerHist = new ArrayList<Mat>();
        Mat centerHistMat = new Mat(0, k, CvType.CV_32FC1);

        imgMatIter = sources.listIterator();

        //Create histogram based on nearest centers for all images
        int ptr = 0;
        int cnt = 0;
        int curNumKey = -1;
        while(imgMatIter.hasNext()) {

            Mat thisMat = null;
            if(featureType == TYPE_RGB) {
                thisMat = new Mat(bestLabels, new Range(ptr, ptr + numPatch), new Range(0, 1));
                ptr += numPatch;
            }
            else if(featureType == TYPE_SIFT) {
                curNumKey = (numKeyList.get(cnt++).toArray())[0];
                thisMat = new Mat(bestLabels, new Range(ptr, ptr + curNumKey), new Range(0, 1));
                ptr += curNumKey;
                //System.out.println(ptr);
            }

            Mat mat = new Mat();
            thisMat.convertTo(mat, CvType.CV_32F);

            ArrayList<Mat> bestLabelList = new ArrayList<Mat>();
            bestLabelList.add(mat);

            Mat thisHist = new Mat();

            Imgproc.calcHist(bestLabelList, new MatOfInt(0), new Mat(),
                    thisHist, new MatOfInt(k), bestLabelRange);



            float[] data = new float[thisHist.rows()];
            thisHist.get(0, 0, data);

            for(int i = 0; i < data.length; i++) {
                data[i] /= curNumKey;
            }

            thisHist.put(0, 0, data);

            centerHist.add(thisHist);

            centerHistMat.push_back(thisHist.t());
            imgMatIter.next();

        }

        features = new Feature(centerHistMat, centers);
        return 1;
    }

    /**
     * Calculate the feature vector of patches of image based on centers
     * @param thisImage the image to be calculated
     * @return
     */
    public Mat getFeatureVector(Mat thisImage) {
        Mat centers = this.getCenters();

        List<Mat> featureList = new ArrayList<Mat>();
        MatOfInt numKeys = new MatOfInt(0);

        if(this.featureType == TYPE_SIFT) {


            Mat features = ImageProc.calSiftFeature(thisImage, numKeys);
            float[] feature = new float[features.cols()];
            Mat featureMat = new Mat(1, features.cols(), CvType.CV_32F);

            for(int i = 0; i < features.rows(); i++) {
                features.get(i, 0, feature);
                featureMat.put(0, 0, feature);
                featureList.add(featureMat);
            }

        }
        else return null; //Not implemented


        double curDis = -1.0;
        double minDis = 999999.0;
        int bestCenter = -1;

        Mat bestLabelsMat = new Mat(1, featureList.size(), CvType.CV_32F);
        Mat resHist = new Mat();

        double[] bestLabels = new double[featureList.size()];
        for(int i = 0; i < featureList.size(); i++) {
            float[] curPatch = new float[featureList.get(0).cols()];
            //System.out.println("Type" + patches.get(i).type());
            featureList.get(i).get(0, 0,  curPatch);
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

        float[] data = new float[resHist.rows()];
        resHist.get(0, 0, data);

        for(int i = 0; i < resHist.rows(); i++) {
            data[i] /= numKeys.toArray()[0];
        }

        resHist.put(0, 0, data);

        return resHist;
    }



    /**
     * Get the feature vectors for input images
     * @return
     */
    public Mat getCenterHist() {
        if(features != null) return features.getCenterHist();
        else return null;
    }

    /**
     * Get the k-means centers for input images
     * @return
     */
    public Mat getCenters() {
        if(features != null) return features.getCenters();
        else return null;
    }

    private class Feature {
        Mat centerHistMat;
        Mat centers;

        public Feature(Mat centerHistMat, Mat centers) {
            this.centerHistMat = centerHistMat;
            this.centers = centers;
        }

        public Mat getCenterHist() {
            return centerHistMat;
        }

        public Mat getCenters() {
            return centers;
        }

    }


}


