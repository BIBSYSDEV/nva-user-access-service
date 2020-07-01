package no.unit.nva.model;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static no.unit.nva.model.UserDto.ERROR_DUE_TO_INVALID_ROLE;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.UserDb;
import no.unit.nva.exceptions.EmptyUsernameException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.handlers.UserDtoCreator;
import no.unit.nva.model.UserDto.Builder;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class UserDtoTest implements UserDtoCreator {

    public static final List<RoleDto> sampleRoles = createSampleRoles();
    public static final String SOME_INSTITUTION = "someInstitution";

    @Test
    void userDtoHasAConstructorWithoutArgs() {
        new UserDto();
    }

    @Test
    void userDtoShouldHaveABuilder() {
        Builder builder = UserDto.newBuilder();
        assertNotNull(builder);
    }

    @Test
    void builderReturnsUserDtoWhenInstitutionIsEmpty()
        throws InvalidUserInternalException, InvalidRoleInternalException {
        UserDto user = createUserWithRoleWithoutInstitution();
        assertThat(user.getUsername(), is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(), is(equalTo(sampleRoles)));
        assertThat(user.getInstitution(), is(equalTo(null)));
    }

    @Test
    void builderReturnsUserDtoWhenIRolesIsEmpty() throws InvalidUserInternalException {
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
        assertThrows(InvalidUserInternalException.class, action);
    }

    @Test
    void toUserDbReturnsValidUserDbWhenUserDtoIsValid() throws InvalidUserInternalException {
        UserDto userOnlyWithOnlyUsername = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        UserDto actualUserOnlyWithName = convertToUserDbAndBack(userOnlyWithOnlyUsername);
        assertThat(actualUserOnlyWithName, is(equalTo(userOnlyWithOnlyUsername)));
    }

    @ParameterizedTest(name = "fromUserDb throws Exception user contains invalidRole. Rolename:\"{0}\"")
    @NullAndEmptySource
    void fromUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidUserInternalException {
        RoleDb invalidRole = new RoleDb();
        invalidRole.setName(invalidRoleName);
        List<RoleDb> invalidRoles = Collections.singletonList(invalidRole);
        UserDb userDbWithInvalidRole = UserDb.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = () -> UserDto.fromUserDb(userDbWithInvalidRole);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidRoleInternalException.class)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void toUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidUserInternalException, InvalidRoleInternalException {
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setRoleName(invalidRoleName);
        List<RoleDto> invalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = userWithInvalidRole::toUserDb;
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidRoleInternalException.class)));
    }

    @Test
    void roleValidationMethodLogsError()
        throws InvalidUserInternalException, InvalidRoleInternalException {
        TestAppender appender = LogUtils.getTestingAppender(UserDto.class);
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setRoleName(null);
        List<RoleDto> invalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invalidRoles).build();

        Executable action = () -> userWithInvalidRole.toUserDb();
        assertThrows(RuntimeException.class, action);

        assertThat(appender.getMessages(), containsString(ERROR_DUE_TO_INVALID_ROLE));
    }

    @Test
    void copyShouldCopyUserDto() throws InvalidUserInternalException, InvalidRoleInternalException {
        UserDto initialUser = createUserWithRolesAndInstitution();
        UserDto copiedUser = initialUser.copy().build();

        assertThat(copiedUser, is(equalTo(initialUser)));
        assertThat(copiedUser, is(not(sameInstance(initialUser))));
    }

    @Test
    void userDtoIsSerialized() throws InvalidUserInternalException, IOException, InvalidRoleInternalException {
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

    @DisplayName("validate() throws UsernameMissingException when username is missing from UserDto instance")
    @Test
    public void validateThrowsUsernameMissingExceptionWhenUsernameIsMissingFromUserDto()
        throws InvalidUserInternalException, NoSuchMethodException, InvalidRoleInternalException,
               IllegalAccessException, InvocationTargetException {
        UserDto userDto = createUserWithoutUsername();
        Executable action = () -> userDto.validate();
        assertThrows(EmptyUsernameException.class, action);
    }

    private UserDto convertToUserDbAndBack(UserDto userDto) throws InvalidUserInternalException {
        UserDb userDb = userDto.toUserDb();
        return UserDto.fromUserDb(userDb);
    }

    private static List<RoleDto> createSampleRoles() {
        try {
            return Arrays.asList(RoleDto.newBuilder().withName(SOME_ROLENAME).build());
        } catch (InvalidRoleInternalException e) {
            throw new RuntimeException(e);
        }
    }
}
