package no.unit.nva.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.interfaces.WithEnvironment;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GetRoleHandlerTest extends DatabaseAccessor implements WithEnvironment {

    public static final String THE_ROLE = "theRole";
    public static final String BLANK_STR = " ";
    private DatabaseServiceImpl databaseService;
    private GetRoleHandler getRoleHandler;
    private Context context;

    /**
     * init.
     */
    @BeforeEach
    public void init() {
        databaseService = createDatabaseServiceUsingLocalStorage();
        getRoleHandler = new GetRoleHandler(mockEnvironment(), databaseService);
        context = mock(Context.class);
    }

    @Test
    public void statusCodeReturnsOkWhenRequestIsSuccessful() {
        Integer successCode = getRoleHandler.getSuccessStatusCode(null, null);
        assertThat(successCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @DisplayName("processInput returns RoleDto when a role with the input role-name exists")
    @Test
    void processInputReturnsRoleDtoWhenARoleWithTheInputRoleNameExists()
        throws ApiGatewayException {
        addSampleRoleToDatabase();
        RequestInfo requestInfo = queryWithRoleName(THE_ROLE);
        RoleDto roleDto = getRoleHandler.processInput(null, requestInfo, context);
        assertThat(roleDto.getRoleName(), is(equalTo(THE_ROLE)));
    }

    @DisplayName("processInput() throws NotFoundException when there is no role with the input role-name")
    @Test
    void processInputThrowsNotFoundExceptionWhenThereIsNoRoleInTheDatabaseWithTheSpecifiedRoleName() {
        RequestInfo requestInfo = queryWithRoleName(THE_ROLE);
        Executable action = () -> getRoleHandler.processInput(null, requestInfo, context);
        NotFoundException exception = assertThrows(NotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(THE_ROLE));
    }

    @Test
    void processInputThrowsBadRequestExceptionWhenNoRoleNameIsProvided() {
        RequestInfo requestInfoWithoutRoleName = new RequestInfo();
        Executable action = () -> getRoleHandler.processInput(null, requestInfoWithoutRoleName, context);
        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertThat(exception.getMessage(), containsString(GetRoleHandler.EMPTY_ROLE_NAME));
    }

    @Test
    void processInputThrowsBadRequestExceptionWhenBlankRoleNameIsProvided() {
        RequestInfo requestInfoWithBlankRoleName = queryWithRoleName(BLANK_STR);
        Executable action = () -> getRoleHandler.processInput(null, requestInfoWithBlankRoleName, context);
        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertThat(exception.getMessage(), containsString(GetRoleHandler.EMPTY_ROLE_NAME));
    }

    private RequestInfo queryWithRoleName(String roleName) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.getPathParameters().put(GetRoleHandler.ROLE_PATH_PARAMETER, roleName);
        return requestInfo;
    }

    private void addSampleRoleToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        RoleDto existingRole = RoleDto.newBuilder().withName(GetRoleHandlerTest.THE_ROLE).build();
        databaseService.addRole(existingRole);
    }
}