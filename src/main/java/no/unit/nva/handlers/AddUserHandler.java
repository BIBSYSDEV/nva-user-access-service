package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.DataSyncException;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class AddUserHandler extends HandlerWithEventualConsistency<UserDto, UserDto> {


    public static final String SYNC_ERROR_MESSAGE = "Error while trying to retrieve saved user:";
    private final DatabaseService databaseService;

    /**
     * Default constructor.
     */
    @JacocoGenerated
    public AddUserHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public AddUserHandler(Environment environment,
                          DatabaseService databaseService) {
        super(UserDto.class, environment, LoggerFactory.getLogger(AddUserHandler.class));
        this.databaseService = databaseService;
    }

    @Override
    protected UserDto processInput(UserDto input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        databaseService.addUser(input.validate());

        return getEventuallyConsistent(() -> getUser(input))
            .orElseThrow(() -> new DataSyncException(SYNC_ERROR_MESSAGE + input.getUsername()));
    }

    private Optional<UserDto> getUser(UserDto input) {
        return attempt(() -> databaseService.getUser(input))
            .orElseThrow(fail -> unexpectedFailure(fail.getException().getMessage(), fail.getException()));
    }

    @Override
    protected Integer getSuccessStatusCode(UserDto input, UserDto output) {
        return HttpStatus.SC_OK;
    }
}
