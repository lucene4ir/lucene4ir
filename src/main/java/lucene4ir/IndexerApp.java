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

    public IndexParams p;

    public DocumentIndexer di;


    private enum DocumentModel {
        CACM, CLUEWEB, TRECNEWS, TRECAQUAINT, TRECTIPSTER
    }

    private DocumentModel docModel;


    public IndexerApp(){
        System.out.println("Indexer");
    }

    private void setDocParser(String val){
        try {
            docModel = DocumentModel.valueOf(p.indexType.toUpperCase());
        } catch (Exception e){
            System.out.println("Document Parser Not Recognized - Setting to Default");
            System.out.println("Possible Document Parsers are:");
            for(DocumentModel value: DocumentModel.values()){
                System.out.println("<indexType>"+value.name()+"</indexType>");
            }
            docModel = DocumentModel.CACM;

        }
    }



    public void selectDocumentParser(DocumentModel dm){
        docModel = dm;
        di = null;
        switch(dm){
            case CACM:
                System.out.println("CACM Document Parser");
                di = new CACMDocumentIndexer(p.indexName, p.tokenFilterFile);
                break;

            case CLUEWEB:
                System.out.println("CLUEWEB Document Parser");
                // TBA

                // di = new CLUEWEBDocumentIndexer(p.indexName);
                break;

            case TRECNEWS:
                System.out.println("TRECNEWS");
                di = new TRECNEWSDocumentIndexer(p.indexName, p.tokenFilterFile);
                break;

            case TRECTIPSTER:
                System.out.println("TRECTIPSTER");
                di = new TRECTipsterDocumentIndexer(p.indexName, p.tokenFilterFile);
                break;

            case TRECAQUAINT:
                System.out.println("TRECAQUAINT");
                di = new TRECAquaintDocumentIndexer(p.indexName, p.tokenFilterFile);
                break;

            default:
                System.out.println("Default Document Parser");

                break;
        }
    }




    public ArrayList<String> readFileListFromFile(){
        /*
            Takes the name of a file (filename), which contains a list of files.
            Returns an array of the filenames (to be indexed)
         */

        String filename = p.fileList;

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
            p = JAXB.unmarshal(new File(indexParamFile), IndexParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Path to index: " + p.indexName);
        System.out.println("File List: " + p.fileList);
        System.out.println("Index Type: " + p.indexType);

    }

    public IndexerApp(String indexParamFile){
        System.out.println("Indexer App");
        readIndexParamsFromFile(indexParamFile);
        setDocParser(p.indexType);
        selectDocumentParser(docModel);
    }

    public void indexDocumentsFromFile(String filename){
        di.indexDocumentsFromFile(filename);
    }

    public void finished(){
        di.finished();
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

        IndexerApp indexer = new IndexerApp(indexParamFile);

        try {
            ArrayList<String> files = indexer.readFileListFromFile();
            for (String f : files) {
                System.out.println("About to Index Files in: " +  f);
                indexer.indexDocumentsFromFile(f);
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        indexer.finished();
        System.out.println("Done building Index");
    }

}


class IndexParams {
    public String indexName;
    public String fileList;
    public String indexType; /** trecWeb, trecNews, trec678, cacm **/
    public Boolean compressed;
    public String tokenFilterFile;
    public Boolean recordPositions;

}

