package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.utils.trec.TrecRun;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 18/9/17.
 */
public class NQCQPPredictor extends PostQPPredictor {

    private int k;

    public NQCQPPredictor(IndexReader ir, TrecRuns run, Integer k) {
        super(ir, run);
        this.k = k;
    }

    private double calculateMu(double k, TrecRuns topic) {
        double score = 0.0;

        int i = 0;
        for (TrecRun trecRun : topic) {
            if (i > k) {
                break;
            }
            score += trecRun.getScore();
            i++;
        }

        return (1 / k) * score;

    }

    private double sumScores(double k, TrecRuns topic) {
        double mu = calculateMu(k, topic);

        // Only sum the scores of the top k documents.
        double score = 0.0;
        int i = 0;
        for (TrecRun trecRun : topic) {
            if (i > k) {
                break;
            }
            double d = trecRun.getScore();
            score += (1 / k) * Math.pow((d - mu), 2);
            i++;
        }

        return score;
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        TrecRuns topic = run.getTopic(qno);

        // Estimate D using the last score of the run.
        double D = topic.get(topic.size() - 1).getScore();

        return 1 / D * Math.sqrt(sumScores((double) k, topic));

    }
}
