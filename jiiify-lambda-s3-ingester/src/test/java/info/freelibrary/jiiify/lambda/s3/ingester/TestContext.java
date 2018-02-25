
package info.freelibrary.jiiify.lambda.s3.ingester;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * A simple mock implementation of the {@code Context} interface. Default values are stubbed out, and setters are
 * provided so you can customize the context before passing it to your function.
 */
public class TestContext implements Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContext.class, "s3_ingester_messages");

    private String awsRequestId = "EXAMPLE";

    private ClientContext clientContext;

    private String functionName = "EXAMPLE";

    private CognitoIdentity identity;

    private String logGroupName = "EXAMPLE";

    private String logStreamName = "EXAMPLE";

    private LambdaLogger logger = new TestLogger();

    private int memoryLimitInMB = 128;

    private int remainingTimeInMillis = 15000;

    private String functionVersion = "EXAMPLE";

    private String invokedFunctionArn = "EXAMPLE";

    @Override
    public String getAwsRequestId() {
        return awsRequestId;
    }

    public void setAwsRequestId(final String aValue) {
        awsRequestId = aValue;
    }

    @Override
    public ClientContext getClientContext() {
        return clientContext;
    }

    public void setClientContext(final ClientContext aValue) {
        clientContext = aValue;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(final String aValue) {
        functionName = aValue;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(final CognitoIdentity aValue) {
        identity = aValue;
    }

    @Override
    public String getLogGroupName() {
        return logGroupName;
    }

    public void setLogGroupName(final String aValue) {
        logGroupName = aValue;
    }

    @Override
    public String getLogStreamName() {
        return logStreamName;
    }

    public void setLogStreamName(final String aValue) {
        logStreamName = aValue;
    }

    @Override
    public LambdaLogger getLogger() {
        return logger;
    }

    public void setLogger(final LambdaLogger aValue) {
        logger = aValue;
    }

    @Override
    public int getMemoryLimitInMB() {
        return memoryLimitInMB;
    }

    public void setMemoryLimitInMB(final int aValue) {
        memoryLimitInMB = aValue;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return remainingTimeInMillis;
    }

    public void setRemainingTimeInMillis(final int aValue) {
        remainingTimeInMillis = aValue;
    }

    @Override
    public String getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(final String aValue) {
        functionVersion = aValue;
    }

    @Override
    public String getInvokedFunctionArn() {
        return invokedFunctionArn;
    }

    public void setInvokedFunctionArn(final String aValue) {
        invokedFunctionArn = aValue;
    }

    /**
     * A simple {@code LambdaLogger} that prints everything to stderr.
     */
    private static class TestLogger implements LambdaLogger {

        @Override
        public void log(final String aMessage) {
            LOGGER.error(MessageCodes.JLT_001, aMessage);
        }
    }
}
