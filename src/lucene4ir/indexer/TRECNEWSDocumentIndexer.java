package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.io.FileReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * Created by leif on 30/08/2016.
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

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                String text = "";


                String line = br.readLine();
                while (line != null){

                    if (line.startsWith("<DOC>")) {
                        text = line;
                    }
                    else {
                        text = text + " " + line;
                    }

                    if (line.startsWith("<DOCNO>")){
                    //    System.out.println(line);
                    }

                    if (line.startsWith("</DOC>")){
                        System.out.println("end of doc");
                       // System.out.println(text);
                        org.jsoup.nodes.Document jdoc = Jsoup.parse(text);
                        
                        Element docno = jdoc.getElementById("DOCNO");
                        if (docno != null){
                            System.out.println("doc no is null");
                        } else {
                            System.out.println(docno.text());
                        }


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
