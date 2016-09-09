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

import lucene4ir.bm25f.BM25FBooleanTermQuery.BM25FTermWeight;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.util.Bits;

/**
 * Scorer for a query composed by only one term. Documents are scored using the
 * BM25F ranking function.
 * 
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Nov 25, 2012
 */
public class BM25FTermScorer extends Scorer {
  SimScorer[] scorers;
  PostingsEnum[] docsEnums;
  Bits acceptDocs;
  int docId = 0;
  float k1;
  float idf;
  BM25DocIdSetIterator iterator;
  
  
  /**
   * @param bm25fTermWeight
   * @param docs
   * @param scorers
   * @param docFreq
   * @throws IOException 
   */
  public BM25FTermScorer(BM25FTermWeight bm25fTermWeight, SimScorer[] scorers,
      PostingsEnum[] docs, Bits acceptDocs) throws IOException {
    super(bm25fTermWeight);
    this.scorers = scorers;
    this.docsEnums = docs;
    this.acceptDocs = acceptDocs;
    idf = bm25fTermWeight.idf;
    k1 = bm25fTermWeight.k1;
    iterator = new BM25DocIdSetIterator();
    
  }
  
  public int getFieldFreq(int field) throws IOException {
    if (docsEnums[field] == null) {
      return 0;
    }
    return docsEnums[field].freq();
  }
  
  @Override
  public float score() throws IOException {
    float acum = 0;
    
    for (int i = 0; i < scorers.length; i++) {
      if ((docsEnums[i] == null) || (scorers[i] == null)) {
        continue;
      }
      if (docsEnums[i].docID() == docId) {
        acum += scorers[i].score(docId, docsEnums[i].freq());
      }
    }
    
    final float den = acum + k1;
    if (den == 0) {
      return 0;
    }
    final float score = (idf * acum) / den;
    
    return score;
  }
  
  @Override
  public int freq() throws IOException {
    int freq = 0;
    for (int i = 0; i < scorers.length; i++) {
      if ((docsEnums[i] == null) || (scorers[i] == null)) {
        continue;
      }
      if (docsEnums[i].docID() == docId) {
        freq += docsEnums[i].freq();
      }
    }
    return freq;
  }
  
  @Override
  public int docID() {
    return docId;
  }
  
  @Override
  public DocIdSetIterator iterator() {
    return iterator;
  }
  
  public class BM25DocIdSetIterator extends DocIdSetIterator {
    
    public BM25DocIdSetIterator() throws IOException {
      boolean result = false;
      int min = DocIdSetIterator.NO_MORE_DOCS;
      
      for (int i = 0; i < docsEnums.length; i++) {
        if ((docsEnums[i] == null) || (scorers[i] == null)) {
          continue;
        }
        if (docsEnums[i].nextDoc() < DocIdSetIterator.NO_MORE_DOCS) {
          result = true;
          min = Math.min(min, docsEnums[i].docID());
        }
      }
      docId = min;
    }
    
    private int _nextDoc() throws IOException {
      
      int min = DocIdSetIterator.NO_MORE_DOCS;
      for (int i = 0; i < docsEnums.length; i++) {
        if ((docsEnums[i] == null) || (scorers[i] == null)) {
          continue;
        }
        
        if (docsEnums[i].docID() == docId) {
          docsEnums[i].nextDoc();
          
        }
        min = Math.min(min, docsEnums[i].docID());
        
      }
      docId = min;
      
      return docId;
      
    }
    
    @Override
    public int advance(int target) throws IOException {
      docId = 0;
      while ((docId = nextDoc()) < target) {}
      return docId;
    }
    
    @Override
    public long cost() {
      // TODO Auto-generated method stub
      return 0;
    }
    
    @Override
    public int docID() {
      return docId;
    }
    
    @Override
    public int nextDoc() throws IOException {
      int nextDoc = _nextDoc();
      while ((nextDoc != NO_MORE_DOCS) && (acceptDocs != null)
          && !acceptDocs.get(nextDoc)) {
        nextDoc = _nextDoc();
      }
      return nextDoc;
    }
    
  }
  
}
