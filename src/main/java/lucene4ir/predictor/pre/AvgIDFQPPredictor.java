package lucene4ir.predictor.pre;


import lucene4ir.predictor.PreQPPredictor;
import lucene4ir.predictor.QPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import java.lang.Math.*;

import java.io.IOException;

/**
 * Created by leif on 13/08/2017.
 * Averaged Inverse Document Frequency (AvIDF)
 *   IDF = log (N) / df(q) ), where N is number of documents in the collection
 *   then take the average over all q terms.
 */
public class AvgIDFQPPredictor extends PreQPPredictor {


    public AvgIDFQPPredictor(IndexReader ir){
        super(ir);
    }

    public double scoreQuery(String qno, Query q) {

        String[] terms = q.toString().split(" ");

        int qc = 0;
        double aidf = 0.0;
        for (String term : terms) {
            String[] termtext = term.split(":");
            if (termtext.length > 1) {
                // System.out.println(termtext[1]);
                qc = qc + 1;
                aidf = aidf + getIDF(termtext[1]);
            }
        }

        return aidf / qc;
    }

}