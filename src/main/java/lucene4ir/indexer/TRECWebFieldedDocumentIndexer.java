package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

/**
 * Created by colin on 20/08/2018.
 */
public class TRECWebFieldedDocumentIndexer extends DocumentIndexer {

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field allField;
    private Field urlField;
    private Field dochdrField;
    private Field t0c1Field;
    private Field t1c1Field;
    private Field t2c1Field;
    private Field t4c1Field;
    private Field t8c1Field;
    private Field t1c0Field;
    private Field t1c2Field;
    private Field t1c4Field;
    private Field t16c1Field;
    private Field t1c8Field;
    private Document doc;

    private static String [] contentTags = {
            "HTML"
    };
    private static String [] titleTags = {
            "title"
    };

    public TRECWebFieldedDocumentIndexer(String indexPath, String tokenFilterFile, boolean pos, boolean imputing){
        super(indexPath, tokenFilterFile, pos, imputing);
        doc = new Document();
        initFields();
        initWebDoc();
    }

    private void initFields() {
        docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            urlField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
            dochdrField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_DOCHDR, "", Field.Store.YES);
            t0c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T0C1, "", Field.Store.YES);
            t1c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C1, "", Field.Store.YES);
            t2c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T2C1, "", Field.Store.YES);
            t4c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T4C1, "", Field.Store.YES);
            t8c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T8C1, "", Field.Store.YES);
            t16c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T16C1, "", Field.Store.YES);
            t1c0Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C0, "", Field.Store.YES);
            t1c2Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C2, "", Field.Store.YES);
            t1c4Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C4, "", Field.Store.YES);
            t1c8Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C8, "", Field.Store.YES);
        }
        else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            urlField = new TextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
            dochdrField = new TextField(Lucene4IRConstants.FIELD_DOCHDR, "", Field.Store.YES);
            t0c1Field = new TextField(Lucene4IRConstants.FIELD_T0C1, "", Field.Store.YES);
            t1c1Field = new TextField(Lucene4IRConstants.FIELD_T1C1, "", Field.Store.YES);
            t2c1Field = new TextField(Lucene4IRConstants.FIELD_T2C1, "", Field.Store.YES);
            t4c1Field = new TextField(Lucene4IRConstants.FIELD_T4C1, "", Field.Store.YES);
            t8c1Field = new TextField(Lucene4IRConstants.FIELD_T8C1, "", Field.Store.YES);
            t16c1Field = new TextField(Lucene4IRConstants.FIELD_T16C1, "", Field.Store.YES);
            t1c0Field = new TextField(Lucene4IRConstants.FIELD_T1C0, "", Field.Store.YES);
            t1c2Field = new TextField(Lucene4IRConstants.FIELD_T1C2, "", Field.Store.YES);
            t1c4Field = new TextField(Lucene4IRConstants.FIELD_T1C4, "", Field.Store.YES);
            t1c8Field = new TextField(Lucene4IRConstants.FIELD_T1C8, "", Field.Store.YES);

        }
    }

    private void initWebDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(urlField);
        doc.add(dochdrField);
        doc.add(allField);
        doc.add(t0c1Field);
        doc.add(t1c1Field);
        doc.add(t2c1Field);
        doc.add(t4c1Field);
        doc.add(t8c1Field);
        doc.add(t16c1Field);
        doc.add(t1c0Field);
        doc.add(t1c2Field);
        doc.add(t1c4Field);
        doc.add(t1c8Field);
    }

    public Document createTRECWebFieldedDocument(String docid, String url, String dochdr, String title, String content, String all){
        doc.clear();
        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);
        urlField.setStringValue(url);
        dochdrField.setStringValue(dochdr);
        t0c1Field.setStringValue(content);
        t1c1Field.setStringValue(title + " " + content);
        t2c1Field.setStringValue(title + " " + title + " " + content);
        t4c1Field.setStringValue(title + " " + title + " " + title + " " + title + " " + content);
        t8c1Field.setStringValue(title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + content);
        t16c1Field.setStringValue(title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + content);
        t1c0Field.setStringValue(title);
        t1c2Field.setStringValue(title + " " + content + " " + content);
        t1c4Field.setStringValue(title + " " + content + " " + content + " " + content + " " + content);
        t1c8Field.setStringValue(title + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content);


        doc.add(docnumField);
        doc.add(urlField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(dochdrField);
        doc.add(allField);
        doc.add(t0c1Field);
        doc.add(t1c1Field);
        doc.add(t2c1Field);
        doc.add(t4c1Field);
        doc.add(t8c1Field);
        doc.add(t16c1Field);
        doc.add(t1c0Field);
        doc.add(t1c2Field);
        doc.add(t1c4Field);
        doc.add(t1c8Field);

        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        String docnum="";
        String title="";
        String content="";
        String dochdr="";
        String url="";

        String line = "";
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br;
            if(filename.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(filename);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
                br = new BufferedReader(decoder);
            }
            else
                br = new BufferedReader(new FileReader(filename));

            try {
                line = br.readLine();
                while (line != null){
                    if (line.startsWith("<DOC>")) {
                        text = new StringBuilder();
                    }
                    text.append(line + "\n");

                    if (line.startsWith("</DOC>")){

                        System.out.println("Docnum");
                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(text.toString());
                        Elements docnoElements = jsoupDoc.getElementsByTag("DOCNO");
                        if (docnoElements!=null && docnoElements.size()==1) {
                            String docno = docnoElements.text();
                            docnum=docno;
                        }

                        System.out.println("DocHdr");
                        StringBuilder dochdrBuilder = new StringBuilder();
                        Elements dochdrElements = jsoupDoc.getElementsByTag("DOCHDR");
                        if (dochdrElements!=null) {
                            ListIterator<Element> elIterator = dochdrElements.listIterator();
                            while (elIterator.hasNext())
                                dochdrBuilder.append(" ").append(elIterator.next().text());
                            dochdr=dochdrBuilder.toString();
                            url=dochdrBuilder.toString().split(" ")[1];
                        }

                        System.out.println("Content");
                        StringBuilder contentBuilder = new StringBuilder();
                        Elements contentElements = jsoupDoc.getElementsByTag("HTML");
                        if (contentElements!=null) {
                            ListIterator<Element> elIterator = contentElements.listIterator();
                            while (elIterator.hasNext())
                                contentBuilder.append(" ").append(elIterator.next().text());
                            content=contentBuilder.toString();
                        }

                        System.out.println("Title");
                        StringBuilder titleBuilder = new StringBuilder();
                        Elements titleElements = jsoupDoc.getElementsByTag("TITLE");
                        if (titleElements!=null) {
                            ListIterator<Element> elIterator = titleElements.listIterator();
                            while (elIterator.hasNext())
                                titleBuilder.append(" ").append(elIterator.next().text());
                            title=titleBuilder.toString();
                        }

                        String all = title + " " + content + " " + dochdr + " " + url;
                        createTRECWebFieldedDocument(docnum,url,dochdr,title,content,all);
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