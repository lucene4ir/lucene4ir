package lucene4ir.utils.trec;

/**
 * Created by Harry Scells on 14/9/17.
 */
public class TrecRun {

    private String topic;
    private String q;
    private String docId;
    private int rank;
    private double score;
    private String runId;

    public TrecRun(String topic, String q, String docId, int rank, double score, String runId) {
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

    public String getQ() {
        return q;
    }

    public String getDocId() {
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
