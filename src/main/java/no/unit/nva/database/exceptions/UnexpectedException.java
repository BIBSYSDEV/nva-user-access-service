package no.unit.nva.database.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class UnexpectedException extends ApiGatewayException {

    public UnexpectedException(String message, Exception e) {
        super(e, message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
