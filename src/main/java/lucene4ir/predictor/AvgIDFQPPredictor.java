package lucene4ir.predictor;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import java.lang.Math.*;

import java.io.IOException;

/**
 * Created by leif on 13/08/2017.
 */
public class AvgIDFQPPredictor extends QPPredictor {


    public AvgIDFQPPredictor(IndexReader ir){
        super(ir);
    }

    public double scoreQuery(String qno, Query q) {

        String qstr = q.toString();
        String[] terms = q.toString().split(" ");


        int ql = terms.length;
        int qc = 0;
        double aidf = 0.0;
        for (int ti=0; ti < ql; ti++){
            String[] termtext = terms[ti].split(":");
            if (termtext.length > 1) {
                // System.out.println(termtext[1]);
                qc = qc + 1;
                aidf = aidf + getIDF(termtext[1]);
            }
        }

        return aidf / qc;
    }

    public double getIDF(String termText) {

        double idf = 1.0;
        try {
            Term termInstance = new Term("content", termText);
            //long termFreq = reader.totalTermFreq(termInstance);
            long docFreq = reader.docFreq(termInstance);
            // System.out.println(docFreq);

            long numDocs = reader.numDocs();
            // System.out.println(numDocs);
            idf = Math.log((numDocs+1.0) / (docFreq+1.0));
        }
        catch (IOException ioe){
            System.out.println(" caught a " + ioe.getClass() +
                    "\n with message: " + ioe.getMessage());
        }

        return idf;
    }

}