package lucene4ir;

import org.apache.lucene.search.Retriever;
import org.apache.lucene.search.RetrieverOkapiBM25;
import org.apache.lucene.search.RetrieverParams;

import java.io.File;

import javax.xml.bind.JAXB;

/**
 * Application to perform retrieval by Apache Lucene index API.
 *
 * Created by dibuccio on 09/09/2016.
 */
public class RetrievalAppByIndexAPI {

    /**
     * Currently supported retrieval models using directly index API
     */
    private enum RetrievalModel {
        OKAPIBM25
    }

    public static RetrieverParams readParamsFromFile(String paramFile){

        /*
            Reads in the xml formatting parameter file
            Maybe this code should go into the RetrieverParams class.

            Actually, it would probably be neater to create a ParameterFile class
            which these apps can inherit from - and customize accordinging.
         */

        RetrieverParams p = null;

        try {
            p = JAXB.unmarshal(new File(paramFile), RetrieverParams.class);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        if (p.maxResults==0.0) {p.maxResults=1000;}
        if (p.b == 0.0){ p.b = 0.75f;}
        if (p.beta == 0.0){p.beta = 500f;}
        if (p.k ==0.0){ p.k = 1.2f;}
        if (p.delta==0.0){p.delta = 1.0f;}
        if (p.lam==0.0){p.lam = 0.5f;}
        if (p.mu==0.0){p.mu = 500f;}
        if (p.c==0.0){p.c=10.0f;}
        if (p.model == null){
            p.model = "def";
        }
        if (p.runTag == null){
            p.runTag = p.model.toLowerCase();
        }

        if (p.resultFile == null){
            p.resultFile = p.runTag+"_results.res";
        }

        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("Result File: " + p.resultFile);
        System.out.println("Model: " + p.model);
        System.out.println("Max Results: " + p.maxResults);
        System.out.println("b: " + p.b);

        return p;

    }

    /**
     * Factory to create {@link Retriever} according to the given parameters.
     *
     * @param p retriever parameters.
     *
     * @return the requested {@link Retriever}.
     */
    static Retriever createRetriever(RetrieverParams p){

        Retriever retriever;

        RetrievalModel model = RetrievalModel.valueOf(p.model.toUpperCase());

        switch(model){
            case OKAPIBM25:
                System.out.println("Okapi BM25 Retriever");
                retriever = new RetrieverOkapiBM25(p);
                break;
            default:
                System.out.println("Default Retriever");
                retriever = new RetrieverOkapiBM25(p);
                break;
        }

        return retriever;

    }

    public static void main(String []args) {

        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        RetrieverParams p = readParamsFromFile(retrievalParamFile);

        Retriever retriever = createRetriever(p);

        retriever.processQueryFile();

    }

}





