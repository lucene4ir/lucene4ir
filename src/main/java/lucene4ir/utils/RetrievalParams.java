package lucene4ir.utils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RetrievalParams {
    public String indexName;
    public String queryFile;
    public String resultFile;
    public int maxResults;
    public String runTag;
    public String tokenFilterFile;

    // the <model> element
    @XmlElement(name = "model")
    public Model model;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Model {

        @XmlAttribute
        public String className;

        @XmlAttribute(name = "params")
        @XmlList
        public List<Float> params = new ArrayList<>();
    }

}
