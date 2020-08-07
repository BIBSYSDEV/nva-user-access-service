package no.unit.nva.handlers;

import static no.unit.nva.handlers.ApiKeyValidation.ERROR_SETTING_UP_KEY;
import static no.unit.nva.handlers.ApiKeyValidation.WRONG_OR_NO_AUTHORIZATION_KEY;
import static no.unit.nva.handlers.HandlerAccessingUser.USERNAME_PATH_PARAMETER;
import static no.unit.nva.handlers.InternalServiceMock.CORRECT_API_KEY;
import static no.unit.nva.handlers.InternalServiceMock.WRONG_API_KEY;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.ForbiddenException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class GetUserHandlerForInternalServiceTest extends HandlerTest {

    private final UserDto existingUser;
    private final Context context;

    private InternalServiceMock internalService;

    public GetUserHandlerForInternalServiceTest() throws InvalidEntryInternalException {
        existingUser = createSampleUser();
        context = mock(Context.class);
    }

    @BeforeEach
    public void init() throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        this.databaseService = createDatabaseServiceUsingLocalStorage();
        databaseService.addUser(existingUser);
        internalService = new InternalServiceMock(databaseService);
    }

    @Test
    public void handleRequestReturnsStoredUserWhenApiKeyIsCorrect()
        throws IOException {

        ByteArrayOutputStream output = sendValidRequestForGettingExistingUser(defaultHandler());

        GatewayResponse<UserDto> response = GatewayResponse.fromOutputStream(output);
        UserDto actualUser = response.getBodyObject(UserDto.class);

        assertThat(actualUser, is(equalTo(existingUser)));
    }

    private GetUserHandlerForInternalService defaultHandler() {
        return internalService.defaultHandler();
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

        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

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
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        sendRequestWithWrongApiKey();

        assertThat(appender.getMessages(), containsString(WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void loggerLogsErrorInApiKeySetup() throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(GetUserHandlerForInternalService.class);

        GetUserHandlerForInternalService handler = internalService.handlerWithMisconfiguredSecrets();

        sendValidRequestForGettingExistingUser(handler);
        assertThat(appender.getMessages(), containsString(ERROR_SETTING_UP_KEY));
    }



    private ByteArrayOutputStream sendRequestWithWrongApiKey() throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, WRONG_API_KEY);
        return sendRequest(defaultHandler(), headers);
    }

    private ByteArrayOutputStream sendValidRequestForGettingExistingUser(GetUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, CORRECT_API_KEY);
        return sendRequest(handler, headers);
    }

    private ByteArrayOutputStream sendRequestWithoutApiKey() throws IOException {
        return sendRequest(defaultHandler(), null);
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
}