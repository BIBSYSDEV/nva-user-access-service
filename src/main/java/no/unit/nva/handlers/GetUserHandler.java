package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends ApiGatewayHandler<Void, UserDto> {

    private final DatabaseService dbService;

    public GetUserHandler() {
        this(new Environment(),
            LoggerFactory.getLogger(GetUserHandler.class),
            new DatabaseServiceImpl()
        );
    }

    public GetUserHandler(Environment environment, Logger logger, DatabaseService dbService) {
        super(Void.class, environment, logger);
        this.dbService= dbService;
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDto output) {
        return null;
    }
}
