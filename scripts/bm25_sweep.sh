#!/bin/bash

params=(0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.65 0.7 0.75 0.8 0.85 0.9 1.0)
for i in "${params[@]}"; do
    python lucene_models_trec_script_generator.py /Users/colin/Workspace/lucene4ir title.trec2005.aquaint.query aquaint BM25 $i
done