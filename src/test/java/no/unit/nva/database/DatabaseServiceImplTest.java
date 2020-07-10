package no.unit.nva.database;

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
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceImplTest extends DatabaseAccessor {

    public static final String SOME_INSTITUTION = "someInstitution";
    public static final String SOME_USERNAME = "someUsername";

    private UserDto someUser;

    @BeforeEach
    public void init() throws InvalidEntryInternalException {

        someUser = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
    }

    @Test
    public void getUserThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() {

        UserDb userWithoutUsername = new UserDb();
        userWithoutUsername.setInstitution(SOME_INSTITUTION);

        PaginatedQueryList<UserDb> response = mockResponseFromDynamoMapper(userWithoutUsername);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidUser(response);
        DatabaseService service = new DatabaseServiceImpl(mockMapper);

        Executable action = () -> service.getUser(someUser);
        IllegalStateException exception = assertThrows(IllegalStateException.class, action);

        String expectedMessageContent = DatabaseServiceImpl.INVALID_USER_IN_DATABASE;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    @Test
    public void getRoleThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() {

        RoleDb roleWithoutName = new RoleDb();

        PaginatedQueryList<RoleDb> response = mockResponseFromDynamoMapper(roleWithoutName);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidRole(response);
        DatabaseService service = new DatabaseServiceImpl(mockMapper);

        Executable action = () -> service.getUser(someUser);
        IllegalStateException exception = assertThrows(IllegalStateException.class, action);

        String expectedMessageContent = DatabaseServiceImpl.INVALID_USER_IN_DATABASE;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
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