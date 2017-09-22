package lucene4ir.predictor.pre;

import lucene4ir.predictor.PreQPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by Harry Scells on 28/8/17.
 */
public class QSQPPredictor extends PreQPPredictor {

    public QSQPPredictor(IndexReader ir) {
        super(ir);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        // Number of documents containing at least one of the query terms.
        double Nq = Double.POSITIVE_INFINITY;
        String[] termTuples = q.toString().split(" ");

        for (String termTuple : termTuples) {
            String[] terms = termTuple.split(":");
            if (terms.length == 2) {
                String term = terms[1];
                try {
                    double df = getDF(term);
                    // What is the smallest possible df?
                    if (df < Nq && df > 0) {
                        Nq = df;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return -Math.log((1 + Nq) / (1 + reader.numDocs()));
    }
}
