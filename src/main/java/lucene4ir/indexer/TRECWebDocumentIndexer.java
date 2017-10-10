package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.BooleanAttribute;
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

    private static String [] contentTags = {
            "HTML"
    };
    private static String [] titleTags = {
            "title"
    };

    public TRECWebDocumentIndexer(String indexPath, String tokenFilterFile, boolean pos){
        super(indexPath, tokenFilterFile, pos);
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

                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(text.toString());
                        Elements docnoElements = jsoupDoc.getElementsByTag("DOCNO");
                        if (docnoElements!=null && docnoElements.size()==1) {
                            String docno = docnoElements.text();
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
                            Field contentField = new StringField("all", Jsoup.parse(content.toString()).text(), Field.Store.YES);
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
                        addDocumentToIndex(doc);
                        System.out.println(doc.toString());
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

//                        StringBuilder title = new StringBuilder();
//
//                        for (String tag : titleTags) {
//                            Elements contentElements = jsoupDoc.getElementsByTag(tag);
//                            if (contentElements!=null) {
//                                ListIterator<Element> elIterator = contentElements.listIterator();
//                                while (elIterator.hasNext())
//                                    title.append(" ").append(elIterator.next().text());
//                            }
//                        }
//                        Field titleField = new TextField("title", title.toString().trim(), Field.Store.YES);
////                        System.out.println(titleField.name() + ":\n" + titleField.stringValue());
//                        doc.add(titleField);
//
//                        StringBuilder content = new StringBuilder();
//
//                        for (String tag : contentTags) {
//                            Elements contentElements = jsoupDoc.getElementsByTag(tag);
//                            if (contentElements!=null) {
//                                ListIterator<Element> elIterator = contentElements.listIterator();
//                                while (elIterator.hasNext())
//                                content.append(" ").append(elIterator.next().text());
//                            }
//                        }
//                        Field contentField = new TextField("content", content.toString().trim(), Field.Store.YES);
////                        System.out.println(contentField.name() + ":\n" + contentField.stringValue());
//                        doc.add(contentField);
//
//                        Field textField = new TextField("all", (title.toString().trim() + " " + content.toString().trim()), Field.Store.YES);
////                        System.out.println(textField.name() + ":\n" + textField.stringValue());
//                        doc.add(textField);