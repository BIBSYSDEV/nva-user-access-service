package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.exceptions.DataSyncException;
import no.unit.nva.useraccessmanagement.dao.exceptions.InvalidEntryInternalException;
import no.unit.nva.useraccessmanagement.model.RoleDto;
import no.unit.nva.useraccessmanagement.model.exceptions.InvalidInputException;
import nva.commons.exceptions.commonexceptions.ConflictException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.LoggerFactory;

public class AddRoleHandler extends HandlerWithEventualConsistency<RoleDto, RoleDto> {

    public static final String ERROR_FETCHING_SAVED_ROLE = "Could not fetch role with name: ";
    private final DatabaseService databaseService;

    /**
     * Default constructor.
     */
    @JacocoGenerated
    public AddRoleHandler() {
        this(new Environment(), new DatabaseServiceImpl()
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
        throws DataSyncException, ConflictException, InvalidInputException, InvalidEntryInternalException {
        databaseService.addRole(input);
        return getEventuallyConsistent(() -> getRole(input))
            .orElseThrow(() -> new DataSyncException(ERROR_FETCHING_SAVED_ROLE + input.getRoleName()));
    }

    private RoleDto getRole(RoleDto input) throws NotFoundException, InvalidEntryInternalException {
        return databaseService.getRole(input);
    }

    @Override
    protected Integer getSuccessStatusCode(RoleDto input, RoleDto output) {
        return HttpURLConnection.HTTP_OK;
    }
}
