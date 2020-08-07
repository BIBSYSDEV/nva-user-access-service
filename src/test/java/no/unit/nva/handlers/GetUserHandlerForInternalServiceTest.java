package no.unit.nva.handlers;

import static no.unit.nva.handlers.ApiKeyValidation.ERROR_SETTING_UP_KEY;
import static no.unit.nva.handlers.ApiKeyValidation.WRONG_OR_NO_AUTHORIZATION_KEY;
import static no.unit.nva.handlers.InternalServiceMock.CORRECT_API_KEY;
import static no.unit.nva.handlers.InternalServiceMock.WRONG_API_KEY;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.ForbiddenException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.UserDto;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class GetUserHandlerForInternalServiceTest extends ServiceHandlerTest {

    private InternalServiceMock internalService;

    public GetUserHandlerForInternalServiceTest() throws InvalidEntryInternalException {
        super();
    }

    @BeforeEach
    public void init() throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        this.databaseService = createDatabaseServiceUsingLocalStorage();
        databaseService.addUser(sampleUser);
        internalService = new InternalServiceMock(databaseService);
    }

    @Test
    public void handleRequestReturnsStoredUserWhenApiKeyIsCorrect()
        throws IOException {

        ByteArrayOutputStream output = sendValidGetRequestForGettingExistingUser(defaultHandler());

        GatewayResponse<UserDto> response = GatewayResponse.fromOutputStream(output);
        UserDto actualUser = response.getBodyObject(UserDto.class);

        assertThat(actualUser, is(equalTo(sampleUser)));
    }

    @Test
    public void handleRequestReturnsForbiddenForMissingApiKey()
        throws IOException {
        ByteArrayOutputStream output = sendGetRequestWithoutApiKey();

        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        Problem problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));
        assertThat(problem.getDetail(), is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));
    }

    @Test
    public void loggerLogsMissingApiKey()
        throws IOException {

        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        sendGetRequestWithoutApiKey();

        assertThat(appender.getMessages(), containsString(WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void handleRequestReturnsForbiddenForWrongApiKey()
        throws IOException {

        ByteArrayOutputStream output = sendGetRequestWithWrongApiKey();
        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        Problem problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));
        assertThat(problem.getDetail(), is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));
    }

    @Test
    public void loggerLogsWrongApiKey()
        throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        sendGetRequestWithWrongApiKey();

        assertThat(appender.getMessages(), containsString(WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    private GetUserHandlerForInternalService defaultHandler() {
        return internalService.defaultGetHandler();
    }

    private ByteArrayOutputStream sendGetRequestWithWrongApiKey() throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, WRONG_API_KEY);
        return sendGetRequest(defaultHandler(), headers);
    }

    private ByteArrayOutputStream sendValidGetRequestForGettingExistingUser(GetUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, CORRECT_API_KEY);
        return sendGetRequest(handler, headers);
    }

    private ByteArrayOutputStream sendGetRequestWithoutApiKey() throws IOException {
        return sendGetRequest(defaultHandler(), null);
    }

    private ByteArrayOutputStream sendGetRequest(GetUserHandlerForInternalService handler, Map<String, String> headers)
        throws IOException {
        return sendRequest(handler, (Void) null, headers);
    }
}