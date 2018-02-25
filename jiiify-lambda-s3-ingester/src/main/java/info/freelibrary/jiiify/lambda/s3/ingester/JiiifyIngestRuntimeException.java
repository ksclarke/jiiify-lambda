
package info.freelibrary.jiiify.lambda.s3.ingester;

import info.freelibrary.util.I18nRuntimeException;

/**
 * A runtime exception encountered by the tiler.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class JiiifyIngestRuntimeException extends I18nRuntimeException {

    /**
     * The <code>serialVersionUID</code> for the exception.
     */
    private static final long serialVersionUID = 671822685255593439L;

    /**
     * Creates a tiler runtime exception from the supplied message.
     *
     * @param aMessage A details message
     */
    public JiiifyIngestRuntimeException(final String aMessage) {
        super(Constants.RESOURCE_BUNDLE, aMessage);
    }

    /**
     * Creates a tiler runtime exception from the supplied message.
     *
     * @param aMessage A details message
     * @param aDetailsArray Additional details to be inserted into the message
     */
    public JiiifyIngestRuntimeException(final String aMessage, final Object... aDetailsArray) {
        super(Constants.RESOURCE_BUNDLE, aMessage);
    }

    /**
     * Creates a tiler runtime exception from the supplied throwable.
     *
     * @param aThrowable A throwable that caused this tiler runtime exception
     */
    public JiiifyIngestRuntimeException(final Throwable aThrowable) {
        super(aThrowable);
    }

    /**
     * Creates a tiler runtime exception from the supplied details message and throwable.
     *
     * @param aMessage A details message
     * @param aThrowable A throwable that caused this tiler runtime exception
     */
    public JiiifyIngestRuntimeException(final Throwable aThrowable, final String aMessage) {
        super(aThrowable, Constants.RESOURCE_BUNDLE, aMessage);
    }

    /**
     * Creates a tiler runtime exception from the supplied details message and throwable.
     *
     * @param aMessage A details message
     * @param aThrowable A throwable that caused this tiler runtime exception
     * @param aDetailsArray Additional details to be inserted into the message
     */
    public JiiifyIngestRuntimeException(final Throwable aThrowable, final String aMessage,
            final Object... aDetailsArray) {
        super(aThrowable, Constants.RESOURCE_BUNDLE, aMessage, aDetailsArray);
    }

}
