
package info.freelibrary.jiiify.lambda;

/**
 * Environment variables used by the function.
 */
public final class Environment {

    public static final String DERIVATIVE_BATCH_SIZE = "DERIVATIVE_BATCH_SIZE";

    public static final String IMAGE_CONTENT_TYPE = "IMAGE_CONTENT_TYPE";

    public static final String IIIF_SERVER_PREFIX = "IIIF_SERVER_PREFIX";

    public static final String IIIF_SERVER_TILESIZE = "IIIF_SERVER_TILESIZE";

    public static final String DESTINATION_BUCKET = "DESTINATION_BUCKET";

    public static final String IIIF_TILER_FUNCTION = "IIIF_TILER_FUNCTION";

    public static final String IIIF_STORER_FUNCTION = "IIIF_STORER_FUNCTION";

    public static final String DEFAULT_TILER_FUNCTION = "JiiifyTiler";

    public static final String DEFAULT_STORER_FUNCTION = "JiiifyStorer";

    public static final int DEFAULT_BATCH_SIZE = 1;

    public static final int DEFAULT_TILE_SIZE = 1024;

    /**
     * Private constructor.
     */
    private Environment() {
    }

}
