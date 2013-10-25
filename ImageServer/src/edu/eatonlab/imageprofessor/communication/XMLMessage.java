package edu.eatonlab.imageprofessor.communication;

import org.opencv.core.Mat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLMessage {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_FEATURE = 1;
    public static final int TYPE_CONNECTION = 2;
    public static final int TYPE_DATA = 3;

    private int type;
    private String content;

    private List<String> data;
    private List<String> labels;

    private String title;

    public XMLMessage(String content) {
        this.type = TYPE_TEXT;
        this.content = content;
    }

    public XMLMessage(int type, String content) {
        this.type = type;
        this.content = content;
        data = new ArrayList<String>();
        labels = new ArrayList<String>();
    }

    public static XMLMessage connMsg(String connType) {
        return new XMLMessage(TYPE_CONNECTION, connType);
    }

    public static XMLMessage txtMsg(String content) {
        return new XMLMessage(TYPE_TEXT, content);
    }

    public static XMLMessage featureMsg(Mat features) {

        float[] curRow = new float[features.cols()];
        StringBuilder sb = new StringBuilder();
        System.out.println("Feature Rows" + features.rows());
        for(int i = 0; i < features.rows(); i++) {
            features.get(i, 0, curRow);
            for(int j = 0; j < curRow.length; j++) {
                sb.append(curRow[j] + ",");
            }
            sb.append( (i < features.rows() /2) ? 0 : 1 + "|");
        }

        return new XMLMessage(TYPE_FEATURE, sb.toString());
    }

    public static XMLMessage dataMsg() {
        return new XMLMessage(TYPE_DATA, "");
    }

    public void addSet(String values, String label) {
        data.add(values);
        labels.add(label);
    }




    public static XMLMessage fromXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));

        Element root = doc.getDocumentElement();
        String type =
                root.getElementsByTagName("type").item(0).getFirstChild().getTextContent();



        //Filter Mesage Type
        if(Integer.parseInt(type) == TYPE_CONNECTION) {
            String content = root.getElementsByTagName("content").item(0).getFirstChild().getTextContent();
            return connMsg(content);
        }
        else if(Integer.parseInt(type) == TYPE_FEATURE) {

        }
        else if(Integer.parseInt(type) == TYPE_DATA) {

            XMLMessage newMessage = dataMsg();
            Element content =  (Element)root.getElementsByTagName("content").item(0);
            int dataSize = Integer.valueOf(
                    content.getElementsByTagName("size").item(0).getFirstChild().getTextContent());

            Element dataEle = (Element)content.getElementsByTagName("data").item(0);
            for(int i = 0; i < dataSize; i++) {

                Element thisSet = (Element)dataEle.getElementsByTagName("s" + i).item(0);
                String feature = thisSet.getElementsByTagName("feature").item(0).getFirstChild().getTextContent();
                String label = thisSet.getElementsByTagName("label").item(0).getFirstChild().getTextContent();
                newMessage.addSet(feature, label);

            }

            return newMessage;

        }

        //If messsage type is plain text
        String content = root.getElementsByTagName("content").item(0).getFirstChild().getTextContent();
        return txtMsg(content);
    }


    public byte[] toByte() throws
            ParserConfigurationException, TransformerException {

        DocumentBuilder docBuilder = null;
        DocumentBuilderFactory docFactory = null;
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
        }
        catch(ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Document doc = docBuilder.newDocument();
        Element rootDocument = doc.createElement("message");
        doc.appendChild(rootDocument);

        Element type = doc.createElement("type");
        rootDocument.appendChild(type);
        type.appendChild(doc.createTextNode(this.type + ""));

        Element content = doc.createElement("content");
        rootDocument.appendChild(content);



        if(this.type == TYPE_DATA) {


            Element size = doc.createElement("size");
            content.appendChild(size);
            size.appendChild(doc.createTextNode(String.valueOf(data.size())));

            Element dataNode = doc.createElement("data");
            content.appendChild(dataNode);


            for(int i = 0; i < data.size(); i++) {

                Element thisSet = doc.createElement("s" + i);
                dataNode.appendChild(thisSet);
                Element thisFeature = doc.createElement("feature");
                thisSet.appendChild(thisFeature);
                thisFeature.appendChild(doc.createTextNode(data.get(i)));
                Element thisLabel = doc.createElement("label");
                thisSet.appendChild(thisLabel);
                thisLabel.appendChild(doc.createTextNode(labels.get(i)));

            }

        }
        else content.appendChild(doc.createTextNode(this.getContent()));



        //Output
        TransformerFactory transformerFactory = null;
        Transformer transformer = null;

        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();

        // transformer.setOutputProperties(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString() + "\n";

        //Write file for debugging
        try {
            FileWriter fw = new FileWriter(new File("message.xml"));
            fw.write(output);
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }



        System.out.println(output);
        return output.getBytes();

    }



    public String getContent() {
        return this.content;
    }


}
