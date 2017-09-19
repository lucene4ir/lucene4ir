package lucene4ir.utils.trec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Harry Scells on 14/9/17.
 */
public class TrecRuns implements Iterable<TrecRun> {

    private List<TrecRun> results;

    public TrecRuns(List<TrecRun> results) {
        this.results = results;
    }

    @Override
    public Iterator<TrecRun> iterator() {
        return results.iterator();
    }

    public TrecRuns getTopic(String topic) {
        List<TrecRun> filter = new ArrayList<>();
        for (TrecRun run : results) {
            if (run.getTopic().equals(topic)) {
                filter.add(run);
            }
        }
        return new TrecRuns(filter);
    }

    public TrecRun get(int idx) {
        return results.get(idx);
    }

    public int size() {
        return results.size();
    }

    public static TrecRuns loads(String runs) throws Exception {
        List<TrecRun> trecRuns = new ArrayList<>();
        for (String line : runs.split("\n")) {
            String[] runLine = line.split("[ \t]");
            if (runLine.length != 6) {
                throw new Exception("Trec run file line length too long");
            }

            String topic = runLine[0];
            String q = runLine[1];
            String docId = runLine[2];
            int rank = Integer.parseInt(runLine[3]);
            double score = Double.parseDouble(runLine[4]);
            String runId = runLine[5];

            trecRuns.add(new TrecRun(topic, q, docId, rank, score, runId));
        }

        return new TrecRuns(trecRuns);
    }

    public static final TrecRuns load(Path path) throws Exception {
        String data = new String(Files.readAllBytes(path));
        return loads(data);
    }
}
