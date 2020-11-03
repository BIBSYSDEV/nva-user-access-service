package no.unit.nva.model;

import static no.unit.nva.database.AccessRight.APPROVE_DOI_REQUEST;
import static no.unit.nva.database.AccessRight.REJECT_DOI_REQUEST;
import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static no.unit.nva.model.UserDto.ERROR_DUE_TO_INVALID_ROLE;
import static no.unit.nva.utils.EntityUtils.SOME_ROLENAME;
import static no.unit.nva.utils.EntityUtils.SOME_USERNAME;
import static no.unit.nva.utils.EntityUtils.createUserWithRoleWithoutInstitution;
import static no.unit.nva.utils.EntityUtils.createUserWithRolesAndInstitution;
import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.database.AccessRight;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.UserDb;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.model.UserDto.Builder;
import no.unit.nva.utils.EntityUtils;
import nva.commons.utils.attempt.Try;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class UserDtoTest extends DtoTest {

    public static final List<RoleDto> sampleRoles = createSampleRoles();
    public static final String SOME_INSTITUTION = "someInstitution";
    public static final String SOME_OTHER_ROLENAME = "SomeOtherRolename";
    protected static final String USER_TYPE_LITERAL = "User";

    @ParameterizedTest(name = "isValid() returns false when username is \"{0}\"")
    @NullAndEmptySource
    public void isValidReturnsFalseWhenUsernameIsNullOrBlank(String emptyOrNullUsername)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        UserDto userDto = new UserDto();
        Method setter = UserDto.class.getDeclaredMethod("setUsername", String.class);
        setter.setAccessible(true);
        setter.invoke(userDto, emptyOrNullUsername);
        assertThat(userDto.isValid(), is(equalTo(false)));
    }

    @DisplayName("UserDto object contains type with value \"User\"")
    @Test
    public void userDtoSerializedObjectContainsTypeWithValueUser() throws InvalidEntryInternalException {
        UserDto sampleUser = createUserWithRolesAndInstitution();
        ObjectNode json = objectMapper.convertValue(sampleUser, ObjectNode.class);

        String actualType = json.get(JSON_TYPE_ATTRIBUTE).asText();
        assertThat(actualType, is(equalTo(USER_TYPE_LITERAL)));
    }

    @DisplayName("UserDto cannot be created without type value")
    @Test
    public void userDtoCannotBeCreatedWithoutTypeValue() throws InvalidEntryInternalException, JsonProcessingException {
        UserDto sampleUser = createUserWithRolesAndInstitution();
        ObjectNode json = objectMapper.convertValue(sampleUser, ObjectNode.class);
        JsonNode objectWithoutType = json.remove(JSON_TYPE_ATTRIBUTE);
        String jsonStringWithoutType = objectMapper.writeValueAsString(objectWithoutType);

        Executable action = () -> objectMapper.readValue(jsonStringWithoutType, UserDto.class);
        InvalidTypeIdException exception = assertThrows(InvalidTypeIdException.class, action);
        assertThat(exception.getMessage(), containsString(INVALID_TYPE_EXCEPTION_MESSAGE_SAMPLE));
    }

    @DisplayName("UserDto can be created when it contains the right type value")
    @Test
    public void userDtoCanBeDeserializedWhenItContainsTheRightTypeValue()
        throws InvalidEntryInternalException, JsonProcessingException {
        UserDto sampleUser = createUserWithRolesAndInstitution();
        ObjectNode json = objectMapper.convertValue(sampleUser, ObjectNode.class);
        assertThatSerializedItemContainsType(json, USER_TYPE_LITERAL);

        String jsonStringWithType = objectMapper.writeValueAsString(json);

        UserDto deserializedItem = objectMapper.readValue(jsonStringWithType, UserDto.class);

        assertThat(deserializedItem, is(equalTo(sampleUser)));
        assertThat(deserializedItem, is(not(sameInstance(sampleUser))));
    }

    @Test
    public void getAccessRightsReturnsAccessRightsWithoutDuplicates()
        throws InvalidEntryInternalException {
        final UserDto user = EntityUtils.createUserWithRoleWithoutInstitution();
        final Set<AccessRight> expectedAccessRights = new HashSet<>(user.getAccessRights());

        var newRoles = duplicateRoles(user);
        var newUser = user.copy().withRoles(newRoles).build();

        HashSet<AccessRight> actualAccessRights = new HashSet<>(newUser.getAccessRights());
        assertThat(actualAccessRights, is(equalTo(expectedAccessRights)));
    }

    @Test
    public void getAccessRightsReturnsAllAccessRightsContainedInTheUsersRoles()
        throws InvalidEntryInternalException {
        final AccessRight firstRoleAccessRight = APPROVE_DOI_REQUEST;
        final AccessRight secondRoleAccessRight = REJECT_DOI_REQUEST;
        RoleDto firstRole = sampleRole(firstRoleAccessRight, SOME_ROLENAME);
        RoleDto secondRole = sampleRole(secondRoleAccessRight, SOME_OTHER_ROLENAME);

        List<RoleDto> roles = List.of(firstRole, secondRole);
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(roles).build();

        Set<AccessRight> expectedAccessRights = Set.of(firstRoleAccessRight, secondRoleAccessRight);
        assertThat(user.getAccessRights(), is(equalTo(expectedAccessRights)));
    }

    @Test
    void userDtoHasAConstructorWithoutArgs() {
        new UserDto();
    }

    @Test
    void userDtoHasABuilder() {
        Builder builder = UserDto.newBuilder();
        assertNotNull(builder);
    }

    @Test
    void builderReturnsUserDtoWhenInstitutionIsEmpty() throws InvalidEntryInternalException {
        UserDto user = createUserWithRoleWithoutInstitution();
        assertThat(user.getUsername(), is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(), is(equalTo(sampleRoles)));
        assertThat(user.getInstitution(), is(equalTo(null)));
    }

    @Test
    void builderReturnsUserDtoWhenIRolesIsEmpty() throws InvalidEntryInternalException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION).build();
        assertThat(user.getUsername(), is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(), is(equalTo(Collections.emptyList())));
        assertThat(user.getInstitution(), is(equalTo(SOME_INSTITUTION)));
    }

    @ParameterizedTest(name = "build throws exception when username is:\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void buildThrowsExceptionWhenUsernameIsNullOrEmpty(String username) {
        Executable action = () -> UserDto.newBuilder().withUsername(username).build();
        assertThrows(InvalidEntryInternalException.class, action);
    }

    @Test
    void toUserDbReturnsValidUserDbWhenUserDtoIsValid() throws InvalidEntryInternalException {
        UserDto userOnlyWithOnlyUsername = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        UserDto actualUserOnlyWithName = convertToUserDbAndBack(userOnlyWithOnlyUsername);
        assertThat(actualUserOnlyWithName, is(equalTo(userOnlyWithOnlyUsername)));
    }

    @ParameterizedTest(name = "fromUserDb throws Exception user contains invalidRole. Rolename:\"{0}\"")
    @NullAndEmptySource
    void fromUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidEntryInternalException {
        RoleDb invalidRole = new RoleDb();
        invalidRole.setName(invalidRoleName);
        List<RoleDb> invalidRoles = Collections.singletonList(invalidRole);
        UserDb userDbWithInvalidRole = UserDb.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = () -> UserDto.fromUserDb(userDbWithInvalidRole);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidEntryInternalException.class)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void toUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidEntryInternalException {
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setRoleName(invalidRoleName);
        List<RoleDto> invalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = userWithInvalidRole::toUserDb;
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidEntryInternalException.class)));
    }

    @Test
    void roleValidationMethodLogsError()
        throws InvalidEntryInternalException {
        TestAppender appender = LogUtils.getTestingAppender(UserDto.class);
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setRoleName(null);
        List<RoleDto> invalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = userWithInvalidRole::toUserDb;
        assertThrows(RuntimeException.class, action);

        assertThat(appender.getMessages(), containsString(ERROR_DUE_TO_INVALID_ROLE));
    }

    @Test
    void copyShouldCopyUserDto() throws InvalidEntryInternalException {
        UserDto initialUser = createUserWithRolesAndInstitution();
        UserDto copiedUser = initialUser.copy().build();

        assertThat(copiedUser, is(equalTo(initialUser)));
        assertThat(copiedUser, is(not(sameInstance(initialUser))));
    }

    @Test
    void userDtoIsSerialized() throws IOException, InvalidEntryInternalException {
        UserDto initialUser = createUserWithRolesAndInstitution();

        assertThat(initialUser, doesNotHaveNullOrEmptyFields());

        String jsonString = objectMapper.writeValueAsString(initialUser);
        JsonNode actualJson = objectMapper.readTree(jsonString);
        JsonNode expectedJson = objectMapper.convertValue(initialUser, JsonNode.class);
        assertThat(actualJson, is(equalTo(expectedJson)));

        UserDto deserializedObject = objectMapper.readValue(jsonString, UserDto.class);
        assertThat(deserializedObject, is(equalTo(initialUser)));
        assertThat(deserializedObject, is(not(sameInstance(initialUser))));
    }

    private static List<RoleDto> createSampleRoles() {
        try {
            return Collections.singletonList(EntityUtils.createRole(SOME_ROLENAME));
        } catch (InvalidEntryInternalException e) {
            throw new RuntimeException(e);
        }
    }

    private RoleDto sampleRole(AccessRight approveDoiRequest, String someRolename)
        throws InvalidEntryInternalException {
        List<AccessRight> firstRoleAccessRights = Collections.singletonList(approveDoiRequest);
        return RoleDto.newBuilder()
            .withName(someRolename)
            .withAccessRights(firstRoleAccessRights)
            .build();
    }

    private List<RoleDto> duplicateRoles(UserDto user) throws InvalidEntryInternalException {
        List<RoleDto> duplicateRoles = user.getRoles().stream()
            .map(attempt(r -> r.copy().withName(r.getRoleName() + "_copy").build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
        ArrayList<RoleDto> newRoles = new ArrayList<>(user.getRoles());
        newRoles.addAll(duplicateRoles);
        return newRoles;
    }

    private UserDto convertToUserDbAndBack(UserDto userDto) throws InvalidEntryInternalException {
        UserDb userDb = userDto.toUserDb();
        return UserDto.fromUserDb(userDb);
    }
}
