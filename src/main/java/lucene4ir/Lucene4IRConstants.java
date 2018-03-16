package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class Lucene4IRConstants {

    // Common analyzer
    public static final Analyzer ANALYZER = new StandardAnalyzer();

    // Field names
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_URL = "url";
    public static final String FIELD_DOCHDR = "dochdr";
    public static final String FIELD_DOCNUM = "docnum";
    public static final String FIELD_PUBDATE = "pubdate";
    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_ALL = "all";
    public static final String FIELD_HDR = "hdr";
    public static final String FIELD_T0C1 = "t0c1";
    public static final String FIELD_T1C1 = "t1c1";
    public static final String FIELD_T2C1 = "t2c1";
    public static final String FIELD_T4C1 = "t4c1";
    public static final String FIELD_T8C1 = "t8c1";
    public static final String FIELD_T1C0 = "t1c0";
    public static final String FIELD_T1C2 = "t1c2";
    public static final String FIELD_T1C4 = "t1c4";
    public static final String FIELD_T1C8 = "t1c8";
    public static final String FIELD_ANCHOR = "anchor";
}
