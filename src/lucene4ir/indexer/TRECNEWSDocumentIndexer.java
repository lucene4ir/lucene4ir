package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import java.io.BufferedReader;
import java.io.FileReader;
import org.xml.sax.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Created by leif on 30/08/2016.
 * Modified by Yashar on 31/08/2016
 */
public class TRECNEWSDocumentIndexer extends DocumentIndexer {
    public TRECNEWSDocumentIndexer(String indexPath){
        writer = null;
        createWriter(indexPath);
    }

    public static Document createTrecNewsDocument(String docid, String title, String content, String author, String pubdate){
        Document doc = new Document();
        Field docnumField = new StringField("docnum", docid, Field.Store.YES);
        doc.add(docnumField);
        Field titleField = new StringField("title", title, Field.Store.YES);
        doc.add(titleField);
        Field textField = new TextField("content", content, Field.Store.YES);
        doc.add(textField);
        Field authorField = new TextField("author", author, Field.Store.YES);
        doc.add(authorField);
        Field pubdateField = new StringField("pubdate", pubdate, Field.Store.YES);
        doc.add(pubdateField);
        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        java.lang.StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                line = br.readLine();
                while (line != null){

                    if (line.startsWith("<DOC>")) {
                        text = new StringBuilder();
                    }
                    text.append("\n" + line);

                    if (line.startsWith("</DOC>")){
                        System.out.println("end of doc");

                        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder =  builderFactory.newDocumentBuilder();
                        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text.toString())));
                        XPath xPath =  XPathFactory.newInstance().newXPath();

                        System.out.println("*************************");
                        //
                        //this is the line you need change to get the text
                        // String expression = "/DOC/TEXT";
                        //
                        String expression = "/DOC/DOCNO";
                        System.out.println(expression);
                        String email = xPath.compile(expression).evaluate(xmlDocument);
                        System.out.println(email);
                    }
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    };
}