package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import no.unit.nva.exceptions.ForbiddenException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.UserDto;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

public class AddUserHandlerForInternalServiceTest extends ServiceHandlerTest {

    private static final String ADDITIONAL_ROLE = "SomeOtherRole";
    private InternalServiceMock internalService;

    protected AddUserHandlerForInternalServiceTest() throws InvalidEntryInternalException {
        super();
    }

    @BeforeEach
    public void init() {
        this.databaseService = createDatabaseServiceUsingLocalStorage();
        this.internalService = new InternalServiceMock(databaseService);
    }

    @Test
    public void handleRequestAddsUserWhenCalledWithValidApiKey()
        throws IOException, InvalidEntryInternalException, NotFoundException {
        AddUserHandlerForInternalService handler = internalService.defaultAddHandler();
        ByteArrayOutputStream output = sendValidAddRequest(handler);

        GatewayResponse<Void> response = GatewayResponse.fromOutputStream(output);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_OK)));

        UserDto addedUser = databaseService.getUser(sampleUser);
        assertThat(addedUser, is(equalTo(sampleUser)));
    }

    @Test
    public void handleRequestReturnsForbiddenWhenCalledWithInvalidApiKey()
        throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        AddUserHandlerForInternalService handler = internalService.defaultAddHandler();
        ByteArrayOutputStream output = sendAddRequestWithWrongApiKey(handler);

        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));

        String details = response.getBodyObject(Problem.class).getDetail();
        assertThat(details, is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));

        assertThat(appender.getMessages(), containsString(ApiKeyValidation.WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void loggerLogsWrongApiKeyEvent()
        throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        AddUserHandlerForInternalService handler = internalService.defaultAddHandler();
        sendAddRequestWithWrongApiKey(handler);

        assertThat(appender.getMessages(), containsString(ApiKeyValidation.WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void loggerLogsMissingApiKeyEvent()
        throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        AddUserHandlerForInternalService handler = internalService.defaultAddHandler();
        sendAddRequestWithoutApiKey(handler);

        assertThat(appender.getMessages(), containsString(ApiKeyValidation.WRONG_OR_NO_AUTHORIZATION_KEY));
    }

    @Test
    public void handleRequestReturnsForbiddenWhenCalledWithoutApiKey()
        throws IOException {

        AddUserHandlerForInternalService handler = internalService.defaultAddHandler();
        ByteArrayOutputStream output = sendAddRequestWithoutApiKey(handler);

        GatewayResponse<Problem> response = GatewayResponse.fromOutputStream(output);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_FORBIDDEN)));

        String details = response.getBodyObject(Problem.class).getDetail();
        assertThat(details, is(equalTo(ForbiddenException.DEFAULT_ERROR_MESSAGE)));
    }

    private ByteArrayOutputStream sendAddRequestWithoutApiKey(AddUserHandlerForInternalService handler)
        throws IOException {

        return sendAddRequest(handler, null);
    }

    private ByteArrayOutputStream sendAddRequestWithWrongApiKey(AddUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, InternalServiceMock.WRONG_API_KEY);
        return sendAddRequest(handler, headers);
    }

    private ByteArrayOutputStream sendValidAddRequest(AddUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER, InternalServiceMock.CORRECT_API_KEY);
        return sendAddRequest(handler, headers);
    }

    private ByteArrayOutputStream sendAddRequest(AddUserHandlerForInternalService handler,
                                                 Map<String, String> headers)
        throws IOException {
        return sendRequest(handler, sampleUser, headers);
    }
}