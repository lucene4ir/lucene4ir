package lucene4ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by leif on 21/08/2016.
 */


public class ExampleStatsApp {

    public String indexName;
    public IndexReader reader;

    public ExampleStatsApp() {
        System.out.println("ExampleStats");
        /*
        Shows a number of routines to access various term, document and collection statistics

        Assumes index has a docnum (i.e. trec doc id), title and content fields.
         */
        indexName = "";
        reader = null;

    }


    public void readExampleStatsParamsFromFile(String indexParamFile) {
        try {
            IndexParams p = JAXB.unmarshal(new File(indexParamFile), IndexParams.class);
            indexName = p.indexName;

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }


    }


    public void openReader() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void docStats() {
        /*
        How to get the number of docs
         */

        long numDocs = reader.numDocs();
        long maxDocs = reader.maxDoc();
        System.out.println("Number of docs: " + numDocs + ", Max Docs = " + maxDocs);

    }


    public void termPostingsList(String termText){
    /*
        How do we iterate through the term posting list?
     */


    }


    public void iterateThroughDocList(){
        try {
            int n = reader.maxDoc();
            if (n>10) {
                n = 10;
            }
            for (int i = 0; i < n; i++) {
                Document doc = reader.document(i);
                String docnum = doc.get("docnum");
                String title = doc.get("title");
                System.out.println("docnum and title: " + docnum + " " + title);
                System.out.println(doc.get("content"));

                iterateThroughDocTermVector(i);

            }
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void iterateThroughTermPosting(String termText){
    /*
        How do we iterate through the term posting list?
     */


    }

    public void termStats(String termText){
        /*
        How to get the term frequency and document frequency of a term
         */
        try {
            Term termInstance = new Term("content", termText);
            long termFreq = reader.totalTermFreq(termInstance);
            long docFreq = reader.docFreq(termInstance);

            System.out.println("Term: "+termText+", Term Freq. = "+termFreq+", Doc Freq. = "+docFreq);

        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void iterateThroughDocTermVector(int docid){
    /*
        How do we iterate through the term vector list?

        int docid - is the document id assigned by the lucene index.

     */
        try {
            Terms t = reader.getTermVector(docid, "title");



            if ((t != null) && (t.size()>0)) {
                TermsEnum te = t.iterator();
                BytesRef term = null;

               System.out.println(t.size());

                while ((term = te.next()) != null) {
                    System.out.println("BytesRef: " + term.utf8ToString());
                    System.out.println("docFreq: " + te.docFreq());
                    System.out.println("totalTermFreq: " + te.totalTermFreq());

                }

            }
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }


    public static void main(String[] args) {


        String statsParamFile = "";

        try {
            statsParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        ExampleStatsApp statsApp = new ExampleStatsApp();

        statsApp.readExampleStatsParamsFromFile(statsParamFile);

        statsApp.openReader();
        statsApp.docStats();
        statsApp.iterateThroughDocList();
        statsApp.termStats("program");
        statsApp.termStats("programs");
        statsApp.termStats("system");
        statsApp.termStats("systems");
        statsApp.termStats("Evacuation");
        statsApp.iterateThroughDocTermVector(0);





    }
}

class ExampleStatsParams {
    public String indexName;
}
