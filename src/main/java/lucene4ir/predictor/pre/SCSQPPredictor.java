package lucene4ir.predictor.pre;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 28/8/17.
 * <p>
 * Simplified Clarity Score (SCS)
 * sum over all t in q:
 * p(t|q) log p(t|q) / p(q)
 */
public class SCSQPPredictor extends AvgICTFQPPredictor {

    public SCSQPPredictor(IndexReader ir) {
        super(ir);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        double m = q.toString().split(" ").length;
        double avgICTF = super.scoreQuery(qno, q);
        if (avgICTF == 0) {
            return 0;
        }
        return log2(1 / m) + avgICTF;
    }
}
