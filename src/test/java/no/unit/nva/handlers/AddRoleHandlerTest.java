package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.zalando.problem.Problem;

public class AddRoleHandlerTest extends DatabaseAccessor {

    public static final String SOME_ROLE_NAME = "someRoleName";
    private RoleDto sampleRole;
    private AddRoleHandler addRoleHandler;

    private Context context;

    /**
     * init.
     *
     * @throws InvalidRoleInternalException when an invalid role is created
     */
    @BeforeEach
    public void init() throws InvalidRoleInternalException {
        initializeTestDatabase();
        context = mock(Context.class);
        DatabaseService service = new DatabaseServiceImpl(localDynamo);
        addRoleHandler = new AddRoleHandler(mockEnvironment(), service);
        sampleRole = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
    }

    @Test
    public void handleRequestReturnsBadRequestWhenRequestBodyIsEmpty() throws IOException {
        GatewayResponse<RoleDto> response = sendRequest(null);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
    }

    @Test
    public void handleRequestReturnsBadRequestWhenRequestBodyIsAnEmptyObject() throws IOException {
        GatewayResponse<RoleDto> response = sendRequest(new RoleDto());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
    }

    @Test
    public void handlerRequestReturnsOkWheRequestBodyIsValid() throws InvalidRoleInternalException, IOException {
        GatewayResponse<RoleDto> response = sendRequest(RoleDto.newBuilder().withName(SOME_ROLE_NAME).build());
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
    }

    @Test
    public void handlerRequestReturnsTheGeneratedObjectWhenInputIsValid()
        throws InvalidRoleInternalException, IOException {
        RoleDto actualRole = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        GatewayResponse<RoleDto> response = sendRequest(actualRole);
        RoleDto savedRole = response.getBodyObject(RoleDto.class);
        assertThat(savedRole, is(equalTo(actualRole)));
    }

    @Test
    public void handlerRequestReturnsTheGeneratedObjectAfterWaitingForSyncingToComplete()
        throws InvalidRoleInternalException, IOException {
        RoleDto actualRole = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        DatabaseService service = databaseServiceWithSyncDelay();
        addRoleHandler = new AddRoleHandler(mockEnvironment(), service);

        GatewayResponse<RoleDto> response = sendRequest(actualRole);
        RoleDto savedRole = response.getBodyObject(RoleDto.class);
        assertThat(savedRole, is(equalTo(actualRole)));
    }

    @Test
    public void handleRequestReturnsInternalServerErrorWhenDatabaseFailsToSaveTheData()
        throws InvalidRoleInternalException, IOException {
        RoleDto actualRole = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        DatabaseService service = databaseServiceReturningEmpty();
        addRoleHandler = new AddRoleHandler(mockEnvironment(), service);

        GatewayResponse<Problem> response = sendRequest(actualRole);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        Problem problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(AddRoleHandler.ERROR_FETCHING_SAVED_ROLE));
    }

    @Test
    public void statusCodeReturnsOkWhenRequestIsSuccessful() {
        Integer successCode = addRoleHandler.getSuccessStatusCode(null, null);
        assertThat(successCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @Test
    public void addRoleHandlerThrowsUnexpectedExceptionWhenDatabaseServiceCannotFetchSavedRole()
        throws InvalidRoleInternalException {

        Class<InvalidRoleInternalException> expectedExceptionClass = InvalidRoleInternalException.class;
        RoleDto inputRole = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();

        AddRoleHandler addRoleHandler = addRoleHandlerThrowsUnexpectedException(expectedExceptionClass);
        Executable action = () -> addRoleHandler.processInput(inputRole, null, null);

        Exception exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(expectedExceptionClass)));
        assertThat(exception.getMessage(), containsString(AddRoleHandler.UNEXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void errorMessageIsLoggedWhenAddRoleHandlerThrowsUnexpectedException()
        throws InvalidRoleInternalException, IOException {
        TestAppender testingAppender = LogUtils.getTestingAppender(AddRoleHandler.class);
        Class<InvalidRoleInternalException> expectedExceptionClass = InvalidRoleInternalException.class;

        AddRoleHandler addRoleHandler = addRoleHandlerThrowsUnexpectedException(expectedExceptionClass);
        InputStream inputRequest = new HandlerRequestBuilder<>(objectMapper).withBody(sampleRole).build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        addRoleHandler.handleRequest(inputRequest, outputStream, context);

        assertThat(testingAppender.getMessages(), containsString(AddRoleHandler.UNEXPECTED_ERROR_MESSAGE));
    }

    private AddRoleHandler addRoleHandlerThrowsUnexpectedException(
        Class<InvalidRoleInternalException> expectedExceptionClass) throws InvalidRoleInternalException {
        DatabaseService databaseServiceThrowingException = mock(DatabaseService.class);
        when(databaseServiceThrowingException.getRole(any(RoleDto.class))).thenThrow(expectedExceptionClass);
        return new AddRoleHandler(mockEnvironment(), databaseServiceThrowingException);
    }

    private DatabaseServiceImpl databaseServiceWithSyncDelay() {
        return new DatabaseServiceImpl(localDynamo) {
            private int counter = 0;

            @Override
            public Optional<RoleDto> getRole(RoleDto queryObject) throws InvalidRoleInternalException {
                if (counter == 0) {
                    counter++;
                    return Optional.empty();
                }
                return super.getRole(queryObject);
            }
        };
    }

    private DatabaseServiceImpl databaseServiceReturningEmpty() {
        return new DatabaseServiceImpl(localDynamo) {
            @Override
            public Optional<RoleDto> getRole(RoleDto queryObject) {
                return Optional.empty();
            }
        };
    }

    private <T> GatewayResponse<T> sendRequest(RoleDto build) throws IOException {
        InputStream input = new HandlerRequestBuilder<RoleDto>(objectMapper)
            .withBody(build)
            .build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        addRoleHandler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }
}