package edu.eatonlab.imageprofessor.debug;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WordNetTest {

    public static void main(String[] args) {


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

        IIndexWord dog = dict.getIndexWord("dog", POS.NOUN);
        String id = dog.getWordIDs().get(0).getSynsetID().toString();
        String[] idComp = id.split("-");
        System.out.println(idComp[2] + idComp[1]);












    }


}
