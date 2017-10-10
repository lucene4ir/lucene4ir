package lucene4ir.predictor.pre;

import lucene4ir.predictor.PreQPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 28/8/17.
 * Maximum Inverse Document Frequency (MaxIDF)
 * Take max of the IDF
 */
public class MaxIDFQPPredictor extends PreQPPredictor {

    public MaxIDFQPPredictor(IndexReader ir) {
        super(ir);
    }

    public String name() {
        return "MaxIDF";
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        String[] terms = q.toString().split(" ");

        double maxIDF = 0.0;
        for (String term : terms) {
            String[] innerTerms = term.split(":");
            if (innerTerms.length > 1) {
                double termIDF = getIDF(innerTerms[1]);
                if (termIDF > maxIDF) {
                    maxIDF = termIDF;
                }
            }
        }

        return maxIDF;
    }
}
