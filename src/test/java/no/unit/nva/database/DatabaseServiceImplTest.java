package no.unit.nva.database;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import java.util.stream.Stream;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.utils.EntityUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceImplTest extends DatabaseAccessor {

    public static final String SOME_INSTITUTION = "someInstitution";
    public static final String SOME_USERNAME = "someUsername";

    private UserDto someUser;
    private DatabaseServiceImpl databaseService;

    @BeforeEach
    public void init() throws InvalidEntryInternalException {

        someUser = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        databaseService = new DatabaseServiceImpl(initializeTestDatabase(), envWithTableName);
    }

    @Test
    public void getUserThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() {

        UserDb userWithoutUsername = new UserDb();
        userWithoutUsername.setInstitution(SOME_INSTITUTION);

        DatabaseService service = mockServiceReceivingInvalidUserDbInstance();

        Executable action = () -> service.getUser(someUser);
        IllegalStateException exception = assertThrows(IllegalStateException.class, action);

        String expectedMessageContent = DatabaseServiceImpl.INVALID_ENTRY_IN_DATABASE_ERROR;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    @Test
    public void getRoleThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() {

        DatabaseService service = mockServiceReceivingInvalidRoleDbInstance();

        Executable action = () -> service.getUser(someUser);
        IllegalStateException exception = assertThrows(IllegalStateException.class, action);

        String expectedMessageContent = DatabaseServiceImpl.INVALID_ENTRY_IN_DATABASE_ERROR;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    private DatabaseService mockServiceReceivingInvalidUserDbInstance() {
        UserDb userWithoutUsername = new UserDb();
        PaginatedQueryList<UserDb> response = mockResponseFromDynamoMapper(userWithoutUsername);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidUser(response);
        return new DatabaseServiceImpl(mockMapper);
    }

    private DatabaseService mockServiceReceivingInvalidRoleDbInstance() {
        RoleDb roleWithoutName = new RoleDb();
        PaginatedQueryList<RoleDb> response = mockResponseFromDynamoMapper(roleWithoutName);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidRole(response);
        return new DatabaseServiceImpl(mockMapper);
    }

    @Test
    void getRoleLogsWarningWhenNotFoundExceptionIsThrown() throws InvalidEntryInternalException {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        RoleDto nonExistingRole = EntityUtils.createRole(EntityUtils.SOME_ROLENAME);
        attempt(() -> databaseService.getRole(nonExistingRole));
        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.ROLE_NOT_FOUND_MESSAGE));
    }

    @Test
    void getUserLogsWarningWhenNotFoundExceptionIsThrown() {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        UserDto nonExistingUser = someUser;
        attempt(() -> databaseService.getUser(nonExistingUser));
        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.USER_NOT_FOUND_MESSAGE));
    }

    @Test
    void addRoleLogsErrorWhenIllegalStateExceptionIsThrown()
        throws InvalidEntryInternalException {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        RoleDto inputRole = EntityUtils.createRole(EntityUtils.SOME_ROLENAME);

        DatabaseService databaseService = mockServiceReceivingInvalidRoleDbInstance();
        attempt(() -> addRole(databaseService, inputRole));

        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.INVALID_ENTRY_IN_DATABASE_ERROR));
    }

    @Test
    void addUserLogsErrorWhenIllegalStateExceptionIsThrown() {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        UserDto inputUser = someUser;

        DatabaseService databaseService = mockServiceReceivingInvalidUserDbInstance();
        attempt(() -> addUser(databaseService, inputUser));

        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.INVALID_ENTRY_IN_DATABASE_ERROR));
    }

    private Void addUser(DatabaseService databaseService, UserDto inputUser)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        databaseService.addUser(inputUser);
        return null;
    }

    private Void addRole(DatabaseService databaseService, RoleDto inputRole)
        throws ConflictException, InvalidInputException, InvalidEntryInternalException {
        databaseService.addRole(inputRole);
        return null;
    }

    @Test
    void addUserLogsWarningWhenIllegalStateExceptionisThrown() {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        UserDto nonExistingUser = someUser;
        attempt(() -> databaseService.getUser(nonExistingUser));
        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.INVALID_ENTRY_IN_DATABASE_ERROR));
    }

    private DynamoDBMapper mockDynamoMapperReturningInvalidUser(PaginatedQueryList<UserDb> response) {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.query(any(Class.class), any(DynamoDBQueryExpression.class)))
            .thenReturn(response);
        return mockMapper;
    }

    private DynamoDBMapper mockDynamoMapperReturningInvalidRole(PaginatedQueryList<RoleDb> response) {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.query(any(Class.class), any(DynamoDBQueryExpression.class)))
            .thenReturn(response);
        return mockMapper;
    }

    private PaginatedQueryList<UserDb> mockResponseFromDynamoMapper(UserDb userWithoutUsername) {
        PaginatedQueryList<UserDb> response = mock(PaginatedQueryList.class);
        when(response.stream()).thenReturn(Stream.of(userWithoutUsername));
        return response;
    }

    private PaginatedQueryList<RoleDb> mockResponseFromDynamoMapper(RoleDb roleWithoutName) {
        PaginatedQueryList<RoleDb> response = mock(PaginatedQueryList.class);
        when(response.stream()).thenReturn(Stream.of(roleWithoutName));
        return response;
    }
}