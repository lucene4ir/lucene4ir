package org.apache.lucene.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lucene4ir.LuceneConstants;

/**
 * Abstract class that provides common functionalities to perform batch retrieval.
 * This class is intended for implementation of retrieval models by Apache Lucene
 * index API.
 *
 * In order to implement a new retrieval model and/or a new document matching procedure
 * the abstract method {@link #runQuery(String, String) runQuery} should be implemented.
 * See {@link RetrieverOkapiBM25} for an implementation of Okapi BM25.
 *
 * Created by dibuccio on 09/09/2016.
 */
public abstract class Retriever {

    RetrieverParams p;

    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;
    QueryParser parser;

    String fieldToQuery = "content";

    public Retriever(RetrieverParams retrievalParams){
        this.p=retrievalParams;

        try {

            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );

            searcher = new IndexSearcher(reader);

            analyzer = LuceneConstants.ANALYZER;

            parser = new QueryParser("content", analyzer);

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    /**
     * Returns the list of tokens extracted from the query string using the specified analyzer.
     *
     * @param field document field.
     *
     * @param queryTerms query string.
     *
     * @param distinctTokens if true, return the distinct tokens in the query string.
     *
     * @return the list of tokens extracted from the given query.
     *
     * @throws IOException
     */
    List<String> getTokens(String field, String queryTerms, boolean distinctTokens) throws IOException {

        List<String> tokens = new ArrayList<String>();

        StringReader topicTitleReader = new StringReader(queryTerms);

        Set<String> seenTokens = new TreeSet<String>();

        TokenStream tok;
        tok = analyzer.tokenStream(field, topicTitleReader);
        tok.reset();
        while (tok.incrementToken()) {
            Iterator<AttributeImpl> atts = tok.getAttributeImplsIterator();
            AttributeImpl token = atts.next();
            String text = "" + token;
            if (seenTokens.contains(text) && distinctTokens) {
                continue;
            }
            seenTokens.add(text);
            tokens.add(text);
        }
        tok.close();

        return tokens;
    }

    /**
     * Process the query file specified in the retrieval parameters (queryFile)
     * and print the results in trec_eval format in a text file (resultFile).
     */
    public void processQueryFile(){

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
                    for (int i=1; i<parts.length; i++)
                        queryTerms = queryTerms + " " + parts[i];

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
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Performs document matching for the query (queryTerms) with the given identifier (qno).
     *
     * @param qno identifier / number of the query.
     *
     * @param queryTerms string containing the query content.
     *
     * @return array of {@link ScoreDoc} containing the ranked result list.
     *
     */
    public abstract ScoreDoc[] runQuery(String qno, String queryTerms);


}

