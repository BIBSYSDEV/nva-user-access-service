package no.unit.nva.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
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
        UserDto insertedUser = createSampleUser();
        db.addUser(insertedUser);

        UserDto savedUsr = db.getUser(insertedUser.getUsername()).get();
        assertThat(savedUsr, is(equalTo(insertedUser)));
    }

    @Test
    public void databaseServiceShouldReturnNonEmptyUserWhenUsernameExistsInDatabase()
        throws InvalidUserException, InvalidRoleException {
        UserDto insertedUser = createSampleUser();
        db.addUser(insertedUser);

        UserDto savedUsr = db.getUser(insertedUser.getUsername()).get();
        assertThat(savedUsr, is(equalTo(insertedUser)));
    }

    @Test
    public void addUserShouldSaveAUserWithoutInstitution() throws InvalidUserException, InvalidRoleException {
        RoleDto role = RoleDto.newBuilder().withName(SOME_ROLE).build();
        UserDto expectedUser = UserDto.newBuilder()
            .withUsername(SOME_USERNAME)
            .withRoles(Collections.singletonList(role))
            .build();
        db.addUser(expectedUser);
        UserDto actualUser = db.getUser(expectedUser.getUsername()).get();

        assertThat(actualUser, is(equalTo(expectedUser)));
        assertThat(actualUser.getInstitution(), is(equalTo(null)));
    }

    @Test
    public void addUserShouldSaveAUserWithoutRoles() throws InvalidUserException {
        UserDto expectedUser = UserDto.newBuilder()
            .withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION)
            .build();
        db.addUser(expectedUser);
        UserDto actualUser = db.getUser(expectedUser.getUsername()).get();
        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @Test
    public void addUserShouldNotSaveAUserWithoutUsername() {
        Executable illegalAction = () -> db.addUser(userWithoutUsername());
        InvalidUserException exception = assertThrows(InvalidUserException.class, illegalAction);
        assertThat(exception.getClass(), is(equalTo(InvalidUserException.class)));
    }

    @Test
    public void dbServiceShouldHaveAMethodForUpdatingExistingUser() throws InvalidRoleException, InvalidUserException {
        UserDto user = createSampleUser();
        assertThrows(RuntimeException.class, () -> db.updateUser(user));
    }

    private UserDto userWithoutUsername() throws InvalidUserException {
        return UserDto.newBuilder()
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    private UserDto createSampleUser() throws InvalidRoleException, InvalidUserException {
        RoleDto roleDto = RoleDto.newBuilder()
            .withName(SOME_ROLE)
            .build();
        return UserDto.newBuilder()
            .withUsername(SOME_USERNAME)
            .withRoles(Collections.singletonList(roleDto))
            .withInstitution(SOME_INSTITUTION)
            .build();
    }
}
