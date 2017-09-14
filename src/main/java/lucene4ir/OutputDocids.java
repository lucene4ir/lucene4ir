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
public class OutputDocids {

    public String indexName;
    public String outputFile;
    public IndexReader reader;

    public OutputDocids() {
        System.out.println("OutputDocids");

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
                String docno = doc.get("docnum");
                bw.write(docno);
                bw.newLine();
            }
        }
    }

    public void readExampleStatsParamsFromFile(String indexParamFile) {
        try {
            OutputDocidsParams p = JAXB.unmarshal(new File(indexParamFile), OutputDocidsParams.class);
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

        OutputDocids outputDocidsApp = new OutputDocids();

        outputDocidsApp.readExampleStatsParamsFromFile(statsParamFile);
        outputDocidsApp.openReader();
        outputDocidsApp.saveDocid();

    }
}

class OutputDocidsParams {
    public String indexName;
    public String outputFile;
}
