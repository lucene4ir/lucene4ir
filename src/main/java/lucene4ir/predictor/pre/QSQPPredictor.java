package lucene4ir.predictor.pre;

import lucene4ir.predictor.PreQPPredictor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Harry Scells on 28/8/17.
 */
public class QSQPPredictor extends PreQPPredictor {

    public QSQPPredictor(IndexReader ir) {
        super(ir);
    }

    public String name() {
        return "QueryScope";
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        // Number of documents containing at least one of the query terms.
        Set<Integer> Nq = new HashSet<>();
        IndexSearcher searcher = new IndexSearcher(reader);
        String[] termTuples = q.toString().split(" ");

        for (String termTuple : termTuples) {
            String[] terms = termTuple.split(":");
            if (terms.length == 2) {
                String term = terms[1];
                try {
                    // Add the docs that contain this term to the set Nq.
                    TermQuery query = new TermQuery(new Term("all", term));
                    TopDocs docs = searcher.search(query, reader.numDocs());
                    for (ScoreDoc scoreDoc : docs.scoreDocs) {
                        Nq.add(scoreDoc.doc);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        return -Math.log((1.0 + Nq.size()) / (1.0 + reader.numDocs()));
    }
}
