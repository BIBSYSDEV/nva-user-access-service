package no.unit.nva.handlers;

import static no.unit.nva.database.DatabaseServiceImpl.USER_NOT_FOUND_MESSAGE;
import static no.unit.nva.handlers.UpdateUserHandler.INCONSISTENT_USERNAME_IN_PATH_AND_OBJECT_ERROR;
import static no.unit.nva.handlers.UpdateUserHandler.LOCATION_HEADER;
import static no.unit.nva.handlers.UpdateUserHandler.NO_USERS_PATH_PARAMETER_FOUND_ERROR_MESSAGE;
import static no.unit.nva.handlers.UpdateUserHandler.USERNAME_PATH_PARAMETER;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.EntityUtils;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.GatewayResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

public class UpdateUserHandlerTest extends DatabaseAccessor {

    public static final String SAMPLE_ROLE = "someRole";
    public static final String SAMPLE_USERNAME = "some@somewhere";
    public static final String SAMPLE_INSTITUTION = "somewhere";
    public static final String ANOTHER_ROLE = "ANOTHER_ROLE";
    public static final String SOME_OTHER_USERNAME = "SomeOtherUsername";
    private DatabaseServiceImpl databaseService;
    private Context context;

    private ByteArrayOutputStream output;

    @BeforeEach
    public void init() {
        databaseService = new DatabaseServiceImpl(initializeTestDatabase(), envWithTableName);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
    }

    @DisplayName("handleRequest() returns Location header with the URI to updated user when path contains "
        + "an existing user id, and the body contains a valid UserDto and path id is the same as the body id ")
    @Test
    public void processInputReturnsUpdatedUserWhenPathAndBodyContainTheSameUserIdAndTheIdExistsAndBodyIsValid()
        throws ApiGatewayException, IOException {

        UserDto existingUser = storeUserInDatabase(createSampleUser());
        UserDto userUpdate = createUserUpdate(existingUser);

        GatewayResponse<Void> gatewayResponse = sendUpdateRequest(userUpdate.getUsername(), userUpdate);

        Map<String, String> responseHeaders = gatewayResponse.getHeaders();

        assertThat(responseHeaders, hasKey(LOCATION_HEADER));

        String expectedLocationPath = UpdateUserHandler.USERS_RELATIVE_PATH + userUpdate.getUsername();
        assertThat(responseHeaders.get(LOCATION_HEADER), is(equalTo(expectedLocationPath)));
    }

    @DisplayName("handleRequest() returns 202 (Accepted)  when path contains "
        + "an existing user id, and the body contains a valid UserDto and path id is the same as the body id ")
    @Test
    public void processInputReturnsAcceptedWhenPathAndBodyContainTheSameUserIdAndTheIdExistsAndBodyIsValid()
        throws ApiGatewayException, IOException {

        UserDto existingUser = storeUserInDatabase(createSampleUser());
        UserDto userUpdate = createUserUpdate(existingUser);

        GatewayResponse<Void> gatewayResponse = sendUpdateRequest(userUpdate.getUsername(), userUpdate);

        assertThat(gatewayResponse.getStatusCode(), is(equalTo(HttpStatus.SC_ACCEPTED)));
    }

    @DisplayName("handleRequest() returns BadRequest  when path contains an Id that is different from the Id"
        + "of the input Object")
    @Test
    public void processInputReturnsBadRequestWhenPathContainsIdDifferentFromIdOfInputObject()
        throws ApiGatewayException, IOException {

        UserDto existingUser = storeUserInDatabase(createSampleUser());
        UserDto anotherExistingUser = createSampleUser().copy().withUsername(SOME_OTHER_USERNAME).build();
        storeUserInDatabase(anotherExistingUser);

        UserDto userUpdate = createUserUpdate(existingUser);

        String falseUsername = anotherExistingUser.getUsername();
        GatewayResponse<Problem> gatewayResponse = sendUpdateRequest(falseUsername, userUpdate);

        assertThat(gatewayResponse.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));

        Problem problem = gatewayResponse.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(INCONSISTENT_USERNAME_IN_PATH_AND_OBJECT_ERROR));
    }

    @DisplayName("handleRequest() returns BadRequest when input object is invalid")
    @Test
    public void processInputReturnsBadRequestWhenInputObjectIsInvalid()
        throws ApiGatewayException, IOException, NoSuchMethodException, IllegalAccessException,
               InvocationTargetException {

        UserDto existingUser = storeUserInDatabase(createSampleUser());
        UserDto userUpdate = EntityUtils.createUserWithoutUsername();

        GatewayResponse<Problem> gatewayResponse = sendUpdateRequest(existingUser.getUsername(), userUpdate);

        assertThat(gatewayResponse.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));

        Problem problem = gatewayResponse.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(UserDto.INVALID_USER_ERROR_MESSAGE));
    }

    @DisplayName("handleRequest() returns InternalServerError when handler is called without path parameter")
    @Test
    public void processInputReturnsInternalServerErrorWhenHandlerIsCalledWithoutPathParameter()
        throws ApiGatewayException, IOException {

        UserDto existingUser = storeUserInDatabase(createSampleUser());

        UserDto userUpdate = createUserUpdate(existingUser);

        GatewayResponse<Problem> gatewayResponse = sendUpdateRequestWithoutHeaders(userUpdate);

        assertThat(gatewayResponse.getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        Problem problem = gatewayResponse.getBodyObject(Problem.class);
        assertThat(problem.getDetail(),
            containsString(
                ApiGatewayHandler.MESSAGE_FOR_RUNTIME_EXCEPTIONS_HIDING_IMPLEMENTATION_DETAILS_TO_API_CLIENTS));
    }

    @DisplayName("handleRequest() returns NotFound when trying to update non existing user")
    @Test
    public void processInputReturnsNotFoundWhenHandlerWhenTryingToUpdateNonExistingUser()
        throws ApiGatewayException, IOException {

        UserDto nonExistingUser = createSampleUser();
        GatewayResponse<Problem> gatewayResponse = sendUpdateRequest(nonExistingUser.getUsername(), nonExistingUser);

        assertThat(gatewayResponse.getStatusCode(), is(equalTo(HttpStatus.SC_NOT_FOUND)));

        Problem problem = gatewayResponse.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(USER_NOT_FOUND_MESSAGE));
    }

    private <T> GatewayResponse<T> sendUpdateRequest(String userId, UserDto userUpdate)
        throws IOException {
        UpdateUserHandler updateUserHandler = new UpdateUserHandler(envWithTableName, databaseService);
        InputStream input = new HandlerRequestBuilder<UserDto>(objectMapper)
            .withPathParameters(Collections.singletonMap(USERNAME_PATH_PARAMETER, userId))
            .withBody(userUpdate)
            .build();
        updateUserHandler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private GatewayResponse<Problem> sendUpdateRequestWithoutHeaders(UserDto userUpdate)
        throws IOException {
        UpdateUserHandler updateUserHandler = new UpdateUserHandler(envWithTableName, databaseService);
        InputStream input = new HandlerRequestBuilder<UserDto>(objectMapper)
            .withBody(userUpdate)
            .build();
        updateUserHandler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private UserDto storeUserInDatabase(UserDto userDto)
        throws ConflictException, InvalidEntryInternalException, NotFoundException, InvalidInputException {
        databaseService.addUser(userDto);
        return databaseService.getUser(userDto);
    }

    private UserDto createSampleUser() throws InvalidEntryInternalException {
        RoleDto someRole = RoleDto.newBuilder().withName(SAMPLE_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(SAMPLE_USERNAME)
            .withInstitution(SAMPLE_INSTITUTION)
            .withRoles(Collections.singletonList(someRole))
            .build();
    }

    private UserDto createUserUpdate(UserDto userDto) throws InvalidEntryInternalException {
        RoleDto someOtherRole = RoleDto.newBuilder().withName(ANOTHER_ROLE).build();
        return updateRoleList(userDto, someOtherRole);
    }

    private UserDto updateRoleList(UserDto userDto, RoleDto someOtherRole) throws InvalidEntryInternalException {
        return userDto.copy().withRoles(Collections.singletonList(someOtherRole)).build();
    }
}