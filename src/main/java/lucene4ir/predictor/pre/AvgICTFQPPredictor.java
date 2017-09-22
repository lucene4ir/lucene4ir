package lucene4ir.predictor.pre;

import lucene4ir.predictor.PreQPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by Harry Scells on 28/8/17.
 * Averaged Inverse Collection Term Frequency (AvICTF)
 * ICF = log(term_count / tf(q) )
 */
public class AvgICTFQPPredictor extends PreQPPredictor {

    private double termCount;

    public AvgICTFQPPredictor(IndexReader ir) {
        super(ir);
        try {
            termCount = getTermCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        String[] termTuples = q.toString().split(" ");
        double sumICF = 0.0;
        double m = termTuples.length;

        for (String termTuple : termTuples) {
            String[] terms = termTuple.split(":");
            if (terms.length == 2) {
                String term = terms[1];
                try {
                    double tf = getTF(term);
                    sumICF += log2(termCount) - log2(1 + tf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return (1 / m) * sumICF;
    }
}
