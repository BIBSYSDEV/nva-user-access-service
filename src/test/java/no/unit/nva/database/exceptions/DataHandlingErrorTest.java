package no.unit.nva.database.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DataHandlingErrorTest {

    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void dataHandlingErrorHasConstructorWithMessage() {
        Executable action = () -> {
            throw new DataHandlingError(SOME_MESSAGE);
        };
        DataHandlingError exception = assertThrows(DataHandlingError.class, action);
        assertThat(exception.getMessage(), containsString(SOME_MESSAGE));
    }

    @Test
    public void dataHandlingErrorReturnsInternalServerError() {
        DataHandlingError error = new DataHandlingError(SOME_MESSAGE);
        assertThat(error.statusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }
}