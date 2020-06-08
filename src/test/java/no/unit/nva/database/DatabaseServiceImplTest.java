package no.unit.nva.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import java.util.Optional;
import java.util.stream.Stream;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceImplTest extends DatabaseTest {

    public static final String SOME_INSTITUTION = "someInstitution";
    public static final String SOME_USERNAME = "someUsername";
    private DynamoDBMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new DynamoDBMapper(initializeTestDatabase());
    }

    @Test
    public void getUserThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() {

        UserDb userWithoutUsername = new UserDb();
        userWithoutUsername.setInstitution(SOME_INSTITUTION);

        PaginatedQueryList<UserDb> response = mockResponseFromDynamoMapper(userWithoutUsername);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidUser(response);
        DatabaseService service = new DatabaseServiceImpl(mockMapper);

        Executable action = () -> service.getUser(SOME_USERNAME);
        IllegalStateException exception = assertThrows(IllegalStateException.class, action);

        String expectedMessageContent = DatabaseServiceImpl.INVALID_USER_IN_DATABASE;
        assertThat(exception.getMessage(), containsString(expectedMessageContent));
    }

    @Test
    public void getUserReturnsEmptyOptionalWhenUserIsNotFound() throws InvalidUserException {
        DatabaseService service = new DatabaseServiceImpl(mapper);
        Optional<UserDto> result = service.getUser(SOME_USERNAME);
        assertThat(result.isEmpty(), is(equalTo(true)));
    }

    @SuppressWarnings("unchecked")
    private DynamoDBMapper mockDynamoMapperReturningInvalidUser(PaginatedQueryList<UserDb> response) {
        DynamoDBMapper mockMapper = mock(DynamoDBMapper.class);
        when(mockMapper.query(any(Class.class), any(DynamoDBQueryExpression.class)))
            .thenReturn(response);
        return mockMapper;
    }

    @SuppressWarnings("unchecked")
    private PaginatedQueryList<UserDb> mockResponseFromDynamoMapper(UserDb userWithoutUsername) {
        PaginatedQueryList<UserDb> response = mock(PaginatedQueryList.class);
        when(response.stream()).thenReturn(Stream.of(userWithoutUsername));
        return response;
    }
}