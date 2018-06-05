#!/bin/bash

#Takes a single param file
#Extracts the bigram.qry file
#Splts the bigram.qry file into multiple small files
#Creates new param files to run each of the bigram files
#Executes runs based on the new param files (parallellised?)



# Extract the bigrams file from the parameter file
QFILE=$(grep "queryFile" $1 | cut -d ">" -f 2 | cut -d "<" -f 1)
RFILE=$(grep "resultFile" $1 | cut -d ">" -f 2 | cut -d "<" -f 1)
# Split bigram file into multiple files and save those files to a list var
QLIST="$(python scripts/queryfile_splitter.py "$QFILE")"

# Create new param files for each new bigram file and store those to a list
PLIST="$(
while read -r line; do
    python scripts/paramfile_splitter.py $1 $line
done <<<"$QLIST")"

# Work out number of cores for parallelisation
cores="$(sysctl -n hw.ncpu)"

# Begin runnin the retrieval app parallelised
echo $PLIST | xargs -n 1 -P $cores time java -cp $LCP lucene4ir.RetrievalApp > exec.out

RLIST="$(echo $PLIST | xargs -n 1 grep "<resultFile>" | cut -d ">" -f 2 | cut -d "<" -f 1)"

echo $RLIST | xargs -n 1 wc -l

cat $RLIST > $RFILE

mail -s "Run execution finished: $1" c.mclellan.1@research.gla.ac.uk