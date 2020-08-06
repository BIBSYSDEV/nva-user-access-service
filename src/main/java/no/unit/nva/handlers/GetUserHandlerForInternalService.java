package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.exceptions.ForbiddenException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandlerForInternalService extends GetUserHandler {

    public static final String API_KEY_SECRET_NAME_ENV_VARIABLE = "API_KEY_SECRET_NAME";
    public static final String API_KEY_SECRET_ID_ENV_VARIABLE = "API_KEY_SECRET_ID";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String WRONG_OR_NO_AUTHORIZATION_KEY = "Wrong or no authorization key";
    public static final String ERROR_SETTING_UP_KEY = "Api key secret not setup correctly";
    private final AWSSecretsManager secretsClient;

    @JacocoGenerated
    public GetUserHandlerForInternalService() {
        super(defaultLogger());
        this.secretsClient = setupSecretsClient();
    }

    public GetUserHandlerForInternalService(Environment environment,
                                            DatabaseService databaseService,
                                            AWSSecretsManager secretsClient
    ) {
        super(environment, databaseService, defaultLogger());
        this.secretsClient = secretsClient;
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        String customerApiKey = requestInfo.getHeaders().get(AUTHORIZATION_HEADER);
        String secretValue = attempt(this::fetchApiKeyValue).orElseThrow(this::exceptionReadingSecretApiKey);
        checkKeysAreEqual(secretValue, customerApiKey);

        return super.processInput(input, requestInfo, context);
    }

    private static Logger defaultLogger() {
        return LoggerFactory.getLogger(GetUserHandlerForInternalService.class);
    }

    private void checkKeysAreEqual(String secretValue, String customerApiKey) throws ForbiddenException {
        if (!secretValue.equals(customerApiKey)) {
            logger.error(WRONG_OR_NO_AUTHORIZATION_KEY);
            throw new ForbiddenException();
        }
    }

    private IllegalStateException exceptionReadingSecretApiKey(nva.commons.utils.attempt.Failure<String> failure) {
        return new IllegalStateException(ERROR_SETTING_UP_KEY, failure.getException());
    }

    private String fetchApiKeyValue() throws JsonProcessingException {
        GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(fetchSecretName());
        GetSecretValueResult secretValueResult = secretsClient.getSecretValue(request);

        String secretValueJsonString = secretValueResult.getSecretString();
        ObjectNode apiKeyObject = (ObjectNode) JsonUtils.objectMapper.readTree(secretValueJsonString);
        String jsonKey = environment.readEnv(API_KEY_SECRET_ID_ENV_VARIABLE);
        String secretValue = apiKeyObject.get(jsonKey).asText();
        return secretValue;
    }

    private String fetchSecretName() {
        return environment.readEnv(API_KEY_SECRET_NAME_ENV_VARIABLE);
    }

    @JacocoGenerated
    private AWSSecretsManager setupSecretsClient() {
        String region = environment.readEnv(AWS_REGION);
        return AWSSecretsManagerClientBuilder.standard()
            .withRegion(region)
            .build();
    }
}
