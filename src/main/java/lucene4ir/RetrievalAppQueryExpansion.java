package lucene4ir;

import lucene4ir.utils.SynonymProvider;
import net.sf.extjwnl.JWNLException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.Iterator;
import java.util.Set;

public class RetrievalAppQueryExpansion {

    public RetrievalParams p;

    private Similarity simfn;
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private QueryParser parser;
    private CollectionModel colModel;

    private enum SimModel {
        DEF, BM25, LMD, LMJ, PL2, TFIDF
    }

    private SimModel sim;

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


    public void selectSimilarityFunction(SimModel sim){
        colModel = null;
        switch(sim){
            case BM25:
                System.out.println("BM25 Similarity Function");
                simfn = new BM25Similarity(p.k,p.b);
                break;

            case LMD:
                System.out.println("LM Dirichlet Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMDirichletSimilarity(colModel,p.mu);
                break;

            case LMJ:
                System.out.println("LM Jelinek Mercer Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMJelinekMercerSimilarity(colModel,p.lam);
                break;

            case PL2:
                System.out.println("PL2 Similarity Function (?)");
                BasicModel bm = new BasicModelP();
                AfterEffect ae = new AfterEffectL();
                Normalization nn = new NormalizationH2(p.c);
                simfn = new DFRSimilarity(bm,ae,nn);
                break;

            default:
                System.out.println("Default Similarity Function");
                simfn = new BM25Similarity();

                break;
        }
    }



    public void readParamsFromFile(String paramFile){
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */


        try {
            p = JAXB.unmarshal(new File(paramFile), RetrievalParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        setSim(p.model);

        if (p.maxResults==0.0) {p.maxResults=1000;}
        if (p.b == 0.0){ p.b = 0.75f;}
        if (p.beta == 0.0){p.beta = 500f;}
        if (p.k ==0.0){ p.k = 1.2f;}
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


    }

    public void processQueryFile(){
        /*
        Assumes the query file contains a qno followed by the query terms.
        One query per line. i.e.

        Q1 hello world
        Q2 hello hello
        Q3 hello etc
         */
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
                    for (int i=1; i<parts.length; i++) {
                        queryTerms = queryTerms + " " + parts[i];
                    }

                    ScoreDoc[] scored = runQuery(qno, queryTerms);

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
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }



    }

    public ScoreDoc[] runQuery(String qno, String queryTerms){
        ScoreDoc[] hits = null;
        System.out.println("Query No.: " + qno + " " + queryTerms);

        try {

            //Multi-field query
            String[] fields = new String[]{"title", "content"};

            // A query builder for constructing a complex query
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

            // Boost the original query by a factor of 5
            // Query terms are important but the original terms are importantER
            BoostQuery termQuery = new BoostQuery(parser.parse(queryTerms), 5.0f);

            // Add it to the query builder
            queryBuilder.add(termQuery, BooleanClause.Occur.MUST);

            // Print out what Lucene generated
            // System.out.println(query.toString());

            // Find the synonyms of each term in the original query
            StringBuilder sb = new StringBuilder();
            for (String queryTerm : queryTerms.trim().split(" ")) {
                try {
                    Set<String> synonyms = SynonymProvider.getSynonyms(queryTerm);

                    if (synonyms == null) {
                        continue;
                    }

                    Iterator<String> it = synonyms.iterator();

                    while (it.hasNext()) {
                        sb.append(it.next());
                        sb.append(" ");
                    }

                } catch (JWNLException e) {
                    e.printStackTrace();
                }

            }
            String querySynonymized = sb.toString();

            // If we found some synonyms, construct a query and add it to the query builder
            if (querySynonymized.length() > 0) {
                Query queryExpanded = parser.parse(querySynonymized);
                queryBuilder.add(queryExpanded, BooleanClause.Occur.SHOULD);
            }

            // Construct the final query and run it
            Query finalQuery = queryBuilder.build();

            try {
                TopDocs results = searcher.search(finalQuery, 1000);
                hits = results.scoreDocs;
            }
            catch (IOException ioe){
                System.out.println(" caught a " + ioe.getClass() + "\n with message: " + ioe.getMessage());
            }


        } catch (ParseException pe){
            System.out.println("Cant parse query");
        }
        return hits;
    }



    public RetrievalAppQueryExpansion(String retrievalParamFile){
        System.out.println("Retrieval App");
        readParamsFromFile(retrievalParamFile);
        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(p.indexName).toPath()));
            searcher = new IndexSearcher(reader);

            // Create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);

            // Use whatever ANALYZER you want
            analyzer = new StandardAnalyzer();

            parser = new QueryParser("content", analyzer);

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }

    }

    public static void main(String []args) {
        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            System.exit(1);
        }

        RetrievalAppQueryExpansion retriever = new RetrievalAppQueryExpansion(retrievalParamFile);
        retriever.processQueryFile();

    }

}



