package edu.eatonlab.imageprofessor.debug;

import edu.eatonlab.imageprofessor.communication.XMLMessage;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.Enumeration;

public class WekaTest {


    public static XMLMessage getMessage() throws IOException, Exception {
        XMLMessage message = XMLMessage.dataMsg();

        for(int i = 1; i <= 29; i++) {

            BufferedReader reader = new BufferedReader(new FileReader("data/land/data" + String.format("%02d", i) + ".arff"));

            Instances data = new Instances(reader);
            reader.close();

            Remove remove = new Remove();
            remove.setAttributeIndices("10");
            remove.setInputFormat(data);

            Instances instanceNew = Filter.useFilter(data, remove);
            Enumeration values = instanceNew.enumerateInstances();

            StringBuilder valueBulder = new StringBuilder();
            while(values.hasMoreElements()) {
                valueBulder.append(values.nextElement() + "\n");
            }


            StringBuilder labelBuilder = new StringBuilder();
            remove.setAttributeIndices("1-9");
            remove.setInputFormat(data);

            Enumeration labels = Filter.useFilter(data, remove).enumerateInstances();
            while(labels.hasMoreElements()) {
                labelBuilder.append(labels.nextElement() + "\n");
            }

            message.addSet(valueBulder.toString(), labelBuilder.toString());

        }
        return message;

    }


    public static void main(String[] args) {

        try {
            getMessage().toByte();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }






    }

}
