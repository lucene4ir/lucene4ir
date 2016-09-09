package lucene4ir.utils;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import java.util.*;

/**
 * Created by Pablo Arteaga on 08/09/2016.
 */
public class SynonymProvider {
    private static Dictionary dictionary;
    private static JWNLException exc;

    public static void main(String[] args) throws JWNLException {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            System.out.println(getSynonyms(scanner.next()));
        }
    }

    static {
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            dictionary = null;
            exc = e;
        }
    }

    public static Set<String> getSynonyms(String word) throws JWNLException {
        IndexWord[] allWords = getWordArray(word);
        if (allWords.length == 1) {
            Set<String> retData = new HashSet<>();
            for (Synset synset : allWords[0].getSenses()) {
                for (Word wordObj : synset.getWords()) {
                    retData.add(wordObj.getLemma());
                }
            }
            return retData;
        }
        return null;
    }

    private static IndexWord[] getWordArray(String word) throws JWNLException {
        if (dictionary == null) {
            throw exc;
        }
        return dictionary.lookupAllIndexWords(word).getIndexWordArray();
    }

    // Not working yet!
    public static Set<String> getHypernym(String word) throws JWNLException {
        IndexWord[] allWords = getWordArray(word);
        if (allWords.length == 1) {
            Set<String> retData = new HashSet<>();
        }
        return null;
    }
}
