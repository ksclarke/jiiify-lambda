
package info.freelibrary.jiiify.lambda;

import java.util.Objects;

/**
 * A request for a IIIF image to be stored.
 */
public class StorageRequest {

    private String myPath;

    private byte[] myBytes;

    private int mySize;

    /**
     * Constructs a store request event.
     *
     * @param aPath The path for the object to be stored
     * @param aByteArray The byte array of the object to be stored
     */
    public StorageRequest(final String aPath, final byte[] aByteArray) {
        Objects.requireNonNull(aPath, "");
        Objects.requireNonNull(aByteArray, "");

        myBytes = aByteArray;
        myPath = aPath;
        mySize = aByteArray.length;
    }

    /**
     * Creates a storage request for testing.
     *
     * @param aPath The path for the object to be stored
     * @param aSize The size of a byte array
     */
    StorageRequest(final String aPath, final int aSize) {
        Objects.requireNonNull(aPath, "");

        myPath = aPath;
        myBytes = null;
        mySize = aSize;
    }

    /**
     * Private constructor for JSON deserialization.
     */
    private StorageRequest() {
        // empty
    }

    /**
     * Returns the bytes of the object to be stored.
     *
     * @return The bytes of the object to be stored
     */
    public byte[] getBytes() {
        return myBytes;
    }

    /**
     * Sets the bytes to be stored.
     *
     * @param aByteArray The bytes to be stored
     */
    public void setBytes(final byte[] aByteArray) {
        myBytes = aByteArray;
    }

    /**
     * Returns the path at which the object should be stored.
     *
     * @return The path at which the object should be stored
     */
    public String getPath() {
        return myPath;
    }

    /**
     * Sets the path for the storage request.
     *
     * @param aPath The path for the storage request
     */
    public void setPath(final String aPath) {
        myPath = aPath;
    }

    /**
     * Returns the size of the byte array being sent to storage.
     *
     * @return The size of the byte array being sent to storage
     */
    public int getSize() {
        return mySize;
    }

    /**
     * Sets the size of the bytes to be stored.
     *
     * @param aSize The size of the bytes to be stored
     */
    public void setSize(final int aSize) {
        mySize = aSize;
    }

    /**
     * Returns a storage request with the actual bytes zeroed out. This can be used for informational purposes.
     *
     * @return A storage request with the actual bytes zeroed out.
     */
    public StorageRequest clearBytes() {
        return new StorageRequest(myPath, mySize);
    }

}
