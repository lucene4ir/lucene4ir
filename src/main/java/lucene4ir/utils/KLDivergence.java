package lucene4ir.utils;

/**
 * Created by Harry Scells on 18/9/17.
 */
public final class KLDivergence {

    private static final double log2 = Math.log(2);

    public static double calculate(double[] p1, double[] p2) {
        double klDiv = 0.0;

        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0.0) {
                continue;
            }
            if (p2[i] == 0.0) {
                continue;
            }

            klDiv += p1[i] * Math.log(p1[i] / p2[i]);
        }

        return klDiv;
    }

}
