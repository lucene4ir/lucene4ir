package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
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
 * Created by leif on 26/06/2017.
 */

public class PubMedDocumentIndexer extends DocumentIndexer {

    public DocumentBuilderFactory builderFactory;
    public DocumentBuilder builder;
    public XPath xPath;

    public PubMedDocumentIndexer(String indexPath, String tokenFilterFile, Boolean positional){
        super(indexPath, tokenFilterFile, positional);
        builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        xPath =  XPathFactory.newInstance().newXPath();
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        StringBuilder text = new StringBuilder();


        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                line = br.readLine();

                while (line != null){
                    line = line.replaceAll("^\\s+","");
                    if (line.startsWith("<PubmedArticle>")) {
                        text = new StringBuilder();
                    }
                    text.append(line + "\n");

                    if (line.startsWith("</PubmedArticle>")){

                        indexPubMedDocument(text.toString());

                        text.setLength(0);
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
    }

    public void indexPubMedDocument(String text){
        try {

        //System.out.println(text);
        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text)));
        Document doc = new Document();

        String docid = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/PMID");
        //System.out.println(docid);
        Field docnumField = new StringField("docnum", docid, Field.Store.YES);
        doc.add(docnumField);

        String pubyear = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/DateCreated/Year");
        if (pubyear.isEmpty()) {
            System.out.println(docid + " " + pubyear);
        }
        //Field yearIntField = new IntPoint("year", Integer.parseInt(pubyear));
        //doc.add(yearIntField);
        Field yearField = new StringField("year", pubyear, Field.Store.YES);
        doc.add(yearField);


            //expression = "/PubmedArticle/MedlineCitation/Article/ArticleTitle";
        //String title = xPath.compile(expression).evaluate(xmlDocument).trim();
            // System.out.println(title);
            //Field titleField = new TextField("title", title, Field.Store.YES);
            //doc.add(titleField);

        String title = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/ArticleTitle");
        //System.out.println(title);
        addTextFieldToDoc(doc, "title", title);


        /*expression = "/PubmedArticle/MedlineCitation/Article/Abstract";
        String content = xPath.compile(expression).evaluate(xmlDocument).trim();
        //System.out.println(content);
        Field textField = new TextField("content", content, Field.Store.YES);
        doc.add(textField);
          */

        String content = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Abstract");
        addTextFieldToDoc(doc, "content", content);

        String journal = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Journal/Title");
        addTextFieldToDoc(doc, "journal", journal);

        String authors = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/AuthorList");
        addTextFieldToDoc(doc, "authors", authors);

        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_ALL, title + " " + authors + " "+ journal + " " + content );


        /*
        expression = "/PubmedArticle/MedlineCitation/Article/Journal/Title";
        String journal = xPath.compile(expression).evaluate(xmlDocument).trim();

        Field journalField = new TextField("journal", journal, Field.Store.YES);
        doc.add(journalField);
        expression = "/PubmedArticle/MedlineCitation/Article/AuthorList";
        String authors = xPath.compile(expression).evaluate(xmlDocument).trim();
        //System.out.println(authors);
        Field authorsField = new TextField("authors", authors, Field.Store.YES);
        doc.add(authorsField);
*/
        System.out.println("Indexing: "+ docid);
        addDocumentToIndex(doc);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void addTextFieldToDoc(Document doc, String fieldname, String fielddata){
        Field field = new TextField(fieldname, fielddata, Field.Store.YES);
        doc.add(field);
    }

    public String getStringFromXml(org.w3c.dom.Document xmlDocument, String expression){

        String text = "";
        try {
            text = xPath.compile(expression).evaluate(xmlDocument).trim();
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        return text;

    }



}
