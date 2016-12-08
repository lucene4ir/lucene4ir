package lucene4ir;

import lucene4ir.utils.TokenAnalyzerMaker;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by leif on 22/08/2016.
 */
public class RetrievalApp {

    private RetrievalParams params;
    private Similarity similarityFunction;
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private QueryParser parser;
    private CollectionModel colModel;

    private RetrievalParams readParamsFromFile(String paramFile) {
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */
        RetrievalParams params = new RetrievalParams();

        try {
            params = JAXB.unmarshal(new File(paramFile), RetrievalParams.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // TODO: Can't these just be default values (i.e. the constructor?)
        if (params.maxResults == 0.0) {
            params.maxResults = 1000;
        }
        if (params.b == 0.0) {
            params.b = 0.75f;
        }
        if (params.beta == 0.0) {
            params.beta = 500f;
        }
        if (params.k == 0.0) {
            params.k = 1.2f;
        }
        if (params.delta == 0.0) {
            params.delta = 1.0f;
        }
        if (params.lam == 0.0) {
            params.lam = 0.5f;
        }
        if (params.mu == 0.0) {
            params.mu = 500f;
        }
        if (params.c == 0.0) {
            params.c = 10.0f;
        }
        if (params.runTag == null) {
            params.runTag = params.model.className;
        }

        if (params.resultFile == null) {
            params.resultFile = params.runTag + "_results.res";
        }

        System.out.println("Path to index: " + params.indexName);
        System.out.println("Query File: " + params.queryFile);
        System.out.println("Result File: " + params.resultFile);
        System.out.println("Model: " + params.model.className);
        System.out.println("Max Results: " + params.maxResults);
        System.out.println("b: " + params.b);

        if (params.tokenFilterFile != null) {
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(params.tokenFilterFile);
        } else {
            analyzer = LuceneConstants.ANALYZER;
        }

        return params;
    }

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


    private void setSimilarityFunction(RetrievalParams params) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // use reflection to try to look up the class name
        Class<?> func = Class.forName(params.model.className);

        // create some objects to store the params to pass to the class
        Object[] args = new Object[params.model.params.size()];
        Class<?>[] types = new Class[params.model.params.size()];

        // populate the collections with the params from the xml file
        for (int i = 0; i < params.model.params.size(); i++) {
            args[i] = params.model.params.get(i);
            // TODO: The assumption is that the constructor uses the primitive float class
            // (The `Float` class wraps the primitive `float` type)
            types[i] = float.class;
        }

        // let's instantiate!
        similarityFunction = (Similarity) func.getConstructor(types).newInstance(args);
    }

    public RetrievalApp(String retrievalParamFile) throws IllegalAccessException {
        System.out.println("Retrieval App");

        // attempt to build a model of the retrieval params from the xml file
        params = readParamsFromFile(retrievalParamFile);

        // use some of the params to set the similarity function
        // if no class is specified, default to BM25 like before
        if (params.model == null || params.model.className.isEmpty()) {
            similarityFunction = new BM25Similarity();
        } else { // otherwise try to load the class
            try {
                setSimilarityFunction(params);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(params.indexName).toPath()));
            searcher = new IndexSearcher(reader);

            searcher.setSimilarity(similarityFunction);

            parser = new QueryParser("content", analyzer);

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


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
// TODO: Could this class be moved to it's own class (for reuse elsewhere?)
class RetrievalParams {
    public String indexName;
    public String queryFile;
    public String resultFile;
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

    // the <model> element
    @XmlElement(name = "model")
    public Model model;

    @XmlAccessorType(XmlAccessType.FIELD)
    /**
     * Encapsulate the className and parameters given to a similarity function
     */
    public static class Model {

        @XmlAttribute
        public String className;

        @XmlAttribute(name = "params")
        @XmlList
        public List<Float> params;
    }

}



