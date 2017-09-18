package lucene4ir;

import lucene4ir.predictor.PostQPPredictor;
import lucene4ir.predictor.PreQPPredictor;
import lucene4ir.predictor.QPPredictor;
import lucene4ir.utils.DocMap;
import lucene4ir.utils.TokenAnalyzerMaker;
import lucene4ir.utils.trec.TrecRuns;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import javax.print.Doc;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QPPApp {

    public QPPParams p;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
//    protected CollectionModel colModel;

    private QueryParser parser;
    private List<PreQPPredictor> prePredictors;
    private List<PostQPPredictor> postPredictors;

    private void readParamsFromFile(String paramFile) {
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */


        try {
            p = JAXB.unmarshal(new File(paramFile), QPPParams.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


//        String fieldsFile = p.fieldsFile;

        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("QP Prediction File: " + p.qppFile);

        System.out.println("Pre-retrieval QP Prediction Classes: ");
        p.preQPP.forEach(p -> System.out.println("\t" + p));
        System.out.println("Pre-retrieval QP Prediction Classes: ");
        p.postQPP.forEach(p -> System.out.println("\t" + p.reference));

        if (p.fieldsFile != null) {
            System.out.println("Fields File: " + p.fieldsFile);
        }

        if (p.tokenFilterFile != null) {
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(p.tokenFilterFile);
        } else {
            analyzer = Lucene4IRConstants.ANALYZER;
        }

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
            File file = new File(p.qppFile);

            try (BufferedReader br = new BufferedReader(new FileReader(p.queryFile)); FileWriter fw = new FileWriter(file)) {
                String line = br.readLine();
                while (line != null) {

                    String[] parts = line.split(" ");
                    String qno = parts[0];
                    StringBuilder queryTerms = new StringBuilder();
                    for (int i = 1; i < parts.length; i++)
                        queryTerms.append(" ").append(parts[i]);

                    ArrayList<Double> scores = runQuery(qno, queryTerms.toString());
                    StringBuilder vals = new StringBuilder();
                    for (Double score : scores) {
                        vals.append(" ").append(score.toString());
                    }
                    fw.write(qno + vals);
                    fw.write(System.lineSeparator());

                    line = br.readLine();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ArrayList<Double> runQuery(String qno, String queryTerms) {
        ArrayList<Double> predictions = new ArrayList<>();

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            for (QPPredictor predictor : prePredictors) {
                double val = predictor.scoreQuery(qno, query);
                predictions.add(val);
            }

            for (PostQPPredictor predictor : postPredictors) {
                double val;
                if (predictor.getRun().getTopic(qno).size() > 0) {
                    val = predictor.scoreQuery(qno, query);
                } else {
                    val = 0;
                }
                predictions.add(val);
            }


        } catch (ParseException pe) {
            pe.printStackTrace();
            System.exit(1);
        }
        return predictions;
    }

    @SuppressWarnings("WeakerAccess")
    QPPApp(String qppParamFile) {
        System.out.println("Query Performance Prediction App");
        readParamsFromFile(qppParamFile);
        try {

            reader = DirectoryReader.open(FSDirectory.open(new File(p.indexName).toPath()));
            searcher = new IndexSearcher(reader);
            parser = new QueryParser("content", analyzer);

            // [[WARNING: JANK]] Create a mapping of docnum->docid.
            for (int i = 0; i < reader.numDocs(); i++) {
                String docNum = reader.document(i).getField(Lucene4IRConstants.FIELD_DOCNUM).stringValue();
                DocMap.getInstance().add(docNum, i);
            }

            // Instantiate the pre-retrieval QPPs
            prePredictors = new ArrayList<>();
            for (String p : p.preQPP) {
                PreQPPredictor c = (PreQPPredictor) Class.forName(p).getDeclaredConstructor(IndexReader.class).newInstance(reader);
                prePredictors.add(c);
            }

            // Instantiate the post-retrieval QPPs
            postPredictors = new ArrayList<>();
            for (PostQPPParams p : p.postQPP) {
                // Insert the default values that all post retrieval classes have.
                Class[] classes = new Class[p.constructor.size() + 2];
                classes[0] = IndexReader.class;
                classes[1] = TrecRuns.class;
                Object[] objects = new Object[p.constructor.size() + 2];
                objects[0] = reader;
                objects[1] = TrecRuns.load(Paths.get(p.runFile));

                // For each of the arguments in the constructor, parse it into a Java object.
                int i = 2;
                for (ConstructorArgParams param : p.constructor) {
                    // Grab the type of the argument.
                    Class c = Class.forName(param.reference);
                    classes[i] = c;

                    // Create an actual value for the argument.
                    String typeName = c.getTypeName();
                    if (typeName.equals(Double.class.getTypeName())) {
                        objects[i] = Double.parseDouble(param.value);
                    } else if (typeName.equals(Integer.class.getTypeName())) {
                        objects[i] = Integer.parseInt(param.value);
                    } else if (typeName.equals(Long.class.getTypeName())) {
                        objects[i] = Long.parseLong(param.value);
                    } else {
                        throw new Exception(String.format("Cannot parse %s as %s", param.reference, param.value));
                    }
                    i++;
                }

                // Now that we have a valid signature for the constructor, we can instantiate the new class.
                Constructor c = Class.forName(p.reference).getDeclaredConstructor(classes);
                PostQPPredictor qpp = (PostQPPredictor) c.newInstance(objects);

                // And add it to the list of post retrieval QPPs.
                postPredictors.add(qpp);
            }

            System.out.print("Number of Pre-retrieval Predictors: ");
            System.out.println(prePredictors.size());

            System.out.print("Number of Post-retrieval Predictors: ");
            System.out.println(postPredictors.size());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {

        String qppParamFile = "";

        try {
            qppParamFile = args[0];
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        QPPApp predicter = new QPPApp(qppParamFile);
        predicter.processQueryFile();

    }

}

@SuppressWarnings("WeakerAccess")
@XmlRootElement(name = "QPPParams")
class QPPParams {
    public String indexName;
    public String queryFile;
    public String qppFile;
    @XmlElementWrapper
    @XmlElement(name = "predictor")
    public List<String> preQPP;
    @XmlElementWrapper
    @XmlElement(name = "predictor")
    public List<PostQPPParams> postQPP;
    public String tokenFilterFile;
    public String fieldsFile;
}

@XmlRootElement(name = "predictor")
final class PostQPPParams {
    public String reference;
    public String runFile;
    @XmlElementWrapper
    @XmlElement(name = "arg")
    public List<ConstructorArgParams> constructor;
}

final class ConstructorArgParams {
    public String reference;
    public String value;
}
