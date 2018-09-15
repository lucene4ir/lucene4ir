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
                Integer ac = ((ac = all.length()) != null) ? ac : 0;
                Integer aw = ((aw = all.split(" ").length) != null) ? aw : 0;
                Long at = ((at = allterms.size()) != null) ? at : 0;

                Integer cc = ((cc = all.length()) != null) ? cc : 0;
                Integer cw = ((cw = all.split(" ").length) != null) ? cw : 0;
                Long ct = ((ct = allterms.size()) != null) ? ct : 0;

                Integer tc = ((tc = all.length()) != null) ? tc : 0;
                Integer tw = ((tw = all.split(" ").length) != null) ? tw : 0;
                Long tt = ((tt = allterms.size()) != null) ? tt : 0;

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
