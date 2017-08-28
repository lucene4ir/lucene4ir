package lucene4ir;

import lucene4ir.predictor.QPPredictor;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class QPPApp {

    public QPPParams p;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
//    protected CollectionModel colModel;

    private QueryParser parser;
    private List<QPPredictor> predictors;

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
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }


//        String fieldsFile = p.fieldsFile;

        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("QP Prediction File: " + p.qppFile);
        System.out.println("QP Prediction Classes: ");
        p.predictorClasses.forEach(p -> System.out.println("\t" + p));
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
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    private ArrayList<Double> runQuery(String qno, String queryTerms) {
        ArrayList<Double> predictions = new ArrayList<>();

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            System.out.println(query.toString());
            double val;
            for (QPPredictor predictor : predictors) {
                val = predictor.scoreQuery(qno, query);
                // System.out.println(val);
                predictions.add(val);
            }


        } catch (ParseException pe) {
            System.out.println("Can't parse query");
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

            predictors = new ArrayList<>();

            for (String p : p.predictorClasses) {
                QPPredictor c = (QPPredictor) Class.forName(p).getDeclaredConstructor(IndexReader.class).newInstance(reader);
                predictors.add(c);
            }
            System.out.print("Number of Predictors: ");
            System.out.println(predictors.size());

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public static void main(String[] args) {

        String qppParamFile = "";

        try {
            qppParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
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
    public List<String> predictorClasses;
    public String tokenFilterFile;
    public String fieldsFile;
}



