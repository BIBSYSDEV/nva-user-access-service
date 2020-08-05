package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.Collections;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.TypedObjectsDetails;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;

public class HandlerTest extends DatabaseAccessor {

    public static final String DEFAULT_USERNAME = "someUsername@inst";
    public static final String DEFAULT_ROLE = "SomeRole";
    public static final String DEFAULT_INSTITUTION = "SomeInstitution";

    protected DatabaseService databaseService;

    protected UserDto insertSampleUserToDatabase(String username, String institution)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto sampleUser = createSampleUser(username, institution);
        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    protected UserDto insertSampleUserToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto sampleUser = createSampleUser(DEFAULT_USERNAME, DEFAULT_INSTITUTION);
        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    protected UserDto createSampleUser(String username, String institution) throws InvalidEntryInternalException {
        RoleDto someRole = RoleDto.newBuilder().withName(DEFAULT_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(username)
            .withRoles(Collections.singletonList(someRole))
            .withInstitution(institution)
            .build();
    }

    protected <T> InputStream createRequestInputStream(T bodyObject)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<T>(objectMapper)
            .withBody(bodyObject)
            .build();
    }

    protected <I> ObjectNode createInputObjectWithoutType(I dtoObject) {
        ObjectNode objectWithoutType = objectMapper.convertValue(dtoObject, ObjectNode.class);
        objectWithoutType.remove(TypedObjectsDetails.TYPE_ATTRIBUTE);
        return objectWithoutType;
    }
}
