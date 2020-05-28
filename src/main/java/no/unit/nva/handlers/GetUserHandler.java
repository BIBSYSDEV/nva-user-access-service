package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.database.UserDb;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends ApiGatewayHandler<Void, UserDb> {

    public GetUserHandler() {
        this(new Environment(), LoggerFactory.getLogger(GetUserHandler.class));
    }

    public GetUserHandler(Environment environment, Logger logger) {
        super(Void.class, environment, logger);
    }

    @Override
    protected UserDb processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDb output) {
        return null;
    }
}
