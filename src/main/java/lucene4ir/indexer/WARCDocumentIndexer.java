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
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

/**
 * Created by colin on 21/06/2018.
 */

// TODO:- Extract and store anchor text in a field.
// TODO:- Create Files of Docid -> URL

// parse through each html doc, find anchors, output : source_doc_id, url, and anchor text
// then make a map, {doc_id, url}
// then convert the anchor text file, to source_doc_id, to_doc_id, anchortext
// then you can group all the to_doc_id's together, to get all the anchor text for one doc

public class WARCDocumentIndexer extends DocumentIndexer {

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field allField;
    private Field urlField;
    private Field dochdrField;
    private Document doc;

    private static String [] contentTags = {
            "HTML"
    };
    private static String [] titleTags = {
            "title"
    };

    public WARCDocumentIndexer(String indexPath, String tokenFilterFile, boolean pos, boolean imputing){
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
        }
        else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            urlField = new TextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
            dochdrField = new TextField(Lucene4IRConstants.FIELD_DOCHDR, "", Field.Store.YES);
        }
    }

    private void initWebDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(urlField);
        doc.add(dochdrField);
        doc.add(allField);
    }

    public Document createWARCDocument(String docid, String url, String dochdr, String title, String content, String all){
        doc.clear();
        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);
        urlField.setStringValue(url);
        dochdrField.setStringValue(dochdr);

        doc.add(docnumField);
        doc.add(urlField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(dochdrField);
        doc.add(allField);
        System.out.println("Adding page: "+ url + " #"  + docid + " Title: " + title);
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

                boolean inDoc=false ;

                while (line != null){
                    if (line.startsWith("WARC/1.0") && !inDoc) { //Begin Recording text for new doc.
                        System.out.println(true);
                        inDoc=true;
                        text = new StringBuilder();
                        text.append(line + "\n");
                        line = br.readLine();
                    }

                    else if (line.startsWith("WARC-Type: warcinfo") && inDoc) { // WARC info block, ignore
                        System.out.println(false);
                        inDoc=false;
                        text = new StringBuilder();
                    }

                    else if (line.startsWith("WARC-TREC-ID: ") && inDoc) {
                        docnum=line.substring(line.indexOf(":")+1).trim();
                        text.append(line + "\n");
                        line = br.readLine();
                    }

                    else if (line.startsWith("WARC-Target-URI: ") && inDoc) {
                        url=line.substring(line.indexOf(":")+1).trim();
                        text.append(line + "\n");
                        line = br.readLine();
                    }

                    else if (line.startsWith("WARC/1.0") && inDoc) { // Signifies the end of a doc
                        System.out.println(false);
                        inDoc=false; // set flag to not recording.

                        // Deal with the raw text here. Locate the page title from the <head>
                        // What should we do with documents that contain no HTML, thus no content?

                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(text.toString());

                        StringBuilder titleBuilder = new StringBuilder();
                        Elements titleElements = jsoupDoc.getElementsByTag("TITLE");
                        if (titleElements!=null) {
                            ListIterator<Element> elIterator = titleElements.listIterator();
                            while (elIterator.hasNext())
                                titleBuilder.append(" ").append(elIterator.next().text());
                            title=titleBuilder.toString();
                        }
                        text = new StringBuilder();

                        StringBuilder contentBuilder = new StringBuilder();
                        Elements contentElements = jsoupDoc.getElementsByTag("HTML");
                        if (contentElements!=null) {
                            ListIterator<Element> elIterator = contentElements.listIterator();
                            while (elIterator.hasNext())
                                contentBuilder.append(" ").append(elIterator.next().text());
                            content=Jsoup.parse(contentBuilder.toString()).text();
                        }
                        System.out.println(Jsoup.parse(content).text());

                        String all = title + " " + content + " " + url;
                        createWARCDocument(docnum,url,dochdr,title,content,all);
                        addDocumentToIndex(doc);
                        text = new StringBuilder();

                    }
                    else if  (inDoc){ // Otherwise, record text to current doc
                        text.append(line + "\n");
                        line = br.readLine();
                    }
                    else{
                        line = br.readLine();
                    }
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