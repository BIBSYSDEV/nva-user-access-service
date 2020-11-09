package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.useraccessmanagement.exceptions.InvalidEntryInternalException;
import no.unit.nva.useraccessmanagement.exceptions.InvalidInputException;
import no.unit.nva.useraccessmanagement.model.RoleDto;
import no.unit.nva.useraccessmanagement.model.UserDto;
import nva.commons.exceptions.commonexceptions.ConflictException;
import nva.commons.exceptions.commonexceptions.NotFoundException;

public class HandlerTest extends DatabaseAccessor {

    public static final String DEFAULT_USERNAME = "someUsername@inst";
    public static final String DEFAULT_ROLE = "SomeRole";
    public static final String DEFAULT_INSTITUTION = "SomeInstitution";
    public static final String TYPE_ATTRIBUTE = "type";
    private static final String SPECIAL_CHARACTER = "@";
    private static final String ENCODED_SPECIAL_CHARACTER = "%40";
    protected DatabaseService databaseService;

    protected UserDto insertSampleUserToDatabase(String username, String institution)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto sampleUser = createSampleUserWithExistingRoles(username, institution);

        databaseService.addUser(sampleUser);
        return sampleUser;
    }

    protected UserDto insertSampleUserToDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        return insertSampleUserToDatabase(DEFAULT_USERNAME, DEFAULT_INSTITUTION);
    }

    protected UserDto createSampleUserWithExistingRoles(String username, String institution)
        throws InvalidEntryInternalException, NotFoundException {
        UserDto sampleUser = createSampleUser(username, institution);
        sampleUser.getRoles().forEach((this::insertRole));
        RoleDto role = databaseService.getRole(
            RoleDto.newBuilder().withName(sampleUser.getRoles().get(0).getRoleName()).build());
        assertThat(role, is(not(nullValue())));
        return sampleUser;
    }

    protected UserDto createSampleUserWithExistingRoles() throws InvalidEntryInternalException, NotFoundException {
        return createSampleUserWithExistingRoles(DEFAULT_USERNAME, DEFAULT_INSTITUTION);
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
        objectWithoutType.remove(TYPE_ATTRIBUTE);
        return objectWithoutType;
    }

    protected String encodeString(String inputContainingSpecialCharacter) {
        assertThat(inputContainingSpecialCharacter, containsString(SPECIAL_CHARACTER));
        String output = URLEncoder.encode(inputContainingSpecialCharacter, StandardCharsets.UTF_8);
        assertThat(output, containsString(ENCODED_SPECIAL_CHARACTER));
        return output;
    }

    private void insertRole(RoleDto role) {
        try {
            databaseService.addRole(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
