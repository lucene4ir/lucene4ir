package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 28/8/17.
 * Averaged Query Length (AvQL)
 * average number of characters - really just QL.
 */
public class AvgQLQPPredictor extends QPPredictor {

    public AvgQLQPPredictor(IndexReader ir) {
        super(ir);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        return q.toString().length();
    }
}
