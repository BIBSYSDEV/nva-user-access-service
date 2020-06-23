package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.DataHandlingError;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.UnexpectedException;
import no.unit.nva.model.RoleDto;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class AddRoleHandler extends ApiGatewayHandler<RoleDto, RoleDto> {

    public static final String INTERRUPTION_ERROR = "Interuption while waiting to get role.";
    public static final String ERROR_FETCHING_SAVED_ROLE = "Could not fetch role with name: ";
    private static final int MAX_EFFORTS_FOR_FETCHING_ROLE = 2;
    private static final long WAITING_TIME = 100;
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
        return getEventuallyConsistentRole(input)
            .orElseThrow(() -> new DataHandlingError(ERROR_FETCHING_SAVED_ROLE + input.getRoleName()));
    }

    @Override
    protected Integer getSuccessStatusCode(RoleDto input, RoleDto output) {
        return HttpStatus.SC_OK;
    }

    private Optional<RoleDto> getEventuallyConsistentRole(RoleDto input)
        throws InvalidRoleInternalException, UnexpectedException {
        Optional<RoleDto> role = databaseService.getRole(input);
        int counter = 0;
        while (role.isEmpty() && counter < MAX_EFFORTS_FOR_FETCHING_ROLE) {
            waitForEventualConsistency();
            role = databaseService.getRole(input);
            counter++;
        }
        return role;
    }

    private void waitForEventualConsistency() throws UnexpectedException {
        try {
            Thread.sleep(WAITING_TIME);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTION_ERROR, e);
            throw new UnexpectedException(INTERRUPTION_ERROR, e);
        }
    }
}
