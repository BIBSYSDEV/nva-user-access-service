package no.unit.nva.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class InvalidUserInternalExceptionTest {

    private static final String SOME_MESSAGE = "SomeMessage";

    @Test
    public void invalidUserInternalExceptionHasConstructorWithMessage() {
        Executable action = () -> {
            throw new InvalidUserInternalException(SOME_MESSAGE);
        };
        InvalidUserInternalException exception = assertThrows(InvalidUserInternalException.class, action);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
    }

    @Test
    public void invalidUserInternalExceptionReturnsBadRequest() {
        InvalidUserInternalException error = new InvalidUserInternalException(SOME_MESSAGE);
        assertThat(error.statusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }
}