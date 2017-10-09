package lucene4ir.predictor.post;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.utils.DocMap;
import lucene4ir.utils.KLDivergence;
import lucene4ir.utils.LanguageModel;
import lucene4ir.utils.trec.TrecRun;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * Created by Harry Scells on 29/8/17.
 * Clarity Score predictor
 */
public class CSQPPredictor extends PostQPPredictor {

    private double lambda;

    public CSQPPredictor(IndexReader ir, TrecRuns run, Double lambda) {
        super(ir, run);
        this.lambda = lambda;
    }

    public String name() {
        return "ClarityScore";
    }

    @Override
    public double scoreQuery(String qno, Query q) {
        TrecRuns topic = run.getTopic(qno);
        int[] docIds = new int[topic.size()];
        double[] scores = new double[topic.size()];
        double[] weights = new double[scores.length];

        // Create the language model with the doc ids from the run.
        int i = 0;
        for (TrecRun trecRun : topic) {
            docIds[i] = DocMap.getInstance().get(trecRun.getDocId());
            scores[i] = trecRun.getScore();
            i++;
        }

        // Find the average score for normalisation.
        double avgScore = 0.0;
        OptionalDouble optionalAvgScore = Arrays.stream(scores).average();
        if (optionalAvgScore.isPresent()) {
            avgScore = optionalAvgScore.getAsDouble();
        }

        // Calculate the weights for the language model.
        for (int j = 0; j < scores.length; j++) {
            weights[j] = (scores[j] / avgScore) / topic.size();
        }

        // Now that we have the weights, we can create the language model.
        LanguageModel qm = new LanguageModel(reader, docIds, weights);

        return qm.KLDivergence(lambda);
//        String[] terms = q.toString().split(" ");
//        double[] p1 = new double[terms.length];
//        double[] p2 = new double[terms.length];
//
//        // Calculate the probabilities for each term in the query.
//
//        reader.
//        i = 0;
//        for (String term : terms) {
//
//            String[] parts = term.split(":");
//            if (parts.length == 2) {
//                p1[i] = lm.getJMTermProb(parts[1], lambda);
//                p2[i] = lm.getCollectionTermProb(parts[1]);
//                i++;
//            }
//        }
//
//        // Clarity Score is the KL divergence between the query language model and the collection language model.
//        return KLDivergence.calculate(p1, p2);
    }
}
