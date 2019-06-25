package lucene4ir.RetrievabilityCalculator;

import lucene4ir.BiGramGenerator.DumpTermsApp;
import lucene4ir.RetrievalApp;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;


// Created by Abdulaziz on 19/06/2019.


public class RetrievabilityCalculator {


    // Properties
    public RetrievabilityCalculatorParams p;


    private String sourceParameterFile;
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
            p = JAXB.unmarshal(new File(sourceParameterFile), RetrievabilityCalculatorParams.class );
            if (p.indexName.toString().isEmpty())
                displayMsg ("IndexName Parameter is Missing");
            System.out.println("Index: " + p.indexName);
            if (p.retFile.toString().isEmpty())
                displayMsg ("Query Output Path Parameter is Missing");
            if (p.resFile.toString().isEmpty())
                displayMsg ("Result File Parameter is Missing");
            if (p.queryFile.toString().isEmpty())
                displayMsg ("Query File Parameter is Missing");
            if (p.b < 1)
                p.b = 0;
            if (p.c < 1)
                p.c = 0;

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }

    private double calculateG(ArrayList<Double> sortedRValues)
    {
        /*
        Given sorted array of R values >>> Calculate G Coefficient
         */
        int N;
        double r , numerator = 0 , denominator = 0 , result = 0;

        N = sortedRValues.size() - 1;
        for (int i = 1 ; i <= sortedRValues.size() ; i++)
        {
            r = sortedRValues.get(i-1);
            numerator += (2 * i - N) * r;
            denominator += r;
        }
        result = numerator / (++N * denominator);
        return result;
    }

    private void displayResults(String outPath) throws Exception
    {
        /*
        Display The Results as Needed For the whole Document Vector
         */
        String line;
        Map.Entry item;
        Iterator it = docMap.entrySet().iterator();
        int docID ;
        double r , G;
        ArrayList<Double> rValues = new ArrayList<Double>();

        PrintWriter pr = new PrintWriter(p.retFile);
        while (it.hasNext()) {
            item = (Map.Entry) it.next();
            r = (double) item.getValue();
            docID = (int) item.getKey();
            // N already deducted  by 1 to save process
            rValues.add(r);
            line = String.format("%d %f\n", docID, r);
            pr.print(line);
            System.out.print(line);
        }

        Collections.sort(rValues);
        G = calculateG(rValues);
        
        line = "The G Coefficient = " + G;
        System.out.println(line);
        pr.close();
    }

    private double costFunction (int rank )
    {
        double result = 0;
        if (rank <= p.c)
            result = 1.0 / Math.pow(rank,p.b);
        return result;
    }
    private double calculateR (int qryID , int rank)
    {
        double result  , weight;
       weight = qryMap.get(qryID);
        result = weight * costFunction(rank);
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
           /* docMap.put(docid, docMap.get(docid) + 1);*/
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

        final String docIDField = "docnum";
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

        String RetrievalParamsFile = "params/retrieval_params.xml";
        // Mystro Method that coordinate the process
        try {
            readParamsFromFile();
            RetrievalApp re = new RetrievalApp(RetrievalParamsFile);
            re.processQueryFile();
            initDocumentMap();
            initQueryMap(p.queryFile);
            readRetrievalResults(p.resFile);
            displayResults(p.retFile);
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
        public String indexName , retFile ,resFile , queryFile ;
        public int   b , c;
    }
}

