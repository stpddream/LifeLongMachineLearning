package edu.eatonlab.imageprofessor.client;

import edu.eatonlab.imageprofessor.communication.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MonitorClient {

	private static final String ipAdd = "localhost";
	private static final int port = 8887;  
	private static Socket socket = null;
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;

	public static void main(String[] args) {
		try {
			socket = new Socket(ipAdd, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
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

		try {
			Message welcome = (Message) in.readObject();
			System.out.println(welcome.getMsg());
			out.writeObject(new Message(Message.TYPE_CONNECTION, "ADMIN"));
			Message title = (Message) in.readObject();
			System.out.println(title.getMsg());
			Message status = (Message) in.readObject();
			System.out.println(status.getMsg());
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}




}
