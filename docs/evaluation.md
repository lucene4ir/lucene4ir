# Lucene4IR - Evaluation

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