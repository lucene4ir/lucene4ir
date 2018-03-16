package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.*;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Indexer for TIPSTER test collections relying on JSOUP.
 *
 * Created by dibuccio on 26/09/2016.
 * Edited by kojayboy on 16/08/2017.
 *
 * TODO: Titles not being read correctly by JSoup
 * TODO: Impute titles.
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

    public TRECTipsterDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional, boolean imputing){
        super(indexPath, tokenFilterFile, positional, imputing);

        doc = new Document();
        initFields();
        initTipsterDoc();
    }

    private void initFields() {
        docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
        }
        else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
        }
    }

    private void initTipsterDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
    }

    public Document createTipsterDocument(String docid, String title, String content, String all){
        doc.clear();

        docnumField.setStringValue(docid);
        if(title.isEmpty() && !content.isEmpty()) {
            System.out.println("Imputing Title for " + docid);
            int str_len = 35;
            if (content.length()<str_len)
                str_len=content.length();
            String[] terms = content.substring(0,str_len).split(" ");
            for(int i = 0; i<(terms.length-1); i++){
                title+=terms[i] + " ";
            }
            System.out.println("New Title: " + title);
        }
        titleField.setStringValue(title);
        allField.setStringValue(all);
        if(content.isEmpty() && !title.isEmpty()) {
            System.out.println("Imputing Content for " + docid);
            content=title;
            System.out.println("New Content: " + content);
        }
        textField.setStringValue(content);

        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);

        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";

        try {
            BufferedReader br = openDocumentFile(filename);
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
                        }

                        for (String tag : titleTags) {
                            Elements titleElements = jsoupDoc.select(tag);
                            if (titleElements!=null) {
                                System.out.println(titleElements.size() + " " + titleElements.text());
                                title.append(" ").append(titleElements.text());
                            }
                        }

                        for (String tag : contentTags) {
                            Elements contentElements = jsoupDoc.getElementsByTag(tag);
                            if (contentElements!=null) {
                                    content.append(" ").append(contentElements.text());
                            }
                        }

                        String all = title.toString().trim() + " " + content.toString().trim();
                        createTipsterDocument(docnum.trim(), title.toString().trim(), content.toString().trim(), all);
                        addDocumentToIndex(doc);

                        docnum = "";
                        text = new StringBuilder();
                        content = new StringBuilder();
                        title = new StringBuilder();
                    }

                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
