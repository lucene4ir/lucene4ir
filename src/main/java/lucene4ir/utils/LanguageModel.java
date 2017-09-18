package lucene4ir.utils;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.index.*;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

/**
 * Created by leif on 14/09/2017.
 */


public class LanguageModel {

    protected IndexReader reader;
    IndexSearcher searcher;
    CollectionStatistics collectionStats;
    public String field = Lucene4IRConstants.FIELD_ALL;
    public int[] doc_ids;
    public long doc_len;
    public HashMap<String,Long> termcounts = new HashMap<String,Long>();
    public long token_count;

    public LanguageModel(IndexReader ir, int doc_id) {
        reader = ir;
        searcher = new IndexSearcher(reader);
        doc_ids = new int[1];
        doc_ids[0] = doc_id;
        doc_len = getDocLength(doc_id);
        updateTermCountMap(doc_id);
        try {
            collectionStats = searcher.collectionStatistics(field);
            token_count = collectionStats.sumTotalTermFreq();
        }
        catch (IOException e) {
            System.out.println("Collection Statistics is Broken");
        }
    }

    public LanguageModel(IndexReader ir, int[] doc_ids) {
        reader = ir;
        searcher = new IndexSearcher(reader);
        this.doc_ids = doc_ids;
        int size = doc_ids.length;
        doc_len = 0;
        for (int i=0; i<size; i++)
        {
            doc_len = doc_len + getDocLength( doc_ids[i] );
            updateTermCountMap( doc_ids[i] );
            //System.out.println(doc_ids[i]);
        }
        //doc_len = getDocLength( doc_id );

        try {
            collectionStats = searcher.collectionStatistics(field);
            token_count = collectionStats.sumTotalTermFreq();
        }
        catch (IOException e) {
            System.out.println("Collection Statistics is Broken");
        }
    }


    public double getTermProb(String termText){
        if (termcounts.containsKey(termText)){
            long tf = termcounts.get(termText);

            return (tf+0.0)/(doc_len+0.0);
        }
        else {
            return 0.0;
        }
    };


    public double getDocumentTermProb(String termText){
        if (termcounts.containsKey(termText)){
            long tf = termcounts.get(termText);

            return (tf+0.0)/(doc_len+0.0);
        }
        else {
            return 0.0;
        }
    };

    public double getDocumentTermCount(String termText){
        if (termcounts.containsKey(termText)){
            long tf = termcounts.get(termText);
            return (tf+0.0);
        }
        else {
            return 0.0;
        }
    };


    public double getCollectionTermProb(String termText){
        try {
            Term termInstance = new Term(field, termText);
            long termFreq = reader.totalTermFreq(termInstance);

            double prob = (termFreq + 0.0) / (token_count +1.0);
            return prob;
        }
        catch (IOException e) {
            return 0.0;
        }
    };

    protected long getDocLength(int doc_id){

        try {
            Terms t = reader.getTermVector(doc_id, field);

            return t.getSumDocFreq();
        }
     catch (IOException e) {
            return 1;
        }
    };

    protected void updateTermCountMap(int doc_id){

        try {
            Terms t = reader.getTermVector(doc_id, field);

            if ((t != null) && (t.size() > 0)) {
                TermsEnum te = t.iterator();
                BytesRef term = null;
                PostingsEnum p = null;
                while ((term = te.next()) != null) {
                    if (termcounts.containsKey(term)){
                        long v =  termcounts.get(term);
                        termcounts.put(term.utf8ToString(), v+ te.totalTermFreq());
                    } else {
                        termcounts.put(term.utf8ToString(), te.totalTermFreq());
                    }
                    p = te.postings( p, PostingsEnum.ALL );
                }
            }
        }  catch (IOException e) {
            System.out.println("Something is wrong with the term vector");
            //
        }

    }


    public double getJMTermProb(String termText, double lambda){
        return (lambda * getDocumentTermProb(termText)) + (1-lambda)*getCollectionTermProb(termText);
    }

    public double getDirichletTermProb(String termText, double mu){
        return (getDocumentTermCount(termText) + mu * getCollectionTermProb(termText)) / ( doc_len + mu );
    }


    public void printTermVector(){
        for(Map.Entry m:termcounts.entrySet()){
            String termText = (String)m.getKey();
            double prob = getDocumentTermProb(termText);
            double cprob = getCollectionTermProb(termText);
            double jmprob = getJMTermProb(termText, 0.5);
            double dirprob = getDirichletTermProb(termText, 100);
            System.out.println(m.getKey()+" "+m.getValue() + " " + prob + " " + cprob + " " +  jmprob + " " + dirprob);
        }

    }


}

