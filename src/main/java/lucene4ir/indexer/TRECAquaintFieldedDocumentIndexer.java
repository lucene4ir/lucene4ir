package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;

/**
 * Created by kojayboy on 16/03/2018.
 */
public class TRECAquaintFieldedDocumentIndexer extends DocumentIndexer{
    Whitelist whiteList;

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field allField;
    private Field sourceField;
    private Field pubdateField;
    private Field t0c1Field;
    private Field t1c1Field;
    private Field t2c1Field;
    private Field t4c1Field;
    private Field t8c1Field;
    private Field t1c0Field;
    private Field t1c2Field;
    private Field t1c4Field;
    private Field t1c8Field;
    private Document doc;

    public TRECAquaintFieldedDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional, boolean imputing){
        super(indexPath, tokenFilterFile, positional, imputing);

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

        doc = new Document();
        initFields();
        initAQUAINTDoc();
    }

    private void initFields() {
        docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        pubdateField = new StringField(Lucene4IRConstants.FIELD_PUBDATE, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            sourceField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_SOURCE, "", Field.Store.YES);
            t0c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T0C1, "", Field.Store.YES);
            t1c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C1, "", Field.Store.YES);
            t2c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T2C1, "", Field.Store.YES);
            t4c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T4C1, "", Field.Store.YES);
            t8c1Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T8C1, "", Field.Store.YES);
            t1c0Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C0, "", Field.Store.YES);
            t1c2Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C2, "", Field.Store.YES);
            t1c4Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C4, "", Field.Store.YES);
            t1c8Field = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_T1C8, "", Field.Store.YES);
        }
        else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            sourceField = new TextField(Lucene4IRConstants.FIELD_SOURCE, "", Field.Store.YES);
            t0c1Field = new TextField(Lucene4IRConstants.FIELD_T0C1, "", Field.Store.YES);
            t1c1Field = new TextField(Lucene4IRConstants.FIELD_T1C1, "", Field.Store.YES);
            t2c1Field = new TextField(Lucene4IRConstants.FIELD_T2C1, "", Field.Store.YES);
            t4c1Field = new TextField(Lucene4IRConstants.FIELD_T4C1, "", Field.Store.YES);
            t8c1Field = new TextField(Lucene4IRConstants.FIELD_T8C1, "", Field.Store.YES);
            t1c0Field = new TextField(Lucene4IRConstants.FIELD_T1C0, "", Field.Store.YES);
            t1c2Field = new TextField(Lucene4IRConstants.FIELD_T1C2, "", Field.Store.YES);
            t1c4Field = new TextField(Lucene4IRConstants.FIELD_T1C4, "", Field.Store.YES);
            t1c8Field = new TextField(Lucene4IRConstants.FIELD_T1C8, "", Field.Store.YES);
        }
    }

    private void initAQUAINTDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(sourceField);
        doc.add(pubdateField);
        doc.add(allField);
        doc.add(allField);
        doc.add(t0c1Field);
        doc.add(t1c1Field);
        doc.add(t2c1Field);
        doc.add(t4c1Field);
        doc.add(t8c1Field);
        doc.add(t1c0Field);
        doc.add(t1c2Field);
        doc.add(t1c4Field);
        doc.add(t1c8Field);
    }

    public Document createTRECAQUAINTDocument(String docid, String pubdate, String source, String title, String content, String all){
        doc.clear();

        docnumField.setStringValue(docid);
        if(title.isEmpty() && !content.isEmpty() && imputeTitles) {
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
        textField.setStringValue(content);
        sourceField.setStringValue(source);
        pubdateField.setStringValue(pubdate);
        t0c1Field.setStringValue(content);
        t1c1Field.setStringValue(title + " " + content);
        t2c1Field.setStringValue(title + " " + title + " " + content);
        t4c1Field.setStringValue(title + " " + title + " " + title + " " + title + " " + content);
        t8c1Field.setStringValue(title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + title + " " + content);
        t1c0Field.setStringValue(title);
        t1c2Field.setStringValue(title + " " + content + " " + content);
        t1c4Field.setStringValue(title + " " + content + " " + content + " " + content + " " + content);
        t1c8Field.setStringValue(title + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content + " " + content);

        doc.add(docnumField);
        doc.add(pubdateField);
        doc.add(sourceField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(t0c1Field);
        doc.add(t1c1Field);
        doc.add(t2c1Field);
        doc.add(t4c1Field);
        doc.add(t8c1Field);
        doc.add(t1c0Field);
        doc.add(t1c2Field);
        doc.add(t1c4Field);
        doc.add(t1c8Field);
        System.out.println("Adding document: " + docid + " Title: " + title);
        return doc;
    }

    public void indexDocumentsFromFile(String filename){
        String line = "";
        java.lang.StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = openDocumentFile(filename);
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
    }

    public void extractFieldsFromXmlAndIndex(String xmlString){
        String docnum;
        String title;
        String content;
        String source="Unknown";
        String pubdate;

        String safeText = org.jsoup.Jsoup.clean(xmlString,whiteList);
        org.jsoup.nodes.Document jdoc = org.jsoup.Jsoup.parse(safeText);

        docnum = getFieldText(jdoc, "docno").trim();
        title = getFieldText(jdoc, "headline");
        System.out.println(title);
        if (title == "")
            title = getFieldText(jdoc, "slug");
        content = getFieldText(jdoc, "text");
        pubdate = getFieldText(jdoc, "date_time");
        if (docnum.startsWith("NYT"))
            source = "New York Times";
        if (docnum.startsWith("AP"))
            source = "Associated Press";
        if (docnum.startsWith("XIE"))
            source = "XIE";

        String all = title + " " + content + " " + source + " " + pubdate;
        doc = createTRECAQUAINTDocument(docnum,pubdate,source,title,content,all);
        addDocumentToIndex(doc);
    }

    private String getFieldText(org.jsoup.nodes.Document jdoc, String fieldName){
        String fieldText = "";
        org.jsoup.select.Elements dns = jdoc.getElementsByTag(fieldName);
        for (org.jsoup.nodes.Element dn : dns) {
            fieldText = dns.text();
        }
        return fieldText;
    }
}

