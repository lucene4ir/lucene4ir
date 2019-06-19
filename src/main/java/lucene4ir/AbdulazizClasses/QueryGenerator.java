package lucene4ir.AbdulazizClasses;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryGenerator {

    // Public Variables
    public QueryGeneratorParams p;
    public String sourceParameterFile;
    public HashMap<String,queryInfo> biGramMap, uniGramMap;

    private final int maxShingles = 2;

    // Constructor Method
    public QueryGenerator (String inputParameterFile) {
        if (inputParameterFile.isEmpty())
            sourceParameterFile = "params/RetrievabilityCalculator.xml";
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
    /*
    Generating Queries Methods
     */
    private boolean equalString (String str1 , String str2)
    {
        return str1.toLowerCase().compareTo(str2.toLowerCase()) == 0;
    }

    private void readParamsFromFile() {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(sourceParameterFile), QueryGeneratorParams.class );
            if (p.indexName.toString().isEmpty())
                displayMsg ("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.gramsOutputPath.toString().isEmpty())
                displayMsg ("Query Output Path Parameter is Missing");

            if (p.biCutoff < 1) {
                p.biCutoff = 0;
            }
            System.out.println("biGram Cutoff: " + p.biCutoff);

            if (p.uniCutoff < 1) {
                p.uniCutoff = 0;
            }
            System.out.println("uniGram Cutoff: " + p.uniCutoff);

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
        dump.getGramList(fieldName, biGramMap,p.biCutoff, uniGramMap,p.uniCutoff);
    }

    public static Comparator<queryInfo> weightComparator = new Comparator<queryInfo>() {
        /*
        This is an array sorting method based on the following url
        https://dzone.com/articles/sorting-java-arraylist
         */
        @Override
        public int compare(queryInfo o1, queryInfo o2) {
            return (o2.weight < o1.weight ? -1 : o2.weight == o1.weight ? 0 : 1) ;
        }
    };

   private void calculateScores()
    {
       /* This Function is Used To do the following :
           1- calculate the weight for each input Bigrams in the Term Vector
        */
        Map.Entry currentItem;
        double pi , pj , pij , currentWeight ;
        long v1 , v2 , currentFreq  , biGramSize , uniGramSize;
        Iterator it;
        queryInfo currentQryInfo ;
        String currentGram;

        // Identify main Parameters for current gram according to its input type (Single , Bi)
        it = biGramMap.entrySet().iterator();
        biGramSize = biGramMap.size();
        uniGramSize = uniGramMap.size();

        while (it.hasNext()) {
            currentItem = (Map.Entry) it.next();
            currentGram = currentItem.getKey().toString();
            currentQryInfo = (queryInfo) currentItem.getValue();
            currentFreq = currentQryInfo.collFreq;
            // Bigram Score
            pij = (double) ((currentFreq + 1) / (biGramSize + 1));
            String[] terms = currentGram.split(" ");
            v1 = 1;
            v2 = 1;
            if (uniGramMap.containsKey(terms[0]))
                v1 =  uniGramMap.get(terms[0]).collFreq;
            if (uniGramMap.containsKey(terms[1]))
                v2 =  uniGramMap.get(terms[1]).collFreq;

            pi = (double) ((v1 + 1.0) / (uniGramSize + 1));
            pj = (double) ((v2 + 1.0) / (uniGramSize + 1));
            currentQryInfo.weight = Math.log(pij / (pi * pj));
            System.out.println("Added Query " + currentGram + " to QueryList");
        } // End while
    }

    private void outputGrams(int gramSize) throws Exception
    {
        String shortOutFile ,
                longOutFile  ,
                outputPrefix = "" ,
                line , qry;
        int qryID = 1;

        queryInfo qryinfo = new queryInfo();
        Map.Entry item;
        Iterator it = uniGramMap.entrySet().iterator();

        switch (gramSize)
        {
            case 1:
                if (uniGramMap.size() < 1)
                {
                    System.out.println("No Unigrams to print");
                    return;
                }
                outputPrefix = "Uni";
                break;
            case 2:
                if (biGramMap.size() < 1)
                {
                    System.out.println("No Bigrams to print");
                    return;
                }
                outputPrefix = "Bi";
                it = biGramMap.entrySet().iterator();
                break;
        } // End Switch

        shortOutFile = String.format("%s/short%sgram.txt", p.gramsOutputPath , outputPrefix);
        longOutFile = String.format("%s/long%sgram.txt", p.gramsOutputPath , outputPrefix);
        line = String.format("\n\n\n%sGrams\n---------\n" , outputPrefix);
        PrintWriter prShort = new PrintWriter(shortOutFile);
        PrintWriter prLong = new PrintWriter(longOutFile);
        System.out.print(line);
        qryID = 1;
        while (it.hasNext())
        {
            item = (Map.Entry) it.next();
            qry = item.getKey().toString();
            qryinfo = (queryInfo) item.getValue();
            line = qryID++ + " " + qry ;
            prShort.write(line + "\n");
            line += " " + qryinfo.collFreq + " " + qryinfo.weight + "\n";
            prLong.write(line);
            System.out.print(line);
        }
        prShort.close();
        prLong.close();
    }
    private void generateQueries  ()
    {
        // Reading Parameters from Retrievability Counter XML File

        try {
            readParamsFromFile();
            readFilterBigrams();
            calculateScores();
            outputGrams(1);
            outputGrams(2);
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
        qg.generateQueries();
    }

    // SubClasses
    class queryInfo
    {
        long collFreq;
        double weight;
    }

    @XmlRootElement(name = "QueryGeneratorParams")
    static
    class QueryGeneratorParams {

        public String indexName , gramsOutputPath  ;
        public int uniCutoff , biCutoff  ;
        // public float b , k;
    }
}
