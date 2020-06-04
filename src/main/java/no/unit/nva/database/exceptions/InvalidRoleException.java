package no.unit.nva.database.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InvalidRoleException extends ApiGatewayException {

    public InvalidRoleException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
