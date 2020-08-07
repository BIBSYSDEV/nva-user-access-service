package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.interfaces.WithEnvironment;
import nva.commons.utils.Environment;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class InternalServiceMock implements WithEnvironment {

    public static final String SOME_API_KEY_NAME = "SomeKeyName";
    public static final String SOME_API_KEY_ID = "SomeKeyId";
    public static final String CORRECT_API_KEY = "SomeKeyValue";
    public static final String WRONG_API_KEY = "SomeOtherKey";
    private AWSSecretsManager secretsManager;
    private Environment environment;
    private DatabaseService databaseService;

    public InternalServiceMock(DatabaseService databaseService) {
        environment = mockEnvironmentWithApiKeyDetails();
        setUpFunctioningSecretsManager();
        this.databaseService = databaseService;
    }

    public GetUserHandlerForInternalService defaultHandler() {
        return new GetUserHandlerForInternalService(environment, databaseService,
            secretsManager);
    }

    public GetUserHandlerForInternalService handlerWithMisconfiguredSecrets() {
        secretsManager = mock(AWSSecretsManager.class);
        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        GetUserHandlerForInternalService handler =
            new GetUserHandlerForInternalService(mockEnvironment(), databaseService, secretsManager);
        return handler;
    }

    private void setUpFunctioningSecretsManager() {
        secretsManager = mock(AWSSecretsManager.class);
        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
            .thenAnswer(generateGetSecretValueResult());
    }

    private Answer<GetSecretValueResult> generateGetSecretValueResult() {
        return new Answer<>() {
            @Override
            public GetSecretValueResult answer(InvocationOnMock invocation) throws Throwable {
                GetSecretValueRequest request = fetchRequest(invocation);
                if (requestContainsCorrectSecretId(request)) {
                    String secretString = writeSecretAsJsonString();
                    return new GetSecretValueResult().withSecretString(secretString);
                } else {
                    throw new RuntimeException("Not valid secret");
                }
            }

            private boolean requestContainsCorrectSecretId(GetSecretValueRequest request) {
                return request.getSecretId().equals(SOME_API_KEY_NAME);
            }

            private GetSecretValueRequest fetchRequest(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        };
    }

    private String writeSecretAsJsonString() throws JsonProcessingException {
        Map<String, String> secretMap = map(SOME_API_KEY_ID, CORRECT_API_KEY);
        String secretString = objectMapper.writeValueAsString(secretMap);
        return secretString;
    }

    private Map<String, String> map(String key, String value) {
        return Collections.singletonMap(key, value);
    }

    private Environment mockEnvironmentWithApiKeyDetails() {
        Map<String, String> envVariables = new ConcurrentHashMap<>();
        envVariables.put(ApiKeyValidation.API_KEY_SECRET_NAME_ENV_VARIABLE, SOME_API_KEY_NAME);
        envVariables.put(ApiKeyValidation.API_KEY_SECRET_ID_ENV_VARIABLE, SOME_API_KEY_ID);
        return mockEnvironment(envVariables, DEFAULT_ENV_VALUE);
    }
}
