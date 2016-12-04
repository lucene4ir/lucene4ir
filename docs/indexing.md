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

which uses the *standard tokenizer* that divides text on word boundaries (and removes most punctuation, and is typically the best choice for most languages).
For other tokenizers, see: https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenizers.html

It then includes the *TokenFilters* where the terms are converted to lowercase and then porter stemmed.

Two things to keep in mind when specifying Token Filters:  The analysis you do at index time should also be done at query time to have matching tokens.
Certain Filters expect tokens to be in a certain form (e.g. already lowercase) so the order of ```TokenFilters``` can be of impact.

See

In the data directory we have provided some sample files to show how the indexing works.

#### Indexing Process
When plain text is passed to Lucene for indexing and querying it goes through the process of analysis.
This process starts with so called tokenization where the stream of text is broken into small indexing elements called tokens.
Besides breaking text into tokens a deeper analysis of those tokens (adding synonyms, removing stopwords, altering tokens by stemming) might be required.
To customize analysis you have to build a [```CustomAnalyzer```](https://lucene.apache.org/core/6_2_0/analyzers-common/org/apache/lucene/analysis/custom/CustomAnalyzer.html) in the ```DocumentIndexer```

```
Analyzer analyzer = CustomAnalyzer.builder(Paths.get("/path/to/config/dir"))
                    .withTokenizer("standard")
                    .addTokenFilter("lowercase")
                    .addTokenFilter("porterstem")
                    .addTokenFilter("stop", "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                    .build();
```
The analyzer allows for several kind of *Tokenizers* and *TokenFilters* to be passed to. Those filters can be adjusted via the ```/params/token_filter_params.xml``` parameter file.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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

For TokenFilters that can have multiple parameters you can add those via ```<key> <value> ``` pairs in a ```<param>`` tag.

```
    <tokenFilter>
        <name>stop</name>
        <param>
            <key>ignoreCase</key>
            <value>false</value>
        </param>
        <param>
            <key>words</key>
            <value>stopwords.txt</value>
        </param>
        <param>
            <key>format</key>
            <value>wordset</value>
        </param>
    </tokenFilter>
</tokenFilters>

```


To index character n-grams, you can use the *ngram tokenFilter*, where you can specify the minimum and maximum of the n-grams.

```
<tokenFilter>
        <name>ngram</name>
        <param>
            <key>minGramSize</key>
            <value>1</value>
        </param>
        <param>
            <key>maxGramSize</key>
            <value>3</value>
        </param>
    </tokenFilter>
```

To index word n-grams, you need to use the *shingle tokenFilter*, where you can set the minimum and maximum n-grams (for words).

```
 <tokenFilter>
        <name>shingle</name>
        <param>
            <key>minShingleSize</key>
            <value>2</value>
        </param>
        <param>
            <key>maxShingleSize</key>
            <value>3</value>
        </param>
 </tokenFilter>
```

This will still index unigrams, but also index bi-grams and tri-grams.

For other TokenFilters, see: https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenfilters.html