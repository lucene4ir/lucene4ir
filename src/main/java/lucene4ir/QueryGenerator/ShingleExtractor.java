package lucene4ir.QueryGenerator;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ShingleExtractor {
    public HashMap<Integer,Integer> shingleMap ;

    public boolean extractShingles (String paramFile)
    {
        boolean result = true , foundOne = false;
        int gramSize , cutoff;
        RetrievabilityCalculatorParams rc =  JAXB.unmarshal(new File(paramFile), RetrievabilityCalculatorParams.class);

        shingleMap = new HashMap<Integer,Integer>();
       for (Param p : rc.shParams.getParams())
       {
           gramSize = Integer.parseInt( p.getGramSize());
           cutoff = Integer.parseInt(p.getCutoff());
           if (gramSize < 1)
           {
               System.out.println("Error : Shingle Size < 1");
               result = false;
               break;
           }
           else if (gramSize == 1)
               foundOne = true;
           shingleMap.put(gramSize,cutoff);
       }

       if (!foundOne)
       {
           System.out.println("Error : Shingle Size 1 is missing");
           result = false;
       }
        return result;
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class RetrievabilityCalculatorParams
        {
            @XmlElement(name = "shingleParams", type = shingleParams.class)
            public shingleParams shParams;
        }

@XmlRootElement (name = "shingleParams")
@XmlAccessorType(XmlAccessType.FIELD)
class shingleParams {
    @XmlElement(name = "param", type = Param.class)
    private ArrayList<Param> params = new ArrayList<>();
    public ArrayList<Param> getParams() {
        return params;
    }
   /* public void setParams(ArrayList<Param> params) {
        this.params = params;
    }*/
}

@XmlRootElement(name = "param")
@XmlAccessorType(XmlAccessType.FIELD)
class Param {
    private String gramSize;
    private String cutoff;
    public String getGramSize() {
        return gramSize;
    }
  /*  public void setShingle(String shingles) {
        this.shingles = shingles;
    }*/
    public String getCutoff() {
        return cutoff;
    }
   /* public void setCutoff(String cutoff) {
        this.cutoff = cutoff;
    }*/
}
