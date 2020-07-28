package no.unit.nva.handlers;

import java.util.Collections;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;

public class HandlerTest extends DatabaseAccessor {

    public static final String SOME_USERNAME = "sampleUsername";
    public static final String SOME_ROLE = "SomeRole";
    public static final String SOME_INSTITUTION = "SomeInstitution";

    protected DatabaseService databaseService;


    protected UserDto insertSampleUserToDatabase(String username,String institution)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto sampleUser = createSampleUser(username,institution);
        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    protected UserDto insertSampleUserToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto sampleUser = createSampleUser(SOME_USERNAME,SOME_INSTITUTION);
        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    protected UserDto createSampleUser(String username,String institution) throws InvalidEntryInternalException {
        RoleDto someRole = RoleDto.newBuilder().withName(SOME_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(username)
            .withRoles(Collections.singletonList(someRole))
            .withInstitution(institution)
            .build();
    }
}
