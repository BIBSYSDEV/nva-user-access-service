package no.unit.nva.database.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class ExceptionsTest {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void invalidUserExceptionReturnsInternalSeverError() {
        Integer statusCode = new InvalidUserException(SOME_MESSAGE).statusCode();
        assertThat(statusCode, is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }

    @Test
    public void invalidRoleExceptionReturnsInternalSeverError() {
        Integer statusCode = new InvalidRoleException(SOME_MESSAGE).statusCode();
        assertThat(statusCode, is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }
}