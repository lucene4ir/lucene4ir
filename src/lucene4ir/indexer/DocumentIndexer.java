package lucene4ir.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by leif on 21/08/2016.
 */
public class DocumentIndexer {

    public IndexWriter writer;

    public DocumentIndexer(){};

    public DocumentIndexer(String indexPath){
        writer = null;
        createWriter(indexPath);
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


    public void createWriter(String indexPath){
        /*
        The indexPath specifies where to create the index
         */

        // I am can imagine that there are lots of ways to create indexers -
        // We could add in some parameters to customize its creation

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Analyzer analyzer = new StandardAnalyzer();
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
