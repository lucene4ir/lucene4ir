package lucene4ir.similarity;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;

import java.io.IOException;

/**
 * Created by leif on 09/09/2016.
 */
public class BM25LSimilarity extends BM25Similarity {

    private final float delta;

    public BM25LSimilarity(float k1, float b, float delta){
        super(k1,b);
        if (Float.isFinite(delta) == false || delta<0.0){
            throw new IllegalArgumentException("illegal delta value " + delta + ", must be non-negative finite value.");
        }
        this.delta = delta;
    }

    public BM25LSimilarity(){
        this(1.2f, 0.75f, 1.0f);
    }

    @Override
    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        BM25Stats bm25stats = (BM25Stats) stats;
        return new BM25LDocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
    }

    protected class BM25LDocScorer extends BM25DocScorer {

        BM25LDocScorer (BM25Stats stats, NumericDocValues norms) throws IOException {
            super(stats, norms);
        }

        @Override
        public float score(int doc, float freq) {
            // if there are no norms, we act as if b=0
            float norm = norms == null ? k1 : cache[(byte)norms.get(doc) & 0xFF];
            return (weightValue * freq / (freq + norm) )+ (delta * stats.idf.getValue());
        }

    }

}



