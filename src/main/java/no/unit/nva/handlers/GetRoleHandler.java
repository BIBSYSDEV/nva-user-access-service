package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import java.util.function.Supplier;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.ResourceNotFoundException;
import no.unit.nva.model.RoleDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRoleHandler extends ApiGatewayHandler<Void, RoleDto> {

    public static final String LOG_ROLE_NOT_FOUND = "Could not find role:";
    public static final String EMPTY_ROLE_NAME = "Role-name cannot be empty";
    public static String ROLE_NOT_FOUND_ERROR_MESSAGE = "Could not find role:";
    public static final String ROLE_PATH_PARAMETER = "role";
    private final DatabaseService databaseService;
    private static final Logger logger = LoggerFactory.getLogger(GetRoleHandler.class);

    public GetRoleHandler() {
        this(new Environment(), new DatabaseServiceImpl());
    }

    public GetRoleHandler(Environment environment, DatabaseService databaseService
    ) {
        super(Void.class, environment, logger);
        this.databaseService = databaseService;
    }

    @Override
    protected RoleDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        String rolename = Optional.ofNullable(requestInfo.getPathParameters().get(ROLE_PATH_PARAMETER))
            .orElseThrow(() -> new BadRequestException(EMPTY_ROLE_NAME));

        Optional<RoleDto> searchResult = databaseService.getRole(RoleDto.newBuilder().withName(rolename).build());
        return searchResult.orElseThrow(roleNotFound(rolename));
    }

    private Supplier<ResourceNotFoundException> roleNotFound(String rolename) {
        logger.warn(LOG_ROLE_NOT_FOUND + rolename);
        return () -> new ResourceNotFoundException(ROLE_NOT_FOUND_ERROR_MESSAGE + rolename);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, RoleDto output) {
        return HttpStatus.SC_OK;
    }
}
