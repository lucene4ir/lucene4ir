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
 * Indexer for TRECWEB test collections relying on JSOUP.
 *
 * Created by kojayboy 28/07/2017.
 */

// TODO:- Extract and store anchor text in a field.
// TODO:- Create Files of Docid -> URL
// TODO:- Impute titles.

    //parse through each html doc, find anchors, output : source_doc_id, url, and anchor text
    //then make a map, {doc_id, url}
    //then convert the anchor text file, to source_doc_id, to_doc_id, anchortext
//    then you can group all the to_doc_id's together, to get all the anchor text for one doc

public class TRECWebDocumentIndexer extends DocumentIndexer {

    private static String [] contentTags = {
            "HTML"
    };
    private static String [] titleTags = {
            "title"
    };
    private final static Pattern IdPat = Pattern.compile("(.+)$");

    private final static Pattern
            scriptPat = Pattern.compile("<script(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            anchorPat = Pattern.compile("<a ([^>]*)href=[\"']?([^> '\"]+)([^>]*)>(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            relUrlPat = Pattern.compile("^/"),
            absUrlPat = Pattern.compile("^[a-z]+://"),
            nofollowPat = Pattern.compile("rel=[\"']?nofollow", Pattern.CASE_INSENSITIVE); // ignore links with rel="nofollow"
    private final static String noIndexHTML = "/$|/index\\.[a-z][a-z][a-z][a-z]?$";

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field hdrField;
    private Field allField;
    private Document doc;

    public TRECWebDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional, boolean imputing) {
        super(indexPath, tokenFilterFile, positional, imputing);

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
            hdrField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_HDR, "", Field.Store.YES);
        } else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            hdrField = new TextField(Lucene4IRConstants.FIELD_HDR, "", Field.Store.YES);
        }
    }

    private void initNEWSDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(hdrField);
    }

    public Document createWEBDocument(String docid, String hdr, String title, String content, String all) {
        doc.clear();

        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);
        hdrField.setStringValue(hdr);

        doc.add(docnumField);
        doc.add(hdrField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        StringBuilder text = new StringBuilder();
        Document doc = new Document();

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

                        String docString = text.toString();

                        // Remove all escaped entities from the string.
                        docString = docString.replaceAll("&[a-zA-Z0-9]+;", "");
                        docString = docString.replaceAll("&", "");

                        StringBuilder all = new StringBuilder();
                        String docno="";

                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(docString);
                        Elements docnoElements = jsoupDoc.getElementsByTag("DOCNO");
                        if (docnoElements!=null && docnoElements.size()==1) {
                            docno = docnoElements.text();
                            Field docnoField = new StringField("docno", docno, Field.Store.YES);
                            doc.add(docnoField);
                        }
                        StringBuilder dochdr = new StringBuilder();
                        Elements dochdrElements = jsoupDoc.getElementsByTag("DOCHDR");
                        if (dochdrElements!=null) {
                            ListIterator<Element> elIterator = dochdrElements.listIterator();
                            while (elIterator.hasNext())
                                dochdr.append(" ").append(elIterator.next().text());
                            Field dochdrField = new StringField("dochdr", dochdr.toString(), Field.Store.YES);
                            doc.add(dochdrField);
                        }
                        StringBuilder content = new StringBuilder();
                        Elements contentElements = jsoupDoc.getElementsByTag("HTML");
                        if (contentElements!=null) {
                            ListIterator<Element> elIterator = contentElements.listIterator();
                            while (elIterator.hasNext())
                                content.append(" ").append(elIterator.next().text());
                            Field contentField = new StringField("content", Jsoup.parse(content.toString()).text(), Field.Store.YES);
                            doc.add(contentField);
                        }

                        StringBuilder title = new StringBuilder();
                        Elements titleElements = jsoupDoc.getElementsByTag("TITLE");
                        if (titleElements!=null) {
                            ListIterator<Element> elIterator = titleElements.listIterator();
                            while (elIterator.hasNext())
                                title.append(" ").append(elIterator.next().text());
                            Field titleField = new StringField("title", title.toString(), Field.Store.YES);
                            doc.add(titleField);
                        }

                        all.append(title.toString() + " " + dochdr.toString() + " " + content.toString());
                        createWEBDocument(docno,dochdr.toString(),title.toString(),content.toString(), all.toString());

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