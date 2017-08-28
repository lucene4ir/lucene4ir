package lucene4ir;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Created by colin on 21/12/16.
 */

public class BigramGenerator {

    public BigramGeneratorParams p;
    public IndexReader reader;

    public BigramGenerator() {
        System.out.println("BigramGenerator");
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
            if (p.outputName == null) {
                p.outputName = "data/bigram.qry";
            }
            System.out.println("Output File: " + p.outputName);
            if (p.cutoff < 1) {
                p.cutoff = 0;
            }
            System.out.println("Cutoff: " + p.cutoff);
            if (p.field == null) {
                p.field = "all";
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

//
//        // The Terms object gives us some stats for this term within the segment
//        System.out.println("Number of docs with this term:" + terms.getDocCount());
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
        Files.write(Paths.get(p.outputName), output.getBytes());
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
    }
}

class BigramGeneratorParams {
    public String indexName;
    public String outputName;
    public int cutoff;
    public String field;
}