package edu.eatonlab.imageprofessor.debug;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;

public class ImageReduction {
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		String path = "imgdb/fox/fox0.jpg";
		
		Mat imgMat = Highgui.imread(path);
		Mat featureMat = new Mat();
		Mat featureVector = new Mat(1, 5, CvType.CV_32F);
		
		double[] entry = new double[5];
		double[] cs;
		
		for(int i = 0; i < imgMat.rows(); i++) { 
			for(int j = 0; j < imgMat.cols(); j++) {
				cs = imgMat.get(i, j);
				entry[0] = cs[0]; entry[1] = cs[1]; entry[2] = cs[2];
				entry[3] = i;
				entry[4] = j;
				featureVector.put(0, 0, entry);
				featureMat.push_back(featureVector);
			}
			
		}
		
		Mat bestLabels = new Mat();
		Mat centers = new Mat();
		
		System.out.println("ROWS" + featureMat.rows());
		Core.kmeans(featureMat, 50, bestLabels, 
				new TermCriteria(TermCriteria.EPS, 0, Math.pow(Math.E, -10)), 
				0, Core.KMEANS_RANDOM_CENTERS, centers);
		
		System.out.println("Best label" + bestLabels.dump());
		System.out.println("Centers" + centers.dump());
		
		float[] feature = new float[5];
		
		
		System.out.println("RC" + imgMat.rows() + " " + imgMat.cols());
		
		for(int i = 0; i < imgMat.rows(); i++) {
			for(int j = 0; j < imgMat.cols(); j++) {
				System.out.println("(R, C)" + i + " " +j);
				System.out.println(i*imgMat.cols() + j);
				double best = bestLabels.get(i*imgMat.cols() + j, 0)[0];
				centers.get((int) best, 0, feature); 
				System.out.println(feature.length);
				
				double[] color = {feature[0], feature[1], feature[2]};
				imgMat.put(i, j, color);
			}
		}

		Highgui.imwrite("output/hahaha.jpg", imgMat);
		
		
		//System.out.println(featureMat.dump());
		
	}
	
		
	

}
