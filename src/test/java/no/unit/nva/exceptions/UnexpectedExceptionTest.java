package no.unit.nva.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class UnexpectedExceptionTest {

    public static final String SOME_MESSAGE = "Some message";
    public static final String INTERNAL_EXCEPTION_MESSAGE = "internal message";
    public static final Exception SOME_EXCEPTION = new Exception(INTERNAL_EXCEPTION_MESSAGE);

    @Test
    public void unexpectedExceptionHasConstructorWithMessageAndException() {
        Executable action = () -> {
            throw new UnexpectedException(SOME_MESSAGE, SOME_EXCEPTION);
        };
        UnexpectedException exception = assertThrows(UnexpectedException.class, action);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
    }

    @Test
    public void unexpectedExceptionMessageContainsBothOwnMessageAndInternalExceptionMessage() {
        Executable action = () -> {
            throw new UnexpectedException(SOME_MESSAGE, SOME_EXCEPTION);
        };
        UnexpectedException exception = assertThrows(UnexpectedException.class, action);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
        String stackTrace = getStackTraceString(exception);
        assertThat(stackTrace, containsString(INTERNAL_EXCEPTION_MESSAGE));
    }

    @Test
    public void unexpectedExceptionReturnsInternalServerError() {
        UnexpectedException error = new UnexpectedException(SOME_MESSAGE, SOME_EXCEPTION);
        assertThat(error.statusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }

    private String getStackTraceString(UnexpectedException exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);

        return stringWriter.toString();
    }
}