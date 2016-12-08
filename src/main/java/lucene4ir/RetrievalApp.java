package lucene4ir;

import lucene4ir.utils.RetrievalParams;
import lucene4ir.utils.RetrievalParamsReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.*;

/**
 * Created by leif on 22/08/2016.
 */
public class RetrievalApp {

    private RetrievalParams params;

    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser parser;
    private LMSimilarity.CollectionModel colModel;

    private void processQueryFile() {
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
                while (line != null) {

                    String[] parts = line.split(" ");
                    String qno = parts[0];
                    String queryTerms = "";
                    for (int i = 1; i < parts.length; i++)
                        queryTerms = queryTerms + " " + parts[i];

                    ScoreDoc[] scored = runQuery(qno, queryTerms);

                    int n = Math.min(params.maxResults, scored.length);

                    for (int i = 0; i < n; i++) {
                        Document doc = searcher.doc(scored[i].doc);
                        String docno = doc.get("docnum");
                        fw.write(qno + " QO " + docno + " " + (i + 1) + " " + scored[i].score + " " + params.runTag);
                        fw.write(System.lineSeparator());
                    }

                    line = br.readLine();
                }

            } finally {
                br.close();
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    private ScoreDoc[] runQuery(String qno, String queryTerms) {
        ScoreDoc[] hits = null;

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));

            try {
                TopDocs results = searcher.search(query, 1000);
                hits = results.scoreDocs;
            } catch (IOException ioe) {
                System.out.println(" caught a " + ioe.getClass() +
                        "\n with message: " + ioe.getMessage());
            }


        } catch (ParseException pe) {
            System.out.println("Can't parse query");
        }
        return hits;
    }

    public RetrievalApp(String retrievalParamFile) throws IllegalAccessException {
        System.out.println("Retrieval App");

        RetrievalParamsReader paramsReader = new RetrievalParamsReader();
        // attempt to build a model of the retrieval params from the xml file
        params = paramsReader.read(retrievalParamFile);

        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(params.indexName).toPath()));
            searcher = new IndexSearcher(reader);

            searcher.setSimilarity(paramsReader.getSimilarityFunction());

            parser = new QueryParser("content", paramsReader.getAnalyzer());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public static void main(String[] args) throws IllegalAccessException {


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



