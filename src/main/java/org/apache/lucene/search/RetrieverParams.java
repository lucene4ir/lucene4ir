package org.apache.lucene.search;

/**
 * See {@link lucene4ir.RetrievalParams}
 */
public class RetrieverParams {
    public String indexName;
    public String queryFile;
    public String resultFile;
    public String model;
    public int maxResults;
    public float k;
    public float b;
    public float lam;
    public float beta;
    public float mu;
    public float c;
    public float delta;
    public String runTag;
}
