package no.unit.nva.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import java.util.stream.Stream;
import no.unit.nva.database.exceptions.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceImplTest {

    @Test
    public void getUserThrowsIllegalStateExceptionWhenItReceivesInvalidUserFromDatabase() throws InvalidUserException {

        UserDb userWithoutUsername = new UserDb();
        userWithoutUsername.setInstitution("someInstitution");

        PaginatedQueryList<UserDb> response = mockResponseFromDynamoMapper(userWithoutUsername);
        DynamoDBMapper mockMapper = mockDynamoMapperReturningInvalidUser(response);
        DatabaseService service = new DatabaseServiceImpl(mockMapper);

        Executable action = () -> service.getUser("someUsername");
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

    private PaginatedQueryList<UserDb> mockResponseFromDynamoMapper(UserDb userWithoutUsername) {
        PaginatedQueryList<UserDb> response = mock(PaginatedQueryList.class);
        when(response.stream()).thenReturn(Stream.of(userWithoutUsername));
        return response;
    }
}