package lucene4ir.BiGramGenerator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by leif on 21/08/2016.
 */

public class DumpTermsApp {

    public String indexName;
    public IndexReader reader;

    public DumpTermsApp() {
        System.out.println("DumpTerms");
        /*
        Shows a number of routines to access various term, document and collection statistics

        Assumes index has a docnum (i.e. trec doc id), title and content fields.
         */
        indexName = "";
        reader = null;
    }


    public void readParamsFromFile(String indexParamFile) {
        try {
            DumpTermsParams p = JAXB.unmarshal(new File(indexParamFile), DumpTermsParams.class);
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


    public void termsList(String field) throws IOException {

        // again, we'll just look at the first segment.  Terms dictionaries
        // for different segments may well be different, as they depend on
        // the individual documents that have been added.
        LeafReader leafReader = reader.leaves().get(0).reader();

        System.out.println(reader.leaves().size());
        Terms terms = leafReader.terms(field);

        // The Terms object gives us some stats for this term within the segment
        System.out.println("Number of docs with this term:" + terms.getDocCount());

        TermsEnum te = terms.iterator();
        BytesRef term;
        while ((term = te.next()) != null) {
            System.out.println(term.utf8ToString() + " DF: " + te.docFreq() + " CF: " + te.totalTermFreq());
        }
    }

    public void getGramList(
            String field ,
            HashMap<String, BiGramGenerator.queryInfo> biGramMap ,
            int biCutoff ,
            HashMap<String, BiGramGenerator.queryInfo> uniGramMap
            ) throws IOException {


        LeafReader leafReader = reader.leaves().get(0).reader();
        String currentTerm;
        long currentFreq;
        BiGramGenerator.queryInfo currentQryInfo;

        Terms terms = leafReader.terms(field);
        TermsEnum te = terms.iterator();
        BytesRef term;


        while ((term = te.next()) != null) {
            currentTerm = term.utf8ToString().trim();
            if (!currentTerm.contains("_"))
            {
                currentQryInfo =  new BiGramGenerator("").new queryInfo();
                currentFreq = te.totalTermFreq();
                currentQryInfo.collFreq = currentFreq;
                if (currentTerm.contains(" ") && currentFreq >= biCutoff)
                    biGramMap.put(currentTerm,currentQryInfo);
                else if (!currentTerm.contains(" "))
                    uniGramMap.put(currentTerm,currentQryInfo);
            }

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


    public void reportCollectionStatistics()throws IOException {

        IndexSearcher searcher = new IndexSearcher(reader);

        CollectionStatistics collectionStats = searcher.collectionStatistics(lucene4ir.Lucene4IRConstants.FIELD_ALL);
        long token_count = collectionStats.sumTotalTermFreq();
        long doc_count = collectionStats.docCount();
        long sum_doc_count = collectionStats.sumDocFreq();
        long avg_doc_length = token_count / doc_count;

        System.out.println("ALL: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);

        collectionStats = searcher.collectionStatistics(lucene4ir.Lucene4IRConstants.FIELD_TITLE);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();
        avg_doc_length = token_count / doc_count;

        System.out.println("TITLE: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);


        collectionStats = searcher.collectionStatistics(lucene4ir.Lucene4IRConstants.FIELD_CONTENT);
        token_count = collectionStats.sumTotalTermFreq();
        doc_count = collectionStats.docCount();
        sum_doc_count = collectionStats.sumDocFreq();
        avg_doc_length = token_count / doc_count;

        System.out.println("CONTENT: Token count: " + token_count+ " Doc Count: " + doc_count + " sum doc: " + sum_doc_count + " avg doc len: " + avg_doc_length);

    }

  public void extractBigramsFromStoredText() throws IOException {

        HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        int n = reader.maxDoc();

        for (int i = 0; i < n; i++) {

            Document doc = reader.document(i);
            String all = doc.get(lucene4ir.Lucene4IRConstants.FIELD_ALL);
            
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

        DumpTermsApp dtApp = new DumpTermsApp();
        dtApp.readParamsFromFile(statsParamFile);

        dtApp.openReader();
        dtApp.reportCollectionStatistics();
        dtApp.termsList(lucene4ir.Lucene4IRConstants.FIELD_ALL);

    }

}

class DumpTermsParams {
    public String indexName;
}
