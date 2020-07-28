package no.unit.nva.handlers;

import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.utils.Environment;
import org.slf4j.Logger;

public abstract class HandlerAccessingUser<I, O> extends ApiGatewayHandler<I, O> {

    protected static String USERS_RELATIVE_PATH = "/users/";
    protected static String USERNAME_PATH_PARAMETER = "username";

    public static final String EMPTY_USERNAME_PATH_PARAMETER_ERROR =
        "Path parameter \"" + USERNAME_PATH_PARAMETER + "\" cannot be empty";

    public HandlerAccessingUser(Class<I> iclass, Environment environment, Logger logger) {
        super(iclass, environment, logger);
    }
}
