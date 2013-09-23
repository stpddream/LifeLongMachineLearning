package edu.eatonlab.imageprofessor.debug;


import edu.eatonlab.imageprofessor.imageproc.FeatureExtractor;
import edu.eatonlab.imageprofessor.imageproc.ImageProc;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ExtractorTest {
    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        int numBins = 8;
        int patchWidth = 40;
        int patchHeight = 40;
        int numPatch = 2000;
        int imgWidth = 200;
        int imgHeight = 200;
        int k = 50;
        int c = 0;

        /*
        try {
            Mat newMat = ImageProc.bufToMat(ImageIO.read(new File("base/Red/images (1).jpg")));
            Highgui.imwrite("output/test.jpg", newMat);
        } catch(IOException e) {
            e.printStackTrace();

        } */


        ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

        try {
            File files = new File("imgdb/cat");
            for(final File fileEntry : files.listFiles()) {
                if(!fileEntry.isDirectory()) {
                    imageList.add(ImageIO.read(fileEntry));
                }
            }

            files = new File("base/Chair");

            for(File fileEntry : files.listFiles()) {
                if(!fileEntry.isDirectory()) {
                    imageList.add(ImageIO.read(fileEntry));
                }
            }


        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        int graphWidth = 200;
        int graphHeight = 100;
        Mat heatGraph = new Mat(graphHeight, graphWidth, CvType.CV_8UC3, new Scalar(255, 255, 255));

        /*
        for(int i = 1; i < 200; i++) {

        } */

        //System.out.println("Size" + imageList.size());

      //  double d;
        int a;
        //for(int o = 10; o < 20; o++) {

            //for(a = 1, d = Math.pow(Math.E, a); a < 5; a++) {
          //      System.out.println("o " + o + "a " + a);

                int o = 200;
                double d = Math.pow(Math.E, 10);

                Mat preMat = null;

                FeatureExtractor featureExtractor = new FeatureExtractor();      //Default feature extractor
                featureExtractor.setK(o);

                featureExtractor.buildFeatureDic(imageList);

                preMat = featureExtractor.getCenters();



                //System.out.println(preMat.dump());


                //SVM Verification

                Mat labelMat = new Mat(imageList.size(), 1, CvType.CV_32FC1);

                double[] labels = new double[40];
                for(int i = 0; i < 20; i++) { labels[i]  = 1; labels[i + 20] = -1; }
                labelMat.put(0, 0, labels);

                CvSVMParams params = new CvSVMParams();
                params.set_kernel_type(CvSVM.LINEAR);
                params.set_C(d);
                CvSVM svm = new CvSVM();


                svm.train(featureExtractor.getCenterHist(), labelMat, new Mat(), new Mat(), params);
                svm.save("base/haha.txt");
                String basePath = "test/catvsbook/";

                int cor = 0;

                try {
                    for(int m = 1; m < 21; m++) {
                        Mat testImg = ImageProc.bufToMat(ImageIO.read(new File(basePath + m + ".jpg")), imgWidth, imgHeight);

                        Mat testData = featureExtractor.getFeatureVector(testImg);

                        float res = svm.predict(testData);

                        if( (m <= 10 && res == 1.0) || (m > 10 && res == -1.0)) cor++;
                        System.out.println("Img" + m + " " + svm.predict(testData));



                    }

                    System.out.println(" Correctness " + (float) cor / 20);


                    /********* Test Code to produce RGB histogram visualization *********/

                    //Core.circle(heatGraph, new Point(o, a), 1, new Scalar(cor*255, 0, 0));
                    /*
                    Core.rectangle(heatGraph, new Point(o*10, a*10), new Point(o*10 + 10, a*10 + 10),
                            new Scalar( (cor - 0.2) / 0.6*255, (1 - (cor - 0.2) / 0.6)*255, 0), -1);
                      */
                    /*********************************************/





                }catch(IOException e) {
                    e.printStackTrace();
                    System.exit(-1);

                }


          //  }

        //}
       // Highgui.imwrite("output/heat.jpg", heatGraph);

    }

}
