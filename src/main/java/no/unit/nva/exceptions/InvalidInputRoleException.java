package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InvalidInputRoleException extends ApiGatewayException {

    public InvalidInputRoleException(String message) {
        super(message);
    }

    public InvalidInputRoleException(Exception exception) {
        super(exception);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_BAD_REQUEST;
    }
}
