# lucene4ir

Lucene for Information Retrieval Research and Evaluation

## Setup
We are using IntelliJ by JetBrains (https://www.jetbrains.com/idea/download/),
so once you clone the repo you'll need to see up a few things so that you can run and compile
the apps.

Assuming you are using IntelliJ, go to File, Project Structure, then the Project tab.

- Set the Project SDK, we are using Java 1.8.
- Set the Project Language Level, we are using 8.
- Set the Project Compilier output directory, we have pointed it to ~/lucene4ir/out/

Next, go to the Modules tab and mark the following directories as follows.
- Mark the ~/lucene4ir/src directory as Sources
- Mark the ~/lucene4ir/test directory as Tests

Also, +Add Content Root, to be ~/lucene4ir/

Then, go to the SDKs tab, and add in the jars in ~/lucene4ir/jars. We are using Lucene 6.2 for this demo code.


## Code and Data
In ~/lucene4ir/data, there are a number of folders contain different data sets (or part there of).

- In ~/lucene4ir/data/cacm, there is a very small collection, called, CACM, which is about 3000 abstracts from the ACM library along with queries and relevance judgements.
- TBA, sample TREC data for various collections


In ~/lucene4ir/src/, there are a currently three apps, an Indexing Application (IndexerApp), a Retrieval Application (RetrievalApp) and an application that pulls out various statistics (ExampleStatsApp). Each of these apps are configured based on XML parameter files (see ~/lucene4ir/params for examples).


### Run Configuration Setups

Again assuming that you are using the IntelliJ IDE, create the following run configurations.
Go to the Run menu, and select Edit Configurations. In the top left hand side of the Run/Debug Configurations window, click the add button (+) to add a new configuration, and select Application. Repeat and set up for the following apps.

- Name: IndexerApp, Main Class: lucene4ir.IndexerApp, Program Arguments, params/index_params.xml, Working Directory ~/lucene4ir
- Name: RetrievalApp, Main Class: lucene4ir.RetrievalApp, Program Arguments, params/retrieval_params.xml, Working Directory ~/lucene4ir
- Name: ExampleStatsApp, Main Class: lucene4ir.ExampleStatsApp, Program Arguments, params/example_stats_params.xml, Working Directory ~/lucene4ir


Now that you have these applications set up, you can try them out. First, run the IndexerApp, which given index_params.xml, will index the CACM collection. It will take about 30 seconds.

Then you can run the ExampleStatsApp, this will read in the CACM index, and spit out some different statistics. Finally, you can run the RetrievalApp, which will take a list of queries, and run them against the index using BM25, and save the results to a result file (~/lucene4ir/data/cacm/bm25_results.res).

To evaluate the output you will need to download and install the trec_eval from NIST, http://trec.nist.gov/trec_eval/ 

In ~/lucene4ir/data/cacm/ the list of documents relevant to each query is in the file, cacm.qrels, using trec_eval, we can measure the precision, recall, etc:

```
trec_eval ~/lucene4ir/data/cacm/cacm.qrels ~/lucene4ir/data/cacm/bm25_results.res
```

which will output something like:

```
num_q          	all	51
num_ret        	all	4317
num_rel        	all	795
num_rel_ret    	all	264
map            	all	0.1477
gm_ap          	all	0.0590
R-prec         	all	0.1798
bpref          	all	0.4347
recip_rank     	all	0.4573

. . . .

P5             	all	0.2549
P10            	all	0.2059
P15            	all	0.1660
P20            	all	0.1422
P30            	all	0.1105
P100           	all	0.0518
P200           	all	0.0259
P500           	all	0.0104
P1000          	all	0.0052
```



















