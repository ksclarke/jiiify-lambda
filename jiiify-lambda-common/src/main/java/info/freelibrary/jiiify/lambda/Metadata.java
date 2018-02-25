/**
 *
 */

package info.freelibrary.jiiify.lambda;

/**
 * Metadata properties used by the function. None of these are required. If the id isn't supplied, the tiler function
 * will use the S3 object key (without a file extension). If the width or height are not supplied, the object will be
 * retrieved from S3 and those values calculated.
 */
public final class Metadata {

    public static final String ID = "id";

    public static final String WIDTH = "width";

    public static final String HEIGHT = "height";

    public static final String PATH = "path";

    public static final String BUCKET = "bucket";

    public static final String KEY = "key";

    /**
     * Private constructor.
     */
    private Metadata() {
    }

}
