package edu.eatonlab.imageprofessor.debug;


import edu.eatonlab.imageprofessor.client.StudentConnection;

import java.io.IOException;

public class LandMineTest {

    public static void main(String[] args) {


        StudentConnection studentConnection = new StudentConnection();

        try {
            if(studentConnection.connect("127.0.0.1", 8887) != 0) System.out.println("Connection Failed");
            String[][][][] data = studentConnection.queryTest();

            for(int i = 0; i < data[1].length; i++) {
                System.out.println("Set " + i);

                for(int j = 0; j < data[1][i].length; j++) {
                    System.out.print("\n");
                    for(int k = 0; k < data[1][i][j].length; k++) {
                        System.out.print(data[1][i][j][k] + " ");
                    }
                }
            }



        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }






    }




}
