package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import java.io.BufferedReader;
import java.io.FileReader;

import org.jsoup.safety.Whitelist;
import org.xml.sax.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.jsoup.Jsoup;


/**
 * Created by leif on 03/09/2016.
 */
public class TRECAquaintDocumentIndexer extends DocumentIndexer {
    Whitelist whiteList;

    public TRECAquaintDocumentIndexer(String indexPath, String tokenFilterFile){
        super(indexPath, tokenFilterFile);

        try {

            whiteList = Whitelist.relaxed();
            whiteList.addTags("docno");
            whiteList.addTags("doc");
            whiteList.addTags("headline");
            whiteList.addTags("text");
            whiteList.addTags("date_time");
            whiteList.addTags("slug");


        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public static Document createTrecAquaintDocument(String docid, String title, String content, String source, String pubdate){
        Document doc = new Document();
        Field docnumField = new StringField("docnum", docid, Field.Store.YES);
        doc.add(docnumField);
        Field titleField = new StringField("title", title, Field.Store.YES);
        doc.add(titleField);
        Field textField = new TextField("content", content, Field.Store.YES);
        doc.add(textField);
        Field sourceField = new TextField("source", source, Field.Store.YES);
        doc.add(sourceField);
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
                    text.append(System.lineSeparator() + line);

                    if (line.startsWith("</DOC>")){
                        extractFieldsFromXmlAndIndex(text.toString());

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


    public void extractFieldsFromXmlAndIndex(String xmlString){

        String docnum;
        String title;
        String content;
        String source="Unknown";
        String pubdate;

        String safeText = org.jsoup.Jsoup.clean(xmlString,whiteList);
        org.jsoup.nodes.Document jdoc = org.jsoup.Jsoup.parse(safeText);

        docnum = getFieldText(jdoc, "docno");
        title = getFieldText(jdoc, "headline");
        if (title == ""){
            title = getFieldText(jdoc, "slug");
        }
        content = getFieldText(jdoc, "text");
        pubdate = getFieldText(jdoc, "date_time");

        if (docnum.startsWith("NYT")) {
            source = "New York Times";
        }
        if (docnum.startsWith("AP")) {
            source = "Associated Press";
        }
        if (docnum.startsWith("XIE")) {
            source = "XIE";
        }

        Document doc = createTrecAquaintDocument(docnum,title,content,source,pubdate);
        addDocumentToIndex(doc);


    };

    private String getFieldText(org.jsoup.nodes.Document jdoc, String fieldName){
        String fieldText = "";

        org.jsoup.select.Elements dns = jdoc.getElementsByTag(fieldName);
        for (org.jsoup.nodes.Element dn : dns) {
            fieldText = dns.text();
        }
        return fieldText;


    }



}
