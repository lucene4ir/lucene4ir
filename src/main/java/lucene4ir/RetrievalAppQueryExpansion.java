package lucene4ir;

import lucene4ir.utils.RetrievalParams;
import lucene4ir.utils.RetrievalParamsReader;
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

    private RetrievalParams params;

    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser parser;
    private CollectionModel colModel;

    public void processQueryFile(){
        /*
        Assumes the query file contains a qno followed by the query terms.
        One query per line. i.e.

        Q1 hello world
        Q2 hello hello
        Q3 hello etc
         */
        try {
            BufferedReader br = new BufferedReader(new FileReader(params.queryFile));
            File file = new File(params.resultFile);
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

                    int n = Math.min(params.maxResults, scored.length);

                    for(int i=0; i<n; i++){
                        Document doc = searcher.doc(scored[i].doc);
                        String docno = doc.get("docnum");
                        fw.write(qno + " QO " + docno + " " + (i+1) + " " + scored[i].score + " " + params.runTag);
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

        RetrievalParamsReader paramsReader = new RetrievalParamsReader();
        params = paramsReader.read(retrievalParamFile);

        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(params.indexName).toPath()));
            searcher = new IndexSearcher(reader);

            // Create similarity function and parameter
            searcher.setSimilarity(paramsReader.getSimilarityFunction());

            // Use whatever ANALYZER you want
            // I commented this out since you can load it from XML...?
            // analyzer = new StandardAnalyzer();

            parser = new QueryParser("content", paramsReader.getAnalyzer());

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



