package no.unit.nva.useraccessmanagement.dao.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Test;

public class InvalidEntryInternalExceptionTest {

    public static final String SOME_MESSAGE = "SomeMessage";

    @Test
    public void getStatusCodeReturnsInternalServerError() {
        InvalidEntryInternalException exception = new InvalidEntryInternalException(SOME_MESSAGE);
        assertThat(exception.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_INTERNAL_ERROR)));
    }

    @Test
    public void constructorWithCauseStoresCause() {
        Exception cause = new Exception(SOME_MESSAGE);
        InvalidEntryInternalException exception = new InvalidEntryInternalException(cause);
        Exception actualCause = (Exception) exception.getCause();
        assertThat(actualCause, is(equalTo(cause)));
    }
}