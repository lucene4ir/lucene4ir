package lucene4ir;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import lucene4ir.indexer.*;

/**
 *
 * Created by leif on 21/08/2016.
 */

public class IndexerApp {

    public String fileList="";
    public String indexName="";
    public String indexType="";

    public IndexerApp(){
        System.out.println("Indexer");
    }


    public static ArrayList<String> readFileListFromFile(String filename){
        /*
            Takes the name of a file (filename), which contains a list of files.
            Returns an array of the filenames (to be indexed)
         */

        ArrayList<String> files = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {
                String line = br.readLine();
                while (line != null){
                    files.add(line);
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        return files;
    }

    public void readIndexParamsFromFile(String indexParamFile){
        try {
            IndexParams p = JAXB.unmarshal(new File(indexParamFile), IndexParams.class);
            indexName = p.indexName;
            fileList = p.fileList;
            indexType = p.indexType;
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }


    }



    public static void main(String []args) {


        String indexParamFile = "";

        try {
            indexParamFile = args[0];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        IndexerApp indexer = new IndexerApp();

        indexer.readIndexParamsFromFile(indexParamFile);

        System.out.println("Path to index: " + indexer.indexName);
        System.out.println("Files to index contained in;" + indexer.fileList);
        System.out.println("File format: " + indexer.indexType);


        // At the moment the IndexerApp only indexs CACM documents..
        // Need to add in some conditions to control how the index is created
        // And to select the document format / type


        CACMDocumentIndexer cdi = new CACMDocumentIndexer(indexer.indexName);
        try {
            ArrayList<String> files = indexer.readFileListFromFile(indexer.fileList);
            for (String f : files) {
                System.out.println("About to Index Files in: " +  f);
                cdi.indexDocumentsFromFile(f);
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        cdi.finished();
        System.out.println("Done building Index");
    }

}


class IndexParams {
    public String indexName;
    public String fileList;
    public String indexType; /** trecWeb, trecNews, trec678, cacm **/
    public Boolean compressed;
}

