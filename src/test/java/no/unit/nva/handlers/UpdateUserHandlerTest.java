package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Collections;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateUserHandlerTest extends DatabaseAccessor {

    public static final String SAMPLE_ROLE = "someRole";
    public static final String SAMPLE_USERNAME = "some@somewhere";
    public static final String SAMPLE_INSTITUTION = "somewhere";
    public static final String ANOTHER_ROLE = "ANOTHER_ROLE";
    private DatabaseServiceImpl databaseService;
    private Context context;

    @BeforeEach
    public void init() {
        databaseService = new DatabaseServiceImpl(initializeTestDatabase(), envWithTableName);
        context = mock(Context.class);
    }

    @DisplayName("processInput() returns updated user when path contains an exisitng user id, "
        + "body contains a valid UserDto and path id is the same as the body id ")
    @Test
    public void processInputReturnsUpdatedUserWhenPathAndBodyContainTheSameUserIdAndTheIdExistsAndBodyIsValid()
        throws ApiGatewayException {
        UserDto existingUser = storeInitialUserDirectlyToDatabase(createSampleUser());

        UpdateUserHandler updateUserHandler = new UpdateUserHandler(envWithTableName, databaseService);

        UserDto userUpdate = updateUser(existingUser);
        RequestInfo requestInfo = createRequestInfoForQuery(existingUser);
        UserDto actualUser = updateUserHandler.processInput(userUpdate, requestInfo, context);

        assertThat(actualUser, is(equalTo(userUpdate)));
    }

    private RequestInfo createRequestInfoForQuery(UserDto existingUser) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setPathParameters(
            Collections.singletonMap(UpdateUserHandler.USER_ID_FIELD_NAME, existingUser.getUsername()));
        return requestInfo;
    }

    private UserDto storeInitialUserDirectlyToDatabase(UserDto userDto)
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

    private UserDto updateUser(UserDto userDto) throws InvalidEntryInternalException {
        RoleDto someOtherRole = RoleDto.newBuilder().withName(ANOTHER_ROLE).build();
        return updateRoleList(userDto, someOtherRole);
    }

    private UserDto updateRoleList(UserDto userDto, RoleDto someOtherRole) throws InvalidEntryInternalException {
        return userDto.copy().withRoles(Collections.singletonList(someOtherRole)).build();
    }
}