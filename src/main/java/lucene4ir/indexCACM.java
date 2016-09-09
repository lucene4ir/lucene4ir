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
     * @param args 1) Index path, 2) File path
     */
    public static void main(String[] args) {

        CACMDocumentIndexer indexer = new CACMDocumentIndexer(args[0]);
        indexer.indexDocumentsFromFile(args[1]);
    }
}
