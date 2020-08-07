package no.unit.nva.handlers;

import static no.unit.nva.handlers.ApiKeyValidation.ERROR_SETTING_UP_KEY;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiKeyValidationTest extends ServiceHandlerTest {

    private InternalServiceMock internalService;

    public ApiKeyValidationTest() throws InvalidEntryInternalException {
        super();
    }

    @BeforeEach
    public void init() throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        this.databaseService = createDatabaseServiceUsingLocalStorage();
        databaseService.addUser(sampleUser);
        internalService = new InternalServiceMock(databaseService);
    }

    @Test
    public void loggerLogsErrorInApiKeySetup() throws IOException {
        TestAppender appender = LogUtils.getTestingAppender(ApiKeyValidation.class);

        GetUserHandlerForInternalService handler = internalService.handlerWithMisconfiguredSecrets();

        sendValidGetRequestForGettingExistingUser(handler);
        assertThat(appender.getMessages(), containsString(ERROR_SETTING_UP_KEY));
    }

    private ByteArrayOutputStream sendValidGetRequestForGettingExistingUser(GetUserHandlerForInternalService handler)
        throws IOException {
        Map<String, String> headers = map(ApiKeyValidation.AUTHORIZATION_HEADER,
            InternalServiceMock.CORRECT_API_KEY);
        return sendRequest(handler, null, headers);
    }
}