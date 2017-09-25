package lucene4ir.utils;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.index.*;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leif on 14/09/2017.
 */


public class LanguageModel {

    protected IndexReader reader;
    IndexSearcher searcher;
    CollectionStatistics collectionStats;
    public String field = Lucene4IRConstants.FIELD_ALL;
    public int[] doc_ids;
    public double doc_len;
    public HashMap<String, Double> termcounts = new HashMap<>();
    public long token_count;

    public LanguageModel(IndexReader ir, int doc_id) {
        reader = ir;
        searcher = new IndexSearcher(reader);
        doc_ids = new int[1];
        doc_ids[0] = doc_id;
        doc_len = 0.0;
        updateTermCountMap(doc_id, 1.0);
        try {
            collectionStats = searcher.collectionStatistics(field);
            token_count = collectionStats.sumTotalTermFreq();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public LanguageModel(IndexReader ir, int[] doc_ids) {
        reader = ir;
        searcher = new IndexSearcher(reader);
        this.doc_ids = doc_ids;
        int size = doc_ids.length;
        doc_len = 0.0;
        for (int i = 0; i < size; i++) {
            System.out.println("doc id: " +  doc_ids[i]);
            updateTermCountMap(doc_ids[i], 1.0);
        }

        try {
            collectionStats = searcher.collectionStatistics(field);
            token_count = collectionStats.sumTotalTermFreq();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public LanguageModel(IndexReader ir, int[] doc_ids, double[] weights) {
        assert doc_ids.length == weights.length;

        reader = ir;
        searcher = new IndexSearcher(reader);
        this.doc_ids = doc_ids;
        int size = doc_ids.length;
        doc_len = 0.0;
        for (int i = 0; i < size; i++) {
            updateTermCountMap(doc_ids[i], weights[i]);
        }


        try {
            collectionStats = searcher.collectionStatistics(field);
            token_count = collectionStats.sumTotalTermFreq();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public double getDocumentTermProb(String termText) {
        if (termcounts.containsKey(termText)) {
            double tf = termcounts.get(termText);

            return (tf + 0.0) / (doc_len + 0.0);
        } else {
            System.out.println("Term does not occur in document.");
            return 0.0;
        }
    }

    ;

    public double getDocumentTermCount(String termText) {
        if (termcounts.containsKey(termText)) {
            double tf = termcounts.get(termText);
            return (tf + 0.0);
        } else {
            System.out.println("Term does not occur in document.");
            return 0.0;
        }
    }

    ;


    public double getCollectionTermProb(String termText) {
        double prob = 0.0;
        try {
            Term termInstance = new Term(field, termText);
            long termFreq = reader.totalTermFreq(termInstance);

            prob = (termFreq + 0.0) / (token_count + 1.0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return prob;
    }


    protected void updateTermCountMap(int doc_id, double weight) {

        try {
            Terms t = reader.getTermVector(doc_id, field);

            if ((t != null) && (t.size() > 0)) {
                TermsEnum te = t.iterator();
                BytesRef term = null;
                PostingsEnum p = null;
                while ((term = te.next()) != null) {
                    if (termcounts.containsKey(term)) {
                        double v = termcounts.get(term);
                        termcounts.put(term.utf8ToString(), v + (te.totalTermFreq() * weight));
                    } else {
                        termcounts.put(term.utf8ToString(), (te.totalTermFreq() * weight));
                    }
                    doc_len = doc_len +  (te.totalTermFreq() * weight);

                    p = te.postings(p, PostingsEnum.ALL);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }


    public double getJMTermProb(String termText, double lambda) {
        return (lambda * getDocumentTermProb(termText)) + (1 - lambda) * getCollectionTermProb(termText);
    }

    public double getDirichletTermProb(String termText, double mu) {
        return (getDocumentTermCount(termText) + mu * getCollectionTermProb(termText)) / (doc_len + mu);
    }


    public void printTermVector() {
        double tprob = 0.0;
        double tcount = 0.0;

        for (Map.Entry m : termcounts.entrySet()) {
            String termText = (String) m.getKey();
            double count = getDocumentTermCount(termText);
            tcount = tcount + count;

            double prob = getDocumentTermProb(termText);
            double cprob = getCollectionTermProb(termText);
            double jmprob = getJMTermProb(termText, 0.5);
            double dirprob = getDirichletTermProb(termText, 100);
            //System.out.println(m.getKey() + " " + m.getValue() + " " + prob + " " + cprob + " " + jmprob + " " + dirprob);
            tprob = tprob + prob;

        }
        System.out.println("Total prob mass: " + tprob + " total term count:" + tcount + " Doc size:" + doc_len);

    }


}

