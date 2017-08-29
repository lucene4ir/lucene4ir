#!/bin/bash

params=(0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0)
for i in "${params[@]}"; do
    python lucene_models_bigram_script_generator.py /Users/kojayboy/Workspace/Lucene-code/lucene4ir ap_bigram.qry ap BM25 $i
done
