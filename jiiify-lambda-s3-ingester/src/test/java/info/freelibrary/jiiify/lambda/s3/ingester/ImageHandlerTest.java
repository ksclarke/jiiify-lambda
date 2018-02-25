
package info.freelibrary.jiiify.lambda.s3.ingester;

import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.jiiify.lambda.Metadata;
import info.freelibrary.jiiify.lambda.TilingRequest;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageHandlerTest {

    private static final String TEST_RESPONSE_SET_KEY = "src/test/resources/json/lambda-response-set-key.json";

    private static final String TEST_RESPONSE_NATIVE_KEY = "src/test/resources/json/lambda-response-native-key.json";

    private final String TEST_ID = "asdf";

    private final String CONTENT_TYPE = "image/tiff";

    private final String FILE_NAME = "test.tif";

    private final ObjectMapper mySerializer = new ObjectMapper();

    private S3Event myS3Event;

    @Mock
    private AmazonS3 myS3Client;

    @Mock
    private S3Object myS3Object;

    private ObjectMetadata myMetadata;

    private JsonObject myExpectedNativeKey;

    private JsonObject myExpectedSetKey;

    @Captor
    private ArgumentCaptor<GetObjectRequest> myGetObjectRequest;

    @Before
    public void setUp() throws IOException {
        final FileInputStream fileStream = new FileInputStream("src/test/resources/images/" + FILE_NAME);
        final S3ObjectInputStream inputStream = new S3ObjectInputStream(fileStream, new HttpGet());
        final Buffer setKeyBuffer = Vertx.factory.vertx().fileSystem().readFileBlocking(TEST_RESPONSE_SET_KEY);
        final Buffer nativeKeyBuffer = Vertx.factory.vertx().fileSystem().readFileBlocking(TEST_RESPONSE_NATIVE_KEY);

        myExpectedSetKey = new JsonObject(setKeyBuffer);
        myExpectedNativeKey = new JsonObject(nativeKeyBuffer);
        myMetadata = new ObjectMetadata();
        myS3Event = TestUtils.parse("/s3-event.put.json", S3Event.class);
        myMetadata.setContentType(CONTENT_TYPE);

        when(myS3Object.getObjectMetadata()).thenReturn(myMetadata);
        when(myS3Object.getObjectContent()).thenReturn(inputStream);
        when(myS3Client.getObject(myGetObjectRequest.capture())).thenReturn(myS3Object);
    }

    private Context createContext() {
        final TestContext context = new TestContext();

        context.setFunctionName(ImageHandler.class.getSimpleName());

        return context;
    }

    @Test
    public void testHandlerNativeKey() throws JsonProcessingException {
        when(myS3Object.getKey()).thenReturn(FILE_NAME);

        final ImageHandler handler = new ImageHandler(myS3Client);
        final TilingRequest tilingRequest = handler.handleRequest(myS3Event, createContext());

        Assert.assertEquals(myExpectedNativeKey, new JsonObject(mySerializer.writeValueAsString(tilingRequest)));
    }

    @Test
    public void testHandlerSetKey() throws JsonProcessingException {
        myMetadata.addUserMetadata(Metadata.ID, TEST_ID);

        final ImageHandler handler = new ImageHandler(myS3Client);
        final TilingRequest tilingRequest = handler.handleRequest(myS3Event, createContext());

        Assert.assertEquals(myExpectedSetKey, new JsonObject(mySerializer.writeValueAsString(tilingRequest)));
    }

}
