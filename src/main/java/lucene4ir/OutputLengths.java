package lucene4ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.file.Paths;

import static net.sf.extjwnl.dictionary.morph.Util.split;

/**
 * Created by colin on 31/08/2017.
 */
public class OutputLengths {

    public String indexName;
    public String outputFile;
    public IndexReader reader;

    public OutputLengths() {
        System.out.println("OutputLengths");

        indexName = "";
        reader = null;
        outputFile = "";
    }

    public void openReader() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void saveDocid() throws IOException {
        int n = reader.maxDoc();

        File fout = new File(outputFile);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
//        n=1; // COMMENT OUT FOR PRODUCTION!!!
        for (int i = 0; i < n; i++) {
            Document doc = reader.document(i);
            if (doc != null) {
                Terms allterms = reader.getTermVector(i, Lucene4IRConstants.FIELD_ALL);
                Terms conterms = reader.getTermVector(i, Lucene4IRConstants.FIELD_CONTENT);
                Terms titterms = reader.getTermVector(i, Lucene4IRConstants.FIELD_TITLE);

                String docno = doc.get(Lucene4IRConstants.FIELD_DOCNUM);
                String all = doc.get(Lucene4IRConstants.FIELD_ALL);
                String content = doc.get(Lucene4IRConstants.FIELD_CONTENT);
                String title = doc.get(Lucene4IRConstants.FIELD_TITLE);

//                int ac = all.length();
                Integer ac = (Integer) all.length() != null ? all.length() : 0;
                Integer aw = ((Integer) all.split(" ").length != null) ? all.split(" ").length : 0;
                Long at = ((Long) allterms.size()) != null ? allterms.size() : 0;

                Integer cc = (Integer) content.length() != null ? content.length() : 0;
                Integer cw = ((Integer) content.split(" ").length != null) ? content.split(" ").length : 0;
                Long ct = ((Long) conterms.size()) != null ? conterms.size() : 0;

//                Integer tc = (Integer) title.length() != null ? title.length() : 0;
//                Integer tw = ((Integer) title.split(" ").length != null) ? title.split(" ").length : 0;
//                Long tt = ((Long) titterms.size()) != null ? titterms.size() : 0;

                int tc = title.length();
                tc = (Integer)  tc == null ? 0 : tc;

                int tw = title.split(" ").length;
                tw = (Integer)  tc == null ? 0 : tc;

                long tt = titterms.size();
                tt = (Long) tt == null ? 0 : tc;


                String output=docno + " " + ac + " " + aw + " " + at + " " + cc + " " + cw + " " + ct + " " + tc + " " + tw + " " + tt;
//                System.out.println(i + " " + output);

                bw.write(output);
                bw.newLine();
            }
        }
    }

    public void readExampleStatsParamsFromFile(String indexParamFile) {
        try {
            OutputLengthsParams p = JAXB.unmarshal(new File(indexParamFile), OutputLengthsParams.class);
            indexName = p.indexName;
            outputFile = p.outputFile;
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        String statsParamFile = "";

        try {
            statsParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        OutputLengths outputLengthsApp = new OutputLengths();

        outputLengthsApp.readExampleStatsParamsFromFile(statsParamFile);
        outputLengthsApp.openReader();
        outputLengthsApp.saveDocid();

    }
}

class OutputLengthsParams {
    public String indexName;
    public String outputFile;
}
