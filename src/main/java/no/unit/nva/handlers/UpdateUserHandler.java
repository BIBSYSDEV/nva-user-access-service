package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.DataSyncException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateUserHandler extends HandlerWithEventualConsistency<UserDto, UserDto> {

    public static final String USER_ID_FIELD_NAME = "username";
    private static final String ERROR_FETCHING_SAVED_USER = "Could not fetch saved user: ";
    private final DatabaseService databaseService;

    public UpdateUserHandler() {
        this(new Environment(),
            new DatabaseServiceImpl());
    }

    protected UpdateUserHandler(Environment environment, DatabaseService databaseService) {
        super(UserDto.class, environment, createLogger());
        this.databaseService = databaseService;
    }

    @Override
    protected UserDto processInput(UserDto input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        databaseService.updateUser(input);
        return getEventuallyConsistent(() -> getUser(input))
            .orElseThrow(() -> new DataSyncException(ERROR_FETCHING_SAVED_USER + input.getUsername()));
    }

    private UserDto getUser(UserDto input) throws NotFoundException, InvalidEntryInternalException {
        return databaseService.getUser(input);
    }

    @Override
    protected Integer getSuccessStatusCode(UserDto input, UserDto output) {
        return HttpStatus.SC_OK;
    }

    private static Logger createLogger() {
        return LoggerFactory.getLogger(UpdateUserHandler.class);
    }
}
