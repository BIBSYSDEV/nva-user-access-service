package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.DataHandlingError;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.UnexpectedException;
import no.unit.nva.model.RoleDto;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class AddRoleHandler extends HandlerWithEventualConsistency<RoleDto, RoleDto> {

    public static final String ERROR_FETCHING_SAVED_ROLE = "Could not fetch role with name: ";
    public static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error while trying to access role";
    private final DatabaseService databaseService;

    /**
     * Default constructor.
     */
    @JacocoGenerated
    public AddRoleHandler() {
        this(new Environment(),
            new DatabaseServiceImpl()
        );
    }

    public AddRoleHandler(
        Environment environment,
        DatabaseService databaseService
    ) {
        super(RoleDto.class, environment, LoggerFactory.getLogger(AddRoleHandler.class));
        this.databaseService = databaseService;
    }

    @Override
    protected RoleDto processInput(RoleDto input, RequestInfo requestInfo, Context context)
        throws DataHandlingError, UnexpectedException, InvalidInputRoleException, InvalidRoleInternalException {
        databaseService.addRole(input);
        return getEventuallyConsistent(() -> getRole(input))
            .orElseThrow(() -> new DataHandlingError(ERROR_FETCHING_SAVED_ROLE + input.getRoleName()));
    }

    private Optional<RoleDto> getRole(RoleDto input) {
        return attempt(() -> databaseService.getRole(input))
            .orElseThrow(fail -> unexpectedFailure(UNEXPECTED_ERROR_MESSAGE, fail.getException()));
    }

    @Override
    protected Integer getSuccessStatusCode(RoleDto input, RoleDto output) {
        return HttpStatus.SC_OK;
    }
}
