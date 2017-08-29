package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by leif on 13/08/2017.
 */
public abstract class QPPredictor {

    protected IndexReader reader;
    public String field = "content";

    QPPredictor(IndexReader ir) {
        reader = ir;
    }

    public abstract double scoreQuery(String qno, Query q);

    double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    double getDF(String term) throws IOException {
        return reader.docFreq(new Term(field, term)) + 1;
    }

    double getIDF(String termText) {

        double idf = 1.0;
        try {
            Term termInstance = new Term(field, termText);
            //long termFreq = reader.totalTermFreq(termInstance);
            long docFreq = reader.docFreq(termInstance);
            // System.out.println(docFreq);

            long numDocs = reader.numDocs();
            // System.out.println(numDocs);
            idf = Math.log((numDocs + 1.0) / (docFreq + 1.0));
        } catch (IOException ioe) {
            System.out.println(" caught a " + ioe.getClass() +
                    "\n with message: " + ioe.getMessage());
        }

        return idf;
    }

    double getTF(String term) throws IOException {
        return reader.totalTermFreq(new Term(field, term));
    }

    final double getTermCount() throws IOException {
        int numDocs = reader.numDocs();
        long termCount = 0;
        for (int i = 0; i < numDocs; i++) {
            termCount += reader.document(i).getField(field).stringValue().split(" ").length;
        }
        return termCount;
    }
}


/*


• Query Scope (QS) [71].
  = -log (Nq )/ (doc_count)
  where doc_count is the number of docs containing at least one query term
  need to retrieve the set
  (could be approximated with longest postings list)
 */

