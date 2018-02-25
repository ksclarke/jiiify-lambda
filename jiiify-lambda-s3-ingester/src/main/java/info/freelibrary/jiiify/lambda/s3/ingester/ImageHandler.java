
package info.freelibrary.jiiify.lambda.s3.ingester;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.TooManyRequestsException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.iiif.image.Image;
import info.freelibrary.iiif.image.ImageFactory;
import info.freelibrary.iiif.image.util.Tiler;
import info.freelibrary.jiiify.lambda.Environment;
import info.freelibrary.jiiify.lambda.Metadata;
import info.freelibrary.jiiify.lambda.TilingRequest;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

/**
 * A handler that processes images uploaded to an S3 bucket into IIIF tiles and an info.json file.
 */
public class ImageHandler implements RequestHandler<S3Event, TilingRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHandler.class, Constants.RESOURCE_BUNDLE);

    private AmazonS3 myS3Client = AmazonS3ClientBuilder.standard().build();

    private final AWSLambdaAsync myLambdaClient = AWSLambdaAsyncClientBuilder.defaultClient();

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
    public TilingRequest handleRequest(final S3Event aS3Event, final Context aContext) {
        final String bucket = aS3Event.getRecords().get(0).getS3().getBucket().getName();
        final String key = aS3Event.getRecords().get(0).getS3().getObject().getKey();

        try {
            final S3Object s3Object = myS3Client.getObject(new GetObjectRequest(bucket, key));
            final String contentType = s3Object.getObjectMetadata().getContentType();
            final List<String> paths;

            if (contentType.equals(System.getenv(Environment.IMAGE_CONTENT_TYPE))) {
                final S3ObjectInputStream s3ObjStream = s3Object.getObjectContent();
                final ObjectMetadata s3Metadata = s3Object.getObjectMetadata();
                final String tileSizeValue = System.getenv(Environment.IIIF_SERVER_TILESIZE);
                final String batchSizeValue = System.getenv(Environment.DERIVATIVE_BATCH_SIZE);
                final String tileBucket = System.getenv(Environment.DESTINATION_BUCKET);
                final String widthValue = s3Metadata.getUserMetaDataOf(Metadata.WIDTH);
                final String heightValue = s3Metadata.getUserMetaDataOf(Metadata.HEIGHT);
                final List<String> batch;

                Objects.requireNonNull(tileSizeValue, MessageCodes.JLT_005);
                Objects.requireNonNull(tileBucket, MessageCodes.JLT_006);

                String prefix = System.getenv(Environment.IIIF_SERVER_PREFIX);
                String id = s3Metadata.getUserMetaDataOf(Metadata.ID);
                int batchSize;
                int tileSize;
                int height;
                int width;

                // If we don't have a IIIF server prefix set, use the S3 bucket name
                if (StringUtils.trimToNull(prefix) == null) {
                    prefix = tileBucket;
                }

                // If we don't have a metadata ID set, use the S3 object key minus its file extension
                if (id == null) {
                    id = FileUtils.stripExt(s3Object.getKey());
                }

                // Get tile size
                try {
                    tileSize = Integer.parseInt(tileSizeValue);
                } catch (final NumberFormatException details) {
                    LOGGER.error(details, MessageCodes.JLT_007, tileSizeValue);
                    tileSize = Environment.DEFAULT_TILE_SIZE;
                }

                try {
                    batchSize = Integer.parseInt(batchSizeValue);
                } catch (final NumberFormatException details) {
                    LOGGER.error(details, MessageCodes.JLT_008, batchSizeValue);
                    batchSize = Environment.DEFAULT_BATCH_SIZE;
                }

                // Get width and height values
                if ((widthValue != null) && (heightValue != null)) {
                    try {
                        width = Integer.parseInt(widthValue);
                        height = Integer.parseInt(heightValue);
                    } catch (final NumberFormatException details) {
                        // No width and height metadata to use? We have to do it the hard way...
                        final Image image = ImageFactory.getImage(IOUtils.readBytes(s3ObjStream));

                        width = image.getWidth();
                        height = image.getHeight();
                    }
                } else {
                    // No width and height metadata to use? We have to do it the hard way...
                    final Image image = ImageFactory.getImage(IOUtils.readBytes(s3ObjStream));

                    width = image.getWidth();
                    height = image.getHeight();
                }

                paths = Tiler.getPaths(prefix, id, tileSize, width, height);
                batch = new ArrayList<>(batchSize);

                for (int index = 0; index < paths.size(); index++) {
                    if (batch.size() < batchSize) {
                        batch.add(paths.get(index));
                        LOGGER.debug(MessageCodes.JLT_010, batch.get(batch.size() - 1));
                    } else {
                        sendBatch(batch, bucket, key);
                        index -= 1; // Reset index so we don't miss the first of next batch
                    }
                }

                sendBatch(batch, bucket, key);
            } else {
                LOGGER.debug(MessageCodes.JLT_004, contentType);
                paths = new ArrayList<String>(1);
            }

            return getTilingRequest(paths, bucket, key);
        } catch (final Exception details) {
            throw new JiiifyIngestRuntimeException(details, MessageCodes.JLT_003, key, bucket);
        }
    }

    private TilingRequest getTilingRequest(final List<String> aPathsList, final String aBucket, final String aKey) {
        final TilingRequest tilingRequest = new TilingRequest(aBucket, aKey);

        for (int batchIndex = 0; batchIndex < aPathsList.size(); batchIndex++) {
            tilingRequest.addTile(aPathsList.get(batchIndex));
        }

        return tilingRequest;
    }

    private void sendBatch(final List<String> aBatch, final String aBucket, final String aKey) {
        final TilingRequest tilingRequest = new TilingRequest(aBucket, aKey);
        final int batchSize = aBatch.size();
        final InvokeRequest invokeRequest;
        final Future<InvokeResult> future;
        final InvokeResult result;
        final byte[] bytes;

        String tiler = System.getenv(Environment.IIIF_TILER_FUNCTION);

        if (StringUtils.trimToNull(tiler) == null) {
            tiler = Environment.DEFAULT_TILER_FUNCTION;
        }

        for (int batchIndex = 0; batchIndex < aBatch.size(); batchIndex++) {
            tilingRequest.addTile(aBatch.get(batchIndex));
        }

        LOGGER.debug(MessageCodes.JLT_009, batchSize, batchSize == 1 ? "" : "s", tiler);

        try {
            bytes = mySerializer.writeValueAsBytes(tilingRequest);
        } catch (final JsonProcessingException details) {
            throw new JiiifyIngestRuntimeException(details);
        }

        invokeRequest = new InvokeRequest().withFunctionName(tiler).withPayload(ByteBuffer.wrap(bytes));

        try {
            future = myLambdaClient.invokeAsync(invokeRequest);

            // FIXME. I shouldn't have to call future.get() to get it to run; I want it to run async.
            if (StringUtils.trimToNull(System.getenv("TEST_ENVIRONMENT")) == null) {
                result = future.get(1, TimeUnit.SECONDS);
            }
        } catch (final TimeoutException details) {
            // eat this for now
        } catch (final TooManyRequestsException | ExecutionException | InterruptedException details) {
            throw new JiiifyIngestRuntimeException(details);
        }

        aBatch.clear();
    }
}
