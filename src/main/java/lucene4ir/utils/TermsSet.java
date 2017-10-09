package lucene4ir.utils;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Harry Scells on 26/9/17.
 */
public class TermsSet implements Iterable<String> {

    private static TermsSet instance;
    private static Set<String> terms;

    private TermsSet(IndexReader ir) {
        terms = getTerms(ir);
    }

    private Set<String> getTerms(IndexReader ir) {
        Set<String> t = new HashSet<>();
        for (int i = 0; i < ir.leaves().size(); i++) {
            Terms termsList;
            try {
                // Get all the terms at this level of the tree.
                termsList = ir.leaves().get(i).reader().terms(Lucene4IRConstants.FIELD_ALL);
                if (termsList != null && termsList.size() > 0) {
                    TermsEnum te = termsList.iterator();
                    BytesRef termBytes;
                    while ((termBytes = te.next()) != null) {
                        t.add(termBytes.utf8ToString());
                    }
                }

                // Get all the terms at the next level of the tree.
                if (ir.leaves().get(i).children() != null && ir.leaves().get(i).children().size() > 0) {
                    for (IndexReaderContext c : ir.leaves().get(i).children()) {
                        t.addAll(getTerms(c.reader()));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return t;
    }

    public static TermsSet getInstance(IndexReader ir) {
        if (instance == null) {
            instance = new TermsSet(ir);
        }
        return instance;
    }


    public int size() {
        return terms.size();
    }

    @Override
    public Iterator<String> iterator() {
        return terms.iterator();
    }
}
