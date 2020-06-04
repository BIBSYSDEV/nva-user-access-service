package no.unit.nva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import no.unit.nva.database.UserDb;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.UserDto.Builder;
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
    public void userDtoShouldHaveABuilder() {
        Builder builder = UserDto.newBuilder();
    }

    @Test
    public void builderReturnsUserDtoWhenInstitutionIsEmpty() throws InvalidUserException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME)
            .withRoles(sampleRoles).build();
        assertThat(user.getUsername(),is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(),is(equalTo(sampleRoles)));
        assertThat(user.getInstitution(),is(equalTo(null)));
    }

    @Test
    public void builderReturnsUserDtoWhenIRolesIsEmpty() throws InvalidUserException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION).build();
        assertThat(user.getUsername(),is(equalTo(SOME_USERNAME)));
        assertThat(user.getRoles(),is(equalTo(Collections.emptyList())));
        assertThat(user.getInstitution(),is(equalTo(SOME_INSTITUTION)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    public void buildThrowsExceptionWhenUsernameisNullOrEmpty(String username) {
        Executable action = () -> UserDto.newBuilder().withUsername(username).build();
        assertThrows(InvalidUserException.class, action);
    }

    @Test
    public void toUserDbReturnsValidUserDbWhenUserDtoIsValid() throws InvalidUserException {
        UserDto userOnlyWithOnlyUsername = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        UserDto actualUserOnlyWithName = convertToUserDbAndBack(userOnlyWithOnlyUsername);
        assertThat(actualUserOnlyWithName,is(equalTo(userOnlyWithOnlyUsername)));
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
