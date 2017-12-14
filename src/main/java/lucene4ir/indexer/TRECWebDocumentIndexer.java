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
 * Indexer for TRECWEB test collections relying on JSOUP.
 *
 * Created by kojayboy 28/07/2017.
 */
public class TRECWebDocumentIndexer extends DocumentIndexer {

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

    public TRECWebDocumentIndexer(String indexPath, String tokenFilterFile, boolean pos){
        super(indexPath, tokenFilterFile, pos);
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
            dochdrField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
        }
        else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            urlField = new TextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
            dochdrField = new TextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
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

    public Document createTRECWebDocument(String docid, String url, String dochdr, String title, String content, String all){
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
                        doc = createTRECWebDocument(docnum,url,dochdr,title,content,all);
                        text = new StringBuilder();
                        addDocumentToIndex(doc);
                        System.out.println("Final");
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