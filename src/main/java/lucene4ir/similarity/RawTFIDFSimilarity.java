package lucene4ir.similarity;

/**
 * Created by colin on 28/07/2018.
 * Another version fo ClassicSimilarity (i.e TF.IDF)
 * This version bins the length normalisation feature
 * to provide a Raw version of TFIDF for the boys.
 */

    import org.apache.lucene.index.FieldInvertState;
    import org.apache.lucene.search.similarities.TFIDFSimilarity;
    import org.apache.lucene.util.BytesRef;
    import org.apache.lucene.util.SmallFloat;

public class RawTFIDFSimilarity extends TFIDFSimilarity {

    /** Cache of decoded bytes. */
    private static final float[] NORM_TABLE = new float[256];

    static {
        for (int i = 0; i < 256; i++) {
            NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
        }
    }

    /** Sole constructor: parameter-free */
    public RawTFIDFSimilarity() {}

    /** Implemented as <code>overlap / maxOverlap</code>. */
    @Override
    public float coord(int overlap, int maxOverlap) {
        return overlap / (float)maxOverlap;
    }

    /** Implemented as <code>1/sqrt(sumOfSquaredWeights)</code>. */
    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
    }

    /**
     * Encodes a normalization factor for storage in an index.
     * <p>
     * The encoding uses a three-bit mantissa, a five-bit exponent, and the
     * zero-exponent point at 15, thus representing values from around 7x10^9 to
     * 2x10^-9 with about one significant decimal digit of accuracy. Zero is also
     * represented. Negative numbers are rounded up to zero. Values too large to
     * represent are rounded down to the largest representable value. Positive
     * values too small to represent are rounded up to the smallest positive
     * representable value.
     *
     * @see org.apache.lucene.document.Field#setBoost(float)
     * @see org.apache.lucene.util.SmallFloat
     */
    @Override
    public final long encodeNormValue(float f) {
        return SmallFloat.floatToByte315(f);
    }

    /**
     * Decodes the norm value, assuming it is a single byte.
     *
     * @see #encodeNormValue(float)
     */
    @Override
    public final float decodeNormValue(long norm) {
        return NORM_TABLE[(int) (norm & 0xFF)];  // & 0xFF maps negative bytes to positive above 127
    }

    /** Implemented as
     *  <code>state.getBoost()*lengthNorm(numTerms)</code>, where
     *  <code>numTerms</code> is {@link FieldInvertState#getLength()} if {@link
     *  #setDiscountOverlaps} is false, else it's {@link
     *  FieldInvertState#getLength()} - {@link
     *  FieldInvertState#getNumOverlap()}.
     *
     *  @lucene.experimental */
    @Override
    public float lengthNorm(FieldInvertState state) {
        return 1.0f;
    }
    /** Implemented as <code>sqrt(freq)</code>. */
    @Override
    public float tf(float freq) {
        return freq;
    }

    /** Implemented as <code>1 / (distance + 1)</code>. */
    @Override
    public float sloppyFreq(int distance) {
        return 1.0f / (distance + 1);
    }

    /** The default implementation returns <code>1</code> */
    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1;
    }

    /** Implemented as <code>log((docCount+1)/(docFreq+1)) + 1</code>. */
    @Override
    public float idf(long docFreq, long docCount) {
        return (float)(Math.log((docCount+1)/(double)(docFreq+1)) + 1.0);
    }

    /**
     * True if overlap tokens (tokens with a position of increment of zero) are
     * discounted from the document's length.
     */
    protected boolean discountOverlaps = true;

    /** Determines whether overlap tokens (Tokens with
     *  0 position increment) are ignored when computing
     *  norm.  By default this is true, meaning overlap
     *  tokens do not count when computing norms.
     *
     *  @lucene.experimental
     *
     *  @see #computeNorm
     */
    public void setDiscountOverlaps(boolean v) {
        discountOverlaps = v;
    }

    /**
     * Returns true if overlap tokens are discounted from the document's length.
     * @see #setDiscountOverlaps
     */
    public boolean getDiscountOverlaps() {
        return discountOverlaps;
    }

    @Override
    public String toString() {
        return "RawTFIDFSimilarity";
    }
}
