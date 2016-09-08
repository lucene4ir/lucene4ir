package lucene4ir;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

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

    // A lucene index consists of a number of immutable segments
    // The top-level index reader contains a LeafReader for each segment
    // Fields and Terms are accessed through these LeafReaders

    public void fieldsList() throws IOException {

        // we'll just look at the first segment - generally, the fields
        // list will be the same for all segments
        LeafReader leafReader = reader.leaves().get(0).reader();
        for (String field : leafReader.fields()) {
            System.out.println(field);
        }

    }

    public void termsList(String field) throws IOException {

        // again, we'll just look at the first segment.  Terms dictionaries
        // for different segments may well be different, as they depend on
        // the individual documents that have been added.
        LeafReader leafReader = reader.leaves().get(0).reader();
        Terms terms = leafReader.terms(field);

        // The Terms object gives us some stats for this term within the segment
        System.out.println("Number of docs with this term:" + terms.getDocCount());

        TermsEnum te = terms.iterator();
        BytesRef term;
        while ((term = te.next()) != null) {
            System.out.println(term.utf8ToString());
        }

    }

    public void termPostingsList(String field, String termText) throws IOException {

        LeafReader leafReader = reader.leaves().get(0).reader();
        Terms terms = leafReader.terms(field);
        TermsEnum te = terms.iterator();
        te.seekCeil(new BytesRef(termText));

        PostingsEnum postings = te.postings(null);
        int doc;
        while ((doc = postings.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
            System.out.println(doc);
            // you can also iterate positions for each doc
            int position;
            int numPositions = postings.freq();
            for (int i = 0; i < numPositions; i++) {
                System.out.println(postings.nextPosition());
            }
        }

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
