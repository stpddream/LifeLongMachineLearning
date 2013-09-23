package edu.eatonlab.imageprofessor.server;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Properties;

import javax.imageio.ImageIO;

import edu.eatonlab.imageprofessor.imageproc.ImageUtil;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public class ImageGate {


    private static String urlBase = "https://www.googleapis.com/customsearch/v1";
    private static final String IMAGENET_URL = "http://www.image-net.org/api/text/imagenet.synset.geturls?wnid=";

    private static String apiKey;
    private static String customSearchID;
    private static Properties prop;

    //Image width and height
    private int width;
    private int height;

    public ImageGate(Properties prop) throws IOException {
        ImageGate.prop = prop;
        apiKey = prop.getProperty("GoogleAPIKey").trim();
        customSearchID = prop.getProperty("CustomSearchID").trim();
    }


    /**
     * Load Images with the given key and amount from source
     * @param key
     * @param amount
     * @return a list of images
     * @throws IOException
     * @throws NotEnoughImagesException
     */
    public LinkedList<BufferedImage> loadImages(String key, int amount)
            throws IOException, NotEnoughImagesException {
        if(prop.getProperty("ImageSource").trim().equals("0")) {
            return this.getImagesLocal(key, amount);
        }
        else if(prop.getProperty("ImageSource").trim().equals("2")) {
            return this.getImagesFromImageNet(key, amount);
        }

        //Default case: load from google
        return getImagesFromGoogle(key, amount);

    }


    /**
     * Store images to local repository
     * @param keyword
     * @param imageList
     * @throws IOException
     */
    public void storeLocalRepo(String keyword,
                               LinkedList<BufferedImage> imageList) throws IOException {
        String path = prop.getProperty("LocalRepository", "").trim();
        File destFolder = new File(path + keyword);
		/*
		if(destFolder.isDirectory() && destFolder.exists()) {
			File[] files = destFolder.listFiles();
		}
		else {*/
        destFolder.mkdir();
        String destination = path + keyword + "/";
        System.out.println(path + keyword);
        ImageUtil.store(imageList, keyword, destination);
        //}

    }


    /*********** Private Methods **************/


    /**
     * Helper function: get images from local repository
     * @param keyword
     * @param size
     * @return
     * @throws IOException
     * @throws NotEnoughImagesException
     */
    private LinkedList<BufferedImage> getImagesLocal(String keyword, int size)
            throws IOException, NotEnoughImagesException {
        LinkedList<BufferedImage> resultList = new LinkedList<BufferedImage>();

        File path = new File(prop.getProperty("LocalRepository", "") + keyword);

        if(path.exists() && path.isDirectory()) {
            if(path.listFiles().length < size) throw new NotEnoughImagesException();
            for(int i = 0; i < size; i++)  {
                System.out.println(path + "/" + keyword + i + ".jpg");
                resultList.add(ImageIO.read(new File(path + "/" + keyword + i + ".jpg")));
            }
            return resultList;
        }
        else throw new NotEnoughImagesException();
    }



    /**
     * Connect to custom search api and set up the query parameters
     * @param key keyword for images
     * @param amount amount of images querying (should <= 10, or will crash because of google limit)
     * @param start startIndex of the query
     * @throws MalformedURLException
     * @throws IOException
     */
    private synchronized InputStream connectToGoogle(String key, int amount, int start) throws MalformedURLException,
            IOException {
        URL url;

        String urlStr = urlBase + "?cx=" + URLEncoder.encode(customSearchID, "UTF-8") +
                "&num=" + amount + "&key=" + URLEncoder.encode(apiKey, "UTF-8") + "&q=" + key +
                "&searchType=image&fileType=jpg";

        System.out.println(urlStr);

        url = new URL(urlStr);
        URLConnection connection;
        connection = url.openConnection();
        HttpURLConnection httpConnection = (HttpURLConnection)connection;

        int responseCode = httpConnection.getResponseCode();

        try {
            if(responseCode == HttpURLConnection.HTTP_OK) {
                return httpConnection.getInputStream();
            }
            else {
                throw new IOException();
            }
        } catch(SocketTimeoutException e) {
            throw new IOException();
        }
    }

    /**
     * Connect to Google Search and download images
     * @param key
     * @return
     */
    private LinkedList<BufferedImage> getImagesFromGoogle(String key, int amount) {

        System.out.println("Amount is " + amount);

        LinkedList<BufferedImage> imageList = new LinkedList<BufferedImage>();
        try {

            //Continue sending requests until there are enough images
            int imgCnt = 0;
            int startIndex = 0;
            int eachAmount = 0;
            LinkedList<BufferedImage> tempList;
            while(imgCnt != amount) {
                eachAmount = (amount - imgCnt) > 10 ? 10 : (amount - imgCnt);
                tempList = this.parseImagesFromGoogle(this.connectToGoogle(key, eachAmount, startIndex));

                imageList.addAll(tempList);
                imgCnt += tempList.size();
                System.out.println(tempList.size());
                startIndex += 10;
            }
            return imageList;
        } catch(IOException e) {
            //TODO: Exception
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Helper Function: Get and parse images from Google Image Search
     * @return a linked list of images
     * @throws IOException
     */
    private LinkedList<BufferedImage> parseImagesFromGoogle(InputStream str) throws IOException {

        LinkedList<BufferedImage> imageList = new LinkedList<BufferedImage>(); //Result List

        //Size of the image
        width = Integer.valueOf(prop.getProperty("ImageWidth", "-1"));
        height= Integer.valueOf(prop.getProperty("ImageHeight", "-1"));

        //Retrieving source json file
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(str));
        String line = "";
        while((line = br.readLine()) != null) {
            sb.append(line + "\n") ;
        }

        String jsonFile = sb.toString();

		/* Parsing Json File */
        JSONObject json = null;
        JSONArray imageItems = null;
        try {
            json = new JSONObject(jsonFile);
            imageItems = json.getJSONArray("items");

            JSONObject imgObject = null;
            JSONObject imgItem = null;
            JSONObject imgLink = null;
            String imageLink = null;
            URL imageURL = null;
            BufferedImage image = null;

            System.out.println("LENGTH " + imageItems.length());

            for(int i = 0; i < imageItems.length(); i++) {
                try {
                    imgObject = imageItems.getJSONObject(i);
                    imgItem = imgObject.getJSONObject("image");
                    imageLink = imgObject.getString("link");
                    // if(imageLink.startsWith("http://eofdreams.com")) throw new IOException(); //This is a bad website!!!!
                    imageURL = new URL(imageLink);
                    System.out.print(imageURL + " ");

                    image = ImageIO.read(imageURL);
                } catch(IOException e) {
                    System.out.println("FAIL");
                    continue;
                } catch(CMMException e) {
                    System.out.println("FAIL");
                    continue;
                }
                System.out.println("");
                imageList.add(image);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }

        return imageList;
    }

    private LinkedList<BufferedImage> getImagesFromImageNet(String keyword, int size)
            throws NotEnoughImagesException{

        LinkedList<BufferedImage> imageList = new LinkedList<BufferedImage>();
        IDictionary dict = null;
        try {
            dict = new Dictionary(new URL("file", null, "/usr/local/WordNet-3.0/dict"));
            dict.open();
        } catch(MalformedURLException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        IIndexWord keyIndexWord = dict.getIndexWord(keyword, POS.NOUN);
        String id = keyIndexWord.getWordIDs().get(0).getSynsetID().toString();
        String[] idComp = id.split("-");
        String imageNetId = (idComp[2] + idComp[1]).toLowerCase();    //All ID has to be lower case

        try {
            URL url = new URL(IMAGENET_URL + imageNetId);

            System.out.println(IMAGENET_URL + imageNetId);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            int cnt = 0;
            String line;
            while( (line = br.readLine()) != null) {


                if(cnt == size) break;
                /// Print ///
                System.out.println(line);
                try {
                    imageList.add(ImageIO.read(new URL(line)));
                } catch(IOException e) {
                    System.out.println("skipped");
                    continue;
                }
                cnt++;
            }

            if(cnt < size) throw new NotEnoughImagesException();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);

        }
        return imageList;
    }

}
