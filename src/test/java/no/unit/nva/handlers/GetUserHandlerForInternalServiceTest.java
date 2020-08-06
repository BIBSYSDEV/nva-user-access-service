package no.unit.nva.handlers;

import static no.unit.nva.handlers.GetUserHandlerForInternalService.WRONG_OR_NO_AUTHORIZATION_KEY;
import static no.unit.nva.handlers.HandlerAccessingUser.USERNAME_PATH_PARAMETER;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.ForbiddenException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.Environment;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zalando.problem.Problem;

class GetUserHandlerForInternalServiceTest extends HandlerTest {

    public static final String SOME_API_KEY_NAME = "SomeKeyName";
    public static final String SOME_API_KEY_ID = "SomeKeyId";
    public static final String SOME_API_KEY_VALUE = "SomeKeyValue";
    public static final String WRONG_API_KEY = "SomeOtherKey";
    private final UserDto existingUser;
    private final Context context;
    private Environment environment;
    private AWSSecretsManager secretsManager;

    public GetUserHandlerForInternalServiceTest() throws InvalidEntryInternalException {
        setupMockEnvironment();
        existingUser = createSampleUser();
        context = mock(Context.class);
    }

    @BeforeEach
    public void init() throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        this.databaseService = createDatabaseServiceUsingLocalStorage();
        databaseService.addUser(existingUser);
        secretsManager = mock(AWSSecretsManager.class);
        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
            .thenAnswer(generateGetSecretValueResult());
    }

    @Test
    public void handleRequestReturnsStoredUserWhenApiKeyIsCorrect()
        throws IOException {

        ByteArrayOutputStream output = sendValidRequestForGettingExistingUser(defaultHandler());

        GatewayResponse<UserDto> response = GatewayResponse.fromOutputStream(output);
        UserDto actualUser = response.getBodyObject(UserDto.class);

        assertThat(actualUser, is(equalTo(existingUser)));
    }

    @Test
    public void handleRequestReturnsForbiddenForMissingApiKey()
        throws IOException {
        ByteArrayOutputStream output = sendRequestWithoutApiKey();

        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        Problem problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));
        assertThat(problem.getDetail(), is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));
    }

    @Test
    public void loggerLogsMissingApiKey()
        throws IOException {

        TestAppender appender = LogUtils.getTestingAppender(GetUserHandlerForInternalService.class);

        sendRequestWithoutApiKey();

        assertThat(appender.getMessages(), containsString(WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void handleRequestReturnsForbiddenForWrongApiKey()
        throws IOException {

        ByteArrayOutputStream output = sendRequestWithWrongApiKey();
        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        Problem problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));
        assertThat(problem.getDetail(), is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));
    }

    @Test
    public void loggerLogsWrongApiKey()
        throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(GetUserHandlerForInternalService.class);

        sendRequestWithWrongApiKey();

        assertThat(appender.getMessages(), containsString(WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void loggerLogsErrorInApiKeySetup() throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(GetUserHandlerForInternalService.class);

        GetUserHandlerForInternalService handler = hanlderWithMisconfiguredSecrets();

        sendValidRequestForGettingExistingUser(handler);
        assertThat(appender.getMessages(), containsString(GetUserHandlerForInternalService.ERROR_SETTING_UP_KEY));
    }

    private GetUserHandlerForInternalService hanlderWithMisconfiguredSecrets() {
        secretsManager = mock(AWSSecretsManager.class);
        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        GetUserHandlerForInternalService handler =
            new GetUserHandlerForInternalService(mockEnvironment(), databaseService, secretsManager);
        return handler;
    }

    private ByteArrayOutputStream sendRequestWithWrongApiKey() throws IOException {
        Map<String, String> headers = map(GetUserHandlerForInternalService.AUTHORIZATION_HEADER, WRONG_API_KEY);
        return sendRequest(defaultHandler(), headers);
    }

    private ByteArrayOutputStream sendValidRequestForGettingExistingUser(GetUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(GetUserHandlerForInternalService.AUTHORIZATION_HEADER, SOME_API_KEY_VALUE);
        return sendRequest(handler, headers);
    }

    private ByteArrayOutputStream sendRequestWithoutApiKey() throws IOException {

        return sendRequest(defaultHandler(), null);
    }

    private GetUserHandlerForInternalService defaultHandler() {
        return new GetUserHandlerForInternalService(environment, databaseService,
            secretsManager);
    }

    private ByteArrayOutputStream sendRequest(GetUserHandlerForInternalService handler, Map<String, String> headers)
        throws IOException {

        Map<String, String> pathParameters = map(USERNAME_PATH_PARAMETER, existingUser.getUsername());

        InputStream input = new HandlerRequestBuilder<Void>(objectMapper)
            .withPathParameters(pathParameters)
            .withHeaders(headers)
            .build();
        ByteArrayOutputStream output = outputStream();
        handler.handleRequest(input, output, context);
        return output;
    }

    private Map<String, String> map(String key, String value) {
        return Collections.singletonMap(key, value);
    }

    private UserDto createSampleUser() throws InvalidEntryInternalException {
        RoleDto sampleRole = RoleDto.newBuilder().withName(DEFAULT_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(DEFAULT_USERNAME)
            .withInstitution(DEFAULT_INSTITUTION)
            .withRoles(Collections.singletonList(sampleRole))
            .build();
    }

    private void setupMockEnvironment() {
        Map<String, String> envVariables = new ConcurrentHashMap<>();
        envVariables.put(GetUserHandlerForInternalService.API_KEY_SECRET_NAME_ENV_VARIABLE, SOME_API_KEY_NAME);
        envVariables.put(GetUserHandlerForInternalService.API_KEY_SECRET_ID_ENV_VARIABLE, SOME_API_KEY_ID);
        environment = mockEnvironment(envVariables, DEFAULT_ENV_VALUE);
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
        Map<String, String> secretMap = map(SOME_API_KEY_ID, SOME_API_KEY_VALUE);
        String secretString = objectMapper.writeValueAsString(secretMap);
        return secretString;
    }
}