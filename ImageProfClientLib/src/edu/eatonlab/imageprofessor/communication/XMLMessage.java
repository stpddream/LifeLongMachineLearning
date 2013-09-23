package edu.eatonlab.imageprofessor.communication;

import org.opencv.core.Mat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

public class XMLMessage {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_FEATURE = 1;
    public static final int TYPE_CONNECTION = 2;

    private int type;
    private String content;
    private String title;

    public XMLMessage(String content) {
        this.type = TYPE_TEXT;
        this.content = content;
    }

    public XMLMessage(int type, String content) {
        this.type = type;
        this.content = content;
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
            sb.append("|");

        }

        return new XMLMessage(TYPE_FEATURE, sb.toString());

    }




    public static XMLMessage fromXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));

        Element root = doc.getDocumentElement();
        String type =
                root.getElementsByTagName("type").item(0).getFirstChild().getTextContent();

        String content = root.getElementsByTagName("content").item(0).getFirstChild().getTextContent();

        //Filter Mesage Type
        if(Integer.parseInt(type) == TYPE_CONNECTION) {
            return connMsg(content);
        }
        else if(Integer.parseInt(type) == TYPE_FEATURE) {

        }
        //If messsage type is plain text
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

        content.appendChild(doc.createTextNode(this.getContent()));

        TransformerFactory transformerFactory = null;
        Transformer transformer = null;

        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        // transformer.setOutputProperties(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString() + "\n";

        System.out.println(output);
        return output.getBytes();

    }



    public String getContent() {
        return this.content;
    }


}
