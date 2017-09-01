package lucene4ir;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import org.apache.lucene.search.CollectionStatistics;

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
                    System.out.println(pos);
                }
            }
        }
    }

    public void iterateThroughDocList()  throws IOException {
        int n = reader.maxDoc();
        if (n>100) {
            n = 100;
        }
        for (int i = 0; i < n; i++) {
            Document doc = reader.document(i);
            // the doc.get pulls out the values stored - ONLY if you store the fields
            String docnum = doc.get("docnum");
            String title = doc.get("title");
            System.out.println("ID: " + i);
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

        So, How do we create this run time memory index.

     */
        Terms t = reader.getTermVector(docid, "title");

        if ((t != null) && (t.size()>0)) {
            TermsEnum te = t.iterator();
            BytesRef term = null;
            System.out.println(t.size());
            PostingsEnum p = null;
            while ((term = te.next()) != null) {
                System.out.println("BytesRef: " + term.utf8ToString());
                System.out.println("docFreq: " + te.docFreq());
                System.out.println("totalTermFreq: " + te.totalTermFreq());
                p = te.postings( p, PostingsEnum.ALL );
                //Print term positions
                while( p.nextDoc() != PostingsEnum.NO_MORE_DOCS ) {
                    int freq = p.freq();
                    for( int i = 0; i < freq; i++ ) {
                        int pos = p.nextPosition();
                        System.out.println("Term: " + term.utf8ToString() + " Pos: " + pos);
                    }
                }
            }
        }
    }

    public void printTermVectorWithPosition(int docid, Set<String> fields) throws IOException {

	    	Map<String, Map<String, List<Integer>>> fieldToTermToPos =
	    			this.buildTermVectorWithPosition(docid, Collections.singleton("title"));
	
	    	System.out.println("docid:" + docid);
	
	    	for(String field : fieldToTermToPos.keySet()) {
	    		System.out.println("field:" + field);
	
	    		Map<String, List<Integer>> termToPos = fieldToTermToPos.get(field);
	
	    		for(String term : termToPos.keySet()) {
	    			System.out.println("term:" + term + " freq:" + termToPos.get(term).size());
                    System.out.println("term:" + term + " pos:" + termToPos.get(term));
	    			StringBuilder posBuilder = new StringBuilder("positions:");
	    			for(int pos : termToPos.get(term)) {
	    				posBuilder.append(" ").append(pos);
	    			}
	    			posBuilder.toString();
	    		}
		    }
    	}
    
    	public Map<String, Map<String, List<Integer>>> buildTermVectorWithPosition(int docid, Set<String> fields) throws IOException {

	    	Map<String, Map<String, List<Integer>>> fieldToTermVector = new HashMap<>();
	
	    	Document doc = reader.document(docid, fields);
	
	    	MemoryIndex mi = MemoryIndex.fromDocument(doc, new StandardAnalyzer());
	    	IndexReader mr = mi.createSearcher().getIndexReader();
	
	    	for (LeafReaderContext leafContext : mr.leaves()) {
	
	    		LeafReader leaf = leafContext.reader();
	
	    		for (String field : fields) {
	    			Map<String, List<Integer>> termToPositions = new HashMap<>();
	
	    			Terms t = leaf.terms(field);
	
	    			if(t != null) {
	    				fieldToTermVector.put(field, termToPositions);
	    				TermsEnum tenum = t.iterator();
	
	    				BytesRef termBytes = null;
	    				PostingsEnum postings = null;
	    				while ((termBytes = tenum.next()) != null) {
	
	    					List<Integer> positions = new ArrayList<>();
	    					termToPositions.put(termBytes.utf8ToString(), positions);
	    					postings = tenum.postings(postings);
	    					postings.advance(0);
	
	    					for (int i = 0; i < postings.freq(); i++) {
	    						positions.add(postings.nextPosition());
	    					}
	    				}
	    			}
	    		}
	
	    	}
	    	return fieldToTermVector;
    }


    public void reportCollectionStatistics()throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);

        CollectionStatistics collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_ALL);
        long token_count = collectionStats.sumTotalTermFreq();
        long doc_count = collectionStats.docCount();
        long sum_doc_count = collectionStats.sumDocFreq();
        long avg_doc_length = token_count / doc_count;

        System.out.println("ALL: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);

        collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_TITLE);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();

        System.out.println("TITLE: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count);


        collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_CONTENT);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();

        System.out.println("CONTENT: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count);


    }


    public void countFieldData() throws IOException {
        int n = reader.maxDoc();
        int nt = 0;
        int nc = 0;

        for (int i = 0; i < n; i++) {
            Document doc = reader.document(i);

            // the doc.get pulls out the values stored - ONLY if you store the fields
            String title = doc.get(Lucene4IRConstants.FIELD_TITLE);
            String content = doc.get(Lucene4IRConstants.FIELD_CONTENT);
            if (title.length()>0){
                nt++;
            }
            if (content.length()>0){
                nc++;
            }
        }
        System.out.println("Num Docs: " +n + " Docs with Title text: " + nt + " Docs with Contents text: "+ nc);


    }


    public void extractBigramsFromStoredText() throws IOException {

        HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        int n = reader.maxDoc();

        for (int i = 0; i < n; i++) {

            Document doc = reader.document(i);
            String all = doc.get(Lucene4IRConstants.FIELD_ALL);

            //String[] words = all.split(" ");
            //for(String w: words ){
            //    System.out.println(w);
            //}

//        int n = words.length;
            //      for (int i=1; i<n; i++){
            //        System.out.println(words[i-1].toLowerCase().trim() + " " + words[i].toLowerCase().trim());
            //   }

            Analyzer a = new StandardAnalyzer();
            TokenStream ts = a.tokenStream(null, all);
            ts.reset();
            String w1 = "";
            String w2 = "";
            while (ts.incrementToken()) {
                w1 = w2;
                w2 = ts.getAttribute(CharTermAttribute.class).toString();
                if (w1 != "") {
                    //System.out.println(w1 + " " + w2);

                    String key = w1 + " " + w2;
                    if (hmap.containsKey(key)==true) {
                        int v = hmap.get(key);
                        hmap.put(key,v+1);
                    }
                    else {
                        hmap.put(key, 1);
                    }

                }
            }
        }

        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            if ((int)me.getValue() > 2) {
                System.out.print(me.getKey() + ": ");
                System.out.println(me.getValue());
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

        statsApp.printTermVectorWithPosition(0, Collections.singleton("title"));

        statsApp.reportCollectionStatistics();
        statsApp.countFieldData();
        statsApp.extractBigramsFromStoredText();
    	}

}

class ExampleStatsParams {
    public String indexName;
}
