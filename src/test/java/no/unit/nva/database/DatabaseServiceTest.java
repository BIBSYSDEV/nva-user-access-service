package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.model.DoesNotHaveNullFields.doesNotHaveNullFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceTest extends DatabaseTest {

    public static final String SOME_USERNAME = "someusername";
    public static final String SOME_ROLE = "SomeRole";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    private DatabaseService db;

    @BeforeEach
    public void init() {
        db = new DatabaseServiceImpl(initializeTestDatabase());
    }

    @Test
    public void databaseServiceShouldHaveAMethodForGettingAUserByUsername() throws InvalidUserException {
        db.getUser(SOME_USERNAME);
    }

    @Test
    public void databaseServiceShouldHaveAMethodForInsertingAUser() throws InvalidUserException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        db.addUser(user);
    }

    @Test
    public void databaseServiceShouldInsertValidItemInDatabase() throws InvalidUserException, InvalidRoleException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        db.addUser(insertedUser);
        UserDto savedUser = getUser(insertedUser);
        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @Test
    public void databaseServiceShouldReturnNonEmptyUserWhenUsernameExistsInDatabase()
        throws InvalidUserException, InvalidRoleException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto savedUser = getUser(insertedUser);
        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @Test
    public void addUserShouldSaveAUserWithoutInstitution() throws InvalidUserException, InvalidRoleException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, null, SOME_ROLE);
        UserDto actualUser = getUser(expectedUser);
        assertThat(actualUser, is(equalTo(expectedUser)));
        assertThat(actualUser.getInstitution(), is(equalTo(null)));
    }

    @Test
    public void addUserShouldSaveUserWithoutRoles() throws InvalidUserException, InvalidRoleException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, null);
        UserDto actualUser = getUser(expectedUser);
        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @Test
    public void addUserShouldNotSaveUserWithoutUsername() {
        Executable illegalAction = () -> db.addUser(userWithoutUsername());
        InvalidUserException exception = assertThrows(InvalidUserException.class, illegalAction);
        assertThat(exception.getClass(), is(equalTo(InvalidUserException.class)));
    }

    @Test
    public void dbServiceShouldHaveMethodForUpdatingExistingUser() throws InvalidRoleException, InvalidUserException {
        UserDto user = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        assertThrows(RuntimeException.class, () -> db.updateUser(user));
    }

    private UserDto userWithoutUsername() throws InvalidUserException {
        return UserDto.newBuilder()
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    private UserDto getUser(UserDto insertedUser) throws InvalidUserException {
        return db.getUser(insertedUser.getUsername())
            .orElseThrow(() -> new RuntimeException("Expected to find a user"));
    }

    private UserDto createSampleUserAndAddUserToDb(String username, String institution, String roleName)
        throws InvalidRoleException, InvalidUserException {
        UserDto userDto = UserDto.newBuilder()
            .withRoles(createRoleList(roleName))
            .withInstitution(institution)
            .withUsername(username)
            .build();
        db.addUser(userDto);
        return userDto;
    }

    private List<RoleDto> createRoleList(String rolename) throws InvalidRoleException {
        if (nonNull(rolename)) {
            RoleDto roleDto = RoleDto.newBuilder().withName(rolename).build();
            return Collections.singletonList(roleDto);
        } else {
            return Collections.emptyList();
        }
    }
}
