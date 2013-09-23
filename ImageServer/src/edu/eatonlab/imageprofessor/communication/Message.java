package edu.eatonlab.imageprofessor.communication;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class Message implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_IMG = 1;
	public static final int TYPE_CONNECTION = 2;
	
	private String ID;
	private int type;
	private String message;
	private transient BufferedImage image;	
	
	public Message(String message) {
		this.message = message;
		this.type = Message.TYPE_TEXT;
	}
	
	public Message(int type, String tpConnect) {
		this.type = Message.TYPE_CONNECTION;
		this.message = tpConnect;
	}
	
	public Message(String ID, int type, String message, BufferedImage data) {
		this.ID = ID;
		this.type = type;
		this.message = message;
		this.image = data;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if(this.type == Message.TYPE_IMG) ImageIO.write(image, "jpg", out);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if(this.type == Message.TYPE_IMG) this.image = ImageIO.read(in);
	}
	public String getMsg() { return message; }
	public BufferedImage getObject() { return this.image; }
	
}
