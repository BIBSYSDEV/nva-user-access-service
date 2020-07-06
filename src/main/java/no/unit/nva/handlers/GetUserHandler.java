package no.unit.nva.handlers;

import static java.util.function.Predicate.not;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.UserDto;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class GetUserHandler extends ApiGatewayHandler<Void, UserDto> {

    public static final String USERNAME_PATH_PARAMETER = "username";
    public static final String EMPTY_USERNAME_PATH_PARAMETER_ERROR = "Path parameter \"username\" cannot be empty";
    public static final String USER_NOT_FOUND_MESSAGE = "Could not find user with username:";
    private final DatabaseService databaseService;

    /**
     * Default constructor.
     */
    @JacocoGenerated
    public GetUserHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public GetUserHandler(Environment environment,
                          DatabaseService databaseService) {
        super(Void.class, environment, LoggerFactory.getLogger(GetUserHandler.class));
        this.databaseService = databaseService;
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws BadRequestException, InvalidUserInternalException, NotFoundException {
        String username = extractValidUserNameOrThrowException(requestInfo);
        UserDto queryObject = UserDto.newBuilder().withUsername(username).build();
        return databaseService
            .getUser(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + username));
    }

    private String extractValidUserNameOrThrowException(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(requestInfo)
            .map(RequestInfo::getPathParameters)
            .map(map -> map.get(USERNAME_PATH_PARAMETER))
            .filter(not(String::isBlank))
            .orElseThrow(() -> new BadRequestException(EMPTY_USERNAME_PATH_PARAMETER_ERROR));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserDto output) {
        return HttpStatus.SC_OK;
    }
}
