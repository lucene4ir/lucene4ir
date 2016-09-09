package lucene4ir.similarity;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;
import java.io.IOException;
import java.util.Collections;

public class OKAPIBM25Similarity extends Similarity {

    float k1;
    float b;
    
    public OKAPIBM25Similarity() {

	/* force it to balk */
	
	this.k1 = -1;
	this.b  = -1;
	if (k1 < 0 || b < 0)
	    throw new IllegalArgumentException("Must set k1 and b, no defaults.");
    }
    
    public OKAPIBM25Similarity(float k1, float b) {
	if (Float.isFinite(k1) == false || k1 < 0)
	    throw new IllegalArgumentException("k1 = " + k1);
	if (Float.isNaN(b) || b < 0 || b > 1)
	    throw new IllegalArgumentException("b = " + b);
	this.k1 = k1;
	this.b  = b;
    }

    public float coord(int overlap, int maxOverlap)
    {
	return 1f;
    }

    public float queryNorm(float valueForNormalization)
    {
	return 1f;
    }

    protected float idf(long n, long N) {
	return (float) Math.log(1 + (N - n + 0.5D)/(n + 0.5D));
    }

    @Override
    public final SimWeight computeWeight(CollectionStatistics collectionStats,
					 TermStatistics... termStats)
    {
	long  N, n;
	float idf_, avdl;

	idf_ = 1.0f;

	N    = collectionStats.docCount();
	if (N == -1)
	    N = collectionStats.maxDoc();

	avdl = collectionStats.sumTotalTermFreq() / N;
	
	if (termStats.length == 1) {
	    n    = termStats[0].docFreq();
	    idf_ = idf(n, N);
	}
	else { /* computation for a phrase */
	    for (final TermStatistics stat : termStats) {
		n     = stat.docFreq();
		idf_ += idf(n, N);
	    }
	}
	
	return new TFIDFWeight(collectionStats.field(), idf_, avdl);
    }

    @Override
    public final SimScorer simScorer(SimWeight sw, LeafReaderContext context)
	throws IOException
    {
	TFIDFWeight tw = (TFIDFWeight) sw;
	return new TFIDFScorer(tw, context.reader().getNormValues(tw.field));
    }

    public class TFIDFScorer extends SimScorer
    {
	private final TFIDFWeight tw;
	private final NumericDocValues norms;
    
	TFIDFScorer(TFIDFWeight tw, NumericDocValues norms)
	    throws IOException
	{
	    this.tw    = tw;
	    this.norms = norms;
	}

	@Override
	public float score(int doc, float tf)
	{
	    float idf_, dl, avdl, K, w;
	    idf_ = tw.idf_;
	    avdl = tw.avdl;
	    dl   = (float)norms.get(doc);
	    K    = k1 * (1.0f - b + b * (dl / avdl));
	    w    = ((k1 + 1.0f) * tf) / (K + tf) * idf_;
	    return w;
	}

	@Override
	public float computeSlopFactor(int distance)
	{
	    // return 1.0f / (distance + 1);
	    return 1.0f;
	}

	@Override
	public float computePayloadFactor(int doc, int start, int end, BytesRef payload)
	{
	    return 1.0f;
	}
    }
  
    public static class TFIDFWeight extends SimWeight
    {
	private final String field;
	private final float  idf_;
	private final float  avdl;
	
	public TFIDFWeight(String field, float idf_, float avdl)
	{
	    this.field = field;
	    this.idf_  = idf_;
	    this.avdl  = avdl;
	}

	@Override
	public float getValueForNormalization()
	{
	    return 1.0f;
	}

	@Override
	public void normalize(float queryNorm, float boost) {}
    }    

    @Override
    public final long computeNorm(FieldInvertState state)
    {
	return state.getLength();
    }
}
