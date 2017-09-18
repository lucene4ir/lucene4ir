package lucene4ir.utils;

import java.util.HashMap;

/**
 * Created by Harry Scells on 18/9/17.
 * Let's go full jank.
 */
public class DocMap {

    private static DocMap instance = null;
    private HashMap<String, Integer> map;

    private DocMap() {
        map = new HashMap<>();
    }

    public static DocMap getInstance() {
        if (instance == null) {
            instance = new DocMap();
        }
        return instance;
    }

    public void add(String docNum, int docId) {
        map.put(docNum, docId);
    }

    public Integer get(String docId) {
        return map.get(docId);
    }

}
