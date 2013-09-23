package edu.eatonlab.imageprofessor.debug;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;

public class GraphTest {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);


        Mat graph = new Mat(200, 100, CvType.CV_8UC3, new Scalar(0, 0, 0));

        Core.rectangle(graph, new Point(10, 10), new Point(50, 50), new Scalar(0, 255, 0), -1);
        Highgui.imwrite("output/test.jpg", graph);





    }

}
