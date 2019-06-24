package lucene4ir.RetrievabilityCalculator;

import lucene4ir.QueryGenerator.DumpTermsApp;
import lucene4ir.RetrievalApp;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// Created by Abdulaziz on 19/06/2019.


public class RetrievabilityCalculator {


    // Properties
    public RetrievabilityCalculatorParams p;


    private String sourceParameterFile;
    private final String docIDField = "docnum";
    private HashMap<Integer, Double> docMap , qryMap;
    private double totalWeights;

    // Constructor Method
    public RetrievabilityCalculator(String inputParameters) {
        if (inputParameters.isEmpty())
            sourceParameterFile = "params/RetrievabilityCalculator.xml";
        else
            sourceParameterFile = inputParameters;
        docMap = new HashMap<Integer, Double>();
        qryMap = new HashMap<Integer, Double>();
    }
    private void displayMsg(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    }
    private void readParamsFromFile() {
        System.out.println("Reading Param File");
        try {
            p = JAXB.unmarshal(new File(sourceParameterFile), RetrievabilityCalculator.RetrievabilityCalculatorParams.class );
            if (p.indexName.toString().isEmpty())
                displayMsg ("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.outputPath.toString().isEmpty())
                displayMsg ("Query Output Path Parameter is Missing");
            if (p.resultFile.toString().isEmpty())
                displayMsg ("Result File Parameter is Missing");
            if (p.queryFile.toString().isEmpty())
                displayMsg ("Query File Parameter is Missing");
            if (p.maxResults < 1)
                displayMsg ("Max Results Parameter is Missing");

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }

    private void displayResults(String outPath) throws Exception
    {

        /*
        Display The Results as Needed For the whole Document Vector
         */
        String line;
        Map.Entry item;
        Iterator it = docMap.entrySet().iterator();
        int docID , N , i ;
        double r , numerator = 0 , denominator = 0 , G;

        N = docMap.size() - 1;
        i = 1;
        PrintWriter pr = new PrintWriter(p.outputPath + "/RetrievabilityList.txt");
        while (it.hasNext()) {
            item = (Map.Entry) it.next();
            r = (double) item.getValue();
            docID = (int) item.getKey();
            // N already deducted  by 1 to save process
            numerator += (2 * i++ - N) * r;
            denominator += r;
            line = String.format("%d %f\n", docID, r);
            pr.print(line);
            System.out.print(line);
        }

        // Get N Value Back
        G = numerator / (++N * denominator);
        line = "The G Coefficient = " + G;
        System.out.println(line);
        pr.print(line);
        pr.close();
    }

    private double costFunction (char costFunction , int rank )
    {
        double result = 0;
        if (costFunction == 'c')
            result = 1;
        else
            result = 1.0 / Math.pow(rank,p.maxResults);

        return result;
    }
    private double calculateR (int qryID , int rank)
    {
        char costFunction;
        double result = 0 , weight;
        weight = qryMap.get(qryID);
        costFunction =  p.costFunction.toLowerCase().charAt(0);
        result = weight * costFunction(costFunction,rank);
        return result;
    }


    private void readRetrievalResults(String retrievalResultFile) throws Exception {
        /*
        Read RetrievalAPP Results File
        Update the counter ( number of occurances in Results File ) for each document in the Document Map
         */
        String line, parts[];
        int docid , qryid , rank ;
        double r ;

        BufferedReader br = new BufferedReader(new FileReader(retrievalResultFile));

        line = br.readLine();
        while (line != null) {
            parts = line.split(" ", 5);
            qryid = Integer.parseInt(parts[0]);
            docid = Integer.parseInt(parts[2]);
            rank = Integer.parseInt(parts[3]);
            r = calculateR(qryid,rank);
            docMap.put(docid, docMap.get(docid) + r);
            line = br.readLine();
        }
        br.close();
    }

    private void initQueryMap(String qryFile ) throws Exception
    {
        String line, parts[];
        int qryID;
        double weight;

        BufferedReader br = new BufferedReader(new FileReader(qryFile));
        line = br.readLine();
        while (line != null)
        {
            parts = line.split(" ",7);
            qryID = Integer.parseInt(parts[0]);
            weight = Double.parseDouble(parts[6]);
            qryMap.put(qryID,weight);
            line = br.readLine();
        } // End While
        br.close();
    }
    private void initDocumentMap() throws Exception {
        // Initialize Document Hash MAP (docid , docCounter = 0)
        DumpTermsApp dump = new DumpTermsApp();
        dump.indexName = p.indexName;
        dump.openReader();
        int docid = 0;
        for (int i = 0; i < dump.reader.maxDoc(); i++) {
            docid = Integer.parseInt(dump.reader.document(i).get(docIDField));
            docMap.put(docid,0.0);
        }
    }

    public void main() {

        // Mystro Method that coordinate the process
        try {
            readParamsFromFile();
            RetrievalApp re = new RetrievalApp(sourceParameterFile);
            re.processQueryFile();
            initDocumentMap();
            initQueryMap(p.queryFile.replace("short","long"));
            readRetrievalResults(p.resultFile);
            displayResults(p.outputPath + "/RetrievabilityScores.txt");
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        // The Main Method to make the class runs individually
        RetrievabilityCalculator rc = new RetrievabilityCalculator("");
        rc.main();
    }

    @XmlRootElement(name = "RetrievabilityCalculatorParams")
    static
    public class RetrievabilityCalculatorParams {
        public String indexName , outputPath,resultFile , queryFile , costFunction;
        public int maxResults;
    }
}

