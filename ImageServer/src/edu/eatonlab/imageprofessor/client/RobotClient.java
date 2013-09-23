package edu.eatonlab.imageprofessor.client;
import edu.eatonlab.imageprofessor.communication.XMLMessage;
import org.opencv.core.Core;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;


public class RobotClient {

	//private static final String ipAdd = "165.106.10.186";
	private static final String ipAdd = "localhost";
	private static final int port = 8887;  
	private static final String path = "../pictures/";
	private static Socket socket = null;
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;

    private static BufferedOutputStream bos;
    private static InputStreamReader inreader = null;
    private static BufferedReader br = null;
	private static LinkedList<BufferedImage> imageList;
    private static final String CONNTYPE = "QUERY";


	public static void main(String[] args) throws IOException {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		try {

			socket = new Socket(ipAdd, port);

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

            inreader = new InputStreamReader(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
            br = new BufferedReader(inreader);

		} catch(UnknownHostException e) {
			//Todo: Exception Hanlder
			System.err.println("Unknown Host");
			e.printStackTrace();
			System.exit(-1);
		} catch(IOException e) {
			//TODretrieveImagesO: Exception Handler
			System.err.println("IOException");
			e.printStackTrace();
			System.exit(-1);
		}


        System.out.println("Start Connecting...");

		try {
            XMLMessage welcome = readMsg();
            System.out.println(welcome.getContent());

            //Specify conection type
            sendMsg(XMLMessage.connMsg(CONNTYPE));

			while(true) {
                XMLMessage msg = readMsg();
                System.out.println(msg.getContent());
				BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));		
				String keyword = userIn.readLine();
                sendMsg(XMLMessage.txtMsg(keyword));
                XMLMessage amountMsg = readMsg();
                System.out.println(amountMsg.getContent());
				int amount = Integer.valueOf(userIn.readLine());
                sendMsg(XMLMessage.txtMsg("" + amount));

				if(keyword.equals("exit")) break;
                int size = Integer.parseInt(readMsg().getContent());

                XMLMessage features = readMsg();
                XMLMessage budgetMsg = readMsg();

                System.out.println("Features" + features.getContent());

				System.out.println("Budget Left " + budgetMsg.getContent());

			}
		} catch(TransformerException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch(SAXException e) {
            e.printStackTrace();
        }

		out.close();
		in.close();
		//userIn.close();
		socket.close();
	}


    private static XMLMessage readMsg() throws IOException, SAXException, ParserConfigurationException{
        if(br != null) return XMLMessage.fromXML(br.readLine());
        throw new IOException("BufferedReader Null Pointer");
    }

    private static void sendMsg(XMLMessage msg) throws ParserConfigurationException,
            TransformerException, IOException {
        bos.write(msg.toByte());
        bos.flush();
    }
}
