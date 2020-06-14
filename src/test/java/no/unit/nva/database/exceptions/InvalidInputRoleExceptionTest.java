package no.unit.nva.database.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class InvalidInputRoleExceptionTest {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void invalidInputRoleExceptionHasConstructorWithMessage() {
        Executable action = () -> {
            throw new InvalidRoleException(SOME_MESSAGE);
        };
        InvalidRoleException exception = assertThrows(InvalidRoleException.class, action);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
    }

    @Test
    public void invalidInputRoleExceptionrReturnsBadRequest() {
        InvalidRoleException error = new InvalidRoleException(SOME_MESSAGE);
        assertThat(error.statusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
    }
}