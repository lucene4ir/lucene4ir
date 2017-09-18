package lucene4ir.utils.trec;

/**
 * Created by Harry Scells on 14/9/17.
 */
public class TrecRun {

    private String topic;
    private int q;
    private int docId;
    private int rank;
    private double score;
    private String runId;

    public TrecRun(String topic, int q, int docId, int rank, double score, String runId) {
        this.topic = topic;
        this.q = q;
        this.docId = docId;
        this.rank = rank;
        this.score = score;
        this.runId = runId;
    }

    public String getTopic() {
        return topic;
    }

    public int getQ() {
        return q;
    }

    public int getDocId() {
        return docId;
    }

    public int getRank() {
        return rank;
    }

    public double getScore() {
        return score;
    }

    public String getRunId() {
        return runId;
    }
}
