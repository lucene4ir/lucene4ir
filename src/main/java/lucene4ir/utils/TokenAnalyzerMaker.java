package lucene4ir.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;

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
 * Created by leif on 06/12/2016.
 */
public class TokenAnalyzerMaker {

    public TokenAnalyzerMaker(){

    }

    public Analyzer createAnalyzer( String tokenFilterFile) {
        Analyzer analyzer = null;
        try {
            lucene4ir.utils.TokenFilters tokenFilters = JAXB.unmarshal(new File(tokenFilterFile), lucene4ir.utils.TokenFilters.class);
            CustomAnalyzer.Builder builder;
            if (tokenFilters.getResourceDir() != null) {
                builder = CustomAnalyzer.builder(Paths.get(tokenFilters.getResourceDir()));
            } else {
                builder = CustomAnalyzer.builder();
            }

            builder.withTokenizer(tokenFilters.getTokenizer());
            for (lucene4ir.utils.TokenFilter filter : tokenFilters.getTokenFilters()) {
                System.out.println("Token filter: " + filter.getName());
                List<lucene4ir.utils.Param> params = filter.getParams();
                if (params.size() > 0) {
                    Map<String, String> paramMap = new HashMap<>();
                    for (lucene4ir.utils.Param param : params) {
                        paramMap.put(param.getKey(), param.getValue());
                    }
                    builder.addTokenFilter(filter.getName(), paramMap);
                } else {
                    builder.addTokenFilter(filter.getName());
                }
            }
            analyzer = builder.build();


        }
        catch (IOException ioe){
            System.out.println(" caught a " + ioe.getClass() +
                    "\n with message: " + ioe.getMessage());
        }

        return analyzer;

    }

}


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tokenFilters")
class TokenFilters {
    private String tokenizer;

    private String resourceDir;

    @XmlElement(name = "tokenFilter", type = lucene4ir.utils.TokenFilter.class)
    private List<lucene4ir.utils.TokenFilter> tokenFilters = new ArrayList<>();

    public TokenFilters(){}

    public TokenFilters(String tokenizer, List<lucene4ir.utils.TokenFilter> tokenFilters) {
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

    public List<lucene4ir.utils.TokenFilter> getTokenFilters() {
        return tokenFilters;
    }

    public void setTokenFilters(List<lucene4ir.utils.TokenFilter> tokenFilters) {
        this.tokenFilters = tokenFilters;
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tokenFilter")
class TokenFilter {
    private String name;

    @XmlElement(name = "param", type = lucene4ir.utils.Param.class)
    private List<lucene4ir.utils.Param> params = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<lucene4ir.utils.Param> getParams() {
        return params;
    }

    public void setParams(List<lucene4ir.utils.Param> params) {
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
