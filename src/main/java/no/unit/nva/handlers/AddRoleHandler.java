package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.exceptions.DataHandlingError;
import no.unit.nva.database.exceptions.InvalidInputRoleException;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.UnexpectedException;
import no.unit.nva.model.RoleDto;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class AddRoleHandler extends ApiGatewayHandler<RoleDto, RoleDto> {

    private static final int EFFORTS_RETRIEVING_SAVED_ROLE = 2;
    private static final long WAITING_TIME = 100;
    public static final String INTERRPTION_ERROR = "Interuption while waiting to get role.";
    public static final String ERROR_RETRIEVING_SAVED_ROLE = "Could not retrieve role with name: ";
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
        throws DataHandlingError, UnexpectedException, InvalidInputRoleException, InvalidRoleException {
        databaseService.addRole(input);
        return getEventuallyConsistentRole(input)
            .orElseThrow(() -> new DataHandlingError(ERROR_RETRIEVING_SAVED_ROLE + input.getRoleName()));
    }

    private Optional<RoleDto> getEventuallyConsistentRole(RoleDto input)
        throws InvalidRoleException, UnexpectedException {
        Optional<RoleDto> role = databaseService.getRole(input);
        for (int i = 0; keepQuerying(i, role); i++) {
            waitForEventualConsistency();
            role = databaseService.getRole(input);
        }
        return role;
    }

    private void waitForEventualConsistency() throws UnexpectedException {
        try {
            Thread.sleep(WAITING_TIME);
        } catch (InterruptedException e) {
            logger.error(INTERRPTION_ERROR, e);
            throw new UnexpectedException(INTERRPTION_ERROR, e);
        }
    }

    private boolean keepQuerying(int i, Optional<RoleDto> role) {
        return role.isEmpty() && i < EFFORTS_RETRIEVING_SAVED_ROLE;
    }

    @Override
    protected Integer getSuccessStatusCode(RoleDto input, RoleDto output) {
        return HttpStatus.SC_OK;
    }
}