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
 * Edited by kojayboy on 16/08/2017.
 */
public class TRECNEWSDocumentIndexer extends DocumentIndexer {

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field authorField;
    private Field allField;
    private Document doc;

    public TRECNEWSDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        super(indexPath, tokenFilterFile, positional);

        doc = new Document();
        initFields();
        initNEWSDoc();
    }

    private void initFields() {
        docnumField = new StringField(LuceneConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(LuceneConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(LuceneConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(LuceneConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TermVectorEnabledTextField(LuceneConstants.FIELD_AUTHOR, "", Field.Store.YES);
        }
        else {
            titleField = new TextField(LuceneConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(LuceneConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(LuceneConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TextField(LuceneConstants.FIELD_AUTHOR, "", Field.Store.YES);
        }
    }

    private void initNEWSDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(authorField);
    }

    public Document createNEWSDocument(String docid, String author, String title, String content, String all){
        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);
        authorField.setStringValue(author);

        doc.add(docnumField);
        doc.add(authorField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
//        System.out.println("Adding document: " + docid + " Title: " + title);
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
                    text.append(line + "\n");

                    if (line.startsWith("</DOC>")){
                        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder =  builderFactory.newDocumentBuilder();
                        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text.toString())));
                        XPath xPath =  XPathFactory.newInstance().newXPath();

                        String expression = "/DOC/DOCNO";
                        String docid = xPath.compile(expression).evaluate(xmlDocument).trim();

                        expression = "/DOC/HEAD";
                        String title = xPath.compile(expression).evaluate(xmlDocument).trim();

                        expression = "/DOC/TEXT";
                        String content = xPath.compile(expression).evaluate(xmlDocument).trim();

                        expression = "/DOC/BYLINE";
                        String author = xPath.compile(expression).evaluate(xmlDocument).trim();

                        String all = title + " " + content + " " + author;
                        doc.clear();
                        createNEWSDocument(docid,author,title,content,all);
                        addDocumentToIndex(doc);

                        text = new StringBuilder();
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
