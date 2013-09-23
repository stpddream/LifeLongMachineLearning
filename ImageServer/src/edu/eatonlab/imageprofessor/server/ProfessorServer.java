package edu.eatonlab.imageprofessor.server;
import org.opencv.core.Core;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.imageio.ImageIO;

public class ProfessorServer {


    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    /* Status Variables */
    private static Properties prop;
    private static int clientCnt;



    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        prop = new Properties();
        clientCnt = 0;

        try {

            //Load Configuration
            prop.load(new FileInputStream("config/config.properties"));
            int port = Integer.valueOf(prop.getProperty("Port", "8888"));
            ServerStatus.budget = Integer.valueOf(prop.getProperty("Budget", "30"));

            serverSocket = new ServerSocket(port);
            System.out.println("Server Starts");

            //Server starts to operate
            while(true) {
                System.out.println("Start Accepting clients");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted!!");
                new ProfessorServerThread(clientSocket, prop).start();
                System.out.println("Connection " + (clientCnt++) + " established");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
