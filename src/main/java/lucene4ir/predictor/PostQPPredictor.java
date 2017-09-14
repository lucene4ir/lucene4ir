package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by leif on 13/08/2017.
 */
public abstract class PostQPPredictor {

    protected IndexReader reader;
    public String field = "content";

    public PostQPPredictor(IndexReader ir) {
        reader = ir;
    }

    public abstract double scoreQuery(String qno, Query q);
}