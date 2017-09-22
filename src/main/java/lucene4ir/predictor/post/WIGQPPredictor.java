package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 14/9/17.
 */
public class WIGQPPredictor extends PostQPPredictor {

    private int k;

    public WIGQPPredictor(IndexReader ir, TrecRuns run, Integer k) {
        super(ir, run);
        this.k = k;
    }

    public String name() {
        return "WIG";
    }

    private double sumScores(double queryLength, double d, double D) {
        return (1 / Math.sqrt(queryLength)) * (d - D);
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        double queryLength = q.toString().split(" ").length;
        TrecRuns topic = run.getTopic(qno);
        double D = topic.get(topic.size() - 1).getScore();
        double totalScore = 0;
        for (int i = 0; i < k; i++) {
            double d = topic.get(i).getScore();
            totalScore += sumScores(queryLength, d, D);
        }

        return (1 / (double) k) * totalScore;
    }

}
