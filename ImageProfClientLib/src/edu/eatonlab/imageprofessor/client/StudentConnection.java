package edu.eatonlab.imageprofessor.client;

import edu.eatonlab.imageprofessor.communication.XMLMessage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;


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
    private static BufferedInputStream bis;
    private static InputStreamReader inreader = null;
    private static BufferedReader br = null;
    private static LinkedList<BufferedImage> imageList;
    private static final String CONNTYPE = "QUERY";

    private int budget = 0;


    private static String TYPE_TASK = "Task";
    private static String TYPE_QUERY = "Query";


    public StudentConnection() {
        int i = 5;
    }


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

           // inreader = new InputStreamReader(socket.getInputStream());
            bis = new BufferedInputStream(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
            //br = new BufferedReader(inreader);

        int status = -1;
        try {

            status =  Integer.valueOf(readMsg().getContent());
            System.out.println(status);
            sendMsg(XMLMessage.connMsg(CONNTYPE));
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
            sendMsg(XMLMessage.txtMsg("Query"));
            if(Integer.parseInt(readMsg().getContent()) != 0) throw new IOException();

            //Connection Success
            sendMsg(XMLMessage.txtMsg(keyword[0] + "," + keyword[1]));
            XMLMessage features = readMsg();

            return features.getContent();

        } catch(ParserConfigurationException e) {
            throw new IOException();
        } catch(TransformerException e) {
            throw new IOException("Transformer Exception !");
        } catch(SAXException e) {
            throw new IOException("SAXException");
        }

    }

    public String[][][][] queryTest() throws IOException {

        try {
            sendMsg(XMLMessage.txtMsg("QueryTest"));
            XMLMessage encodings = readMsg();

            List<String> features = encodings.getFeatureSet();
            List<String> labels = encodings.getLabelSet();

            String[][][][] featuresArr = new String[2][features.size()][][];

            for(int i = 0; i < features.size(); i++) {

                String[] lines = features.get(i).split("\n");

                featuresArr[0][i] = new String[lines.length][];
                for(int j = 0; j < lines.length; j++) {
                    featuresArr[0][i][j] = lines[j].split(",");
                }

                String[] labelCols = labels.get(i).split("\n");

                featuresArr[1][i] = new String[labelCols.length][1];

                for(int j = 0; j < labelCols.length; j++) {

                    featuresArr[1][i][j][0] = labelCols[j];
                }


            }

            return featuresArr;

        } catch(ParserConfigurationException e) {
           throw new IOException();
        } catch(TransformerException e) {
            throw new IOException("Transformer Exception");
        } catch(SAXException e) {
            throw new IOException("SAXException");
        }
    }


    private static XMLMessage readMsg() throws IOException, SAXException, ParserConfigurationException {

        StringBuilder sb = new StringBuilder();
        sb.append((char)bis.read());
        while(bis.available() > 0) {
            sb.append((char)bis.read());
        }

        /////// Debugging Purposes //////
        //Write file for debugging
        try {
            FileWriter fw = new FileWriter(new File("message.xml"));
            fw.write(sb.toString());
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        ////// Debugging Purposes ///////

        return XMLMessage.fromXML(sb.toString());
    }

    private static void sendMsg(XMLMessage msg) throws ParserConfigurationException,
            TransformerException, IOException {
        bos.write(msg.toByte());
        bos.flush();
    }





}
