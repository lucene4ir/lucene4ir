package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by leif on 13/08/2017.
 */
public class CharLenQPPredictor extends QPPredictor {


    public CharLenQPPredictor(IndexReader ir){
        super(ir);
    }

    public double scoreQuery(String qno, Query q) {

        String qstr = q.toString();
        String[] terms = q.toString().split(" ");

        int w = terms.length;
        int c = qstr.length();

        return (double)(c - (w-1));
    }

}
