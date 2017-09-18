package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Harry Scells on 29/8/17.
 * Clarity Score predictor
 */
// TODO Remove this when the class is implemented.
@Deprecated
public class CSQPPredictor extends PostQPPredictor {

    private double lambda;

    public CSQPPredictor(IndexReader ir, double lambda) {
        super(ir);
        this.lambda = 0.6;
    }


    /**
     * Relative frequency of the term in a collection as a whole.
     * @param term
     * @return
     */
    private double Pcoll_w(String term) {
//        int numDocs = reader.numDocs();
//        long termCount = 0;
//        for (int i = 0; i < numDocs; i++) {
//            termCount += Arrays.stream(reader.document(i).
//                    getFields().
//                    stream().
//                    map(field -> field.stringValue().split(" ")).
//                    toArray()).
//                    filter(t -> t == term).toArray().length;
//        }
//        return termCount;
        try {
            return reader.totalTermFreq(new Term(term));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Relative frequency of term $w$ in documents $D$.
     * @param term
     * @param documents
     * @return
     */
    private double Pml_wD(String term, List<Document> documents) {
        return documents.stream().
                map(d -> Arrays.stream(d.getFields().stream().
                        map(field -> field.stringValue().split(" ")).
                        toArray()).
                        filter(t -> t == term).
                        toArray().length).
                mapToInt(a -> a).
                sum() / documents.size();
    }


    private double PwQ(String term, List<Document> documents, String query) {
//        PwD(term, documents, lambda) *
        return 0.0;
    }

    /**
     * Relative frequencies of terms linearly smoothed with collection frequencies.
     * @param term
     * @param documents
     * @param lambda
     * @return
     */
    public double PwD(String term, List<Document> documents, double lambda) {
        return lambda * Pml_wD(term, documents) + (1 - lambda) * Pcoll_w(term);
    }


    /**
     * @param V Collection vocabulary
     * @return
     */
    public double calculateKLDivergence(List<String> V) {
        for (String term : V) {

        }
        return 0.0;
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        double sum = 0.0;
        return 0;
    }
}
