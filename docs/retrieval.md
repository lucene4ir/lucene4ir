# Lucene4IR - Retrieval

Lucene for Information Retrieval Research and Evaluation


## RetrievalApp

The Retrieval Application lets you specify the collection/index, the queries and the retrieval model, along with how it is parameterized.

Below is an example of the retrieval parameters.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<indexParams>
    <indexName>path/to/the/index</indexName>
    <queryFile>data/cacm/title.query</queryFile>
    <maxResults>100</maxResults>
    <model>bm25</model>
    <resultFile>data/cacm/bm25_results.res</resultFile>
    <b>0.75</b>
    <k>1.2</k>
</indexParams>
```

where:

- **indexName**: the path to the index (the application currently assumes that the indexer creates documents with a content field)
- **queryFile**: the name of the file that contains the list of queries to issue (format: query_num query_text, one per line)
- **maxResults**: the maximum number of results to output per query
- **model**: the retrieval algorithm to use (called similarity function in Lucene)
	  - **bm25** - best match 25 (b,k)
	  - **lmj** - language model with jelinek mercer smoothing (lam)
	  - **lmd** - language model with dirichlet prior smoothing (mu)
	  - **pl2** - divergence from randomness model (c)
	  - **default** - bm25 b=0.75 and k=0.75
- **resultFile**: the name of the file to output the results to
- parameters: **b**, **c**, **k**, **mu**, **beta**, **lam**, values for the retrieval algorithm selected.

If no model is given, the default model is selected. If no parameters are provided, default values are used. If no resultsfile is provided, a result file name is auto generated from the model name.













