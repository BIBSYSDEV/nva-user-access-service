package no.unit.nva.handlers.authorizer;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import nva.commons.exceptions.ForbiddenException;
import nva.commons.handlers.authentication.ServiceAuthorizerHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Failure;

public class LambdaAuthorizer extends ServiceAuthorizerHandler {

    public static final String DEFAULT_PRINCIPAL_ID = "ServiceAccessingUsersAndRoles";
    public static final String AWS_SECRET_NAME_ENV_VAR = "AWS_SECRET_NAME";
    public static final String AWS_SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";
    private final AWSSecretsManager awsSecretsManager;

    public LambdaAuthorizer() {
        this(newAwsSecretsManager(), new Environment());
    }

    public LambdaAuthorizer(AWSSecretsManager awsSecretsManager, Environment environment) {
        super(environment);
        this.awsSecretsManager = awsSecretsManager;
    }

    @Override
    protected String principalId() {
        return DEFAULT_PRINCIPAL_ID;
    }

    @Override
    protected String fetchSecret() throws ForbiddenException {
        final String secretName = environment.readEnv(AWS_SECRET_NAME_ENV_VAR);

        GetSecretValueResult getSecretResult = awsSecretsManager.getSecretValue(
            new GetSecretValueRequest().withSecretId(secretName));
        return extractApiKey(getSecretResult);
    }

    private String extractApiKey(GetSecretValueResult getSecretResult)
        throws ForbiddenException {

        final String secretKey = environment.readEnv(AWS_SECRET_KEY_ENV_VAR);
        String secretString = getSecretResult.getSecretString();
        String secretValue = attempt(() -> JsonUtils.objectMapper.readTree(secretString))
            .map(secretJson -> secretJson.get(secretKey))
            .map(JsonNode::textValue)
            .orElseThrow(this::logErrorAndThrowException);
        return secretValue;
    }

    private <I> ForbiddenException logErrorAndThrowException(Failure<I> failure) {
        logger.error(failure.getException().getMessage(), failure.getException());
        return new ForbiddenException();
    }

    private static AWSSecretsManager newAwsSecretsManager() {
        return AWSSecretsManagerClientBuilder.defaultClient();
    }
}
