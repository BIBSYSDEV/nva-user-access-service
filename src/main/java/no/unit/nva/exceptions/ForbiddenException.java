package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class ForbiddenException extends ApiGatewayException {

    public static final String DEFAULT_ERROR_MESSAGE = "Forbidden";

    public ForbiddenException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_FORBIDDEN;
    }
}
