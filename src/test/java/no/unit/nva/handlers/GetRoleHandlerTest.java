package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;
import no.unit.nva.database.intefaces.WithEnvironment;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ResourceNotFoundException;
import no.unit.nva.model.RoleDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GetRoleHandlerTest extends DatabaseTest implements WithEnvironment {

    public static final String THE_ROLE = "theRole";
    private DatabaseServiceImpl databaseService;
    private GetRoleHandler getRoleHandler;
    private Context context;

    /**
     * init.
     */
    @BeforeEach
    public void init() {
        databaseService = new DatabaseServiceImpl(initializeTestDatabase());
        getRoleHandler = new GetRoleHandler(mockEnvironment(), databaseService);
        context = mock(Context.class);
    }

    @Test
    void statusCodeReturnsOkWhenRequestIsSuccessful() {
        Integer successCode = getRoleHandler.getSuccessStatusCode(null, null);
        assertThat(successCode, is(equalTo(HttpStatus.SC_OK)));
    }

    @Test
    void processInputReturnsARoleDtoWhenARoleWithTheInputRoleNameExists()
        throws ApiGatewayException {
        RoleDto existingRole = RoleDto.newBuilder().withName(THE_ROLE).build();
        databaseService.addRole(existingRole);
        RequestInfo requestInfo = queryWithRoleName();
        RoleDto roleDto = getRoleHandler.processInput(null, requestInfo, null);
        assertThat(roleDto.getRoleName(), is(equalTo(THE_ROLE)));
    }

    @Test
    void processInputThrowsNotFoundExceptionWhenThereIsNoRoleInTheDatabaseWithTheSpecifiedRolename() {
        RequestInfo requestInfo = queryWithRoleName();
        Executable action = () -> getRoleHandler.processInput(null, requestInfo, null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, action);
        assertThat(exception.getMessage(), containsString(THE_ROLE));
    }

    @Test
    void processInputLogsWarningWhenResourceNotFoundExceptionIsThrown() {
        TestAppender testAppender = LogUtils.getTestingAppender(GetRoleHandler.class);
        RequestInfo requestInfo = queryWithRoleName();
        attempt(() -> getRoleHandler.processInput(null, requestInfo, null));
        assertThat(testAppender.getMessages(), containsString(GetRoleHandler.LOG_ROLE_NOT_FOUND));
    }

    @Test
    void processInputThrowsBadRequestExceptionWhenNoRolenameIsProvided() {
        RequestInfo requestInfoWithoutRoleName = new RequestInfo();
        Executable action = () -> getRoleHandler.processInput(null, requestInfoWithoutRoleName, null);
        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertThat(exception.getMessage(), containsString(GetRoleHandler.EMPTY_ROLE_NAME));
    }

    private RequestInfo queryWithRoleName() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.getPathParameters().put(GetRoleHandler.ROLE_PATH_PARAMETER, THE_ROLE);
        return requestInfo;
    }
}