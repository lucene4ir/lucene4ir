# Lucene4IR - Setup

Lucene for Information Retrieval Research and Evaluation


## Code and Data
In ~/lucene4ir/data, there are a number of folders contain different data sets (or part there of).

- In ~/lucene4ir/data/cacm, there is a very small collection, called, CACM, which is about 3000 abstracts from the ACM library along with queries and relevance judgements.
- TBA, sample TREC data for various collections


In ~/lucene4ir/src/, there are a currently three apps, an Indexing Application (IndexerApp), a Retrieval Application (RetrievalApp) and an application that pulls out various statistics (ExampleStatsApp). Each of these apps are configured based on XML parameter files (see ~/lucene4ir/params for examples).

The code is based on examples developed by https://github.com/isoboroff/trec-demo and https://github.com/lintool/Anserini

## Setups

### IntelliJ Setup
We are using IntelliJ by JetBrains (https://www.jetbrains.com/idea/download/),
so once you clone the repo you'll need to see up a few things so that you can run and compile
the apps.

Assuming you are using IntelliJ, go to File, Project Structure, then the Project tab.

- Set the Project SDK, we are using Java 1.8.
- Set the Project Language Level, we are using 8.
- Set the Project Compilier output directory, we have pointed it to ~/lucene4ir/out/

Next, go to the Modules tab and mark the following directories as follows.
- Mark the ~/lucene4ir/src directory as Sources
- Mark the ~/lucene4ir/src/main directory as Sources
- Mark the ~/lucene4ir/src/main/java directory as Sources
- Mark the ~/lucene4ir/test directory as Tests

Also, +Add Content Root, to be ~/lucene4ir/

Then, go to the SDKs tab, and add in the jars in ~/lucene4ir/jars. We are using Lucene 6.2 for this demo code and JSoup 1.6.2.

#### Run Configuration Setups
Again assuming that you are using the IntelliJ IDE, create the following run configurations.
Go to the Run menu, and select Edit Configurations. In the top left hand side of the Run/Debug Configurations window, click the add button (+) to add a new configuration, and select Application. Repeat and set up for the following apps.

- **IndexerApp**
	- **Name**: IndexerApp
	- **Main Class**: lucene4ir.IndexerApp
	- **Program Arguments**: params/index/index_params.xml
	- **Working Directory**: ~/lucene4ir
- **RetrievalApp**
	- **Name**: RetrievalApp
	- **Main Class**: lucene4ir.RetrievalApp
	- **Program Arguments**: params/retrieval_params.xml
	- **Working Directory**: ~/lucene4ir
- **ExampleStatsApp**
	- **Name**: ExampleStatsApp
	- **Main Class**: lucene4ir.ExampleStatsApp
	- **Program Arguments**: params/example_stats_params.xml
	- **Working Directory**: ~/lucene4ir

### Eclipse Setup
#### Project creation and build path configuration
1. Start a new java project using File->Import...->Existing Maven project
2. Uncheck Use default location option and set Project name to Lucene. 
3. Set the location option to the path PATH/TO/lucene4IR/ and click Finish.

####Class Execution

Classes with main method can be executed by right clicking on class name, selecting Run As option and selecting Java application.
If a class needs command line parameters, right click and select Run configuration. Add the parameters in Arguments tab of Run 
Configurations window. Example parameter arguments for IndexerApp and RetrievalApp are as follows:

IndexerApp:  params/index_params.xml
RetrievalApp: params/retrieval_params.xml

####JAR creation.

run `mvn package` from the shell, executable jar will be in `target/lucene4ir-0.0.1-SNAPSHOT.jar` 

## Console Setup
TBA


#


## Apps

### IndexerApp

The Indexer Application lets you specific the type of TREC documents to be indexed and location of the index. 

Below is an example of the index parameters

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<indexParams>
    <indexName>index</indexName>
    <fileList>data/cacm_file_list</fileList>
    <indexType>cacm</indexType>
</indexParams>
```

where:
- *indexName*: the path to the index
- *fileList*: a file containing a list of files that are to be indexed
- *indexType*: the format/type of the documents to be indexed
	- *cacm*: an old sample collection of abstracts from ACM
	- *trecnews*: TREC 123 Newspaper articles
	- *trecaquaint*: TREC Aquaint Newspaper collection
	- *clueweb*: TREC Clueweb
	 
In the data directory we have provided some sample files to show how the indexing works.

### RetrievalApp

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


### ExampleStatsApp
This application provides a number of examples on how to access various statistics given the index. It is a work in progress and is designed to show how to read through postings lists, how to access fields, how to get document/field counts, etc.

Once you have indexed a collection, simply specify the location of the index.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<exampleStatsParams>
    <indexName>index</indexName>
</exampleStatsParams>
```















