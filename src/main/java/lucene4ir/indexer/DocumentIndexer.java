package lucene4ir.indexer;

import lucene4ir.LuceneConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leif on 21/08/2016.
 */
public class DocumentIndexer {

    public IndexWriter writer;

    public DocumentIndexer(){};

    public DocumentIndexer(String indexPath, String tokenFilterFile){
        writer = null;
        createWriter(indexPath, tokenFilterFile);
    }

    protected void finalize(){
        try {
            if (writer != null){
                writer.close();
            }
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    public void createWriter(String indexPath, String tokenFilterFile){
        /*
        The indexPath specifies where to create the index
         */

        // I am can imagine that there are lots of ways to create indexers -
        // We could add in some parameters to customize its creation

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            System.out.println("Indexing to directory '" + indexPath + "'...");
            TokenFilters tokenFilters = JAXB.unmarshal(new File(tokenFilterFile), TokenFilters.class);

            CustomAnalyzer.Builder builder;
            if (tokenFilters.getResourceDir() != null) {
                builder = CustomAnalyzer.builder(Paths.get(tokenFilters.getResourceDir()));
            }
            else {
                builder = CustomAnalyzer.builder();
            }

            builder.withTokenizer(tokenFilters.getTokenizer());
            for(TokenFilter filter : tokenFilters.getTokenFilters())  {
                System.out.println("Token filter: " + filter.getName());
                List<Param> params = filter.getParams();
                if (params.size() > 0) {
                    Map<String, String> paramMap = new HashMap<>();
                    for (Param param : params) {
                        paramMap.put(param.getKey(), param.getValue());
                    }
                    builder.addTokenFilter(filter.getName(), paramMap);
                } else {
                    builder.addTokenFilter(filter.getName());
                }
            }
            Analyzer analyzer = builder.build();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(dir, iwc);

        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void addDocumentToIndex(Document doc){
        try {
            writer.addDocument(doc);
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void indexDocumentsFromFile(String filename){
        /* to be implemented in sub classess*/
    };

    public void finished(){
        try {
            if (writer != null){
                writer.close();
            }
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tokenFilters")
class TokenFilters {
    private String tokenizer;

    private String resourceDir;

    @XmlElement(name = "tokenFilter", type = TokenFilter.class)
    private List<TokenFilter> tokenFilters = new ArrayList<>();

    public TokenFilters(){}

    public TokenFilters(String tokenizer, List<TokenFilter> tokenFilters) {
        this.tokenizer = tokenizer;
        this.tokenFilters = tokenFilters;
    }

    public String getTokenizer() {
        return tokenizer;
    }

    public void setTokenizer(String tokenizer) {
        this.tokenizer = tokenizer;
    }

    public String getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(String resourceDir) {
        this.resourceDir = resourceDir;
    }

    public List<TokenFilter> getTokenFilters() {
        return tokenFilters;
    }

    public void setTokenFilters(List<TokenFilter> tokenFilters) {
        this.tokenFilters = tokenFilters;
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tokenFilter")
class TokenFilter {
    private String name;

    @XmlElement(name = "param", type = Param.class)
    private List<Param> params = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "param")
class Param {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
