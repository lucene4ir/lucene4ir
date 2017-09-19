package lucene4ir.predictor.pre;

import lucene4ir.predictor.PreQPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harry Scells on 28/8/17.
 * Standard Deviation of IDF (DevIDF)
 */
public class StdDevIDFQPPredictor extends PreQPPredictor {

    public StdDevIDFQPPredictor(IndexReader ir) {
        super(ir);
    }

    private double calculateVariance(double mean, List<Double> scores) {
        double scoresSum = scores.stream().reduce(Double::sum).orElse(0.0);
        double scoresSumMinusAverage = scoresSum - mean;
        return scoresSumMinusAverage * scoresSumMinusAverage / (scores.size() - 1);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        String[] terms = q.toString().split(" ");

        // It doesn't make sense to have a std. dev. of 0 or 1 items.
        if (terms.length <= 1) {
            return 0.0;
        }

        int queryTermCount = 0;
        double avgIDF = 0.0;
        List<Double> idf = new ArrayList<>();
        for (String term : terms) {
            String[] innerTerms = term.split(":");
            if (innerTerms.length > 1) {
                double termIDF = getIDF(innerTerms[1]);
                idf.add(termIDF);
                queryTermCount = queryTermCount + 1;
                avgIDF = avgIDF + termIDF;
            }
        }

        double mean = avgIDF / queryTermCount;
        return Math.sqrt(calculateVariance(mean, idf));
    }
}
