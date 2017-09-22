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
}