package lucene4ir.QueryGenerator;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryGenerator {

    // Public Variables
    public QueryGeneratorParams p;
    public ArrayList<ShingleInfo> shingles;
    private double log2;
    private queryInfo currentQryInfo ;
    private String currentGram  , sourceParameterFile ;
    private ArrayList<Long> uniFrequencies;
    private int  qryID = 1 ;
    private long currentFreq;
    private PrintWriter  prShort , prLong;

    // Constructor Method
    public QueryGenerator (String inputParameterFile) {
        Iterator it;
        Map.Entry item;
        ShingleInfo currentShingle;

        if (inputParameterFile.isEmpty())
            sourceParameterFile = "params/RetrievabilityCalculator.xml";
        else
            sourceParameterFile = inputParameterFile;

        ShingleExtractor se = new ShingleExtractor();
        if (!se.extractShingles(sourceParameterFile))
            System.exit(0);

        shingles = new ArrayList<ShingleInfo>();
        it =  se.shingleMap.entrySet().iterator();
        while (it.hasNext())
        {
            item = (Map.Entry) it.next();
            currentShingle = new ShingleInfo(Integer.parseInt(item.getKey().toString()) ,
                             Integer.parseInt(item.getValue().toString()));
            shingles.add(currentShingle);
        }
        uniFrequencies = new ArrayList<Long>();
        log2 = Math.log10(2);
    }
    private void displayMsg(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    }

    private String getPrefix (int gramSize)
    {
        String result;
        if (gramSize < 2)
            result = gramSize + " Shingle - ";
        else
            result = gramSize + " Shingles - ";
        return   result;
    }
    private HashMap<String,queryInfo> getGramMap( int gramSize)
    {
        HashMap<String,queryInfo> result = new HashMap<String,queryInfo>();
        boolean found = false;
        for (ShingleInfo sh : shingles)
            if (sh.gramSize == gramSize)
            {
                found = true;
                result = sh.qryMap;
                break;
            }
        if (!found)
            displayMsg("Shingle " + gramSize + " is not Exist in the List of Shingles");

            return result;
    }

    private long getUnigramFrequency (String term)
    {
        long result = 1; // Default 1
        HashMap<String,queryInfo> uniGramMap;

        uniGramMap = getGramMap(1);
        if (uniGramMap.containsKey(term))
            result = uniGramMap.get(term).collFreq;
        return result;
    }


    private void readParamsFromFile() {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(sourceParameterFile), QueryGeneratorParams.class );
            if (p.indexName.toString().isEmpty())
                displayMsg ("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.outputPath.toString().isEmpty())
                displayMsg ("Query Output Path Parameter is Missing");

            for (ShingleInfo sh:shingles)
                System.out.println(getPrefix(sh.gramSize) + "Cutoff: " + sh.cutoff);


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
        String allField = lucene4ir.Lucene4IRConstants.FIELD_ALL;

        DumpTermsApp dump = new DumpTermsApp();
        dump.indexName = p.indexName;
        dump.openReader();
        dump.getGramList(allField,shingles);
    }
    private void iterateGrams(ShingleInfo inputShingle) throws Exception
    {
        Iterator it;
        Map.Entry currentItem;
        String shortOutFile ,
                longOutFile  ,
                line ;
        int gramSize;

        // Initialize Variables;
        if (inputShingle.qryMap.size() < 1)
            return;

        it = inputShingle.qryMap.entrySet().iterator();

        // Write GramMap Header on Screen
        gramSize = inputShingle.gramSize;
        line = String.format("\n\n\n%sGrams\n---------\n" , getPrefix(inputShingle.gramSize));
        System.out.print(line);

        // Iterate Through Chosen QryMap
        // 1- Calculate Score
        // 2- Output Gram
        while (it.hasNext()) {
            currentItem = (Map.Entry) it.next();

            // Current Information for Current Gram Map
            currentGram = currentItem.getKey().toString();
            currentQryInfo = (queryInfo) currentItem.getValue();
            currentFreq = currentQryInfo.collFreq;
            currentQryInfo.weight = getScore(gramSize,currentItem);

            // Output

            // ShortLine
            line = qryID + " " + currentGram ;
            prShort.write(line + "\n");

            // LongLine
            if (gramSize == 1)
                line += " " + currentQryInfo.collFreq + " " + currentQryInfo.weight + "\n";
            else
            {

                for (int i = 0 ; i < uniFrequencies.size() ; i++)
                    line += " " + uniFrequencies.get(i);
                line += String.format(" %d %f\n" ,
                        currentQryInfo.collFreq ,
                        currentQryInfo.weight
                );
            } // End Else
            qryID++;
            prLong.write(line);
            System.out.print(line);
        } // End While
    }
    private double getScore(int gramSize , Map.Entry currentItem)
    {
        double score , p , denominator = 1 , numerator ;
        long uniFreq  , multiGramSize , uniGramSize;
        String terms[];

        if (gramSize == 1)
            score = currentFreq;
        else {
            // Multi Score
            multiGramSize = getGramMap(gramSize).size();
            uniGramSize = getGramMap(1).size();
            numerator = currentFreq * 1.0 / multiGramSize;
            uniFrequencies.clear();
            terms = currentGram.split(" ");
            for (int i = 0 ; i < terms.length ; i++)
            {
                uniFreq = getUnigramFrequency(terms[i]);
                uniFrequencies.add(uniFreq);
                p  = uniFreq * 1.0 / uniGramSize;
                denominator *= p;
            }

            score = Math.log10(numerator / denominator) / log2;
        }
       return score;
    }
    public void main()
    {
        String shortOutFile,longOutFile;
        // Reading Parameters from Retrievability Counter XML File
        try {
            readParamsFromFile();
            readFilterBigrams();
            // Initialize Output
            shortOutFile = String.format("%s/shortGrams.txt", p.outputPath);
            longOutFile = String.format("%s/longGrams.txt", p.outputPath);
            prShort = new PrintWriter(shortOutFile);
            prLong = new PrintWriter(longOutFile);

            for (ShingleInfo sh:shingles)
                iterateGrams(sh);
            prShort.close();
            prLong.close();
        }
        catch (Exception ex)
        {
            System.out.println("RunTime Error During Query Generation");
            System.out.println(ex.getMessage());
        }
    }

    public static void main (String args[])
    {
        QueryGenerator qg = new QueryGenerator("");
        qg.main();
    }
    // SubClasses
    class queryInfo
    {
        long collFreq;
        double weight;
    }
    class ShingleInfo
    {
        int gramSize, cutoff;
        HashMap<String,queryInfo> qryMap;

        // Constructor
        public ShingleInfo(int inShingle , int inCutoff )
        {
            this.gramSize = inShingle;
            this.cutoff = inCutoff;
            this.qryMap =  new HashMap<String,queryInfo>();
        }
    }
    @XmlRootElement(name = "QueryGeneratorParams")
    static
    public class QueryGeneratorParams {
        public String indexName , outputPath;
    }
}
