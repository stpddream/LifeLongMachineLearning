package edu.eatonlab.imageprofessor.debug;

import edu.eatonlab.imageprofessor.client.StudentConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConnectionTest {

    private static final String ipAdd = "localhost";
    private static final int port = 8887;


    public static void main(String[] args) {

        StudentConnection conn = new StudentConnection();
        try {
            if(conn.connect(ipAdd, port) != 0) throw new IOException("Connection Failed");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                System.out.println("Connection Type?");
                String type = br.readLine();
                if(type.equalsIgnoreCase("TASK")) {
                    System.out.println("Please enter keyword");
                    String line = br.readLine();
                    System.out.println("Number of image for each type?");
                    int amount = Integer.valueOf(br.readLine());
                    String[] keywords = line.split(",");
                    conn.initTask(keywords, amount);
                }
                else if(type.equalsIgnoreCase("QUERY")) {

                    String[] keys = br.readLine().split(",");

                    System.out.println(conn.query(keys));
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }
}
