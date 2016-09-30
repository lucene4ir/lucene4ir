package org.apache.lucene.search;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.io.IOException;
import java.util.List;

/**
 *  A subclass of {@link Retriever} that provides batch retrieval
 *  by Okapi BM25 retrieval model Apache Lucene index API.
 *
 *  Created by dibuccio on 09/09/2016.
 */
public class RetrieverOkapiBM25 extends Retriever {

    public RetrieverOkapiBM25(RetrieverParams retrievalParams){
        super(retrievalParams);
    }

    @Override
    public ScoreDoc[] runQuery(String qno, String queryTerms) {

        // set BM25 parameters
        float k1 = p.k;
        float b = p.b;
        float k3 = Float.POSITIVE_INFINITY;

        ScoreDoc[] hits = new ScoreDoc[0];

        int docCount = reader.numDocs(); // TODO: should I use numDocs OR maxDocs?

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {

            // Get tokens constituting the query
            List<String> queryTokens = getTokens(fieldToQuery, queryTerms, true);

            // TODO: handle query term frequency
            // Currently only the distinct query tokens are extracted; in order to use k3,
            // the query term frequencies should be extracted (use false as last parameter value
            // to obtain the sequence of terms with repetitions)
            int[] qtf = new int[queryTokens.size()];
            for (int qt = 0; qt < queryTokens.size(); qt++) {
                qtf[qt] = 1;
            }

            // TODO: use the number of documents in the index instead of the number of documents with the field?
            final double avgDocLength = 1.0d * reader.getSumTotalTermFreq(fieldToQuery) / reader.getDocCount(fieldToQuery);

            final double[] IDFs = new double[queryTokens.size()];

            Term[] qTerms = new Term[queryTokens.size()];

            // compute the IDF for the query terms
            for (int qt = 0; qt < queryTokens.size(); qt++) {

                Term qTerm = new Term(fieldToQuery, queryTokens.get(qt));

                qTerms[qt] = qTerm;

                int docFreq = reader.docFreq(qTerm);

                IDFs[qt] = Math.log(docCount - docFreq + 0.5D) - Math.log(docFreq + 0.5D);

            }

            // priority queue where the top p.maxResult will be stored;
            // the queue is pre-populated with sentiment elements (score -inf).
            HitQueue pq = new HitQueue(p.maxResults, true);

            // number of hits currently in the queue
            int totalHits = 0;

            // iterate over the reader leaves and access one segment at a time
            for (LeafReaderContext leafReaderContext : reader.leaves()) {

                // get the posting lists of the query terms for the current leaf reader

                PostingsEnum[] postingLists = new PostingsEnum[qTerms.length];

                Terms terms = leafReaderContext.reader().terms(fieldToQuery);

                TermsEnum te = terms.iterator();

                for (int qt = 0; qt < qTerms.length; qt++) {

                    if (te.seekExact(qTerms[qt].bytes())) { // if the query term is in the term vocabulary

                        // initialize the posting list for the current term
                        postingLists[qt] = te.postings(null);

                    } else { // otherwise the posting list is set to null
                        postingLists[qt] = null;
//                        System.out.println("=> No posting list for term \""+ qTerms[qt].text()+"\"  (found: \""+te.term().utf8ToString()+"\")");
                    }

                }

                // retrieve norms that stores document lengths of the field to query
                NumericDocValues norms = leafReaderContext.reader().getNormValues(fieldToQuery);

                // iterate over the documents in the index segment
                for (int doc = 0; doc < leafReaderContext.reader().numDocs(); doc++) {

//                    System.out.printf("...processing document %s (%s) %n",
//                            reader.document(leafReaderContext.docBase + doc).getField("docnum").stringValue(),
//                            leafReaderContext.docBase + doc
//                    );

                    float score = 0.0f;

                    long docLength = norms.get(doc);

                    // iterate over all the posting list associated to query terms
                    for (int qt = 0; qt < postingLists.length; qt++) {

                        PostingsEnum pl = postingLists[qt];

                        if (pl == null) {
                            continue;
                        }

                        if (pl.docID() < doc) {
                            // advance the iterator in the current posting list until the pointed
                            // document id (pl.docID()) is equal or greater than the current document id (doc)
                            while (pl.nextDoc() < doc && pl.docID() < leafReaderContext.reader().maxDoc()) {
                                ;
                            }
                        }

                        // if the position of the iterator is in the current document (doc)
                        // then compute the document score for the corresponding query term
                        if (pl.docID() == doc) {

                            double K = k1 * ((1 - b) + b * docLength / avgDocLength);

                            if (Float.isInfinite(k3)) {
                                score += 1.0f  * (k1 + 1)
                                        * (pl.freq() / (pl.freq() + K))
                                        * IDFs[qt];
                            } else {
                                score += 1.0f * (k1 + 1)
                                        * (pl.freq() / (pl.freq() + K))
                                        * IDFs[qt]
                                        * (k3 + 1) * qtf[qt] / (k3 + qtf[qt]);
                            }

                        }

                    }

                    // if the score of this document is greater than zero adds it to the queue
                    if (score > 0) {
                        ScoreDoc sd = new ScoreDoc(doc + leafReaderContext.docBase, score);
                        pq.insertWithOverflow(sd);
                        if (totalHits< p.maxResults){
                            totalHits++;
                        }
                    }

                }

            }

            // pop all the elements from the priority queue and populate the result list (ScoreDoc[])
            ScoreDoc[] results = new ScoreDoc[totalHits];

            if (totalHits > 0 && pq.size() > 0) {
                // pop all the sentinel elements (there are pq.size() - totalHits).
                for (int i = pq.size() - totalHits; i > 0; i--) pq.pop();
                // pop the truly added elements.
                for (int i = totalHits - 1; i>= 0; i--) {
                    results[i] = pq.pop();
                }
            }

            hits = results;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return hits;

    }

}
