package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.model.DoesNotHaveNullFields.doesNotHaveNullFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
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
    private UserDto sampleUser;

    @BeforeEach
    public void init() throws InvalidUserInternalException {
        db = new DatabaseServiceImpl(initializeTestDatabase());
        sampleUser = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
    }

    @Test
    public void databaseServiceShouldHaveAMethodForGettingAUserByUsername() throws InvalidUserInternalException {
        db.getUser(sampleUser);
    }

    @Test
    public void databaseServiceShouldHaveAMethodForInsertingAUser()
        throws InvalidUserInternalException, ConflictException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        db.addUser(user);
    }

    @Test
    public void databaseServiceShouldInsertValidItemInDatabase()
        throws InvalidUserInternalException, InvalidRoleInternalException, ConflictException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        Optional<UserDto> savedUser = db.getUser(insertedUser);

        assertThat(savedUser.isPresent(), is(true));
        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser.get(), is(equalTo(insertedUser)));
    }

    @Test
    public void databaseServiceShouldReturnNonEmptyUserWhenUsernameExistsInDatabase()
        throws InvalidUserInternalException, InvalidRoleInternalException, ConflictException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        Optional<UserDto> savedUser = db.getUser(insertedUser);

        assertThat(savedUser.isPresent(), is(true));
        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser.get(), is(equalTo(insertedUser)));
    }

    @Test
    public void addUserShouldSaveAUserWithoutInstitution() throws InvalidUserInternalException,
                                                                  InvalidRoleInternalException, ConflictException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, null, SOME_ROLE);
        Optional<UserDto> actualUser = db.getUser(expectedUser);

        assertThat(actualUser.isPresent(), is(true));
        assertThat(actualUser.get(), is(equalTo(expectedUser)));
        assertThat(actualUser.get().getInstitution(), is(equalTo(null)));
    }

    @Test
    public void addUserShouldSaveUserWithoutRoles()
        throws InvalidUserInternalException, InvalidRoleInternalException, ConflictException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, null);
        Optional<UserDto> actualUser = db.getUser(expectedUser);

        assertThat(actualUser.isPresent(), is(true));
        assertThat(actualUser.get(), is(equalTo(expectedUser)));
    }

    @Test
    public void addUserShouldNotSaveUserWithoutUsername() {
        Executable illegalAction = () -> db.addUser(userWithoutUsername());
        InvalidUserInternalException exception = assertThrows(InvalidUserInternalException.class, illegalAction);
        assertThat(exception.getClass(), is(equalTo(InvalidUserInternalException.class)));
    }

    @Test
    public void dbServiceShouldHaveMethodForUpdatingExistingUser()
        throws InvalidRoleInternalException, InvalidUserInternalException, ConflictException {
        UserDto user = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        assertThrows(RuntimeException.class, () -> db.updateUser(user));
    }

    private UserDto userWithoutUsername() throws InvalidUserInternalException {
        return UserDto.newBuilder()
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    private UserDto createSampleUserAndAddUserToDb(String username, String institution, String roleName)
        throws InvalidRoleInternalException, InvalidUserInternalException, ConflictException {
        UserDto userDto = UserDto.newBuilder()
            .withRoles(createRoleList(roleName))
            .withInstitution(institution)
            .withUsername(username)
            .build();
        db.addUser(userDto);
        return userDto;
    }

    private List<RoleDto> createRoleList(String rolename) throws InvalidRoleInternalException {
        if (nonNull(rolename)) {
            RoleDto roleDto = RoleDto.newBuilder().withName(rolename).build();
            return Collections.singletonList(roleDto);
        } else {
            return Collections.emptyList();
        }
    }
}
