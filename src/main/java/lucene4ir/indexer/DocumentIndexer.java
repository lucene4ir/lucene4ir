package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * Created by leifos on 21/08/2016.
 * Edited by kojayboy on 16/08/2017.
 * Edited by Leifos 10/9/2017
 * Added extra method openDocumentFile that can handle different input file types
 * i.e. compressed (gz, etc) and creates the appropriate input reader
 * probably should re-factor class to provide a templated method with the BufferredReader to process for each file
 * and not the file itself.
 */
public class DocumentIndexer {

    protected boolean indexPositions;
    public IndexWriter writer;
    public Analyzer analyzer;

    public DocumentIndexer(){};

    public DocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        writer = null;
        analyzer = Lucene4IRConstants.ANALYZER;
        indexPositions=positional;

        if (tokenFilterFile != null){
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(tokenFilterFile);
        }
        createWriter(indexPath);
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

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(dir, iwc);

        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void addDocumentToIndex(Document doc){
        try {
            writer.addDocument(doc);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void indexDocumentsFromFile(String filename){
        /* to be implemented in sub classess*/
    };

    protected BufferedReader openDocumentFile(String filename){
        BufferedReader br = null;
        try {
            if(filename.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(filename);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
                br = new BufferedReader(decoder);
            }
            else
            {
                // For the weirdness that is TREC collections.
                if (filename.endsWith(".Z") || filename.endsWith(".0Z") || filename.endsWith(".1Z") || filename.endsWith(".2Z")) {
                    InputStream fileStream = new FileInputStream(filename);
                    //InputStream zipStream = new ZCompressorInputStream(fileStream);
                    ZCompressorInputStream zipStream = new ZCompressorInputStream(fileStream);
                    Reader decoder = new InputStreamReader(zipStream, "UTF-8");
                    br = new BufferedReader(decoder);
                }
                else
                    br = new BufferedReader(new FileReader(filename));
            }




        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return br;
    }


    public void finished(){
        try {
            if (writer != null){
                writer.close();
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

}
