package edu.eatonlab.imageprofessor.debug;


import edu.eatonlab.imageprofessor.imageproc.ImageProc;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class BugTest {


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        int f = 30;
        try {
            Mat thisImageMat;
            float[][] rgbVecs = new float[5][30];

            String pathFolder = "base/SuperTest/";
            String type = ".jpg";
            int width = 200;
            int height = 200;

            for(int i = 1; i <= 5; i++) {
                thisImageMat = ImageProc.bufToMat(ImageIO.read(new File(pathFolder + "C" + i + type)), width, height);
                rgbVecs[i] = new float[30];
                thisImageMat.get(0, 0, rgbVecs[i]);
            }


            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) {
                    System.out.print(ImageProc.dis(rgbVecs[i], rgbVecs[j]) + " ");
                }
                System.out.println("");
            }




        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }





}
