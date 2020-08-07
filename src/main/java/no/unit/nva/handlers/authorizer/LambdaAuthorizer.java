package no.unit.nva.handlers.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaAuthorizer implements RequestStreamHandler {

    public Logger logger = LoggerFactory.getLogger(LambdaAuthorizer.class);

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String inputString = IoUtils.streamToString(input);
        System.out.println(inputString);
        logger.info(inputString);
        StatementElement statement = StatementElement.newBuilder()
            .withResource("*")
            .withAction("execute-api:Invoke")
            .withEffect("Allow")
            .build();
        AuthPolicy authPolicy = AuthPolicy.newBuilder().withStatement(Collections.singletonList(statement)).build();

        JsonUtils.objectMapper.writeValue(output, authPolicy);
    }
}
