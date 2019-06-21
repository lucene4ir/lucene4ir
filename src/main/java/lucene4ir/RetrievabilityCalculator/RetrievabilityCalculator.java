package lucene4ir.RetrievabilityCalculator;

import lucene4ir.QueryGenerator.DumpTermsApp;
import lucene4ir.QueryGenerator.QueryGenerator;
import lucene4ir.RetrievalApp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// Created by Abdulaziz on 19/06/2019.


public class RetrievabilityCalculator {


    // Properties
    private String sourceParameterFile, indexName;
    private final String docIDField = "docnum";
    private HashMap<Integer, Integer> docMap;
    private double totalWeights;

    // Constructor Method
    public RetrievabilityCalculator(String inputParameters) {
        if (inputParameters.isEmpty())
            sourceParameterFile = "params/RetrievabilityCalculator.xml";
        else
            sourceParameterFile = inputParameters;
        docMap = new HashMap<Integer, Integer>();
    }

    private void displayResults(String outPath, double G) {

        /*
        Display The Results as Needed
         */
        String line;
        Map.Entry item;
        Iterator it = docMap.entrySet().iterator();
        int ctr;
        while (it.hasNext()) {
            item = (Map.Entry) it.next();
            ctr = (int) item.getValue();
            line = String.format("%d %d %f", (int) item.getKey(), ctr, ctr * totalWeights);
            System.out.println(line);
        }
        line = "TotalWeights = " + totalWeights + "\nThe G Coefficient = " + G;
        System.out.println(line);
    }

    private double calculateG() {

        /*
        Calculate Retrievability (R) for each document
        Calculate G Coefficient from calculated R
         */
        double result, numerator = 0, denominator = 0, r;
        Map.Entry item;
        int N, i = 0;

        N = docMap.size();
        Iterator it = docMap.entrySet().iterator();
        while (it.hasNext()) {
            item = (Map.Entry) it.next();
            r = (int) item.getValue() * totalWeights;
            numerator += (2 * (int) item.getKey() - N - 1) * r;
            denominator += r;
        }
        result = numerator / (N * denominator);
        return result;
    }

    private double getTotalWeights(String inFile) throws Exception {
        /*
        Given Query File From QueryGenerator (inFile)
        Read All Grams Weights and return their total
         */
        double result = 0, currentWeight;
        String line, parts[];

        BufferedReader br = new BufferedReader(new FileReader(inFile));
        line = br.readLine();
        while (line != null) {
            parts = line.split(" ", 7);
            currentWeight = Double.parseDouble(parts[6]);
            result += currentWeight;
            line = br.readLine();
        }
        br.close();
        return result;
    }

    private void fillDocumentMap(String retrievalResultFile) throws Exception {

        /*
        Read RetrievalAPP Results File
        Update the counter ( number of occurances in Results File ) for each document in the Document Map
         */
        String line, parts[];
        int docid;

        BufferedReader br = new BufferedReader(new FileReader(retrievalResultFile));
        line = br.readLine();
        while (line != null) {
            parts = line.split(" ", 4);
            docid = Integer.parseInt(parts[2]);
            docMap.put(docid, docMap.get(docid) + 1);
            line = br.readLine();
        }
        br.close();
    }

    private void initDocumentMap() throws Exception {
        // Initialize Document Hash MAP (docid , docCounter = 0)
        DumpTermsApp dump = new DumpTermsApp();
        dump.indexName = indexName;
        dump.openReader();
        int docid = 0;
        for (int i = 0; i < dump.reader.maxDoc(); i++) {
            docid = Integer.parseInt(dump.reader.document(i).get(docIDField));
            docMap.put(docid, 0);
        }
    }

    public void main() {

        // Mystro Method that coordinate the process
        double G;
        try {
            QueryGenerator qg = new QueryGenerator(sourceParameterFile);
            qg.generateQueries();
            indexName = qg.p.indexName;
            RetrievalApp re = new RetrievalApp(sourceParameterFile);
            re.processQueryFile();
            initDocumentMap();
            fillDocumentMap(qg.p.resultFile);
            totalWeights = getTotalWeights(qg.p.queryFile.replace("short", "long"));
            G = calculateG();
            displayResults(qg.p.gramsOutputPath + "/RetrievabilityScores.txt", G);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String args[]) {
        // The Main Method to make the class runs individually
        RetrievabilityCalculator rc = new RetrievabilityCalculator("");
        rc.main();
    }
}
