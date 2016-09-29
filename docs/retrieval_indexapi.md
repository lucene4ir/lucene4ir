# Lucene4IR - Retrieval

Lucene for Information Retrieval Research and Evaluation


## RetrievalAppByIndexAPI

Retrieval Application that performs document matching through Apache Lucene index API.

The retrieval parameter file is in the same format of that used by the ``lucene4ir.RetrievalApp``.

Below is an example of the retrieval parameters.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<indexParams>
    <indexName>path/to/the/index</indexName>
    <queryFile>data/cacm/title.query</queryFile>
    <maxResults>100</maxResults>
    <model>okapibm25</model>
    <resultFile>data/cacm/bm25_results.res</resultFile>
    <b>0.75</b>
    <k>1.2</k>
</indexParams>
```

where:

- **indexName**: the path to the index (the application currently assumes that the indexer creates documents with a content field)
- **queryFile**: the name of the file that contains the list of queries to issue (format: query_num query_text, one per line)
- **maxResults**: the maximum number of results to output per query
- **model**: the retrieval algorithm to use
	  - **okapibm25** - Okapi Best Match 25 (b,k)
	  - **default** - okapibm25 with b=0.75 and k=1.2
- **resultFile**: the name of the file to output the results to
- parameters: **b**, **c**, **k**, **mu**, **beta**, **lam**, values for the retrieval algorithm selected.

If no model is given, the default model is selected. If no parameters are provided, default values are used. If no resultsfile is provided, a result file name is auto generated from the model name.

### How to implement a new retrieval algorithm

In order to implement a new retrieval model and/or a new document matching strategy the abstract class ``org.apache.lucene.search.Retriever`` should be subclassed.
Indeed, the ``Retriever`` class provides only functionalities that could be useful for a generic retrieval algorithm, e.g. extract tokens from the query string and batch process for all the queries in the input query file. The subclass should provide an implementation of the method ``runQuery(String, String)``. An example of ``Retriever`` subclass is the ``org.apache.lucene.search.RetrieverOkapiBM25`` that provides an implementation of the Okapi BM25 weighting scheme using a Document At A Time (DAAT) document matching strategy.
