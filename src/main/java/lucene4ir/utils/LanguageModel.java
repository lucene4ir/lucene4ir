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
    private IndexSearcher searcher;
    private CollectionStatistics collectionStats;
    public String field = Lucene4IRConstants.FIELD_ALL;
    private int[] doc_ids;
    private double doc_len;
    private HashMap<String, Double> termCounts = new HashMap<>();
    private long token_count;

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
        doc_len = 0.0;
        for (int doc_id : doc_ids) {
            System.out.println("doc id: " + doc_id);
            updateTermCountMap(doc_id, 1.0);
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

    private double getDocumentTermProb(String termText) {
        if (termCounts.containsKey(termText)) {
            double tf = termCounts.get(termText);
            return (tf + 0.0) / (doc_len + 0.0);
        } else {
            System.out.println("Term does not occur in document.");
            return 0.0;
        }
    }

    private double getDocumentTermCount(String termText) {
        if (termCounts.containsKey(termText)) {
            double tf = termCounts.get(termText);
            return (tf + 0.0);
        } else {
            System.out.println("Term does not occur in document.");
            return 0.0;
        }
    }

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


    private void updateTermCountMap(int doc_id, double weight) {
        try {
            Terms t = reader.getTermVector(doc_id, field);
            if ((t != null) && (t.size() > 0)) {
                TermsEnum te = t.iterator();
                BytesRef term;
                PostingsEnum p = null;
                while ((term = te.next()) != null) {
                    String termText = term.utf8ToString();
                    if (termCounts.containsKey(termText)) {
                        double v = termCounts.get(termText);
                        termCounts.put(termText, v + (te.totalTermFreq() * weight));
                    } else {
                        termCounts.put(termText, (te.totalTermFreq() * weight));
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

    @SuppressWarnings("WeakerAccess")
    public double getDirichletTermProb(String termText, double mu) {
        return (getDocumentTermCount(termText) + mu * getCollectionTermProb(termText)) / (doc_len + mu);
    }


    public void printTermVector() {
        double tProb = 0.0;
        double tCount = 0.0;

        for (Map.Entry m : termCounts.entrySet()) {
            String termText = (String) m.getKey();
            double count = getDocumentTermCount(termText);
            tCount = tCount + count;

            double prob = getDocumentTermProb(termText);
            double cProb = getCollectionTermProb(termText);
            double jmProb = getJMTermProb(termText, 0.5);
            double dirProb = getDirichletTermProb(termText, 100);
            System.out.println(m.getKey() + " " + m.getValue() + " " + prob + " " + cProb + " " + jmProb + " " + dirProb);
            tProb = tProb + prob;

        }

        System.out.println("Total prob mass: " + tProb + " total term count:" + tCount + " Doc size:" + doc_len);
    }


}

