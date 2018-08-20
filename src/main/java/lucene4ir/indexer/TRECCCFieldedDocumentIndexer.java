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

/**
 * Created by colin on 20/08/2018.
 */

public class TRECCCFieldedDocumentIndexer extends DocumentIndexer {
    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field authorField;
    private Field allField;
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

    public TRECCCFieldedDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional, boolean imputing) {
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
            authorField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_AUTHOR, "", Field.Store.YES);
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

        } else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TextField(Lucene4IRConstants.FIELD_AUTHOR, "", Field.Store.YES);
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

    private void initNEWSDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(authorField);
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

    public Document createCCFieldedDocument(String docid, String author, String title, String content, String all) {
        doc.clear();

        docnumField.setStringValue(docid);
        if (title.isEmpty() && !content.isEmpty() && imputeTitles) {
            System.out.println("Imputing Title for " + docid);
            int str_len = 35;
            if (content.length() < str_len)
                str_len = content.length();
            String[] terms = content.substring(0, str_len).split(" ");
            for (int i = 0; i < (terms.length - 1); i++) {
                title += terms[i] + " ";
            }
            System.out.println("New Title: " + title);
        }
        titleField.setStringValue(title);
        allField.setStringValue(all);
        textField.setStringValue(content);
        authorField.setStringValue(author);
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
        doc.add(authorField);
        doc.add(titleField);
        doc.add(textField);
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

    public void indexDocumentsFromFile(String filename) {

        try {
            String line;
            StringBuilder text = new StringBuilder();

            BufferedReader br = openDocumentFile(filename);

            try {
                line = br.readLine();
                while (line != null) {
                    if (line.startsWith("<nitf")){
                        text=new StringBuilder();
                    }
                    text.append(line).append("\n");

                    if (line.startsWith("</nitf>")){

                        String docString = text.toString();
                        // Remove all escaped entities from the string.
                        docString = docString.replaceAll("&[a-zA-Z0-9]+;", "");
                        docString = docString.replaceAll("&", "");

                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(docString);

                        String docid="";
                        Elements docidElements = jsoupDoc.getElementsByTag("doc-id");
                        if (docidElements!=null && docidElements.size()==1) {
                            String docidstr = docidElements.toString();
                            docid = docidstr.substring((docidstr.indexOf("\"")+1),docidstr.lastIndexOf("\""));
                            Field docidField = new StringField("docid", docid, Field.Store.YES);
                            doc.add(docidField);
                        }

                        String author="";
                        Elements authorElements = jsoupDoc.getElementsByTag("byline");
                        if (authorElements!=null){
                            author=authorElements.text().replace("By","").trim();
                            Field authorField = new StringField("author", author,Field.Store.YES);
                            doc.add(authorField);
                        }

                        StringBuilder title = new StringBuilder();
                        Elements titleElements = jsoupDoc.getElementsByTag("title");
                        if (titleElements!=null) {
                            ListIterator<Element> elIterator = titleElements.listIterator();
                            while (elIterator.hasNext())
                                title.append(" ").append(elIterator.next().text());
                            Field titleField = new StringField("title", title.toString().trim(), Field.Store.YES);
                            doc.add(titleField);
                        }

                        StringBuilder content = new StringBuilder();
                        Elements contentElements = jsoupDoc.getElementsByTag("block");
                        if (contentElements!=null) {
                            ListIterator<Element> elIterator = contentElements.listIterator();
                            while (elIterator.hasNext()) {
                                String txt = elIterator.next().toString();
                                if (txt.startsWith("<block class=\"full_text\"> "))
                                    content.append(txt.replaceAll("<p>","").replaceAll("</p>","\n").replaceAll("<block class=\"full_text\"> ","").replaceAll("</block>", "").trim()).append(" ");
                            }
                            Field contentField = new StringField("content", Jsoup.parse(content.toString()).text(), Field.Store.YES);
                            doc.add(contentField);
                        }

                        System.out.println("Indexing Doc: " + docid + " " + title);
                        StringBuilder all = new StringBuilder(title.toString() + " " + content.toString());
                        createCCFieldedDocument(docid,  author,  title.toString(),  content.toString(),  all.toString());

                        addDocumentToIndex(doc);

                        text = new StringBuilder();
                    }
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        }
        catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }
}