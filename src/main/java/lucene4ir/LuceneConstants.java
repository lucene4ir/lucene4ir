package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneConstants {

    // Common analyzer
    public static final Analyzer ANALYZER = new StandardAnalyzer();

    // Field names
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_DOCNUM = "docnum";
    public static final String FIELD_PUBDATE = "pubdate";
    public static final String FIELD_SOURCE = "source";

}
