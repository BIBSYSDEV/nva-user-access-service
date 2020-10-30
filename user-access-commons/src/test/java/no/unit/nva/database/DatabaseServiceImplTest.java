package no.unit.nva.database;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
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
    public static final String EXPECTED_EXCEPTION_MESSAGE = "ExpectedExceptionMessage";

    private UserDto someUser;
    private DatabaseServiceImpl databaseService;

    @BeforeEach
    public void init() throws InvalidEntryInternalException {

        someUser = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        databaseService = new DatabaseServiceImpl(initializeTestDatabase(), envWithTableName);
    }

    @Test
    public void getUserThrowsInvalidEntryInternalExceptionWhenItReceivesInvalidUserFromDatabase() {

        UserDb userWithoutUsername = new UserDb();
        userWithoutUsername.setInstitution(SOME_INSTITUTION);

        DatabaseService service = mockServiceReceivingInvalidUserDbInstance();

        Executable action = () -> service.getUser(someUser);
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);

        String expectedMessageContent = UserDto.MISSING_FIELD_ERROR;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    @Test
    public void getRoleExceptionWhenItReceivesInvalidRoleFromDatabase()
        throws InvalidEntryInternalException {

        DatabaseService serviceThrowingException = mockServiceThrowsExceptionWhenLoadingRole();
        RoleDto sampleRole = EntityUtils.createRole(EntityUtils.SOME_ROLENAME);
        Executable action = () -> serviceThrowingException.getRole(sampleRole);
        RuntimeException exception = assertThrows(RuntimeException.class, action);

        assertThat(exception.getMessage(), containsString(EXPECTED_EXCEPTION_MESSAGE));
    }

    @Test
    public void getRoleThrowsInvalidEntryInternalExceptionWhenItReceivesInvalidRoleFromDatabase()
        throws InvalidEntryInternalException {

        DatabaseService service = mockServiceReceivingInvalidRoleDbInstance();
        RoleDto sampleRole = EntityUtils.createRole(EntityUtils.SOME_ROLENAME);
        Executable action = () -> service.getRole(sampleRole);
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);

        String expectedMessageContent = RoleDto.MISSING_ROLE_NAME_ERROR;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    @Test
    public void getRoleLogsWarningWhenNotFoundExceptionIsThrown() throws InvalidEntryInternalException {
        TestAppender testAppender = LogUtils.getTestingAppender(DatabaseServiceImpl.class);
        RoleDto nonExistingRole = EntityUtils.createRole(EntityUtils.SOME_ROLENAME);
        attempt(() -> databaseService.getRole(nonExistingRole));
        assertThat(testAppender.getMessages(),
            StringContains.containsString(DatabaseServiceImpl.ROLE_NOT_FOUND_MESSAGE));
    }


    private DatabaseService mockServiceReceivingInvalidUserDbInstance() {
        UserDb userWithoutUsername = new UserDb();
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidUser(userWithoutUsername);
        return new DatabaseServiceImpl(mockMapper);
    }

    private DatabaseService mockServiceReceivingInvalidRoleDbInstance() {
        RoleDb roleWithoutName = new RoleDb();

        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidRole(roleWithoutName);
        return new DatabaseServiceImpl(mockMapper);
    }

    private DatabaseService mockServiceThrowsExceptionWhenLoadingRole() {
        DynamoDBMapper mockMapper = mockMapperThrowingException();
        return new DatabaseServiceImpl(mockMapper);
    }

    private DynamoDBMapper mockMapperThrowingException() {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.load(any())).thenAnswer(invocation -> {
            throw new AmazonDynamoDBException(EXPECTED_EXCEPTION_MESSAGE);
        });
        return mockMapper;
    }

    @SuppressWarnings("unchecked")
    private DynamoDBMapper mockDynamoMapperReturningInvalidUser(UserDb response) {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.load(any(UserDb.class))).thenReturn(response);
        return mockMapper;
    }

    @SuppressWarnings("unchecked")
    private DynamoDBMapper mockDynamoMapperReturningInvalidRole(RoleDb response) {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.load(any(RoleDb.class)))
            .thenReturn(response);
        return mockMapper;
    }
}