package lucene4ir;

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
import lucene4ir.predictor.*;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class QPPApp {

    public QPPParams p;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
    protected QueryParser parser;
    protected CollectionModel colModel;
    protected String fieldsFile;

    protected List<QPPredictor> predictors;


    public void readParamsFromFile(String paramFile){
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */


        try {
            p = JAXB.unmarshal(new File(paramFile), QPPParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }


        fieldsFile = p.fieldsFile;

        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("QP Prediction File: " + p.qppFile);
        if (p.fieldsFile!=null){
            System.out.println("Fields File: " + p.fieldsFile);
        }
        if (p.tokenFilterFile != null){
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(p.tokenFilterFile);
        }
        else{
            analyzer = LuceneConstants.ANALYZER;
        }

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
            File file = new File(p.qppFile);
            FileWriter fw = new FileWriter(file);

            try {
                String line = br.readLine();
                while (line != null){

                    String[] parts = line.split(" ");
                    String qno = parts[0];
                    String queryTerms = "";
                    for (int i=1; i<parts.length; i++)
                        queryTerms = queryTerms + " " + parts[i];

                    ArrayList<Double> scores = runQuery(qno, queryTerms);
                    String vals = "";
                   for(int i=0; i<scores.size(); i++){
                        vals = vals + " " + scores.get(i).toString();
                   }
                    fw.write(qno + vals);
                    fw.write(System.lineSeparator());

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

    public ArrayList<Double> runQuery(String qno, String queryTerms){
        ArrayList<Double> predictions = new ArrayList<Double>();

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            System.out.println(query.toString());
            double val =0.0;
            for (int i=0; i<predictors.size(); i++){
                val = predictors.get(i).scoreQuery(qno, query);
                // System.out.println(val);
                predictions.add(val);

            }


        } catch (ParseException pe){
            System.out.println("Can't parse query");
        }
        return predictions;
    }

    public QPPApp(String qppParamFile){
        System.out.println("Query Performance Prediction App");
        readParamsFromFile(qppParamFile);
        try {

            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);
            parser = new QueryParser("content", analyzer);

            predictors = new ArrayList<QPPredictor>();
            /*
            can this be reflective, and build all?
             */
            predictors.add( new TermLenQPPredictor(reader) );
            predictors.add( new CharLenQPPredictor(reader) );
            predictors.add( new AvgIDFQPPredictor(reader) );

            System.out.print("Number of Predictors: ");
            System.out.println(predictors.size());

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public static void main(String []args) {


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


@XmlRootElement(name = "QPPParams")
class QPPParams {
    public String indexName;
    public String queryFile;
    public String qppFile;
    public String tokenFilterFile;
    public String fieldsFile;
}



