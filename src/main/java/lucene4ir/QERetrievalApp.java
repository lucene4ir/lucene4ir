package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.store.FSDirectory;

import lucene4ir.similarity.SMARTBNNBNNSimilarity;
import lucene4ir.similarity.OKAPIBM25Similarity;
import lucene4ir.similarity.BM25LSimilarity;
import lucene4ir.similarity.BM25Similarity;
import lucene4ir.utils.TokenAnalyzerMaker;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.*;

/**
 * Created by colin on 17/08/2017.
 */
public class QERetrievalApp extends RetrievalApp{

    protected QERetrievalParams qep;
    protected float qeBeta;
    protected int feedbackDocs;
    protected int feedbackTerms;

    private HashMap<Integer, String> id2num = new HashMap<Integer, String>();
    private HashMap<String, Integer> num2id = new HashMap<String, Integer>();

    public QERetrievalApp(String retrievalParamFile){
        super(retrievalParamFile);
        this.readParamsFromFile(qeFile);
        mapDocid();
        feedbackDocs = 5;
        feedbackTerms = 5;
        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);

            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);
            parser = new QueryParser("content", analyzer);

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public ScoreDoc[] runQuery(String qno, String queryTerms){
        ScoreDoc[] hits = null;

        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            try {
                TopDocs results = searcher.search(query, feedbackDocs);
                hits = results.scoreDocs;

                int n = Math.min(feedbackDocs, results.totalHits);

                List<String> docList = new ArrayList<String>();
                for(int i=0; i<n; i++){
                    Document doc = searcher.doc(hits[i].doc);
                    String docno = doc.get("docnum");
                    docList.add(docno);
                }
            }
            catch (IOException ioe){
                System.out.println(" caught a " + ioe.getClass() +
                        "\n with message: " + ioe.getMessage());
            }
        } catch (ParseException pe){
            System.out.println("Can't parse query");
        }
        return hits;
    }

    public void extractTerms(List<String> docList){
        try {
            for (String docnum : docList) {
                int docid = num2id.get(docnum);
                Terms tv = reader.getTermVector(docid, "all");
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void  mapDocid() {
        try {
            int n = reader.maxDoc();

            for (int i = 0; i < n; i++) {
                Document doc = reader.document(i);

                // the doc.get pulls out the values stored - ONLY if you store the fields
                String docno = doc.get("docno");
                id2num.put(i, docno);
                num2id.put(docno, i);
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                "\n with message: " + e.getMessage());
        }
    }

    public void readParamsFromFile(String paramFile){
        try {
            qep = JAXB.unmarshal(new File(paramFile), QERetrievalParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        if (qep.noDocs<1)
            qep.noDocs=10;
        if (qep.noTerms<1)
            qep.noTerms=10;
    }

    public static void main(String []args) {

        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        QERetrievalApp retriever = new QERetrievalApp(retrievalParamFile);
        retriever.processQueryFile();
    }
}

@XmlRootElement(name = "qe")
@XmlAccessorType(XmlAccessType.FIELD)
class QERetrievalParams {
    @XmlElement(name = "QEMethod")
    public String qeMethod;
    @XmlElement(name = "NoDocs")
    public int noDocs;
    @XmlElement(name = "NoTerms")
    public int noTerms;
    @XmlElement(name = "qeBeta")
    public float qeBeta;
}