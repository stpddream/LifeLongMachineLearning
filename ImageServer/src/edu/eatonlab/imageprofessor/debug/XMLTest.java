package edu.eatonlab.imageprofessor.debug;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: stpanda
 * Date: 13-7-1
 * Time: 下午2:42
 * To change this template use File | Settings | File Templates.
 */
public class XMLTest {


    public static void main(String[] args) {

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

        Element content = doc.createElement("content");
        rootDocument.appendChild(content);

        content.appendChild(doc.createTextNode("Good Good!!!"));


        TransformerFactory transformerFactory = null;
        Transformer transformer = null;

        try {
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("base/haha.xml"));

            transformer.transform(source, result);

           // transformer.setOutputProperties(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StringWriter writer = new StringWriter();

            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();

            System.out.println(output);





        } catch(TransformerConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch(TransformerException e) {
            e.printStackTrace();
            System.exit(-1);
        }




    }



}
