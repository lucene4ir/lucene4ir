# Lucene4IR - Indexing

## IndexerApp

The Indexer Application lets you specific the type of TREC documents to be indexed and location of the index. 

Below is an example of the index parameters

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<indexParams>
    <indexName>index</indexName>
    <fileList>data/cacm_file_list</fileList>
    <indexType>cacm</indexType>
    <tokenFilterFile>params/index/example_01.xml</tokenFilterFile>
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
- *tokenFilterFile*: an xml file describing how the tokenization should be performed


An example tokenFilerFile:

```
<tokenFilters>
         <tokenizer>standard</tokenizer>
         <tokenFilter>
             <name>lowercase</name>
         </tokenFilter>
         <tokenFilter>
             <name>porterstem</name>
         </tokenFilter>
</tokenFilters>
```

where the terms are converted to lowercase and then porter stemmed.


In the data directory we have provided some sample files to show how the indexing works.

