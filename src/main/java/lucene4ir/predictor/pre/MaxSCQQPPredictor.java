package lucene4ir.predictor.pre;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by Harry Scells on 28/8/17.
 * Maximum Collection Query Similarity MaxSCQ
 */
public class MaxSCQQPPredictor extends SumSCQQPPredictor {

    public MaxSCQQPPredictor(IndexReader ir) {
        super(ir);
    }

    public String name() {
        return "MaxSCQ";
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        String[] termTuples = q.toString().split(" ");
        double maxSCQ = 0.0;

        for (String termTuple : termTuples) {
            String[] terms = termTuple.split(":");
            if (terms.length == 2) {
                String term = terms[1];
                try {
                    double SCQ = calculateSCQ(term);
                    if (SCQ > maxSCQ) {
                        maxSCQ = SCQ;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return maxSCQ;
    }
}
