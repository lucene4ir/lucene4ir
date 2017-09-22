package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.utils.trec.TrecRun;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harry Scells on 18/9/17.
 */
public class WEGQPPredictor extends PostQPPredictor {

    private int k;

    public WEGQPPredictor(IndexReader ir, TrecRuns run, Integer k) {
        super(ir, run);
        this.k = k;
    }

    public String name() {
        return "WEG";
    }

    private double sumScores(double queryLength, double d, double D) {
        return (1 / Math.sqrt(queryLength)) * d - D;
    }


    /**
     * Estimation of the Centroid of the nprf (top-ranked yet non-pseudo) set of documents.
     *
     * @param k   Top k documents to exclude from the calculation.
     * @param run Run for a topic.
     * @return Cnprf
     */
    private double calculateCnprf(int k, TrecRuns run) {
        int n = run.size() - k;
        int i = 0;

        // Get the nprf documents.
        List<TrecRun> nprf = new ArrayList<>(n);
        for (TrecRun trecRun : run) {
            if (i < n) {
                i++;
                continue;
            }

            nprf.add(trecRun);
        }

        // Sum the scores of the nprf.
        double sumScore = nprf.stream().map(TrecRun::getScore).mapToDouble(Double::doubleValue).sum();

        // Get the average of the scores.
        return sumScore / n;
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        double queryLength = q.toString().split(" ").length;
        TrecRuns topic = run.getTopic(qno);

        // Handle the case that the query retrieves less than k documents.
        int thisK = k;
        if (topic.size() < k) {
            thisK = topic.size();
        }

        double D = calculateCnprf(thisK, topic);
        double totalScore = 0;
        for (int i = 0; i < thisK; i++) {
            double d = topic.get(i).getScore();
            totalScore += sumScores(queryLength, d, D);
        }
        return (1 / (double) thisK) * totalScore;
    }

}
