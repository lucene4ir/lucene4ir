package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

/**
 * Created by Harry Scells on 14/9/17.
 */
public class WIGQPPredictor extends PostQPPredictor {

    private double k;
    private TrecRuns run;

    public WIGQPPredictor(IndexReader ir, TrecRuns run, double k) {
        super(ir);
        this.k = k;
        this.run = run;
    }

    private double sumScores(double queryLength, double d, double D) {
        return (1 / Math.sqrt(queryLength)) * d - D;
    }

    @Override
    public double scoreQuery(String qno, Query q, String topicId) {
        double queryLength = q.toString().split(" ").length;
        TrecRuns topic = run.getTopic(topicId);
        double D = topic.get(topic.size() - 1).getScore();
        double totalScore = 0;
        for (int i = 0; i < k; i++) {
            double d = topic.get(i).getScore();
            totalScore += sumScores(queryLength, d, D);
        }
        return (1 / k) * totalScore;
    }

}
