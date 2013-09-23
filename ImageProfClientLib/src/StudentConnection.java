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

    public void connect(String ipAdd, int port)
            throws UnknownHostException,IOException {

            socket = new Socket(ipAdd, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            inreader = new InputStreamReader(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
            br = new BufferedReader(inreader);

    }





}
