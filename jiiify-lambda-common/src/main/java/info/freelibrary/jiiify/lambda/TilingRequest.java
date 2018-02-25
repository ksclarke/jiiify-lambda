
package info.freelibrary.jiiify.lambda;

import java.util.ArrayList;
import java.util.List;

/**
 * A request for an image to be tiled.
 */
public class TilingRequest {

    private List<String> myTiles;

    private String myBucket;

    private String myKey;

    /**
     * Constructs a new tile request event.
     */
    public TilingRequest(final String aBucket, final String aKey) {
        myTiles = new ArrayList<>();
        myBucket = aBucket;
        myKey = aKey;
    }

    /**
     * Private constructor for JSON deserialization.
     */
    private TilingRequest() {
        myTiles = new ArrayList<>();
    }

    /**
     * Gets the S3 bucket from which the source image should be pulled.
     *
     * @return S3 bucket name
     */
    public String getBucket() {
        return myBucket;
    }

    /**
     * Sets the bucket value for the tiling request.
     *
     * @param aBucket An S3 bucket
     */
    public void setBucket(final String aBucket) {
        myBucket = aBucket;
    }

    /**
     * Gets the S3 bucket key of the source image.
     *
     * @return The S3 bucket key of the source image
     */
    public String getKey() {
        return myKey;
    }

    /**
     * Sets the key value for the tiling request.
     *
     * @param aKey An S3 bucket key
     */
    public void setKey(final String aKey) {
        myKey = aKey;
    }

    /**
     * Gets a list of the tiles to be created.
     *
     * @return A list of IIIF tiles
     */
    public List<String> getTiles() {
        return myTiles;
    }

    /**
     * Sets the tiles for this tiling request.
     *
     * @param aTileList The tiles for the tiling request
     */
    public void setTiles(final List<String> aTileList) {
        myTiles = aTileList;
    }

    /**
     * Add a tile request to the tile request event.
     *
     * @param aTileRequest A IIIF tile to be created
     * @return The tile request event
     */
    public TilingRequest addTile(final String aTileRequest) {
        if (!myTiles.add(aTileRequest)) {
            throw new IllegalArgumentException();
        }

        return this;
    }

}
