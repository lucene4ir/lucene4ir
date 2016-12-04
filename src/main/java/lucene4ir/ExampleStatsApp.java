package lucene4ir;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.document.Document;
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


     public void numSegments(){
        // how do we get the number of segements
         int segments = reader.leaves().size();
         System.out.println("Number of Segments in Index: " + segments);

         // you can use a writer to force merge - and then you will only
         // have one segment
         // the maximum number of documents in a lucene index is approx 2 millio
         // you need to go solr or elastic search for bigger collections
         // solr/es using sharding.

    }

    public void fieldsList()  throws IOException{
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
            System.out.println(term.utf8ToString() + " DF: " + te.docFreq() + " CF: " + te.totalTermFreq());

        }

    }

    public void docLength(int docid) throws IOException{
        /*
        The direct index must be stored for this to work... how do we store it, though?
         */

        Terms t = reader.getTermVector(docid, "title");

        long tot = 0;
        if ((t != null) && (t.size()>0)) {
            tot = tot + t.size();
            System.out.println("title: " + t.size());
        }

        t = reader.getTermVector(docid, "content");
        if ((t != null) && (t.size()>0)) {
            tot = tot + t.size();
            System.out.println("content: " + t.size());
        }
        System.out.println("Doc Length: " + tot);

    }


    public void termPostingsList(String field, String termText)  throws IOException {
        /*
            Note this method only iterates through the termpostings of the first segement
            in the index i.e. reader.leaves().get(0).reader();

            To go through all term postings list for a term, you need to iterate over
            both the segements, and the leafreaders.
           */


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
                    int pos = postings.nextPosition();
                    if (pos > 0){
                        //Only prints out the positions if they are indexed
                        System.out.println(pos );
                    }
                }
            }
    }


    public void iterateThroughDocList()  throws IOException {
            int n = reader.maxDoc();
            if (n>10) {
                n = 10;
            }
            for (int i = 0; i < n; i++) {
                Document doc = reader.document(i);

                // the doc.get pulls out the values stored - ONLY if you store the fields
                String docnum = doc.get("docnum");
                String title = doc.get("title");
                System.out.println("docnum and title: " + docnum + " " + title);
                //System.out.println(doc.get("content"));

                iterateThroughDocTermVector(i);

            }
    }


    public void termStats(String termText)  throws IOException{
        /*
        How to get the term frequency and document frequency of a term
         */
            Term termInstance = new Term("content", termText);
            long termFreq = reader.totalTermFreq(termInstance);
            long docFreq = reader.docFreq(termInstance);

            System.out.println("Term: "+termText+", Term Freq. = "+termFreq+", Doc Freq. = "+docFreq);

    }


    public void buildTermVector(int docid) throws IOException {
        /*

        */

        Set<String> fieldList = new HashSet<>();
        fieldList.add("content");

        Document doc = reader.document(docid, fieldList);
        MemoryIndex mi = MemoryIndex.fromDocument(doc, new StandardAnalyzer());
        IndexReader mr = mi.createSearcher().getIndexReader();

        Terms t = mr.leaves().get(0).reader().terms("content");

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
    }




    public void iterateThroughDocTermVector(int docid)  throws IOException{
    /*
        How do we iterate through the term vector list?

        int docid - is the document id assigned by the lucene index.

        This will only work is you have you have instructed the index to index the
        term vector - but this is not very efficient.

        Apparently you can use an in memory index to take the stored field, and then
        index it at run time in memory so you can iterate through it.

        So, How do we creat this run time memory index.

     */
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


    }


    public static void main(String[] args)  throws IOException {


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


        statsApp.termPostingsList("title","system");
        statsApp.fieldsList();
        statsApp.termsList("title");

        statsApp.iterateThroughDocTermVector(1);
        statsApp.docLength(1);
        statsApp.numSegments();



    }
}

class ExampleStatsParams {
    public String indexName;
}
