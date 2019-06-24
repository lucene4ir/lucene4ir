package lucene4ir.BiGramGenerator;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BiGramGenerator {

    // Public Variables
    public BiGramGeneratorParams p;
    public HashMap<String,queryInfo> biGramMap, uniGramMap;

    private final int maxShingles = 2;
    private double log2;
    private queryInfo currentQryInfo ;
    private String currentGram , terms[] , sourceParameterFile;

    // Constructor Method
    public BiGramGenerator(String inputParameterFile) {
        if (inputParameterFile.isEmpty())
            sourceParameterFile = "params/BiGramGenerator.xml";
        else
            sourceParameterFile = inputParameterFile;
        biGramMap = new HashMap<String, queryInfo>();
        uniGramMap = new HashMap<String, queryInfo>();
    }
    private void displayMsg(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    }
    private long getUnigramFrequency (String term)
    {
        long result = 1; // Default 1
        if (uniGramMap.containsKey(term))
            result = uniGramMap.get(term).collFreq;
        return result;
    }
    private void readParamsFromFile() {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(sourceParameterFile), BiGramGeneratorParams.class );
            if (p.indexName.toString().isEmpty())
                displayMsg ("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.outFilePath.toString().isEmpty())
                displayMsg ("Query Output Path Parameter is Missing");

            if (p.cutoff < 1) {
                p.cutoff = 0;
            }
            System.out.println("biGram Cutoff: " + p.cutoff);

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }
    private void readFilterBigrams() throws Exception
    {
       /* This Function is Used to Read Bigrams and Filter Them based on input cutoff
           and required input field
            */
        String fieldName = lucene4ir.Lucene4IRConstants.FIELD_ALL;

        DumpTermsApp dump = new DumpTermsApp();
        dump.indexName = p.indexName;
        dump.openReader();
        dump.getGramList(fieldName, biGramMap,p.cutoff, uniGramMap);
    }
    private void iterateGrams(int gramSize) throws Exception
    {
        /*
        Iterate Through Grams :
        1- Calculate Score
        2- Output Grams
        */
        Iterator it;
        Map.Entry currentItem;
        String shortOutFile ,
                longOutFile  ,
                outputPrefix = "" ,
                line;
        int qryID;

        // Initialize Variables;
        if(gramSize == 1) {
            // Unigram
            if (uniGramMap.size() < 1)
                return;
            it = uniGramMap.entrySet().iterator();
            outputPrefix = "Uni";
        }
        else
        {
            // Bigram
            if (biGramMap.size() < 1)
                return;
            outputPrefix = "Bi";
            it = biGramMap.entrySet().iterator();
            log2 = Math.log10(2);
        }

        shortOutFile = String.format("%s/short%sgram.txt", p.outFilePath, outputPrefix);
        longOutFile = String.format("%s/long%sgram.txt", p.outFilePath, outputPrefix);
        line = String.format("\n\n\n%sGrams\n---------\n" , outputPrefix);
        PrintWriter prShort = new PrintWriter(shortOutFile);
        PrintWriter prLong = new PrintWriter(longOutFile);
        System.out.print(line);
        qryID = 1;

        while (it.hasNext()) {
            currentItem = (Map.Entry) it.next();
            currentGram = currentItem.getKey().toString();
            calculateScore(gramSize,currentItem);

            // Output

            line = qryID + " " + currentGram ;
            prShort.write(line + "\n");
            if (gramSize == 1)
                line += " " + currentQryInfo.collFreq + " " + currentQryInfo.weight + "\n";
            else
            {
                line += String.format(" %d %d %d %f\n" ,
                        getUnigramFrequency(terms[0]) ,
                        getUnigramFrequency(terms[1]) ,
                        currentQryInfo.collFreq ,
                        currentQryInfo.weight
                );
            } // End Else
            qryID++;
            prLong.write(line);
            System.out.print(line);
        } // End While
        prLong.close();
        prShort.close();
    }
    private void calculateScore(int gramSize , Map.Entry currentItem)
    {
        double score , pi , pj , pij , currentWeight  ;
        long v1 , v2 , currentFreq  , biGramSize , uniGramSize;

        currentQryInfo = (queryInfo) currentItem.getValue();
        currentFreq = currentQryInfo.collFreq;

        if (gramSize == 1)
            score = currentFreq;
        else {
            // Bigram Score
            biGramSize = biGramMap.size();
            uniGramSize = uniGramMap.size();
            pij = currentFreq * 1.0 / biGramSize;
            terms = currentGram.split(" ");
            v1 = getUnigramFrequency(terms[0]);
            v2 = getUnigramFrequency(terms[1]);
            pi = v1 * 1.0 / uniGramSize;
            pj = v2 * 1.0 / uniGramSize;
            score = Math.log10(pij / (pi * pj)) / log2;
        }
        currentQryInfo.weight = score;
    }
    public void main()
    {
        // Reading Parameters from Retrievability Counter XML File
        try {
            readParamsFromFile();
            readFilterBigrams();
            iterateGrams(2);
        }
        catch (Exception ex)
        {
            System.out.println("RunTime Error During Query Generation");
            System.out.println(ex.getMessage());
        }
    }

    public static void main (String args[])
    {
        BiGramGenerator qg = new BiGramGenerator("");
        qg.main();
    }

    // SubClasses
    class queryInfo
    {
        long collFreq;
        double weight;
    }

    @XmlRootElement(name = "BiGramGeneratorParams")
    static
    public class BiGramGeneratorParams {

        public String indexName , outFilePath;
        public int cutoff  ;
    }
}
