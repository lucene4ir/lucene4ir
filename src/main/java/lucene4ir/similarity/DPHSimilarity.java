package lucene4ir.similarity;

import java.io.IOException;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.util.BytesRef;

import static org.apache.lucene.search.similarities.SimilarityBase.log2;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class DPHSimilarity extends SimilarityBase {

    @Override
    protected float score(BasicStats stats, float freq, float docLen) {
        // document length
        float docLength = docLen;
        // relative term frequency
        float f = freq / docLength;

        float norm = (1f-f) * (1f -f)/(freq+1f);

        // collection frequency (prior)


        float tfc = stats.getTotalTermFreq();
        float numberOfDocuments = stats.getNumberOfDocuments();

        float averageDocumentLength = stats.getAvgFieldLength();

        float score = (float) (freq*norm*log2((freq*
                averageDocumentLength/docLength) *
                ( numberOfDocuments/tfc)));
        if ((1f-f) > 0) {
            score += 0.5*log2(2d*Math.PI*freq*(1f-f));
        }

        score = stats.getBoost()*score;
        return score;
    }

    public long computeNorm(FieldInvertState state) {
        return (long) state.getLength();
    }

    public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
//        if (stats instanceof MultiSimilarity.SimWeight) {
//            // a multi term query (e.g. phrase). return the summation,
//            // scoring almost as if it were boolean query
//            SimWeight subStats[] = ((MultiSimilarity.MultiStats) stats).subStats;
//            SimScorer subScorers[] = new SimScorer[subStats.length];
//            for (int i = 0; i < subScorers.length; i++) {
//                BasicStats basicstats = (BasicStats) subStats[i];
//                subScorers[i] = new DPHSimScorer(basicstats, context.reader().getNormValues(basicstats.field));
//            }
//            return new MultiSimilarity.MultiSimScorer(subScorers);
//        } else {
//            BasicStats basicstats = (BasicStats) stats;
//            return new DPHSimScorer(basicstats, context.reader().getNormValues(basicstats.field));
//        }
        BasicStats basicstats = (BasicStats) stats;
//        return new DPHSimScorer(basicstats, context.reader().getNormValues(basicstats.field));
        return new DPHSimScorer(basicstats, context.reader().getNormValues(basicstats.toString()));
    }

//    @Override
//    public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {  // final was removed (KB)
//        BM25Stats bm25stats = (BM25Stats) stats;
//        return new BM25DocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
//    }

    private class DPHSimScorer extends SimScorer {
        private final BasicStats stats;
        private final NumericDocValues norms;

        DPHSimScorer(BasicStats stats, NumericDocValues norms) throws IOException {
            this.stats = stats;
            this.norms = norms;
        }

        @Override
        public float score(int doc, float freq) {
            // We have to supply something in case norms are omitted
            return DPHSimilarity.this.score(stats, freq,
                    norms == null ? 1F : norms.get(doc));
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {
            return DPHSimilarity.this.explain(stats, doc, freq,
                    norms == null ? 1F : norms.get(doc));
        }


        @Override
        public float computeSlopFactor(int distance) {
            return 1.0f / (distance + 1);
        }

        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return 1f;
        }

    }

    @Override
    public String toString() {
        return "DPH";
    }

}