package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class EmptyUsernameException extends ApiGatewayException {

    public static final String MESSAGE = "Username cannot be empty";

    public EmptyUsernameException() {
        super(MESSAGE);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_BAD_REQUEST;
    }
}
