package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.exceptions.ForbiddenException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiKeyValidation {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_KEY_SECRET_NAME_ENV_VARIABLE = "API_KEY_SECRET_NAME";
    public static final String API_KEY_SECRET_ID_ENV_VARIABLE = "API_KEY_SECRET_ID";
    public static final String AWS_REGION = "AWS_REGION";

    public static final String WRONG_OR_NO_AUTHORIZATION_KEY = "Wrong or no authorization key";
    public static final String ERROR_SETTING_UP_KEY = "Api key secret not setup correctly";

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyValidation.class);
    private final Environment environment;

    private AWSSecretsManager secretsManager;

    public ApiKeyValidation(AWSSecretsManager secretsManager, Environment environment) {
        this.secretsManager = secretsManager;
        this.environment = environment;
    }

    public ApiKeyValidation(Environment environment) {
        this(setupSecretsClient(environment), environment);
    }

    public void compareKeys(RequestInfo requestInfo) throws ForbiddenException {
        String customerApiKey = readAuthorizationValue(requestInfo);
        String secretValue = fetchSecretValue();
        checkKeysAreEqual(secretValue, customerApiKey);
    }

    public String readAuthorizationValue(RequestInfo requestInfo) {
        return requestInfo.getHeaders().get(AUTHORIZATION_HEADER);
    }

    public void checkKeysAreEqual(String secretValue, String customerApiKey) throws ForbiddenException {
        if (!secretValue.equals(customerApiKey)) {
            logger.error(WRONG_OR_NO_AUTHORIZATION_KEY);
            throw new ForbiddenException();
        }
    }

    public String fetchApiKeyValue() throws JsonProcessingException {
        GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(fetchSecretName());
        GetSecretValueResult secretValueResult = secretsManager.getSecretValue(request);

        String secretValueJsonString = secretValueResult.getSecretString();
        ObjectNode apiKeyObject = (ObjectNode) JsonUtils.objectMapper.readTree(secretValueJsonString);
        String jsonKey = environment.readEnv(API_KEY_SECRET_ID_ENV_VARIABLE);
        String secretValue = apiKeyObject.get(jsonKey).asText();
        return secretValue;
    }

    public String fetchSecretName() {
        return environment.readEnv(API_KEY_SECRET_NAME_ENV_VARIABLE);
    }

    public IllegalStateException exceptionReadingSecretApiKey(nva.commons.utils.attempt.Failure<String> failure) {
        return new IllegalStateException(ERROR_SETTING_UP_KEY, failure.getException());
    }

    public String fetchSecretValue() {
        return attempt(this::fetchApiKeyValue).orElseThrow(this::exceptionReadingSecretApiKey);
    }

    @JacocoGenerated
    private static AWSSecretsManager setupSecretsClient(Environment environment) {
        String region = environment.readEnv(AWS_REGION);
        return AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
    }
}
