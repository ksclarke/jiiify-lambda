
package info.freelibrary.jiiify.lambda.tiler;

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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.freelibrary.jiiify.lambda.StorageRequest;
import info.freelibrary.jiiify.lambda.TilingRequest;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageHandlerTest {

    private static final String TEST_RESPONSE = "src/test/resources/json/lambda-response.json";

    private final String FILE_NAME = "test.tif";

    private final ObjectMapper mySerializer = new ObjectMapper();

    @Mock
    private AmazonS3 myS3Client;

    @Mock
    private S3Object myS3Object;

    @Captor
    private ArgumentCaptor<GetObjectRequest> myGetObjectRequest;

    private JsonArray myResponse;

    @Before
    public void setUp() throws IOException {
        final FileInputStream fileStream = new FileInputStream("src/test/resources/images/" + FILE_NAME);
        final S3ObjectInputStream inputStream = new S3ObjectInputStream(fileStream, new HttpGet());
        final Buffer responseBuffer = Vertx.factory.vertx().fileSystem().readFileBlocking(TEST_RESPONSE);

        myResponse = new JsonArray(responseBuffer);

        when(myS3Object.getObjectContent()).thenReturn(inputStream);
        when(myS3Client.getObject(myGetObjectRequest.capture())).thenReturn(myS3Object);
    }

    private Context createContext() {
        final TestContext context = new TestContext();

        context.setFunctionName(ImageHandler.class.getSimpleName());

        return context;
    }

    @Test
    public void testTiler() throws JsonProcessingException {
        final TilingRequest tilingRequest = new TilingRequest("test", "asdf");
        final StorageRequest[] storageRequests;

        tilingRequest.addTile("jiiify-lambda-tiles-us-west-1/test/1024,1024,976,976/976,/0/default.jpg");
        tilingRequest.addTile("jiiify-lambda-tiles-us-west-1/test/1024,0,976,1024/976,/0/default.jpg");
        tilingRequest.addTile("jiiify-lambda-tiles-us-west-1/test/0,1024,1024,976/1024,/0/default.jpg");
        tilingRequest.addTile("jiiify-lambda-tiles-us-west-1/test/0,0,1024,1024/1024,/0/default.jpg");

        storageRequests = new ImageHandler(myS3Client).handleRequest(tilingRequest, createContext());

        for (int index = 0; index < storageRequests.length; index++) {
            final JsonObject actual = new JsonObject(mySerializer.writeValueAsString(storageRequests[index]));
            final JsonObject expected = myResponse.getJsonObject(index);

            Assert.assertEquals(expected, actual);
        }
    }

}
