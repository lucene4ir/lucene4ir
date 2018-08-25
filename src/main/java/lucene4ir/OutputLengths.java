package lucene4ir;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.file.Paths;

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
        for (int i = 0; i < n; i++) {
            Document doc = reader.document(i);
            if (doc != null) {
                String docno = doc.get(Lucene4IRConstants.FIELD_DOCNUM);
                String all = doc.get(Lucene4IRConstants.FIELD_ALL);
                String title = doc.get(Lucene4IRConstants.FIELD_TITLE);
                System.out.println(i + " " + docno + " " + all.length() + " " + title.length() );
                long tot=all.length();
                bw.write(docno + " " + tot + " " + title.length());
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
