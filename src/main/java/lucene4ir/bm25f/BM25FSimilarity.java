/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package lucene4ir.bm25f;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

/**
 * BM25FSimililarity implements the BM25F similarity function.
 * 
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Nov 15, 2012
 */
public class BM25FSimilarity extends Similarity {
	/**
	 * Logger for this class
	 */
//	private static final Logger logger = LoggerFactory
//			.getLogger(BM25FSimilarity.class);

	BM25FParameters params;
	Map<String, Float> boosts;
	Map<String, Float> lengthBoosts;
	float k1;

	public BM25FSimilarity() {
		// logger.info("no defaults");
		params = new BM25FParameters();
		boosts = params.getBoosts();
		lengthBoosts = params.getbParams();
		k1 = params.getK1();
	}

	public void setBM25FParams(BM25FParameters bm25fparams) {
		params = bm25fparams;

		boosts = params.getBoosts();
		lengthBoosts = params.getbParams();
		k1 = params.getK1();
	}

	public String[] getFields() {
		return params.getFields();
	}

	public BM25FSimilarity(BM25FParameters params) {
		// logger.info("defaults");
		this.params = params;
		boosts = params.getBoosts();
		lengthBoosts = params.getbParams();
		k1 = params.getK1();
	}

	public BM25FSimilarity(float k1, Map<String, Float> boosts,
			Map<String, Float> lengthBoosts) {
		this.k1 = k1;
		this.boosts = boosts;
		this.lengthBoosts = lengthBoosts;
	}

	// Default true
	protected boolean discountOverlaps = true;

	/** @see #setDiscountOverlaps */
	public boolean getDiscountOverlaps() {
		return discountOverlaps;
	}

	/** Cache of decoded bytes. */
	private static final float[] NORM_TABLE = new float[256];

	// since lucene store the field lengths is a lossy format,
	// which is encoded in 1 byte (i.e., 256 different values).
	// the decoded values are stored in a cache.
	static {
		NORM_TABLE[0] = 0;
		for (int i = 1; i < 256; i++) {
			final float f = SmallFloat.byte315ToFloat((byte) i);

			NORM_TABLE[i] = 1.0f / (f * f);
		}
	}

	/**
	 * Determines whether overlap tokens (Tokens with 0 position increment) are
	 * ignored when computing norm. By default this is true, meaning overlap
	 * tokens do not count when computing norms.
	 */
	public void setDiscountOverlaps(boolean v) {
		discountOverlaps = v;
	}

	/**
	 * The default implementation encodes <code>boost / sqrt(length)</code> with
	 * {@link SmallFloat#floatToByte315(float)}. This is compatible with
	 * Lucene's default implementation. If you change this, then you should
	 * change {@link #decodeNormValue(byte)} to match.
	 */
	protected byte encodeNormValue(float boost, int fieldLength) {
		return SmallFloat
				.floatToByte315(boost / (float) Math.sqrt(fieldLength));
	}

	/**
	 * The default implementation returns <code>1 / f<sup>2</sup></code> where
	 * <code>f</code> is {@link SmallFloat#byte315ToFloat(byte)}.
	 */
	protected float decodeNormValue(byte b) {
		return NORM_TABLE[b & 0xFF];
	}

//	@Override
//	public final void computeNorm(FieldInvertState state, Norm norm) {
//		final int numTerms = discountOverlaps ? state.getLength()
//				- state.getNumOverlap() : state.getLength();
//		norm.setByte(encodeNormValue(state.getBoost(), numTerms));
//	}

	@Override
	public final SimScorer simScorer(SimWeight weight,
			LeafReaderContext context) throws IOException {
		final BM25FSimWeight w = (BM25FSimWeight) weight;

		return new BM25FSimScorer(w, context.reader().getNormValues(w.field));

	}

	
  @Override
  public SimWeight computeWeight(CollectionStatistics arg0,
      TermStatistics... arg1) {
    // TODO Auto-generated method stub
    return null;
  }
	/**
	 * Compute the average length for a field, given its stats.
	 * 
	 * @param the
	 *            length statistics of a field.
	 * @return the average length of the field.
	 */
	private float avgFieldLength(CollectionStatistics stats) {
		// logger.info("sum total term freq \t {}", stats.sumTotalTermFreq());
		// logger.info("doc count \t {}", stats.docCount());
		return (float) stats.sumTotalTermFreq() / (float) stats.docCount();
	}

	/** Implemented as <code>1 / (distance + 1)</code>. */
	protected float sloppyFreq(int distance) {
		return 1.0f / (distance + 1);
	}

	/** The default implementation returns <code>1</code> */
	protected float scorePayload(int doc, int start, int end, BytesRef payload) {
		return 1;
	}

//	public Explanation idfExplain(CollectionStatistics collectionStats,
//			TermStatistics termStats) {
//		final long df = termStats.docFreq();
//		final long max = collectionStats.maxDoc();
//		final float idf = idf(df, max);
//		return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max
//				+ ")");
//	}

//	public Explanation idfExplain(CollectionStatistics collectionStats,
//			TermStatistics termStats[]) {
//		final long max = collectionStats.maxDoc();
//		float idf = 0.0f;
//		final Explanation exp = new Explanation();
//		exp.setDescription("idf(), sum of:");
//		for (final TermStatistics stat : termStats) {
//			final long df = stat.docFreq();
//			final float termIdf = idf(df, max);
//			exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df
//					+ ", maxDocs=" + max + ")"));
//			idf += termIdf;
//		}
//		exp.setValue(idf);
//		return exp;
//	}

	/**
	 * Return the inverse document frequency (IDF), given the document frequency
	 * and the number of document in a collection. Implemented as
	 * 
	 * <code>log(1 + (numDocs - docFreq + 0.5)/(docFreq + 0.5))</code>.
	 * 
	 * @param numDocs
	 *            the number of documents in the index.
	 * @param docFreq
	 *            the number of documents containing the term
	 * @return the inverse document frequency.
	 * 
	 */
	protected float idf(long docFreq, long numDocs) {
		return (float) Math.log(1 + (((numDocs - docFreq) + 0.5D)
				/ (docFreq + 0.5D)));
	}

	/**
	 * @return the saturation parameter.
	 */
	public float getK1() {
		return k1;
	}

//	@Override
//	public SimWeight computeWeight(float queryBoost,
//			CollectionStatistics collectionStats, TermStatistics... termStats) {
//		final Explanation idf = termStats.length == 1 ? idfExplain(collectionStats,
//				termStats[0]) : idfExplain(collectionStats, termStats);
//
//		boosts = params.getBoosts();
//		lengthBoosts = params.getbParams();
//		k1 = params.getK1();
//
//		final String field = collectionStats.field();
//		final float avgdl = avgFieldLength(collectionStats);
//
//		// ignoring query boost, using bm25f query boost
//		float boost = 1;
//		if (boosts.containsKey(field)) {
//			boost = boosts.get(field);
//		}

		// compute freq-independent part of bm25 equation across all norm values
		// float cache[] = new float[256];
		// for (int i = 0; i < cache.length; i++) {
		// cache[i] = ((1 - bField) + bField * decodeNormValue((byte) i)
		// / avgdl);
		// System.out.println("cache " + i + "\t" + cache[i]);
		// }

//		return new BM25FSimWeight(field, idf, boost, avgdl, null, k1);
//	}

//	@Override
//	public SloppySimScorer sloppySimScorer(SimWeight weight,
//			LeafReaderContext context) throws IOException {
//		final BM25FSimWeight w = (BM25FSimWeight) weight;
//		return new BM25FSloppySimScorer(w, context.reader().normValues(w.field));
//	}

	public class BM25FSimScorer extends SimScorer {

		private final BM25FSimWeight stats;
		private final NumericDocValues norms;
		private final Map<String, Float> bParams;
		private final Map<String, Float> boosts;

		// private final float[] cache;

		BM25FSimScorer(BM25FSimWeight stats, NumericDocValues norms)
				throws IOException {

			this.stats = stats;
			bParams = params.getbParams();
			boosts = params.getBoosts();

			// this.cache = stats.cache;

			this.norms = norms;

		}

		@Override
		public float score(int doc, float freq) {

			// return queryBoost * freq / cache[norms[doc] & 0xFF];
			if ((bParams == null) || (boosts == null) || (stats.field == null)
					|| !bParams.containsKey(stats.field)) {
				// bm25f not initialited... ignore
				return 1.0f;
			}
			final float bField = bParams.get(stats.field);
			final float boost = boosts.get(stats.field);
			final float num = freq * boost;

			float den = 1 - bField;
			if (norms == null) {
				//logger.warn("no norms for field {} ", stats.field);
			} else {

				//inal byte norm = this.norms[doc];
				//den += (bField * decodeNormValue(norm)) / stats.avgdl;
			  den = norms.get(doc);
			  // FIXME check this
			}

			if (den == 0) {
        return 0;
      }
			return num / den;

		}

//		@Override
//		public Explanation explain(int doc, Explanation freq) {
//
//			return explainScore(doc, freq, stats, norms,
//					score(doc, (int) freq.getValue()));
//		}

    @Override
    public float computePayloadFactor(int arg0, int arg1, int arg2,
        BytesRef arg3) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public float computeSlopFactor(int arg0) {
      // TODO Auto-generated method stub
      return 0;
    }

  
	}

	public class BM25FSimWeight extends SimWeight {

		String field;
		Explanation idf;
		float queryBoost;
		float avgdl;
		float cache[];
		float k1;

		float topLevelBoost;

		BM25FParameters params;

		/**
		 * @param field
		 * @param idf
		 * @param queryBoost
		 * @param avgdl
		 * @param params
		 */
		public BM25FSimWeight(String field, Explanation idf, float queryBoost,
				float avgdl, float cache[], float k1) {
			this.field = field;
			this.idf = idf;
			this.queryBoost = queryBoost;
			this.avgdl = avgdl;
			this.cache = cache;
			this.k1 = k1;

		}

		@Override
		public float getValueForNormalization() {
			// we return a TF-IDF like normalization to be nice, but we don't
			// actually normalize ourselves.
			final float queryWeight = idf.getValue() * queryBoost;
			return queryWeight * queryWeight;
		}

		@Override
		public void normalize(float queryNorm, float topLevelBoost) {
			// we don't normalize with queryNorm at all, we just capture the
			// top-level boost
			// this.topLevelBoost = topLevelBoost;
			// this.weight = queryBoost * topLevelBoost;
		}

		public String getField() {
			return field;
		}

	}

  @Override
  public long computeNorm(FieldInvertState arg0) {
    // TODO Auto-generated method stub
    return 0;
  }



//	private Explanation explainScore(int doc, Explanation freq,
//			BM25FSimWeight stats, byte[] norms, float finalScore) {
//		boosts = params.getBoosts();
//		lengthBoosts = params.getbParams();
//		k1 = params.getK1();
//
//		// // return queryBoost * freq / cache[norms[doc] & 0xFF];
//		// float bField = params.getbParams().get(stats.field);
//		// float boost = params.getBoosts().get(stats.field);
//		// float num = freq * boost;
//		//
//		// float den = 1 - bField;
//		// if (norms == null) {
//		// logger.warn("no norms for field {} ", stats.field);
//		// } else {
//		//
//		// byte norm = this.norms[doc];
//		// den += bField * decodeNormValue(norm);
//		// }
//		// if (den == 0)
//		// return 0;
//		// return num / den;
//
//		final Explanation result = new Explanation();
//		result.setDescription("score(doc=" + doc + ",field=" + stats.field
//				+ ", freq=" + freq + "), division of:");
//
//		final Explanation num = new Explanation();
//		num.setDescription(" numerator, product of: ");
//		float boost = 0;
//		if (boosts != null) {
//      boost = boosts.get(stats.field);
//    }
//
//		final Explanation boostExpl = new Explanation(boost, "boost[" + stats.field
//				+ "]");
//
//		num.addDetail(freq);
//		num.addDetail(boostExpl);
//		num.setValue(freq.getValue() * boostExpl.getValue());
//		float b = 0;
//		if ((lengthBoosts != null) && (stats != null) && (stats.field != null)) {
//
//			final Float f = lengthBoosts.get(stats.field);
//			if (f != null) {
//        b = f;
//      }
//		}
//		final Explanation bField = new Explanation(b, "lengthBoost(" + stats.field
//				+ ")");
//		final Explanation averageLength = new Explanation(stats.avgdl,
//				"avgFieldLength(" + stats.field + ")");
//
//		float length = -1;
//		Explanation fieldLength;
//		final Explanation product = new Explanation();
//
//		if (norms != null) {
//			product.setDescription("denominator: ((1 - bField) + bField * length / avgFieldLength) :");
//			length = decodeNormValue(norms[doc]);
//
//			product.setValue((1 - b) + (b * (length / stats.avgdl)));
//
//			fieldLength = new Explanation(length, "length(" + stats.field + ")");
//			product.addDetail(fieldLength);
//			product.addDetail(averageLength);
//
//		} else {
//			product.setDescription("[nofieldlength] denominator: ((1 - bField)");
//			product.setValue(1 - b);
//
//		}
//
//		product.addDetail(bField);
//
//		result.addDetail(num);
//		result.addDetail(product);
//		result.setValue(finalScore);
//
//		return result;
//	}

//	public class BM25FSloppySimScorer extends SloppySimScorer {
//
//		private final BM25FSimWeight stats;
//		// private final float weightValue; // boost * idf * (k1 + 1)
//		private final byte[] norms;
//
//		BM25FSloppySimScorer(BM25FSimWeight stats, DocValues norms)
//				throws IOException {
//			this.stats = stats;
//			// this.weightValue = stats.weight ;
//			this.norms = norms == null ? null : (byte[]) norms.getSource()
//					.getArray();
//
//		}
//
//		@Override
//		public float score(int doc, float freq) {
//			// FIXME compute score in sloppy sim scorer
//			return freq;
//		}
//
//		@Override
//		public Explanation explain(int doc, Explanation freq) {
//
//			return explainScore(doc, freq, stats, norms,
//					score(doc, freq.getValue()));
//		}
//
//		@Override
//		public float computeSlopFactor(int distance) {
//			return sloppyFreq(distance);
//		}
//
//		@Override
//		public float computePayloadFactor(int doc, int start, int end,
//				BytesRef payload) {
//			return scorePayload(doc, start, end, payload);
//		}
//
//	}
//
//  @Override
//  public long computeNorm(FieldInvertState arg0) {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public SimWeight computeWeight(CollectionStatistics arg0,
//      TermStatistics... arg1) {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public SimScorer simScorer(SimWeight arg0, LeafReaderContext arg1)
//      throws IOException {
//    // TODO Auto-generated method stub
//    return null;
//  }

}
