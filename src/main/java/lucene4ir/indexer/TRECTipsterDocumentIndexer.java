package lucene4ir.indexer;

import lucene4ir.LuceneConstants;
import org.apache.lucene.document.*;
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
 * Edited by kojayboy on 16/08/2017.
 *
 * TODO: Titles not being read correctly by JSoup
 */
public class TRECTipsterDocumentIndexer extends DocumentIndexer {

    private static String [] contentTags = {
            "TEXT", "DD>", "DATE", "LP", "LEADPARA"
    };
    private static String [] titleTags = {
            "HEAD", "HEADLINE", "TITLE", "HL",
            "TTL"
    };

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field allField;
    private Document doc;

    public TRECTipsterDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        super(indexPath, tokenFilterFile, positional);

        doc = new Document();
        initFields();
        initTipsterDoc();
    }

    private void initFields() {
        docnumField = new StringField(LuceneConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(LuceneConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(LuceneConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField("all", "", Field.Store.YES);
        }
        else {
            titleField = new TextField(LuceneConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(LuceneConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField("all", "", Field.Store.YES);
        }
    }

    private void initTipsterDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
    }

    public Document createTipsterDocument(String docid, String title, String content, String all){

        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);

        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);

        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        //Document doc = new Document();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                line = br.readLine();
                String docnum = "";
                StringBuilder title = new StringBuilder();
                StringBuilder text = new StringBuilder();
                StringBuilder content = new StringBuilder();

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
                            docnum = docidElements.text();
                            //Field docnumField = new StringField("docnum", docid, Field.Store.YES);
                            //doc.add(docnumField);
                        }

                        //StringBuilder title = new StringBuilder();

                        for (String tag : titleTags) {
                            Elements titleElements = jsoupDoc.select(tag);
                            if (titleElements!=null) {
//                                ListIterator<Element> elIterator = contentElements.listIterator();
                                System.out.println(titleElements.size() + " " + titleElements.text());
                                title.append(" ").append(titleElements.text());
//                                while (elIterator.hasNext())
//                                    System.out.println(elIterator.next().text());
                                    //title.append(" ").append(elIterator.next().text());
                            }
                        }
                        //Field titleField = new TextField("title", title.toString().trim(), Field.Store.YES);
                        //doc.add(titleField);

                        //StringBuilder content = new StringBuilder();

                        for (String tag : contentTags) {
                            Elements contentElements = jsoupDoc.getElementsByTag(tag);
                            if (contentElements!=null) {
//                                ListIterator<Element> elIterator = contentElements.listIterator();
                                    content.append(" ").append(contentElements.text());
//                                while (elIterator.hasNext())
//                                    content.append(" ").append(elIterator.next().text());
                            }
                        }
                        //Field contentField = new TextField("content", content.toString().trim(), Field.Store.YES);
                        //doc.add(contentField);

                        //Field textField = new TextField("all", (title.toString().trim() + " " + content.toString().trim()), Field.Store.YES);
                        //doc.add(textField);

                        String all = title.toString().trim() + " " + content.toString().trim();
                        createTipsterDocument(docnum.trim(), title.toString().trim(), content.toString().trim(), all);
                        addDocumentToIndex(doc);

                        docnum = "";
                        text = new StringBuilder();
                        content = new StringBuilder();
                        title = new StringBuilder();
                        //doc = new Document();
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
