package lucene4ir.bm25f;

/**
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.Similarity;

/**
 * A Query that matches documents matching boolean combinations of other
 * queries, documents are scored using the BM25F ranking function [1].
 * 
 * [1] The probabilistic relevance framework: BM25 and beyond, Robertson,
 * Stephen, Zaragoza, Hugo
 */
public class BM25FBooleanQuery extends Query implements Iterable<BooleanClause> {

	private static int maxClauseCount = 1024;

	private BM25FParameters bm25fparams;

	/**
	 * Thrown when an attempt is made to add more than
	 * {@link #getMaxClauseCount()} clauses. This typically happens if a
	 * PrefixQuery, FuzzyQuery, WildcardQuery, or TermRangeQuery is expanded to
	 * many terms during search.
	 */
	public static class TooManyClauses extends RuntimeException {
		/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TooManyClauses() {
			super("maxClauseCount is set to " + maxClauseCount);
		}
	}

	/**
	 * Return the maximum number of clauses permitted, 1024 by default. Attempts
	 * to add more than the permitted number of clauses cause
	 * {@link TooManyClauses} to be thrown.
	 * 
	 * @see #setMaxClauseCount(int)
	 */
	public static int getMaxClauseCount() {
		return maxClauseCount;
	}

	/**
	 * Set the maximum number of clauses permitted per BooleanQuery. Default
	 * value is 1024.
	 */
	public static void setMaxClauseCount(int maxClauseCount) {
		if (maxClauseCount < 1) {
      throw new IllegalArgumentException("maxClauseCount must be >= 1");
    }
		BM25FBooleanQuery.maxClauseCount = maxClauseCount;
	}

	private final ArrayList<BooleanClause> clauses = new ArrayList<BooleanClause>();
	private final boolean disableCoord;

	/** Constructs an empty boolean query. */
	public BM25FBooleanQuery() {
		disableCoord = false;
	}

	/**
	 * Constructs an empty boolean query.
	 * 
	 * {@link Similarity#coord(int,int)} may be disabled in scoring, as
	 * appropriate. For example, this score factor does not make sense for most
	 * automatically generated queries, like {@link WildcardQuery} and
	 * {@link FuzzyQuery}.
	 * 
	 * @param disableCoord
	 *            disables {@link Similarity#coord(int,int)} in scoring.
	 */
	public BM25FBooleanQuery(boolean disableCoord) {
		this.disableCoord = disableCoord;
	}

	public BM25FBooleanQuery(BM25FParameters bm25fparams) {
		this();
		this.bm25fparams = bm25fparams;

	}

	/**
	 * Returns true iff {@link Similarity#coord(int,int)} is disabled in scoring
	 * for this query instance.
	 * 
	 * @see #BooleanQuery(boolean)
	 */
	public boolean isCoordDisabled() {
		return disableCoord;
	}

	/**
	 * Specifies a minimum number of the optional BooleanClauses which must be
	 * satisfied.
	 * 
	 * <p>
	 * By default no optional clauses are necessary for a match (unless there
	 * are no required clauses). If this method is used, then the specified
	 * number of clauses is required.
	 * </p>
	 * <p>
	 * Use of this method is totally independent of specifying that any specific
	 * clauses are required (or prohibited). This number will only be compared
	 * against the number of matching optional clauses.
	 * </p>
	 * 
	 * @param min
	 *            the number of optional clauses that must match
	 */
	public void setMinimumNumberShouldMatch(int min) {
		this.minNrShouldMatch = min;
	}

	protected int minNrShouldMatch = 0;

	/**
	 * Gets the minimum number of the optional BooleanClauses which must be
	 * satisfied.
	 */
	public int getMinimumNumberShouldMatch() {
		return minNrShouldMatch;
	}

	/**
	 * Adds a clause to a boolean query.
	 * 
	 * @throws TooManyClauses
	 *             if the new number of clauses exceeds the maximum clause
	 *             number
	 * @see #getMaxClauseCount()
	 */
	public void add(Query query, BooleanClause.Occur occur) {
		add(new BooleanClause(query, occur));
	}

	/**
	 * Adds a clause to a boolean query.
	 * 
	 * @throws TooManyClauses
	 *             if the new number of clauses exceeds the maximum clause
	 *             number
	 * @see #getMaxClauseCount()
	 */
	public void add(BooleanClause clause) {
		if (clauses.size() >= maxClauseCount) {
      throw new TooManyClauses();
    }

		clauses.add(clause);
	}

	/** Returns the set of clauses in this query. */
	public BooleanClause[] getClauses() {
		return clauses.toArray(new BooleanClause[clauses.size()]);
	}

	/** Returns the list of clauses in this query. */
	public List<BooleanClause> clauses() {
		return clauses;
	}

	/**
	 * Returns an iterator on the clauses in this query. It implements the
	 * {@link Iterable} interface to make it possible to do:
	 * 
	 * <pre>
	 * for (BooleanClause clause : booleanQuery) {
	 * }
	 * </pre>
	 */
	@Override
  public final Iterator<BooleanClause> iterator() {
		return clauses().iterator();
	}

	/**
	 * Expert: the Weight for BooleanQuery, used to normalize, score and explain
	 * these queries.
	 * 
	 * <p>
	 * NOTE: this API and implementation is subject to change suddenly in the
	 * next release.
	 * </p>
	 */
	public class BM25FBooleanWeight extends Weight {
		/** The Similarity implementation. */
		protected Similarity similarity;
		protected ArrayList<Weight> weights;
		protected int maxCoord; // num optional + num required

		public BM25FBooleanWeight(IndexSearcher searcher, Similarity similarity)
				throws IOException {
		  super(BM25FBooleanQuery.this);
			this.similarity = similarity;
			if (this.similarity instanceof BM25FSimilarity) {
				((BM25FSimilarity) this.similarity).setBM25FParams(bm25fparams);
			}
			weights = new ArrayList<Weight>(clauses.size());
			final boolean termConjunction = clauses.isEmpty()
					|| (minNrShouldMatch != 0) ? false : true;
			
			final boolean needsScores = true;
			for ( int i = 0; i < clauses.size(); i++) {
				final TermQuery tq = (TermQuery)clauses.get(i).getQuery();
				final Weight w = new BM25FBooleanTermQuery(tq.getTerm(), bm25fparams)
						.createWeight(searcher, needsScores);
				// if (!(c.isRequired() && (w instanceof TermWeight))) {
				// termConjunction = false;
				// }
				weights.add(w);
				// FIXME
//				if (!c.isProhibited()) {
//          maxCoord++;
//        }
			}
			// FIXME
			//this.termConjunction = termConjunction;
		}

	
		@Override
		public float getValueForNormalization() throws IOException {
			float sum = 0.0f;
			for (int i = 0; i < weights.size(); i++) {
				// call sumOfSquaredWeights for all clauses in case of side
				// effects
				final float s = weights.get(i).getValueForNormalization(); // sum sub
																		// weights
				if (!clauses.get(i).isProhibited()) {
          // only add to sum for non-prohibited clauses
					sum += s;
        }
			}

			// FIXME
			//sum *= getBoost() * getBoost(); // boost each sub-weight

			return sum;
		}

		public float coord(int overlap, int maxOverlap) {
			return similarity.coord(overlap, maxOverlap);
		}

		@Override
		public void normalize(float norm, float topLevelBoost) {
		  // FIXME
			//topLevelBoost *= getBoost(); // incorporate boost
			for (final Weight w : weights) {
				// normalize all clauses, (even if prohibited in case of side
				// affects)
				w.normalize(norm, topLevelBoost);
			}
		}

//		@Override
//		public Explanation explain(AtomicReaderContext context, int doc)
//				throws IOException {
//			final int minShouldMatch = BM25FBooleanQuery.this
//					.getMinimumNumberShouldMatch();
//			final Explanation result = new Explanation();
//			result.setDescription("sum of:");
//
//			for (final Iterator<Weight> wIter = weights.iterator(); wIter.hasNext();) {
//				final Weight w = wIter.next();
//				result.addDetail(w.explain(context, doc));
//
//			}
//			final Scorer s = scorer(context, true, false, context.reader()
//					.getLiveDocs());
//			float score = 0;
//			final int docId = s.advance(doc);
//			if (docId == doc) {
//				score = s.score();
//			}
//			result.setValue(score);
//
//			return result;
//
//		}

		@Override
		public Scorer scorer(LeafReaderContext context)
				throws IOException {
			// TODO investigate on term conjunction
			// if (termConjunction) {
			// // specialized scorer for term conjunctions
			// return createConjunctionTermScorer(context, acceptDocs);
			// }
			final List<Scorer> required = new ArrayList<Scorer>();
			final List<Scorer> prohibited = new ArrayList<Scorer>();
			final List<Scorer> optional = new ArrayList<Scorer>();
			final Iterator<BooleanClause> cIter = clauses.iterator();
			for (final Weight w : weights) {
				final BooleanClause c = cIter.next();
				final Scorer subScorer = w.scorer(context);
				if (subScorer == null) {
					if (c.isRequired()) {
						return null;
					}
				} else if (c.isRequired()) {
					required.add(subScorer);
				} else if (c.isProhibited()) {
					prohibited.add(subScorer);
				} else {
					optional.add(subScorer);
				}
			}
			// FIXME if optional is not empty, it never finish

			// Check if we can return a BooleanScorer
			// FIXME
//			if (!scoreDocsInOrder && topScorer && (required.size() == 0)) {
//				return new BM25FBooleanScorer(this, disableCoord,
//						minNrShouldMatch, required, optional, prohibited,
//						maxCoord, acceptDocs);
//			}

			if ((required.size() == 0) && (optional.size() == 0)) {
				// no required and optional clauses.
				return null;
			} else if (optional.size() < minNrShouldMatch) {
				// either >1 req scorer, or there are 0 req scorers and at least
				// 1
				// optional scorer. Therefore if there are not enough optional
				// scorers
				// no documents will be matched by the query
				return null;
			}
			// FIXME
			return new BM25FBooleanScorer(this, disableCoord, minNrShouldMatch,
					required, optional, prohibited, maxCoord, null);
			// Return a BooleanScorer2
			// return new BooleanScorer2(this, disableCoord, minNrShouldMatch,
			// required, prohibited, optional, maxCoord);
		}

//		private Scorer createConjunctionTermScorer(LeafReaderContext context,
//				Bits acceptDocs) throws IOException {
//
//			// TODO: fix scorer API to specify "needsScores" up
//			// front, so we can do match-only if caller doesn't
//			// needs scores
//
//			final DocsAndFreqs[] docsAndFreqs = new DocsAndFreqs[weights.size()];
//			for (int i = 0; i < docsAndFreqs.length; i++) {
//				final TermWeight weight = (TermWeight) weights.get(i);
//				final Scorer scorer = weight.scorer(context, true, false,
//						acceptDocs);
//				if (scorer == null) {
//					return null;
//				} else {
//					assert scorer instanceof TermScorer;
//					docsAndFreqs[i] = new DocsAndFreqs((TermScorer) scorer);
//				}
//			}
//			return new ConjunctionTermScorer(this, disableCoord ? 1.0f : coord(
//					docsAndFreqs.length, docsAndFreqs.length), docsAndFreqs);
//		}



    @Override
    public Explanation explain(LeafReaderContext arg0, int arg1)
        throws IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void extractTerms(Set<Term> arg0) {
      // TODO Auto-generated method stub
      
    }



	}

//	@Override
//	public Weight createWeight(IndexSearcher searcher) throws IOException {
//		return new BM25FBooleanWeight(searcher);
//	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return this;
	}

//	// inherit javadoc
//	@Override
//	public void extractTerms(Set<Term> terms) {
//		for (final BooleanClause clause : clauses) {
//			clause.getQuery().extractTerms(terms);
//		}
//	}

//	@Override
//	@SuppressWarnings("unchecked")
//	public BM25FBooleanQuery clone() {
//		final BM25FBooleanQuery clone = (BM25FBooleanQuery) super.clone();
//		clone.clauses = (ArrayList<BooleanClause>) this.clauses.clone();
//		return clone;
//	}

	/** Prints a user-readable version of this query. */
//	@Override
//	public String toString(String field) {
//		final StringBuilder buffer = new StringBuilder();
//		// FIXME
////		final boolean needParens = (getBoost() != 1.0)
////				|| (getMinimumNumberShouldMatch() > 0);
////		if (needParens) {
////			buffer.append("(");
////		}
//
//		for (int i = 0; i < clauses.size(); i++) {
//			final BooleanClause c = clauses.get(i);
//			if (c.isProhibited()) {
//        buffer.append("-");
//      } else if (c.isRequired()) {
//        buffer.append("+");
//      }
//
//			final Query subQuery = c.getQuery();
//			if (subQuery != null) {
//				if (subQuery instanceof BooleanQuery) { // wrap sub-bools in
//														// parens
//					buffer.append("(");
//					buffer.append(subQuery.toString(field));
//					buffer.append(")");
//				} else {
//					buffer.append(subQuery.toString(field));
//				}
//			} else {
//				buffer.append("null");
//			}
//
//			if (i != (clauses.size() - 1)) {
//        buffer.append(" ");
//      }
//		}
//
//		if (needParens) {
//			buffer.append(")");
//		}
//
//		if (getMinimumNumberShouldMatch() > 0) {
//			buffer.append('~');
//			buffer.append(getMinimumNumberShouldMatch());
//		}
//
//		// FIXME
////		if (getBoost() != 1.0f) {
////			buffer.append(ToStringUtils.boost(getBoost()));
////		}
//
//		return buffer.toString();
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0; // FIXME
		result = (prime * result)
				+ ((bm25fparams == null) ? 0 : bm25fparams.hashCode());
		result = (prime * result) + ((clauses == null) ? 0 : clauses.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
      return true;
    }
		
		if (getClass() != obj.getClass()) {
      return false;
    }
		final BM25FBooleanQuery other = (BM25FBooleanQuery) obj;
		if (bm25fparams == null) {
			if (other.bm25fparams != null) {
        return false;
      }
		} else if (!bm25fparams.equals(other.bm25fparams)) {
      return false;
    }
		if (clauses == null) {
			if (other.clauses != null) {
        return false;
      }
		} else if (!clauses.equals(other.clauses)) {
      return false;
    }
		return true;
	}

  @Override
  public String toString(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

}
