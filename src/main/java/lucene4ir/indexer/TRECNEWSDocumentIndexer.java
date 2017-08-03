package lucene4ir.indexer;

import lucene4ir.LuceneConstants;
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
    public TRECNEWSDocumentIndexer(String indexPath, String tokenFilterFile){
        super(indexPath, tokenFilterFile);
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        java.lang.StringBuilder text = new StringBuilder();
        Document doc = new Document();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                line = br.readLine();
                while (line != null){

                    if (line.startsWith("<DOC>")) {
                        text = new StringBuilder();
                    }
                    text.append(line + "\n");

                    if (line.startsWith("</DOC>")){

                        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder =  builderFactory.newDocumentBuilder();
                        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text.toString())));
                        XPath xPath =  XPathFactory.newInstance().newXPath();

                        String expression = "/DOC/DOCNO";
                        String docid = xPath.compile(expression).evaluate(xmlDocument).trim();
                        Field docnumField = new StringField(LuceneConstants.FIELD_DOCNUM, docid, Field.Store.YES);
                        doc.add(docnumField);

                        expression = "/DOC/HEAD";
                        String title = xPath.compile(expression).evaluate(xmlDocument).trim();
                        Field titleField = new TextField(LuceneConstants.FIELD_TITLE, title, Field.Store.YES);
                        doc.add(titleField);

                        expression = "/DOC/TEXT";
                        String content = xPath.compile(expression).evaluate(xmlDocument).trim();
                        Field textField = new TextField(LuceneConstants.FIELD_CONTENT, content, Field.Store.YES);
                        doc.add(textField);

                        expression = "/DOC/BYLINE";
                        String author = xPath.compile(expression).evaluate(xmlDocument).trim();
                        Field authorField = new TextField(LuceneConstants.FIELD_AUTHOR, author, Field.Store.YES);
                        doc.add(authorField);

                        addDocumentToIndex(doc);

                        text = new StringBuilder();
                        doc = new Document();
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
}
