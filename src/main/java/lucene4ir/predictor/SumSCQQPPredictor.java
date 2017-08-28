package lucene4ir.predictor;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by Harry Scells on 28/8/17.
 * Summed Collection Query Similarity (SumSCQ)
 * sum over all t in q:
 * (1 +ln( cf(t))) * (1+N/df(t) )
 */
public class SumSCQQPPredictor extends QPPredictor {

    private double docCount;

    public SumSCQQPPredictor(IndexReader ir) {
        super(ir);
        docCount = reader.numDocs() + 1;
    }

    double calculateSCQ(String term) throws IOException {
        double tf = getTF(term);
        double df = getDF(term);
        double idf = (docCount / df);
        return (1 + Math.log(1 + tf)) * Math.log(1 + idf);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        String[] termTuples = q.toString().split(" ");
        double sumSCQ = 0.0;

        for (String termTuple : termTuples) {
            String[] terms = termTuple.split(":");
            if (terms.length == 2) {
                String term = terms[1];
                try {
                    sumSCQ += calculateSCQ(term);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sumSCQ;
    }

}
