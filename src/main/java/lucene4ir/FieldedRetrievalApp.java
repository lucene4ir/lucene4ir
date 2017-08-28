package lucene4ir;

import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by colin on 17/07/2017.
 */
public class FieldedRetrievalApp extends RetrievalApp {
    public Fields fl;

    public FieldedRetrievalApp(String retrievalParamFile) {
        super(retrievalParamFile);
        this.readFieldedParamsFromFile(fieldsFile);

        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);

            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);
            parser = new QueryParser("all",analyzer);


        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public ScoreDoc[] runQuery(String qno, String queryTerms){
        System.out.println("Fielded Scoring docs...");
        ScoreDoc[] hits = null;
        String[] fields = new String[fl.fields.size()];
        Map<String, Float> boosts = new HashMap<>();
        int i = 0;
        for (Field f : fl.fields) {
            System.out.println("Field: " + f.fieldName + " Boost: " + f.fieldBoost);
            fields[i] = f.fieldName;
            boosts.put(f.fieldName, f.fieldBoost);
            i++;
        }
        try {
            MultiFieldQueryParser mfq = new MultiFieldQueryParser(fields, analyzer, boosts);
            Query q = mfq.parse(queryTerms);
            System.out.println(queryTerms);
            System.out.println(q.toString());
            try {
                TopDocs results = searcher.search(q, p.maxResults);
                hits = results.scoreDocs;
                System.out.println(results.totalHits);
            } catch (IOException ioe) {
                System.out.println(" caught a " + ioe.getClass() +
                        "\n with message: " + ioe.getMessage());
            }
        } catch (ParseException pe){
            System.out.println("Can't parse query");
        }
        return hits;
    }

    public void readFieldedParamsFromFile(String paramFile){

        try {
            fl = JAXB.unmarshal(new File(paramFile), Fields.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        for (Field field : fl.fields){
            if(field.fieldName.equals(null))
                field.fieldName="all";
            else if(field.fieldBoost == 0.0f)
                field.fieldBoost=1.0f;
        }
        p.resultFile=fl.resultFile;
        System.out.println("Fielded Results File: " + p.resultFile);
    }

    public static void main(String []args) {

        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        FieldedRetrievalApp retriever = new FieldedRetrievalApp(retrievalParamFile);
        retriever.processQueryFile();
    }
}



@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
class Field {
    @XmlElement(name = "fieldName")
    public String fieldName;
    @XmlElement(name = "fieldBoost")
    public float fieldBoost;
}

@XmlRootElement(name = "fields")
class Fields {
    @XmlElement(name = "field")
    public List<Field> fields;
    @XmlElement(name = "retrievalParamsFile")
    public String retrievalParamsFile;
    @XmlElement(name = "resultFile")
    public String resultFile;
}