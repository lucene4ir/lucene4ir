package lucene4ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static lucene4ir.QETerm.QETermComparator;

/**
 * Created by colin on 17/08/2017.
 * Implements query expansion using Rocchio's expansion by taking the initial query and results list
 * then expanding the query with the top t terms from the top d documents.
 */

//TODO: Stop the inclusion of original query terms in expansion terms.

public class QERetrievalApp extends RetrievalApp{

    protected QERetrievalParams qep;
    protected float qeBeta;
    protected int feedbackDocs;
    protected int feedbackTerms;
    protected long doc_count;

    private HashMap<Integer, String> id2num = new HashMap<Integer, String>();
    private HashMap<String, Integer> num2id = new HashMap<String, Integer>();

    /**
     * Instantiates the QERetrievalApp, setting up RetrievalApp from the retrievalParamFile
     * @param retrievalParamFile
     */
    public QERetrievalApp(String retrievalParamFile){
        super(retrievalParamFile);
        System.out.println("Query Expansion");
        this.readQEParamsFromFile(qeFile);
        mapDocid();

        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);

            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);
//            parser = new QueryParser("content", analyzer);
            CollectionStatistics collectionStats = searcher.collectionStatistics(Lucene4IRConstants.FIELD_ALL);
            doc_count = collectionStats.docCount();
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    /**
     * Performs the scoring of documents from the query issued
     * @param qno
     * @param queryTerms
     * @return
     */
    public ScoreDoc[] runQuery(String qno, String queryTerms){
        ScoreDoc[] hits = null;

        // Original Query
        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            try {
                TopDocs results = searcher.search(query, p.maxResults);
                hits = results.scoreDocs;

                Vector<Terms> terms = getDocsTerms(getDocs(queryTerms,hits));
                // will crash if there were no hits for a query.
                // What should happen if there are no hits? Exit and return regular retrieval?
                if (terms.size()>1) {
                    String[] split = Arrays.stream(query.toString().split("all:")).map(String::trim).toArray(String[]::new);

                    String oldQueryString="";
                    for(String s: split)
                        oldQueryString+= s.trim() + " ";
                    List<String> rankedTerms = rankTerms(combineTerms(terms));

                    List<String> oldTerms = Arrays.asList(split);
                    String qString = "";
                    int numterms = 0;
                    int i =0;
                    while(i < terms.size() && numterms<feedbackTerms){
                        String rankedTerm = rankedTerms.get(i).trim();
                        if (!oldTerms.contains(rankedTerm)) {
                            qString += rankedTerm + " ";
                            numterms ++;
                        }
                        i++;
                    }

                    Query oq = parser.parse(QueryParser.escape(oldQueryString));
                    Query nq = parser.parse(QueryParser.escape(qString.trim()));

                    BoostQuery obq = new BoostQuery(oq,(1-qeBeta));
                    BoostQuery nbq = new BoostQuery(nq,(qeBeta));
                    BooleanClause obc=new BooleanClause(obq,BooleanClause.Occur.SHOULD);
                    BooleanClause nbc=new BooleanClause(nbq,BooleanClause.Occur.SHOULD);
                    BooleanQuery.Builder bqb = new BooleanQuery.Builder();
                    bqb.add(obc);
                    bqb.add(nbc);
                    BooleanQuery q = bqb.build();
                    System.out.println("ExpandedQuery: " + q.toString());
                    results = searcher.search(q, p.maxResults);
                    hits = results.scoreDocs;
                }
            }
            catch (IOException ioe){
                System.out.println(" caught a " + ioe.getClass() +
                        "\n with message: " + ioe.getMessage());
            }
        } catch (ParseException pe){
            System.out.println("Can't parse query");
        }

        // BEFORE returning, this should re-run the query with the expanded terms.
        return hits;
    }



    /**
     * Gets documents that will be used in query expansion.
     * number of docs indicated by <code>QueryExpansion.feedbackDocs</code>
     *
     * @param query - for which expansion is being performed
     * @param hits - list of scored documents
     * @return number of docs indicated by <code>QueryExpansion.feedbackDocs</code>
     * @throws IOException
     */
    private Vector<Document> getDocs( String query, ScoreDoc[] hits ) throws IOException {
        Vector<Document> vHits = new Vector<Document>();
        // Extract only as many docs as necessary
        int n = Math.min(feedbackDocs, hits.length);
        // Convert Hits -> Vector
        for(int i=0; i<n; i++){
            Document doc = searcher.doc(hits[i].doc);
            String docno = doc.get("docnum");
//            System.out.println(docno);
            vHits.add(doc);
        }
        return vHits;
    }

    /**
     * Extracts terms of the documents; Adds them to vector in the same order
     *
     * @param hits - from which to extract terms
     *
     * @return docsTerms docs must be in order
     */
    public Vector<Terms> getDocsTerms( Vector<Document> hits)
            throws IOException, ParseException {
        Vector<Terms> docsTerms = new Vector<>();

        // Process each of the documents
        for ( int i = 0; i < hits.size(); i++ ) {
            Document doc = hits.elementAt( i );
            int docid = num2id.get(doc.get("docnum"));
            Terms t = reader.getTermVector(docid, "all");
            docsTerms.add(t);
        }

        return docsTerms;
    }

    /**
     * Combines the individual term vectors of each document into a single list.
     * @param terms
     * @return
     */
    public HashMap<String, QETerm> combineTerms(Vector<Terms> terms){
        HashMap<String, QETerm> combinedTerms = new HashMap<String, QETerm>();
        int numDocs = terms.size();
        for(Terms ts : terms){
            try {
                TermsEnum te = ts.iterator();
                BytesRef term;
                while ((term = te.next()) != null) {
                    String tString = term.utf8ToString();
                    QETerm qet = new QETerm(tString, te.totalTermFreq(),te.docFreq(),numDocs);
                    if (combinedTerms.containsKey(tString)){
                        QETerm mergedTerm = qet.combine(combinedTerms.get(tString));
                        combinedTerms.replace(tString,mergedTerm);
                    }
                    else
                        combinedTerms.put(tString,qet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return combinedTerms;
    }

    /**
     * Ranks the terms in order of their TF.IDF scores
     * @param terms
     * @return
     */
    public List<String> rankTerms(HashMap<String, QETerm> terms){
        List<String> rankedTerms = new ArrayList<>();
        List<QETerm> qetermList = new ArrayList<>();
        for(String s : terms.keySet()){
            QETerm qet = terms.get(s);
            qetermList.add(qet);
        }

        // Sort the list of QETerms using a custom comparator.
        // The comparator is based on TF.IDF scores.
        qetermList.sort(QETermComparator);
//        Collections.reverse(qetermList);
        for(QETerm qet: qetermList)
            rankedTerms.add(qet.term);
        return rankedTerms;
    }

    /**
     * A helper method to convert document names to lucene docids
     */
    public void  mapDocid() {
        try {
            int n = reader.maxDoc();

            for (int i = 0; i < n; i++) {
                Document doc = reader.document(i);

                // the doc.get pulls out the values stored - ONLY if you store the fields
                String docno = doc.get("docnum");
                id2num.put(i, docno);
                num2id.put(docno, i);
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                "\n with message: " + e.getMessage());
        }
    }


    /**
     * Reads the additional parameters required for expansion.
     * noDocs, noTerms and additional alphas.
     * @param paramFile
     */
    public void readQEParamsFromFile(String paramFile){
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
        qeBeta=qep.qeBeta;
        feedbackDocs = qep.noDocs;
        feedbackTerms = qep.noTerms;
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


/**
 * QE Retrieval App params
 */
@XmlRootElement(name = "qe")
@XmlAccessorType(XmlAccessType.FIELD)
class QERetrievalParams {
    @XmlElement(name = "qeMethod")
    public String qeMethod;
    @XmlElement(name = "noDocs")
    public int noDocs;
    @XmlElement(name = "noTerms")
    public int noTerms;
    @XmlElement(name = "qeBeta")
    public float qeBeta;
}

/**
 * QETerm Class for storing a term and its frequencies.
 */
class QETerm implements Comparable<QETerm>{
    protected String term;
    protected long tf;
    protected int df;
    protected int csize;
    protected double tfidf;

    /**
     * Instantiates a new Query Expansion Term.
     * @param term
     * @param tf
     * @param df
     * @param csize
     */
    public QETerm(String term, long tf, int df, int csize){
        this.term = term;
        this.tf = tf;
        this.df = df;
        this.csize = csize;
        this.tfidf=computeTFIDF();
    }

    /**
     * Combines 2 instantiations of the same term together.
     * Sums the document frequencies and term frequnecies together.
     * @param qet2
     * @return
     */
    public QETerm combine( QETerm qet2){
        return new QETerm(this.term, (this.tf + qet2.tf), (this.df + qet2.df), this.csize);
    }

    /**
     * Converts the QETerm to a nicely formated string
     */
    public String toString(){
        return this.term + " TF: " + this.tf + " DF: " + this.df + " TF.IDF: " + this.tfidf;
    }

    /**
     * Calculates the TF.IDF score for a term using a smoothed Logarithmic TF
     * and a Normalized IDF sum.
     * @return
     */
    public double computeTFIDF(){
        // Smoothed IDF normalized
        double idf = Math.log((df + 0.5)/(csize - df  + 0.5));
        // Smoothed logarithmic TF.IDF to avoid any issues division by 0.
        double tfidf = Math.log(1+tf)*idf;
        return tfidf;
    }

    /**
     * Compares the current QETerm to another QETerm.
     * For use in sorting
     * @param compareQETerm
     * @return
     */
    public int compareTo(QETerm compareQETerm) {
        if (this.tfidf<compareQETerm.tfidf) return 1;
        if (this.tfidf>compareQETerm.tfidf) return -1;
        return 0;
    }

    /**
     * Creates a Comparator to use the custom compareTo method.
     */
    public static Comparator<QETerm> QETermComparator = new Comparator<QETerm>() {
        public int compare(QETerm o1, QETerm o2) {
            return o1.compareTo(o2);
        }
    };
}