package no.unit.nva.exceptions;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class DataHandlingError extends ApiGatewayException {

    public DataHandlingError(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
