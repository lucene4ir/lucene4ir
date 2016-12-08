package lucene4ir.utils;

import lucene4ir.LuceneConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.Similarity;

import javax.xml.bind.JAXB;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Harry Scells on 8/12/16.
 */
public class RetrievalParamsReader {

    private Similarity similarityFunction;
    private Analyzer analyzer;

    public Similarity getSimilarityFunction() {
        return similarityFunction;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public RetrievalParams read(String paramFile) {
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */
        RetrievalParams params = null;
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
        if (params.model == null) {
            params.model = new RetrievalParams.Model();
            params.model.className = "org.apache.lucene.search.similarities.BM25Similarity";
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

        if (params.tokenFilterFile != null) {
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(params.tokenFilterFile);
        } else {
            analyzer = LuceneConstants.ANALYZER;
        }

        // use some of the params to set the similarity function
        try {
            similarityFunction = loadSimilarityFunction(params);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return params;
    }

    private Similarity loadSimilarityFunction(RetrievalParams params) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
        return (Similarity) func.getConstructor(types).newInstance(args);
    }
}

