#!/bin/bash

params=(1 2 3 4 5 6 7 8 16 32 64 128)
for i in "${params[@]}"; do
    python lucene_models_trec_script_generator.py /Users/colin/Workspace/lucene4ir title.trec2005.aquaint.query aquaint PL2 $i
done