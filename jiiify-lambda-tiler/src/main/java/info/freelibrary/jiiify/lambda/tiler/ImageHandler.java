
package info.freelibrary.jiiify.lambda.tiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.iiif.image.Image;
import info.freelibrary.iiif.image.ImageFactory;
import info.freelibrary.iiif.image.api.ImageException;
import info.freelibrary.iiif.image.api.Request;
import info.freelibrary.jiiify.lambda.Environment;
import info.freelibrary.jiiify.lambda.StorageRequest;
import info.freelibrary.jiiify.lambda.TilingRequest;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

/**
 * A handler that processes images uploaded to an S3 bucket into IIIF tiles and an info.json file.
 */
public class ImageHandler implements RequestHandler<TilingRequest, StorageRequest[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHandler.class, Constants.RESOURCE_BUNDLE);

    private final AWSLambdaAsync myLambdaClient = AWSLambdaAsyncClientBuilder.defaultClient();

    private AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

    private final ObjectMapper mySerializer = new ObjectMapper();

    /**
     * Creates an image handler function.
     */
    public ImageHandler() {
    }

    /**
     * A constructor used for testing purposes.
     *
     * @param aS3Client
     */
    ImageHandler(final AmazonS3 aS3Client) {
        myS3Client = aS3Client;
    }

    @Override
    public StorageRequest[] handleRequest(final TilingRequest aTilingRequest, final Context aContext) {
        final List<String> tiles = aTilingRequest.getTiles();
        final String bucket = aTilingRequest.getBucket();
        final String key = aTilingRequest.getKey();
        final S3Object s3Object = myS3Client.getObject(new GetObjectRequest(bucket, key));
        final S3ObjectInputStream s3ObjStream = s3Object.getObjectContent();
        final List<StorageRequest> storageRequests = new ArrayList<StorageRequest>(tiles.size());
        final String storageFunction;

        if (StringUtils.trimToNull(System.getenv(Environment.IIIF_STORER_FUNCTION)) == null) {
            storageFunction = Environment.DEFAULT_STORER_FUNCTION;
        } else {
            storageFunction = System.getenv(Environment.IIIF_STORER_FUNCTION);
        }

        try {
            final Image image = ImageFactory.getImage(IOUtils.readBytes(s3ObjStream), true);

            tiles.forEach(tileRequest -> {
                try {
                    final InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(storageFunction);
                    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    final Request iiifRequest = Request.parse(tileRequest);
                    final StorageRequest storageRequest;
                    final byte[] payload;

                    image.transform(iiifRequest).write(iiifRequest.getFormat(), outStream).revert();
                    storageRequest = new StorageRequest(tileRequest, outStream.toByteArray());
                    payload = mySerializer.writeValueAsBytes(storageRequest);
                    storageRequests.add(storageRequest.clearBytes());

                    LOGGER.debug(MessageCodes.JLT_002, tileRequest);
                    myLambdaClient.invokeAsync(invokeRequest.withPayload(ByteBuffer.wrap(payload)));
                } catch (final ImageException | IOException details) {
                    throw new TilerRuntimeException(details);
                }
            });
        } catch (final IOException details) {
            throw new TilerRuntimeException(details);
        }

        return storageRequests.toArray(new StorageRequest[storageRequests.size()]);
    }
}
