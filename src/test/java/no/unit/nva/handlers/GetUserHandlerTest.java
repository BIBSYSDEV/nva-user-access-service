package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Collections;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GetUserHandlerTest extends DatabaseAccessor {

    public static final String SOME_USERNAME = "sampleUsername";
    public static final String SOME_ROLE = "SomeRole";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    private static final String BLANK_STRING = " ";

    private DatabaseService databaseService;
    private RequestInfo requestInfo;
    private Context context;
    private GetUserHandler getUserHandler;

    @BeforeEach
    public void init() {
        databaseService = createDatabaseServiceUsingLocalStorage();
        getUserHandler = new GetUserHandler(envWithTableName, databaseService);
        context = mock(Context.class);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        Integer actual = getUserHandler.getSuccessStatusCode(null, null);
        assertThat(actual, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("processInput() returns UserDto when path parameter contains the username of an existing user")
    @Test
    void processInputReturnsUserDtoWhenPathParameterContainsTheUsernameOfExistingUser() throws ApiGatewayException {
        requestInfo = createRequestInfoForGetUser(SOME_USERNAME);
        UserDto expected = insertSampleUserToDatabase();
        UserDto actual = getUserHandler.processInput(null, requestInfo, context);
        assertThat(actual, is(equalTo(expected)));
    }

    @DisplayName("processInput() throws NotFoundException when path parameter is a string that is not an existing "
        + "username")
    @Test
    void processInputThrowsNotFoundExceptionWhenPathParameterIsNonExistingUsername() throws ApiGatewayException {
        requestInfo = createRequestInfoForGetUser(SOME_USERNAME);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(NotFoundException.class, action);
    }

    @DisplayName("processInput() throws BadRequestException when path parameter is a blank string")
    @Test
    void processInputThrowBadRequestExceptionWhenPathParameterIsBlank() {
        requestInfo = createRequestInfoForGetUser(BLANK_STRING);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(BadRequestException.class, action);
    }

    @DisplayName("processInput() throws BadRequestException when path parameter is null")
    @Test
    void processInputThrowBadRequestExceptionWhenPathParameterIsNull() {
        requestInfo = createRequestInfoForGetUser(null);
        Executable action = () -> getUserHandler.processInput(null, requestInfo, context);
        assertThrows(BadRequestException.class, action);
    }

    private UserDto insertSampleUserToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto sampleUser = createSampleUser();
        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    private UserDto createSampleUser() throws InvalidEntryInternalException {
        RoleDto someRole = RoleDto.newBuilder().withName(SOME_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(SOME_USERNAME)
            .withRoles(Collections.singletonList(someRole))
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    private RequestInfo createRequestInfoForGetUser(String username) {
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setPathParameters(Collections.singletonMap(GetUserHandler.USERNAME_PATH_PARAMETER, username));
        return reqInfo;
    }
}