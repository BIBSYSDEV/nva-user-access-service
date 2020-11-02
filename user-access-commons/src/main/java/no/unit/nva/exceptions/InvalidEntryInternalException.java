package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InvalidEntryInternalException extends ApiGatewayException {

    public InvalidEntryInternalException(String message) {
        super(message);
    }

    public InvalidEntryInternalException(Exception exception) {
        super(exception);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
