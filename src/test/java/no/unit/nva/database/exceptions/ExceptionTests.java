package no.unit.nva.database.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class ExceptionTests {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void unexpectedExceptionReturnsInternalServerError() {
        Exception e = new Exception();
        UnexpectedException exception = new UnexpectedException(SOME_MESSAGE, e);
        assertThat(exception.getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }
}