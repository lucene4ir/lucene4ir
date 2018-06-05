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
 * Created by kojayboy on 01/05/2018.
 */
public class TRECCCDocumentIndexer extends DocumentIndexer {
    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field authorField;
    private Field allField;
    private Document doc;

    public TRECCCDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional, boolean imputing) {
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
        } else {
            titleField = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            authorField = new TextField(Lucene4IRConstants.FIELD_AUTHOR, "", Field.Store.YES);
        }
    }

    private void initNEWSDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
        doc.add(authorField);
    }

    public Document createCCDocument(String docid, String author, String title, String content, String all) {
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

        doc.add(docnumField);
        doc.add(authorField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(allField);
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
                        createCCDocument(docid,  author,  title.toString(),  content.toString(),  all.toString());

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
//            if (filename.endsWith(".tgz")) {
//                TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(filename)));
//                TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
//                System.out.println(currentEntry.getDirectoryEntries().length);
//                for(TarArchiveEntry F : currentEntry.getDirectoryEntries())
//                    System.out.println(F.getName());
//                while (currentEntry!=null) {
//                    br = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
//                    try {
//                        line = br.readLine();
//                        while (line != null) {
//                            System.out.println(line);
//                        }
//                        line = br.readLine();
//                    } finally {
//                        br.close();
//                    }
//                }
//            }
