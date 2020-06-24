package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import java.util.function.Supplier;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.BadRequestException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRoleHandler extends ApiGatewayHandler<Void, RoleDto> {

    public static final String LOG_ROLE_NOT_FOUND = "Could not find role: ";
    public static final String EMPTY_ROLE_NAME = "Role-name cannot be empty";
    public static String ROLE_NOT_FOUND_ERROR_MESSAGE = "Could not find role: ";
    public static final String ROLE_PATH_PARAMETER = "role";
    private final DatabaseService databaseService;
    private static final Logger logger = LoggerFactory.getLogger(GetRoleHandler.class);

    /**
     * Default constructor used by AWS Lambda.
     */
    @JacocoGenerated
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
        String roleName = Optional.ofNullable(requestInfo.getPathParameters())
            .map(pathParams -> pathParams.get(ROLE_PATH_PARAMETER))
            .filter(String::isBlank)
            .orElseThrow(() -> new BadRequestException(EMPTY_ROLE_NAME));

        RoleDto searchObject = RoleDto.newBuilder().withName(roleName).build();
        Optional<RoleDto> searchResult = databaseService.getRole(searchObject);
        return searchResult.orElseThrow(roleNotFound(roleName));
    }

    private Supplier<NotFoundException> roleNotFound(String roleName) {
        logger.warn(LOG_ROLE_NOT_FOUND + roleName);
        return () -> new NotFoundException(ROLE_NOT_FOUND_ERROR_MESSAGE + roleName);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, RoleDto output) {
        return HttpStatus.SC_OK;
    }
}
