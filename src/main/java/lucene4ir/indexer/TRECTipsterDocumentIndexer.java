package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ListIterator;

/**
 * Indexer for TIPSTER test collections relying on JSOUP.
 *
 * Created by dibuccio on 26/09/2016.
 */
public class TRECTipsterDocumentIndexer extends DocumentIndexer {

    private static String [] contentTags = {
            "TEXT", "HEADLINE", "TITLE", "HL", "HEAD",
            "TTL", "DD>", "DATE", "LP", "LEADPARA"
    };

    public TRECTipsterDocumentIndexer(String indexPath, String tokenFilterFile){
        super(indexPath, tokenFilterFile);
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        StringBuilder text = new StringBuilder();
        Document doc = new Document();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                line = br.readLine();
                while (line != null){

                    if (line.startsWith("<DOC>")) {
                        text = new StringBuilder();
                        text.append("<DOC>");
                    }
                    text.append(line + "\n");

                    if (line.startsWith("</DOC>")){

                        text.append("</DOC>");

                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(text.toString());

                        Elements docidElements = jsoupDoc.getElementsByTag("DOCNO");
                        if (docidElements!=null && docidElements.size()==1) {
                            String docid = docidElements.text();
                            Field docnumField = new StringField("docnum", docid, Field.Store.YES);
                            doc.add(docnumField);
                        }

                        StringBuilder content = new StringBuilder();

                        for (String tag : contentTags) {
                            Elements contentElements = jsoupDoc.getElementsByTag(tag);
                            if (contentElements!=null) {
                                ListIterator<Element> elIterator = contentElements.listIterator();
                                while (elIterator.hasNext())
                                content.append(" ").append(elIterator.next().text());
                            }
                        }
                        Field textField = new TextField("content", content.toString().trim(), Field.Store.YES);
                        doc.add(textField);

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
