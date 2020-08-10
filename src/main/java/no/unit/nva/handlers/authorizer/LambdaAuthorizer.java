package no.unit.nva.handlers.authorizer;

import static java.util.Objects.nonNull;
import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class LambdaAuthorizer extends ApiGatewayHandler<Void, HandlerResponse> {

    public static final int API_GATEWAY_SUBSTRING = 5;
    public static final String API_GATEWAY_PATH_DELIMITER = "/";
    public static final int AWS_ACCOUNT_ID = 4;
    public static final int REGION = 3;
    public static final int API_GATEWAY_SUBSTRING_STAGE = 1;
    private static final int API_GATEWAY_SUBSTRING_API_ID = 0;
    private static final int API_GATEWAY_SUBSTRING_METHOD = 2;
    private static final int API_GATEWAY_SUBSTRING_PATH = 3;

    public LambdaAuthorizer() {
        super(Void.class, LoggerFactory.getLogger(LambdaAuthorizer.class));
    }

    @Override
    protected HandlerResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        String requestInfoStr = attempt(() -> JsonUtils.objectMapper.writeValueAsString(requestInfo))
            .orElse(fail -> null);
        logger.info(requestInfoStr);
        String methodArn = requestInfo.getMethodArn();
        String[] methodArnModules = methodArn.split(":");

        //"methodArn": "arn:aws:execute-api:eu-west-1:884807050265:2lcqynkwke/Prod/POST/roles",
        //String resource= "arn:aws:execute-api:region:account-id:api-id/stage-name/HTTP-VERB/resource-path-specifier"

        logger.info("AWS accountId" + extractAccountId(methodArnModules));
        logger.info("Region:" + extractRegion(methodArnModules));
        logger.info("RestApiId:" + extractRestApiId(methodArnModules));
        logger.info("Stage:" + extractStage(methodArnModules));
        logger.info("Method:" + extractMethod(methodArnModules));

        logger.info("Path:" + extractPath(methodArnModules));

        String path = extractPath(methodArnModules);

        String resource = "/";
        if (nonNull(path) && !path.isBlank()) {
            resource = resource.concat(path);
        }

        StatementElement statement = StatementElement.newBuilder()
            .withResource(methodArn)
            .withAction("execute-api:Invoke")
            .withEffect("Allow")
            .build();
        AuthPolicy authPolicy = AuthPolicy.newBuilder().withStatement(Collections.singletonList(statement)).build();
        HandlerResponse response = HandlerResponse.newBuilder()
            .withPrincipalId("SomeUser")
            .withPolicyDocument(authPolicy)
            .build();

        String responseString = attempt(() -> JsonUtils.objectMapper.writeValueAsString(response))
            .orElse(fail -> "could not serialize response");
        logger.info(responseString);
        return response;
    }

    @Override
    protected void writeOutput(Void input, HandlerResponse output)
        throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String responseJson = JsonUtils.objectMapper.writeValueAsString(output);
            writer.write(responseJson);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, HandlerResponse output) {
        return HttpStatus.SC_ACCEPTED;
    }

    private String extractPath(String[] methodArnModules) {
        return methodArnModules[API_GATEWAY_SUBSTRING].split(API_GATEWAY_PATH_DELIMITER)
            [API_GATEWAY_SUBSTRING_PATH];
    }

    private String extractMethod(String[] methodArnModules) {
        return methodArnModules[API_GATEWAY_SUBSTRING]
            .split(API_GATEWAY_PATH_DELIMITER)[API_GATEWAY_SUBSTRING_METHOD];
    }

    private String extractStage(String[] methodArnModules) {
        return methodArnModules[API_GATEWAY_SUBSTRING]
            .split(API_GATEWAY_PATH_DELIMITER)[API_GATEWAY_SUBSTRING_STAGE];
    }

    private String extractRegion(String[] methodArnModules) {
        return methodArnModules[REGION];
    }

    private String extractRestApiId(String[] methodArn) {
        return methodArn[API_GATEWAY_SUBSTRING].split(API_GATEWAY_PATH_DELIMITER)[API_GATEWAY_SUBSTRING_API_ID];
    }

    private String extractAccountId(String[] methodArn) {
        return methodArn[AWS_ACCOUNT_ID];
    }
}
