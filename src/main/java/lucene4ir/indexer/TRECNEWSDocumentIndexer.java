package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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

    public TRECNEWSDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional) {
        super(indexPath, tokenFilterFile, positional);

        doc = new Document();
        initFields();
        initNEWSDoc();
    }

    private void initFields() {
        docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if (indexPositions) {
            titleField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_AUTHOR, "", Field.Store.YES);
        } else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TextField(Lucene4IRConstants.FIELD_AUTHOR, "", Field.Store.YES);
        }
    }

    private void initNEWSDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(authorField);
    }

    public Document createNEWSDocument(String docid, String author, String title, String content, String all) {
        doc.clear();

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
        return doc;
    }

    public void indexDocumentsFromFile(String filename) {

        String line;
        java.lang.StringBuilder text = new StringBuilder();


        try (BufferedReader br = openDocumentFile(filename)) {
            line = br.readLine();
            while (line != null) {
                if (line.startsWith("<DOC>")) {
                    text = new StringBuilder();
                }
                text.append(line).append("\n");

                if (line.startsWith("</DOC>")) {

                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();


                    String docString = text.toString();

                    // Remove all escaped entities from the string.
                    docString = docString.replaceAll("&[a-zA-Z0-9]+;", "");
                    docString = docString.replaceAll("&", "");
                    // Remove P and ID attributes for FB94
                    docString = docString.replaceAll("P=[0-9]+", "");
                    docString = docString.replaceAll("ID=[-a-zA-Z0-9]+", "");
                    // Remove some random tag for FBIS
                    docString = docString.replaceAll("<3>", "");
                    docString = docString.replaceAll("</3>", "");

                    org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(docString)));
                    XPath xPath = XPathFactory.newInstance().newXPath();

                    String expression = "/DOC/DOCNO";
                    String docid = xPath.compile(expression).evaluate(xmlDocument).trim();

                    // The title can either be a HEAD tag or a HL tag.
                    expression = "/DOC/HEAD/descendant-or-self::*/text()|/DOC/HL/descendant-or-self::*/text()|/DOC/HEADLINE/descendant-or-self::*/text()|/DOC/DOCTITLE/descendant-or-self::*/text()|/DOC/HT/descendant-or-self::*/text()";
                    //String title = xPath.compile(expression).evaluate(xmlDocument).trim();
                    StringBuilder title = new StringBuilder();
                    NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node currentNode = nodeList.item(i);
                        title.append(" ").append(currentNode.getNodeValue());
                    }
                    title = new StringBuilder(title.toString().trim());

                    //String title = xPath.compile(expression).evaluate(xmlDocument).trim();
                    System.out.println(docid + " :" + title + ":");

                    expression = "/DOC/TEXT/descendant-or-self::*/text()";
                    StringBuilder content = new StringBuilder();
                    nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node currentNode = nodeList.item(i);
                        content.append(" ").append(currentNode.getNodeValue());
                    }
                    content = new StringBuilder(content.toString().trim());

                    // Similar to title, the author field can be represented as multiple tags.
                    expression = "/DOC/BYLINE/descendant-or-self::*/text()|/DOC/SO/descendant-or-self::*/text()";
                    String author = xPath.compile(expression).evaluate(xmlDocument).trim();

                    expression = "/DOC/descendant-or-self::*/text()";
                    StringBuilder all = new StringBuilder();
                    nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node currentNode = nodeList.item(i);
                        all.append(" ").append(currentNode.getNodeValue());
                    }
                    all = new StringBuilder(all.toString().trim());
                    System.out.println(all);
                    createNEWSDocument(docid, author, title.toString(), content.toString(), all.toString());
                    addDocumentToIndex(doc);

                    text = new StringBuilder();
                }
                line = br.readLine();
            }
        } catch (IOException | SAXException | XPathExpressionException | ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
