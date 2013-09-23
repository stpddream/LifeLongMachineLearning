package edu.eatonlab.imageprofessor.server;
import edu.eatonlab.imageprofessor.communication.Message;
import edu.eatonlab.imageprofessor.communication.XMLMessage;
import edu.eatonlab.imageprofessor.imageproc.FeatureExtractor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class ProfessorServerThread extends Thread {

	private Socket clientSocket;
	private int numQueries;
	private ObjectOutputStream out;
	private ObjectInputStream in;

    private BufferedOutputStream bos;
    private InputStreamReader inreader;
    private BufferedReader br;

	private Properties prop;
	private int budgetLimit;
	private static int numQueryClient = 0;
	private static Logger queryLog = Logger.getLogger("edu.eatonlab.imageprofessor");
	private int width;
	private int height;

    private boolean taskFlag = false;

    private FeatureExtractor featureExtractor;

    private List<String> keywordsList;
    private int sizeEachType = -1;


    private int STATUS_SUCCESS = 0;
    private int STATUS_FAILURE = -1;

	public ProfessorServerThread(Socket socket, Properties prop) {
		this.clientSocket = socket;
		this.prop = prop;
		this.budgetLimit = Integer.valueOf(prop.getProperty("Budget"));
	}

	public void run() {
		try {
			
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());

            bos = new BufferedOutputStream(clientSocket.getOutputStream());
            inreader = new InputStreamReader(clientSocket.getInputStream());
            br = new BufferedReader(inreader);

			numQueries = Integer.valueOf(prop.getProperty("NumberQueries", "20"));
            sendMsg(new XMLMessage(STATUS_SUCCESS + ""));  //Signal Connection Success

            XMLMessage connType = this.readMsg();

			if(connType.getContent().equals("QUERY")) {
				if(numQueryClient > 1) throw new IOException("Robot client already connected");
				handleQuery();
			}
			else if(connType.getContent().equals("ADMIN")) {
				handleAdmin();
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(BudgetLimitExceededException e) {
			System.out.println("Budget Limit Exceeded!!");
			e.printStackTrace();
			System.exit(-1);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}




	/**
	 * Process image queries 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void handleQuery() throws 
	        IOException, ClassNotFoundException, BudgetLimitExceededException, NotEnoughImagesException,
            ParserConfigurationException, TransformerException, SAXException {
		queryLog.log(Level.INFO, "Robot Client connected");
		
		width = Integer.valueOf(prop.getProperty("ImageWidth"));
		height= Integer.valueOf(prop.getProperty("ImageHeight"));
		
		ProfessorServerThread.numQueryClient++;
		ImageGate imageGate = new ImageGate(prop);
		while(true) {
			if(ServerStatus.budget > Integer.valueOf(prop.getProperty("Budget", "30"))) 
					throw new BudgetLimitExceededException();

            XMLMessage queryType = readMsg();
            String type = queryType.getContent();

            if(type.equals("Task")) {
                keywordsList = new LinkedList<String>();
                String keyStr = readMsg().getContent();
                int amount = Integer.valueOf(readMsg().getContent());
                sizeEachType = amount;

                String[] keywords =  keyStr.split(",");
                keywordsList = Arrays.asList(keywords);

                LinkedList<BufferedImage> imageList = new LinkedList<BufferedImage>();
                LinkedList<BufferedImage> curList;

                String curKey;
                for(int i = 0; i < keywords.length; i++) {
                    curKey = keywords[i];
                    curList = imageGate.loadImages(curKey, amount);
                    System.out.println("I" + i);
                    imageGate.storeLocalRepo(curKey, curList);
                    imageList.addAll(curList);
                }

                featureExtractor = new FeatureExtractor();
                featureExtractor.buildFeatureDic(imageList);
                ServerStatus.budget--;
                Mat centerHist = featureExtractor.getCenterHist();
                System.out.println("Rows" + centerHist.rows());
                sendMsg(XMLMessage.txtMsg(centerHist.rows() + ""));
                //sendMsg(XMLMessage.featureMsg(centerHist));
                sendMsg(XMLMessage.txtMsg(ServerStatus.budget + ""));
                sendMsg(XMLMessage.txtMsg(STATUS_SUCCESS + ""));
                taskFlag = true;

            }
            else if(type.equals("Query")) {

                if(taskFlag != true) {
                    sendMsg(XMLMessage.txtMsg(STATUS_FAILURE + ""));
                    sendMsg(XMLMessage.txtMsg("Please Init Task First"));
                    continue;
                }

                sendMsg(XMLMessage.txtMsg(STATUS_SUCCESS + ""));
                System.out.println("Accepting Queries");

                XMLMessage key = readMsg();
                String[] keywords = key.getContent().split(",");

                queryLog.log(Level.INFO, "Query " + keywords[0] + keywords[1]);

                Iterator<String> keywordsIter = keywordsList.iterator();

                //String[] features = new String[2];
                Mat[] featureMats = new Mat[2];
                int cnt = 0;
                while(keywordsIter.hasNext()) {
                    String curWord =  keywordsIter.next();
                    if(curWord.equals(keywords[0])) {
                        featureMats[0] = new Mat(
                                featureExtractor.getCenterHist(),
                                new Range(cnt*sizeEachType, cnt*(sizeEachType + 1)));

                    }

                    else if(curWord.equals(keywords[1])) {

                        featureMats[1] = new Mat(
                                featureExtractor.getCenterHist(),
                                new Range(cnt*sizeEachType, cnt*(sizeEachType + 1)));

                    }
                    cnt++;
                }

                featureMats[0].push_back(featureMats[1]);
                sendMsg(XMLMessage.featureMsg(featureMats[0]));

            }



		
		}	
	}

	
	private void handleAdmin() throws IOException {
		while(true) {
			out.writeObject(new Message("Professor Server Admin Interface"));
			
			String statusMessage = "Budget: " + ServerStatus.budget + " out of " + 
			         prop.getProperty("Budget") + "\n";
			         
			         if(ServerStatus.getNumQueries() == 0) statusMessage += "No Queries Available";
			         else statusMessage += ServerStatus.getQuery(-1);
			        
			out.writeObject(new Message(statusMessage));
		}
	}


    private XMLMessage readMsg() throws IOException, ParserConfigurationException, SAXException {
        if(br != null) return XMLMessage.fromXML(br.readLine());
        else throw new IOException("BufferedReader - Null Pointer Exception");
    }

    private void sendMsg(XMLMessage msg) throws ParserConfigurationException,
            TransformerException, IOException {
        bos.write(msg.toByte());
        bos.flush();
    }

}