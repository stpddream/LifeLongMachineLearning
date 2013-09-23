package edu.eatonlab.imageprofessor.debug;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import edu.eatonlab.imageprofessor.imageproc.ImageProc;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

public class CVTest {

    private static ArrayList<Mat> sources;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		/***** Configuration Variables *****/
        int imgWidth = 200;
        int imgHeight = 200;
        int numPatch = 2000;
        int patchWidth = 40;
        int patchHeight = 40;
        int k = 200;            //kmeans number of center
        int numBins = 8;

        String filePathRed = "base/Red/";
        String filePathBlack = "base/Black";
        String procPathRed = "base/ProcRed";
        String procPathBlack = "base/ProcBlack";
        /***********************************/


        ArrayList<String> fileNames = new ArrayList<String>();

        sources = new ArrayList<Mat>();

		/* Image IO */
        try {

			/* Read Red Staplers */
            File folder = new File(filePathRed);
            BufferedImage currentImage;
            for(final File fileEntry : folder.listFiles()) {
                if(!fileEntry.isDirectory()) {

                    //Resize Image
                    currentImage = ImageProc.resize(ImageIO.read(fileEntry), imgWidth, imgHeight);

                    File outFile = new File(procPathRed + "/" + fileEntry.getName());
                    ImageIO.write(currentImage, "JPG", outFile);
                    sources.add(Highgui.imread(outFile.getPath()));
                    fileNames.add(outFile.getName());

                }
            }

			/* Read Black Staplers */
            folder = new File(filePathBlack);
            for(final File fileEntry : folder.listFiles()) {
                if(!fileEntry.isDirectory()) {

                    //Resize Image
                    currentImage = ImageProc.resize(ImageIO.read(fileEntry), imgWidth, imgHeight);

                    File outFile = new File(procPathBlack + "/" + fileEntry.getName());
                    ImageIO.write(currentImage, "JPG", outFile);
                    sources.add(Highgui.imread(outFile.getPath()));
                    fileNames.add(outFile.getName());
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

        /****************************************/

        float[] p1 = new float[30];
        float[] p2 = new float[30];

		/* Create Image Patches and calculate color feature vector for each patch */
        Iterator<Mat> imgIter = sources.iterator();
        Mat thisImage;
        Mat featureMat = new Mat();
        List<Mat> imagePatches = null;
        Iterator<Mat> patchIter = null;

        while(imgIter.hasNext()) {

            thisImage = imgIter.next();

            //Randomly Sample Patches
            imagePatches = ImageProc.sampleImage(thisImage, patchWidth, patchHeight, numPatch);
            patchIter = imagePatches.iterator();

            //Create color feature vector for each patch
            while(patchIter.hasNext()) {
                featureMat.push_back(ImageProc.calBGRFeature(patchIter.next(), numBins));
            }

        }

        Mat centers = new Mat();
        Mat bestLabels = new Mat();
        Core.kmeans(featureMat, k, bestLabels,
                new TermCriteria(TermCriteria.EPS, 0, Math.pow(10, -5)),
                0, Core.KMEANS_RANDOM_CENTERS, centers);

        MatOfFloat bestLabelRange = new MatOfFloat(0, k);

        ArrayList<Mat> centerHist = new ArrayList<Mat>();
        Mat centerHistMat = new Mat(0, k, CvType.CV_32FC1);

        imgIter = sources.listIterator();
        Iterator<String> nameIter = fileNames.iterator();

        int ptr = 0;
        int cnt = 0;


        // Output CSV

        try{
            File outCSV = new File("output/res.csv");
            FileWriter fstream = new FileWriter(outCSV);
            BufferedWriter out = new BufferedWriter(fstream);
            StringBuilder sb;
            out.write("@relation staplers\n");
            for(int n = 0; n < 200; n++) {
                out.write("@attribute " + "a" + n + " real\n");
            }

            out.write("@attribute class {RedStapler, BlackStapler}\n\n");
            out.write("@data\n\n");

            while(imgIter.hasNext()) {

                Mat thisMat = new Mat(bestLabels, new Range(ptr, ptr + numPatch), new Range(0, 1));
                Mat mat = new Mat();
                thisMat.convertTo(mat, CvType.CV_32F);

                ArrayList<Mat> bestLabelList = new ArrayList<Mat>();
                bestLabelList.add(mat);

                Mat thisHist = new Mat();
                Imgproc.calcHist(bestLabelList, new MatOfInt(0),  new Mat(),
                        thisHist, new MatOfInt(k), bestLabelRange);

                centerHist.add(thisHist);

                // Create file
                sb = new StringBuilder();

                float[] histArr = new float[(int) thisHist.total()];
                thisHist.get(0, 0, histArr);

                for(int m = 0; m < histArr.length; m++) {
                    sb.append(histArr[m] + ",");
                }

                if( cnt++ < 10) sb.append("RedStapler");
                else sb.append("BlackStapler");


                sb.append("\n");
                out.write(sb.toString());
                //Close the output stream

                centerHistMat.push_back(thisHist.t());
                ptr += numPatch;
                imgIter.next();
            }

            out.close();
        }catch (IOException e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }

		/* Support Vector Machine Validation */
        Mat labelMat = new Mat(sources.size(), 1, CvType.CV_32FC1);

        double[] labels = new double[20];
        for(int i = 0; i < 10; i++) { labels[i]  = 1; labels[i + 10] = -1; }
        labelMat.put(0, 0, labels);

        CvSVMParams params = new CvSVMParams();
        params.set_kernel_type(CvSVM.LINEAR);

        CvSVM svm = new CvSVM();
        svm.train(centerHistMat, labelMat, new Mat(), new Mat(), params);
        svm.save("base/haha.txt");
        String basePath = "base/predict/";

        try {
            File testCSV = new File("output/test.arff");
            FileWriter testStream = new FileWriter(testCSV);
            BufferedWriter testOut = new BufferedWriter(testStream);

            testOut.write("@relation staplers\n");
            for(int n = 0; n < 200; n++) {
                testOut.write("@attribute " + "a" + n + " real\n");
            }

            testOut.write("@attribute class {RedStapler, BlackStapler}\n\n");
            testOut.write("@data\n\n");

            for(int m = 0; m < 21; m++) {

                //System.out.println(basePath + m + ".jpg");
                Mat testImg = Highgui.imread(basePath + m + ".jpg");

                List<Mat> patches = ImageProc.sampleImage(testImg, patchWidth, patchHeight, numPatch);
                List<Mat> features = new ArrayList<Mat>();


                for(int i = 0; i < patches.size(); i++) {

                    Mat testVector = ImageProc.calBGRFeature(patches.get(i), numBins);
                    features.add(testVector);

                }

                Mat testData = ImageProc.calFeatureVector(features, centers);

                StringBuilder testsb = new StringBuilder();
                //String name = nameIter.next();
                //sb.append(name + ",");


                float[] data = new float[testData.cols()];
                testData.get(0, 0, data);

                for(int o = 0; o < data.length; o++) {
                    testsb.append(data[o] + ",");
                }
                if(m < 6) testsb.append("RedStapler");
                else testsb.append("BlackStapler");

                testsb.append("\n");
                testOut.write(testsb.toString());

                System.out.println("Img" + m + " " + svm.predict(testData));


            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }



}
