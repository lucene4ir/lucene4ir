package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 28/8/17.
 * Averaged Collection Query Similarity AvSCQ
 */
public class AvgSCQQPPredictor extends SumSCQQPPredictor {

    public AvgSCQQPPredictor(IndexReader ir) {
        super(ir);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        double m = q.toString().split(" ").length;
        return 1 / m * super.scoreQuery(qno, q);
    }
}
