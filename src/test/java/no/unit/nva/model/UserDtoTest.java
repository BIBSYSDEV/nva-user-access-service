package no.unit.nva.model;

import static no.unit.nva.model.UserDto.ERROR_DUE_TO_INVALID_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.UserDb;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.UserDto.Builder;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class UserDtoTest {

    public static final String SOME_USERNAME = "someUsername";
    public static final String SOME_ROLENAME = "someRolename";
    public static final List<RoleDto> sampleRoles = createSampleRoles();
    public static final String SOME_INSTITUTION = "someInstitution";

    @Test
    void userDtoHasAConstructorWithoutArgs() {
        new UserDto();
    }

    @Test
    void userDtoShouldHaveABuilder() {
        Builder builder = UserDto.newBuilder();
    }

    @Test
    void builderReturnsUserDtoWhenInstitutionIsEmpty() throws InvalidUserException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME)
            .withRoles(sampleRoles).build();
        assertThat(user.getUsername(), is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(), is(equalTo(sampleRoles)));
        assertThat(user.getInstitution(), is(equalTo(null)));
    }

    @Test
    void builderReturnsUserDtoWhenIRolesIsEmpty() throws InvalidUserException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION).build();
        assertThat(user.getUsername(), is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(), is(equalTo(Collections.emptyList())));
        assertThat(user.getInstitution(), is(equalTo(SOME_INSTITUTION)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void buildThrowsExceptionWhenUsernameisNullOrEmpty(String username) {
        Executable action = () -> UserDto.newBuilder().withUsername(username).build();
        assertThrows(InvalidUserException.class, action);
    }

    @Test
    void toUserDbReturnsValidUserDbWhenUserDtoIsValid() throws InvalidUserException {
        UserDto userOnlyWithOnlyUsername = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        UserDto actualUserOnlyWithName = convertToUserDbAndBack(userOnlyWithOnlyUsername);
        assertThat(actualUserOnlyWithName, is(equalTo(userOnlyWithOnlyUsername)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void fromUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidUserException {
        RoleDb invalidRole = new RoleDb();
        invalidRole.setName(invalidRoleName);
        List<RoleDb> invlalidRoles = Collections.singletonList(invalidRole);
        UserDb userDbWithInvalidRole = UserDb.newBuilder().withUsername(SOME_USERNAME).withRoles(invlalidRoles).build();

        Executable action = () -> UserDto.fromUserDb(userDbWithInvalidRole);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidRoleException.class)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void toUserDbThrowsExceptionWhenUserDbContainsInvalidRole(String invalidRoleName)
        throws InvalidUserException, InvalidRoleException {
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setName(invalidRoleName);
        List<RoleDto> invlalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invlalidRoles).build();

        Executable action = () -> userWithInvalidRole.toUserDb();
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(InvalidRoleException.class)));
    }

    @Test
    void roleValidationMethodLogsError()
        throws InvalidUserException, InvalidRoleException {
        TestAppender appender = LogUtils.getTestingAppender(UserDto.class);
        RoleDto invalidRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        invalidRole.setName(null);
        List<RoleDto> invlalidRoles = Collections.singletonList(invalidRole);
        UserDto userWithInvalidRole = UserDto.newBuilder().withUsername(SOME_USERNAME).withRoles(invlalidRoles).build();

        Executable action = () -> userWithInvalidRole.toUserDb();
        assertThrows(RuntimeException.class, action);

        assertThat(appender.getMessages(), containsString(ERROR_DUE_TO_INVALID_ROLE));
    }

    @Test
    void updateShouldUpdateUserInDatabase() throws InvalidUserException {
        UserDto initialUser = UserDto.newBuilder().withUsername(SOME_USERNAME).withInstitution(SOME_INSTITUTION)
            .withRoles(sampleRoles).build();
        UserDto copiedUser = initialUser.copy().build();

        assertThat(copiedUser, is(equalTo(initialUser)));
        assertThat(copiedUser, is(not(sameInstance(initialUser))));
    }

    private UserDto convertToUserDbAndBack(UserDto userDto) throws InvalidUserException {
        UserDb userDb = userDto.toUserDb();
        return UserDto.fromUserDb(userDb);
    }

    private static List<RoleDto> createSampleRoles() {
        try {
            return Arrays.asList(RoleDto.newBuilder().withName(SOME_ROLENAME).build());
        } catch (InvalidRoleException e) {
            throw new RuntimeException(e);
        }
    }
}
