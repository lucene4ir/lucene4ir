package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Created by colin on 21/12/16.
 */

public class BigramGenerator {

    public BigramGeneratorParams p;
    public IndexReader reader;

    public HashMap<String, Integer> hmap;
    public HashMap<String, Integer> tmap;


    public BigramGenerator() {
        System.out.println("BigramGenerator");

        hmap = new HashMap<String, Integer>();
        tmap = new HashMap<String, Integer>();


    /*
    Creates a file containing bigrams from the collection.
    Collection must be indexed with a shingle tokeniser.

    Assumes index has a docnum (i.e. trec doc id), title and content fields.

     */
    }


    public void readBigramGeneratorParamsFromFile(String paramFile) {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(paramFile), BigramGeneratorParams.class);
            if (p.indexName == null) {
                 p.indexName = "apIndex";
            }
            System.out.println("Index: " + p.indexName);

            if (p.outFile == null) {
                p.outFile = "bigram.qry";
            }
            System.out.println("Output File: " + p.outFile);

            if (p.cutoff < 1) {
                p.cutoff = 0;
            }
            System.out.println("Cutoff: " + p.cutoff);

            if (p.field == null) {
                p.field = Lucene4IRConstants.FIELD_ALL;
            }
            System.out.println("Field: " + p.field);
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }


    }


    public void openReader() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(p.indexName)));

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void termsList() throws IOException {

        // again, we'll just look at the first segment.  Terms dictionaries
        // for different segments may well be different, as they depend on
        // the individual documents that have been added.
        System.out.println(reader.leaves().size());
        LeafReader leafReader = reader.leaves().get(0).reader();
        Terms terms = leafReader.terms(p.field);

        System.out.println("Extracting Terms... \n Total terms: " + terms.size());
        TermsEnum te = terms.iterator();
        BytesRef term;
        int i = 1;
        String output="";
        while ((term = te.next()) != null) {
            if (term.utf8ToString().split(" ").length > 1 && te.totalTermFreq() > p.cutoff) {
                System.out.println(term.utf8ToString() + " DF: " + te.docFreq() + " CF: " + te.totalTermFreq());
                output = output + i + " " + term.utf8ToString() + " " + te.docFreq() + " " + te.totalTermFreq() + "\n";
                i++;
            }
        }
        Files.write(Paths.get(p.outFile), output.getBytes());
    }

    public static void main(String[] args)  throws IOException {
        String statsParamFile = "";
        try {
            statsParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        BigramGenerator bigramGenerator = new BigramGenerator();

        bigramGenerator.readBigramGeneratorParamsFromFile(statsParamFile);

        bigramGenerator.openReader();
        bigramGenerator.termsList();
//        bigramGenerator.extractBigramsFromStoredText();
//        bigramGenerator.pruneBigrams();
//        bigramGenerator.outputBigrams();
    }



    public void extractBigramsFromStoredText() throws IOException {

        int n = reader.maxDoc();

        for (int i = 0; i < n; i++) {

            Document doc = reader.document(i);
            String all = doc.get(Lucene4IRConstants.FIELD_ALL);
            Analyzer a = new StandardAnalyzer();
            TokenStream ts = a.tokenStream(null, all);
            ts.reset();
            String w1 = "";
            String w2 = "";
            while (ts.incrementToken()) {
                w1 = w2;
                w2 = ts.getAttribute(CharTermAttribute.class).toString();
                if (w1 != "") {
                    String key = w1 + " " + w2;
                    if (hmap.containsKey(key) == true) {
                        int v = hmap.get(key);
                        hmap.put(key, v + 1);
                    } else {
                        hmap.put(key, 1);
                    }
                }
                if (tmap.containsKey(w1)==true){
                    int w = tmap.get(w1);
                    tmap.put(w1, w+1);
                }
                else {
                    tmap.put(w1,1);
                }
            }
        }
    }

    public void pruneBigrams(){

        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            if ((int)me.getValue() <= p.cutoff) {
                iterator.remove();
            }
        }
    }

    public void outputBigrams() {
        long btotal = 0;
        long ttotal = 0;

        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            btotal = btotal + (int) me.getValue();
        }

        set = tmap.entrySet();
        iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            ttotal = ttotal + (int) me.getValue();
        }

        System.out.println("Total Bigrams: " + btotal);
        System.out.println("Total Unigrams: " + ttotal);

        set = hmap.entrySet();
        iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();

            double pij = (double) (((int) me.getValue() + 1.0) / (btotal + 1.0));

            String bigram = (String) me.getKey();

            String[] terms = bigram.split(" ");

            long v1 = 1;
            long v2 = 1;

            if (tmap.containsKey(terms[0]) == true) {
                v1 = (long) tmap.get(terms[0]);
            }
            if (tmap.containsKey(terms[1]) == true) {
                v2 = (long) tmap.get(terms[1]);

            }


            double pi = (double) ((v1 + 1.0) / (ttotal + 1.0));
            double pj = (double) ((v2 + 1.0) / (ttotal + 1.0));

            double pwmi = Math.log(pij / (pi * pj));
            //System.out.println(v1 + " " + v2  + " " + pij + " " + pi + " " + pj + " " + pwmi);

            me.setValue(pwmi);
        }
        try {
            PrintWriter writer = new PrintWriter(p.outFile, "UTF-8");

            set = hmap.entrySet();
            iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                writer.println(me.getKey() + " " + me.getValue());
            }

            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
};



class BigramGeneratorParams {
    public String indexName;
    public String outFile;
    public int cutoff;
    public String field;
}