package lucene4ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.valuesource.BytesRefFieldSource;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.javatuples.KeyValue;
import org.javatuples.Tuple;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;

/**
 * Created by colin on 31/08/2018.
 */
public class NGramGenerator {

    public NGramGeneratorParams p;
    public IndexReader reader;

    public NGramGenerator(){
        System.out.println("NGramGenerator");
    }

    public void readNGramGeneratorParamsFromFile(String paramFile) {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(paramFile), NGramGeneratorParams.class);
            if (p.indexName == null) {
                System.out.println("No index name provided.");
                System.exit(1);
            }
            System.out.println("Index: " + p.indexName);

            if (p.outFile == null) {
                p.outFile = "5gram.qry";
            }
            System.out.println("Output File: " + p.outFile);

            if (p.cutoff < 1 || p == null) {
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

    public void extractTerms() throws IOException {

        File fout = new File(p.outFile);
        FileOutputStream fos = new FileOutputStream(fout);

        OutputStreamWriter osw = new OutputStreamWriter(fos);

        System.out.println("No. docs: " + reader.maxDoc());

        for (int i=0; i<reader.maxDoc(); i++) {
            LinkedList<KeyValue> top10 = new LinkedList<KeyValue>();
//        for (int i=1; i<2; i++) { // TEST PROCESS
            Terms terms = reader.getTermVector(i, p.field);
            if (terms == null || terms.size()<5) {
                System.out.println("Null or too small Term Vector");
                continue;
            }
            TermsEnum te = terms.iterator();
            BytesRef term;
            while ((term = te.next()) != null) {
                if (te.totalTermFreq() > p.cutoff && !term.utf8ToString().contains("___")){
                    double tfidf = te.totalTermFreq() * (Math.log(reader.maxDoc()/te.docFreq()));
//                    System.out.println(term.utf8ToString() + " DF: " + te.docFreq() + " CF: " + te.totalTermFreq() + " TF.IDF: " + tfidf);

                    KeyValue<String,Double> termVal = KeyValue.with(term.utf8ToString(), tfidf);
                    if (top10.isEmpty())
                        top10.add(termVal);
                    else
                        addTerm(top10,termVal);

                }
            }
            // Now write the terms to file High to Low (0 to 10)

            String output = "";
            for (int j = top10.size()-1; j>=0; j--) {
                if(top10.size()<5)
                    continue;
                KeyValue<String, Double> t = top10.get(j);
                output = output + t.getKey() + " " ;
            }
            if (!output.isEmpty()) {
                System.out.println("Writing: " + output);
                osw.write(output + "\n");
            }
        }
        osw.close();
    }

    public LinkedList addTerm(LinkedList top10, KeyValue<String, Double> newTerm){
//        System.out.println("Adding term " + newTerm.getKey() + " TFIDF: " +newTerm.getValue());
        for (int i = top10.size()-1; i>=0; i--){
            KeyValue<String, Double> currTerm = (KeyValue<String, Double>) top10.get(i);
//            System.out.println("New score: " + newTerm.getValue() + " vs. Score at rank " + i + " " + currTerm.getValue());

            // Don't think this is needed, TermVector should not contain duplicates?
            if (newTerm == currTerm)
                break;

            // Better TF.IDF score, move up to next term in ranked list
            if (newTerm.getValue() > currTerm.getValue())
                if(i==0){
                    top10.add(i, newTerm);
                }
                else
                    continue;

            // Lower TF.IDF score, add to list at current index.
            else if (newTerm.getValue() <=  currTerm.getValue()) {
                top10.add(i+1, newTerm);
                break;
            }
        }

        if (top10.size()>10)
            top10.removeLast();

        return top10;
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

        NGramGenerator ngramGenerator = new NGramGenerator();

        ngramGenerator.readNGramGeneratorParamsFromFile(statsParamFile);

        ngramGenerator.openReader();
        ngramGenerator.extractTerms();
    }

}

class NGramGeneratorParams {
    public String indexName;
    public String outFile;
    public int cutoff;
    public String field;
}