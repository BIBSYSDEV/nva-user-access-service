package no.unit.nva.handlers;

import static no.unit.nva.handlers.AddUserHandler.SYNC_ERROR_MESSAGE;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.DataSyncException;
import no.unit.nva.exceptions.EmptyUsernameException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.GatewayResponse;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.zalando.problem.Problem;

public class AddUserTest extends DatabaseTest implements UserDtoCreator {

    public static final String EXCEPTION_MESSAGE_WHEN_GETTING_USER = "Exception when getting user";
    private AddUserHandler handler;
    private DatabaseServiceImpl databaseService;
    private RequestInfo requestInfo;
    private Context context;

    @BeforeEach
    public void init() {
        databaseService = new DatabaseServiceImpl(initializeTestDatabase());
        handler = new AddUserHandler(mockEnvironment(), databaseService);

        requestInfo = new RequestInfo();
        context = mock(Context.class);
    }

    @DisplayName("getSuccessCode returns OK")
    @Test
    public void getSuccessCodeReturnsOk() {
        Integer successCode = handler.getSuccessStatusCode(null, null);
        assertThat(successCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("processInput() adds user to database when input is a user with username and without roles")
    @Test
    public void processInputAddsUserToDatabaseWhenInputIsUserWithUsernameWithoutRoles() throws ApiGatewayException {
        UserDto expectedUser = createUserWithoutRoles();
        UserDto savedUser = handler.processInput(expectedUser, requestInfo, context);
        assertThat(savedUser, is(not(sameInstance(expectedUser))));
        assertThat(savedUser, is(equalTo(expectedUser)));
    }

    @DisplayName("processInput() adds user to database when input is a user with username and roles")
    @Test
    public void processInputAddsUserToDatabaseWhenInputIsUserWithNamesAndRoles() throws ApiGatewayException {
        UserDto expectedUser = createUserWithRoleWithoutInstitution();
        UserDto savedUser = handler.processInput(expectedUser, requestInfo, context);
        assertThat(savedUser, is(not(sameInstance(expectedUser))));
        assertThat(savedUser, is(equalTo(expectedUser)));
    }

    @DisplayName("processInput() adds user to database when input is a user with username and roles and with"
        + "institutions")
    @Test
    public void processInputAddsUserToDatabaseWhenInputIsUserWithNamesAndRolesAndInstitutions()
        throws ApiGatewayException {
        UserDto expectedUser = createUserWithRolesAndInstitution();
        UserDto savedUser = handler.processInput(expectedUser, requestInfo, context);
        assertThat(savedUser, is(not(sameInstance(expectedUser))));
        assertThat(savedUser, is(equalTo(expectedUser)));
    }

    @DisplayName("processInput() throws ConflictException when input user exists already")
    @Test
    public void processInputThrowsConflictExceptionWhenAddedUserAlreadyExists()
        throws ApiGatewayException {
        UserDto sampleUser = createUserWithRolesAndInstitution();
        addUserFirstTime(sampleUser);
        Executable action = () -> handler.processInput(sampleUser, requestInfo, context);
        assertThrows(ConflictException.class, action);
    }

    @DisplayName("processInput() throws EmptyUsernameException when input user does not have a username")
    @Test
    public void processInputThrowsEmptyUsernameExceptionWhenInputUserDoesNotHaveUsername()
        throws ApiGatewayException, NoSuchMethodException, IllegalAccessException,
               InvocationTargetException {

        UserDto userWithoutUsername = createUserWithoutUsername();

        Executable action = () -> handler.processInput(userWithoutUsername, requestInfo, context);
        assertThrows(EmptyUsernameException.class, action);
    }

    @DisplayName("handleRequest() returns BadRequest when input user does not have a username")
    @Test
    public void processInputThrowsConflictExceptionWhenInputUserDoesNotHaveUsername()
        throws ApiGatewayException, IOException, NoSuchMethodException, IllegalAccessException,
               InvocationTargetException {

        InputStream inputStream = createRequestWithUserWithoutUsername();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        handler.handleRequest(inputStream, outputStream, context);

        GatewayResponse<Problem> response = parseResponseStream(outputStream);

        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
    }

    @DisplayName("processInput() throws RuntimeException when database service throws unexpected exception "
        + "when getting user ")
    @Test
    public void processInputThrowsUnexpectedExceptionWhenDatabaseServiceThrowsUnexpectedExceptionWhenReadingUser()
        throws ApiGatewayException {
        TestAppender testAppender = LogUtils.getTestingAppender(AddUserHandler.class);
        DatabaseService databaseService = databaseServiceThrowsExceptionWhenGettingUserAfterSaving();

        UserDto sampleUser = createUserWithRolesAndInstitution();
        AddUserHandler addUserHandler = new AddUserHandler(mockEnvironment(), databaseService);
        Executable action = () -> addUserHandler.processInput(sampleUser, requestInfo, context);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getMessage(), containsString(EXCEPTION_MESSAGE_WHEN_GETTING_USER));
        assertThat(testAppender.getMessages(), containsString(EXCEPTION_MESSAGE_WHEN_GETTING_USER));
    }

    @DisplayName("processInput() throws DataSyncException when database service cannot return saved item ")
    @Test
    public void processInputThrowsDataSyncExceptionWhenDatabaseServiceCannotReturnSavedItem()
        throws ApiGatewayException {
        DatabaseService databaseService = databaseServiceReturnsAlwaysEmptyUser();

        UserDto sampleUser = createUserWithRolesAndInstitution();
        AddUserHandler addUserHandler = new AddUserHandler(mockEnvironment(), databaseService);
        Executable action = () -> addUserHandler.processInput(sampleUser, requestInfo, context);
        DataSyncException exception = assertThrows(DataSyncException.class, action);
        assertThat(exception.getMessage(), containsString(SYNC_ERROR_MESSAGE));
    }

    private DatabaseService databaseServiceThrowsExceptionWhenGettingUserAfterSaving() {
        return new DatabaseServiceImpl(localDynamo) {
            private boolean checkingIfUserExists = true;

            @Override
            public Optional<UserDto> getUser(UserDto queryObject) throws InvalidUserInternalException {
                if (!checkingIfUserExists) {
                    throw new InvalidUserInternalException(EXCEPTION_MESSAGE_WHEN_GETTING_USER);
                } else {
                    checkingIfUserExists = false;
                    return Optional.empty();
                }
            }
        };
    }

    private DatabaseService databaseServiceReturnsAlwaysEmptyUser() {
        return new DatabaseServiceImpl(localDynamo) {
            @Override
            public Optional<UserDto> getUser(UserDto queryObject) {
                return Optional.empty();
            }
        };
    }

    private GatewayResponse<Problem> parseResponseStream(ByteArrayOutputStream outputStream)
        throws IOException {
        String outputString = outputStream.toString();
        TypeReference<GatewayResponse<Problem>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(outputString, typeReference);
    }

    private void addUserFirstTime(UserDto inputUser) throws ApiGatewayException {
        handler.processInput(inputUser, requestInfo, context);
    }
}
