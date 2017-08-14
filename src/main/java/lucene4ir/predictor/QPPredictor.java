package lucene4ir.predictor;

import lucene4ir.LuceneConstants;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leif on 13/08/2017.
 */
public class QPPredictor {

    protected IndexReader reader;

    public QPPredictor(){};

    public QPPredictor(IndexReader ir){
        reader = ir;

    }

    public double scoreQuery(String qno, Query q) {
        return 0.0;
    }




}


/*
• Averaged Query Length (AvQL) [111],
average number of characters - really just QL.

• Averaged Inverse Document Frequency (AvIDF) [45],
IDF = log (N) / df(q) ), where N is number of documents in the collection
then take the average over all q terms.

• Maximum Inverse Document Frequency (MaxIDF) [128],
Take max of the IDF

• Standard Deviation of IDF (DevIDF) [71],


• Averaged Inverse Collection Term Frequency (AvICTF) [71],
 ICF = log(term_count / tf(q) )


 • Simplified Clarity Score (SCS) [71],
  sum over all t in q:
  p(t|q) log p(t|q) / p(q)


• Summed Collection Query Similarity (SumSCQ) [174],

sum over all t in q:
 (1 +ln( cf(t))) * (1+N/df(t) )

• Averaged Collection Query Similarity AvSCQ [174],
• Maximum Collection Query Similarity MaxSCQ [174], and,
• Query Scope (QS) [71].
  = -log (Nq )/ (doc_count)
  where doc_count is the number of docs containing at least one query term
  need to retrieve the set
  (could be approximated with longest postings list)
 */

