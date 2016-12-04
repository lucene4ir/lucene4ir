package lucene4ir.indexer;

import lucene4ir.LuceneConstants;
import org.apache.lucene.document.*;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by leif on 21/08/2016.
 */
public class CACMDocumentIndexer extends DocumentIndexer {

    private Field docnumField;
    private Field titleField;
    private Field textField;
    private Field authorField;
    private Field pubdateField;
    private Document doc;

    public CACMDocumentIndexer(String indexPath, String tokenFilterFile){
        super(indexPath, tokenFilterFile);

        // Reusable document object to reduce GC overhead
        doc = new Document();
        initFields();
        initCacmDoc();
    }

    private void initFields() {
        docnumField = new StringField(LuceneConstants.FIELD_DOCNUM, "", Field.Store.YES);
        titleField = new TextField(LuceneConstants.FIELD_TITLE, "", Field.Store.YES);
        textField = new TextField(LuceneConstants.FIELD_CONTENT, "", Field.Store.YES);
        authorField = new TextField(LuceneConstants.FIELD_AUTHOR, "", Field.Store.YES);
        pubdateField = new StringField(LuceneConstants.FIELD_PUBDATE, "", Field.Store.YES);
    }

    private void initCacmDoc() {
        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(authorField);
        doc.add(pubdateField);
    }

    public Document createCacmDocument(String docid, String title, String author, String content, String pubdate){

        docnumField.setStringValue(docid);
        titleField.setStringValue(title);
        authorField.setStringValue(author);
        textField.setStringValue(content);
        pubdateField.setStringValue(pubdate);

        doc.add(docnumField);
        doc.add(titleField);
        doc.add(textField);
        doc.add(authorField);
        doc.add(pubdateField);

        return doc;
    }


    public void indexDocumentsFromFile(String filename){

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {

                String[] fields= new String[5];
                for (int i=0; i<fields.length; i++){
                    fields[i]="";
                }
                // 0 - docid, 1 - title, 2-authors, 3-content, 4-pubdate
                int fieldno = 0;


                String line = br.readLine();
                while (line != null){

                    if (line.startsWith(".I")){
                        // if there is an existing document, create doc, and add to index
                        if (fields[0] != ""){
                            doc.clear();
                            doc = createCacmDocument(fields[0],fields[1],fields[2],fields[3],fields[4]);
                            addDocumentToIndex(doc);

                            /*
                            System.out.println("Title: " + fields[1]);
                            System.out.println("Authors: " + fields[2]);
                            System.out.println("Abstract: " + fields[3]);
                            System.out.println("Pub Date: " + fields[4]);
                            */
                        }

                        // reset fields
                        for (int i=0; i<fields.length; i++){
                            fields[i]="";
                        }
                        String[] parts = line.split(" ");
                        // set field 0 to docid
                        fields[0] = parts[1];
                        System.out.println("Indexing document: " + parts[1]);
                        fieldno = 0;
                    }

                    if (line.startsWith(".T")){
                        // set field to title, capture title text
                        fieldno = 1;
                    }

                    if (line.startsWith(".A")){
                        // set field to author
                        fieldno = 2;
                    }

                    if (line.startsWith(".W")){
                        // set field to content
                        fieldno = 3;
                    }

                    if (line.startsWith(".B")){
                        // set field to pub date
                        fieldno = 4;
                    }

                    if ((line.startsWith(".X")) || (line.startsWith(".N")) ){
                        // set field to title, capture title text
                        fieldno = 6;
                    }

                    if ((fieldno > 0) && (fieldno < 5)) {
                        if (line.length()>2) {
                            fields[fieldno] += " " + line;
                        }
                    }
                    line = br.readLine();
                }
                if (fields[0] != ""){
                    Document doc = createCacmDocument(fields[0],fields[1],fields[2],fields[3],fields[4]);
                    addDocumentToIndex(doc);
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    };

}
