package lucene4ir.predictor;

import lucene4ir.utils.trec.TrecRun;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leif on 13/08/2017.
 */
public abstract class PostQPPredictor extends QPPredictor {

    public String field = "content";
    protected TrecRuns run;


    public PostQPPredictor(IndexReader ir, TrecRuns run) {
        super(ir);
        this.run = run;
    }

    public abstract double scoreQuery(String qno, Query q);

    public TrecRuns getRun() {
        return run;
    }

    public List<String> getTopics() {
        List<String> topics = new ArrayList<>();
        String oldTopic = "";
        for (TrecRun trecRun : run) {
            if (!trecRun.getTopic().equals(oldTopic)) {
                topics.add(trecRun.getTopic());
            }
            oldTopic = trecRun.getTopic();
        }
        return topics;
    }
}