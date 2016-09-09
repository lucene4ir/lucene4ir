package lucene4ir;

import lucene4ir.indexer.CACMDocumentIndexer;

/**
 * Created by Aigars on 09.09.2016.
 */

/**
 * Runnable class for indexing CACM documents
 */
public class indexCACM {

    /**
     * @param args Index path as the sole parameter
     */
    public static void main(String[] args) {

        CACMDocumentIndexer indexer = new CACMDocumentIndexer("cacm_index");
        indexer.indexDocumentsFromFile(args[0]);
    }
}
