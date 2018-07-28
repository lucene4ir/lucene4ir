package lucene4ir;

import lucene4ir.similarity.*;
import lucene4ir.similarity.BM25Similarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.store.FSDirectory;

import lucene4ir.utils.TokenAnalyzerMaker;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;

import static lucene4ir.RetrievalApp.SimModel.*;


/**
 * Central App for performing retrieval in Lucene.
 */
public class RetrievalApp {

    public RetrievalParams p;

    protected Similarity simfn;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
    protected QueryParser parser;
    protected CollectionModel colModel;
    protected String fieldsFile;
    protected String qeFile;
    protected SimModel sim;
    protected enum SimModel {
        DEF, BM25, BM25L, LMD, LMJ, PL2, TFIDF, RAWTFIDF,
	OKAPIBM25, SMARTBNNBNN, DFR
    }

    /**
     * Sets the similarity function for use in retrieval.
     * @param val String value of the similarity model to use.
     */
    private void setSim(String val){
        try {
            sim = SimModel.valueOf(p.model.toUpperCase());
        } catch (Exception e){
            System.out.println("Similarity Function Not Recognized - Setting to Default");
            System.out.println("Possible Similarity Functions are:");
            for(SimModel value: SimModel.values()){
                System.out.println("<model>"+value.name()+"</model>");
            }
            sim = SimModel.DEF;
        }
    }

    /**
     * Function to instantiate the appropriate similarity function for retrieval.
     * @param sim :- the Similarity function to use
     */
    public void selectSimilarityFunction(SimModel sim){
        colModel = null;
        switch(sim){
            case OKAPIBM25:
                System.out.println("OKAPI BM25 Similarity Function");
                simfn = new OKAPIBM25Similarity(1.2f, 0.75f);
                break;

            case SMARTBNNBNN:
                System.out.println("SMART bnn.bnn Similarity Function");
                simfn = new SMARTBNNBNNSimilarity();
                break;

            case BM25:
                System.out.println("BM25 Similarity Function");
                simfn = new BM25Similarity(p.k, p.b);
                break;

            case BM25L:
                System.out.println("BM25L Similarity Function");
                simfn = new BM25LSimilarity(p.k, p.b, p.delta);
                break;

            case LMD:
                System.out.println("LM Dirichlet Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMDirichletSimilarity(colModel, p.mu);
                break;

            case LMJ:
                System.out.println("LM Jelinek Mercer Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMJelinekMercerSimilarity(colModel, p.lam);
                break;

            case PL2:
                System.out.println("PL2 Similarity Function");
                BasicModel bm = new BasicModelP();
                AfterEffect ae = new AfterEffectL();
                Normalization nn = new NormalizationH2(p.c);
                simfn = new DFRSimilarity(bm, ae, nn);
                break;

            case DFR:
                System.out.println("DFR Similarity Function with no after effect (?)");
                BasicModel bmd = new BasicModelD();
                AfterEffect aen = new AfterEffect.NoAfterEffect();
                Normalization nh1 = new NormalizationH1();
                simfn = new DFRSimilarity(bmd, aen, nh1);
                break;

            case TFIDF:
                System.out.println("TF.IDF Similarity Function");
                simfn = new ClassicSimilarity();
                break;

            case RAWTFIDF:
                System.out.println("Raw TF.IDF Similarity Function");
                simfn = new RawTFIDFSimilarity();
                break;

            default:
                System.out.println("Default TFIDF Similarity Function");
                simfn = new ClassicSimilarity();

                break;
        }
    }

    /**
     * Reads in the parameters from the specified file.
     * Uses unmarshalling to assign parameters to the RetrievalParams class.
     * @param paramFile:- path to param file.
     */
    public void readParamsFromFile(String paramFile){
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */
        System.out.println("Reading parameters...");
        try {
            p = JAXB.unmarshal(new File(paramFile), RetrievalParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        setSim(p.model);

        if (p.maxResults==0.0) {p.maxResults=1000;}
        if (p.b < 0.0){ p.b = 0.75f;}
        if (p.beta <= 0.0){p.beta = 500f;}
        if (p.k <= 0.0){ p.k = 1.2f;}
        if (p.delta<=0.0){p.delta = 1.0f;}
        if (p.lam <= 0.0){p.lam = 0.5f;}
        if (p.mu <= 0.0){p.mu = 500f;}
        if (p.c <= 0.0){p.c=10.0f;}
        if (p.model == null){
            p.model = "def";
        }
        if (p.runTag == null){
            p.runTag = p.model.toLowerCase();
        }

        if (p.resultFile == null){
            p.resultFile = p.runTag+"_results.res";
        }
        fieldsFile = p.fieldsFile;
        qeFile=p.qeFile;

        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("Result File: " + p.resultFile);
        System.out.println("Model: " + p.model);
        System.out.println("Max Results: " + p.maxResults);
        if (sim==BM25) {
            System.out.println("b: " + p.b);
            System.out.println("k: " + p.k);
        }
        else if (sim==PL2){
            System.out.println("c: " + p.c);
        }
        else if (sim==LMD){
            System.out.println("mu: " + p.mu);
        }
        if (p.fieldsFile!=null){
            System.out.println("Fields File: " + p.fieldsFile);
        }
        if (p.qeFile!=null){
            System.out.println("QE File: " + p.qeFile);
        }

        if (p.tokenFilterFile != null){
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(p.tokenFilterFile);
        }
        else{
            analyzer = Lucene4IRConstants.ANALYZER;
        }
    }

    /**
     * Processes the query file.
     * Iterates thru each line (assuming each line is a query)
     * and issues the query to the index specified using the similarity
     * model specified in the param file.
     */
    public void processQueryFile(){
        /*
        Assumes the query file contains a qno followed by the query terms.
        One query per line. i.e.

        Q1 hello world
        Q2 hello hello
        Q3 hello etc
         */
        System.out.println("Processing Query File...");
        try {
            BufferedReader br = new BufferedReader(new FileReader(p.queryFile));
            File file = new File(p.resultFile);
            FileWriter fw = new FileWriter(file);

            try {
                String line = br.readLine();
                while (line != null){

                    String[] parts = line.split(" ");
                    String qno = parts[0];
                    String queryTerms = "";
                    for (int i=1; i<parts.length; i++)
                        queryTerms = queryTerms + " " + parts[i];

                    ScoreDoc[] scored = runQuery(qno, queryTerms.trim());

                    int n = Math.min(p.maxResults, scored.length);

                    for(int i=0; i<n; i++){
                        Document doc = searcher.doc(scored[i].doc);
                        String docno = doc.get("docnum");
                        fw.write(qno + " QO " + docno + " " + (i+1) + " " + scored[i].score + " " + p.runTag);
                        fw.write(System.lineSeparator());
                    }
                    line = br.readLine();
                }
            } finally {
                br.close();
                fw.close();
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    /**
     * Scores the document using the similarity model specified.
     * Only returns the top maxResuls documents.
     * @param qno :- The query number
     * @param queryTerms :- the raw query terms from the query file.
     * @return :- returns the top maxResults docs.
     */
    public ScoreDoc[] runQuery(String qno, String queryTerms){
        ScoreDoc[] hits = null;

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));

            try {
                TopDocs results = searcher.search(query, p.maxResults);
                hits = results.scoreDocs;
            }
            catch (IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }
        } catch (ParseException pe){
            pe.printStackTrace();
            System.exit(1);
        }
        return hits;
    }

    /**
     * Constructor to build the retrievalapp by reading in the param
     * file and setting up the appropriate helpers.
     * @param retrievalParamFile :- path to retriel parameter file.
     */
    public RetrievalApp(String retrievalParamFile){
        System.out.println("Retrieval App");
        System.out.println("Param File: " + retrievalParamFile);
        readParamsFromFile(retrievalParamFile);
        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);

            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);

            parser = new QueryParser(Lucene4IRConstants.FIELD_ALL, analyzer);

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    /**
     * Main run method.
     * @param args
     */
    public static void main(String []args) {

        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        RetrievalApp retriever = new RetrievalApp(retrievalParamFile);
        retriever.processQueryFile();
    }
}

/**
 * Class for unmarshalling, contains all the required parameters for retrieval.
 */
@XmlRootElement(name = "RetrievalParams")
class RetrievalParams {
    public String indexName;
    public String queryFile;
    public String resultFile;
    public String model;
    public int maxResults;
    public float k;
    public float b;
    public float lam;
    public float beta;
    public float mu;
    public float c;
    public float delta;
    public String runTag;
    public String tokenFilterFile;
    public String fieldsFile;
    public String qeFile;
}