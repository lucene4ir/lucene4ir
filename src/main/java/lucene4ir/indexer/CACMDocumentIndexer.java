package lucene4ir.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by leif on 21/08/2016.
 */
public class CACMDocumentIndexer extends DocumentIndexer {

    public CACMDocumentIndexer(String indexPath){
        writer = null;
        createWriter(indexPath);
    }

    public static Document createCacmDocument(String docid, String title, String content, String author, String pubdate){
        Document doc = new Document();
        Field docnumField = new StringField("docnum", docid, Field.Store.YES);
        doc.add(docnumField);
        Field titleField = new StringField("title", title, Field.Store.YES);
        doc.add(titleField);
        Field textField = new TextField("content", content, Field.Store.YES);
        doc.add(textField);
        Field authorField = new TextField("author", author, Field.Store.YES);
        doc.add(authorField);
        Field pubdateField = new StringField("pubdate", pubdate, Field.Store.YES);
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
                            Document doc = createCacmDocument(fields[0],fields[1],fields[2],fields[3],fields[4]);
                            addDocumentToIndex(doc);

                            /*
                            System.out.println("Title: " + fields[1]);
                            System.out.println("Authors: " + fields[2]);
                            System.out.println("Pub Date: " + fields[4]);
                            System.out.println("Abstract: " + fields[3]);
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
