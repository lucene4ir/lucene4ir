package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

/**
 * Created by leif on 13/08/2017.
 */
public class TermLenQPPredictor extends QPPredictor {

    public TermLenQPPredictor(IndexReader ir){
        super(ir);
    }

    public double scoreQuery(String qno, Query q) {


        String[] terms = q.toString().split(" ");

        return terms.length;
    }

}
