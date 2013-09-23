package edu.eatonlab.imageprofessor.client;

import edu.eatonlab.imageprofessor.communication.XMLMessage;
import org.opencv.core.Mat;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Class provides methods to connect with the
 */
public class StudentConnection {

    //private static final String ipAdd = "165.106.10.186";

    private static final String path = "../pictures/";
    private static Socket socket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;

    private static BufferedOutputStream bos;
    private static InputStreamReader inreader = null;
    private static BufferedReader br = null;
    private static LinkedList<BufferedImage> imageList;
    private static final String CONNTYPE = "QUERY";

    private int budget = 0;


    private static String TYPE_TASK = "Task";
    private static String TYPE_QUERY = "Query";



    /**
     * Get connected with ImageServer
     * @param ipAdd IP Address of the server
     * @param port Port Number of the server
     * @throws UnknownHostException
     * @throws IOException
     */
    public int connect(String ipAdd, int port)
            throws UnknownHostException,IOException {

            socket = new Socket(ipAdd, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            inreader = new InputStreamReader(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
            br = new BufferedReader(inreader);

        int status = -1;
        try {
            sendMsg(XMLMessage.connMsg(CONNTYPE));
            status =  Integer.valueOf(readMsg().getContent());
            if(status != 0) throw new IOException();
        } catch(ParserConfigurationException e) {
            throw new IOException();
        } catch(TransformerException e) {
            throw new IOException();
        } catch(SAXException e) {
            throw new IOException();
        }
        return status;

    }


    public int initTask(String[] keywords, int size) throws IOException {

        //Query Type
        try {
            sendMsg(XMLMessage.txtMsg(TYPE_TASK));

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < keywords.length; i++) {
                sb.append(keywords[i]);
                sb.append(",");
            }

            sendMsg(XMLMessage.txtMsg(sb.toString()));
            System.out.println(sb.toString());
            sendMsg(XMLMessage.txtMsg(size + ""));
            budget = Integer.valueOf(readMsg().getContent());

            int status = Integer.valueOf(readMsg().getContent());
            return status;

        } catch(TransformerException e) {
            throw new IOException();
        } catch(ParserConfigurationException e) {
            throw new IOException();
        } catch(SAXException e) {
            throw new IOException();
        }

    }



    public String query(String[] keyword) throws IOException {

        try {
            sendMsg(XMLMessage.txtMsg(TYPE_QUERY));
            if(Integer.parseInt(readMsg().getContent()) != 0) throw new IOException();

            //Connection Success
            sendMsg(XMLMessage.txtMsg(keyword[0] + "," + keyword[1]));
            XMLMessage features = readMsg();

            features.getContent();

        } catch(ParserConfigurationException e) {
            throw new IOException();
        } catch(TransformerException e) {
            throw new IOException("Transformer Exception !");
        } catch(SAXException e) {
            throw new IOException("SAXException");
        }

        return null;

    }

    private static XMLMessage readMsg() throws IOException, SAXException, ParserConfigurationException {
        if(br != null) return XMLMessage.fromXML(br.readLine());
        throw new IOException("BufferedReader Null Pointer");
    }

    private static void sendMsg(XMLMessage msg) throws ParserConfigurationException,
            TransformerException, IOException {
        bos.write(msg.toByte());
        bos.flush();
    }





}
