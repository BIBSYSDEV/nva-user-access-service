package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.model.DoesNotHaveNullFields.doesNotHaveNullFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class DatabaseServiceTest extends DatabaseAccessor {

    public static final String SOME_USERNAME = "someusername";
    public static final String SOME_ROLE = "SomeRole";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    public static final String SOME_OTHER_ROLE = "SOME_OTHER_ROLE";
    private DatabaseService db;
    private UserDto sampleUser;

    @BeforeEach
    public void init() throws InvalidEntryInternalException {
        db = createDatabaseServiceUsingLocalStorage();
        sampleUser = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
    }

    @Test
    public void databaseServiceHasAMethodForInsertingAUser()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto user = UserDto.newBuilder().withUsername(SOME_USERNAME).build();
        db.addUser(user);
    }

    @Test
    public void databaseServiceInsertsValidItemInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto savedUser = db.getUser(insertedUser);

        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @Test
    public void databaseServiceReturnsNonEmptyUserWhenUsernameExistsInDatabase()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto insertedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto savedUser = db.getUser(insertedUser);

        assertThat(insertedUser, doesNotHaveNullFields());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @Test
    public void addUserSavesAUserWithoutInstitution() throws InvalidEntryInternalException, ConflictException,
                                                             InvalidInputException, NotFoundException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, null, SOME_ROLE);
        UserDto actualUser = db.getUser(expectedUser);

        assertThat(actualUser, is(equalTo(expectedUser)));
        assertThat(actualUser.getInstitution(), is(equalTo(null)));
    }

    @Test
    public void addUserShouldSaveUserWithoutRoles()
        throws InvalidEntryInternalException, ConflictException, InvalidInputException, NotFoundException {
        UserDto expectedUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, null);
        UserDto actualUser = db.getUser(expectedUser);

        assertThat(actualUser, is(true));
        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @Test
    public void addUserShouldNotSaveUserWithoutUsername() {
        Executable illegalAction = () -> db.addUser(userWithoutUsername());
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, illegalAction);
        assertThat(exception.getClass(), is(equalTo(InvalidEntryInternalException.class)));
    }

    @Test
    @Disabled
    public void updateUserUpdatesExistingUserWithInputUserWhenInputUserIsValid()
        throws ConflictException, InvalidEntryInternalException, NotFoundException,
               InvalidInputException {
        UserDto existingUser = createSampleUserAndAddUserToDb(SOME_USERNAME, SOME_INSTITUTION, SOME_ROLE);
        UserDto expectedUser = alterUser(existingUser);

        db.updateUser(expectedUser);
    }

    private UserDto alterUser(UserDto existingUser) throws InvalidEntryInternalException {
        RoleDto someOtherRole = RoleDto.newBuilder().withName(SOME_OTHER_ROLE).build();
        return existingUser.copy().withRoles(Collections.singletonList(someOtherRole)).build();
    }

    private UserDto userWithoutUsername() throws InvalidEntryInternalException {
        return UserDto.newBuilder()
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    private UserDto createSampleUserAndAddUserToDb(String username, String institution, String roleName)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto userDto = UserDto.newBuilder()
            .withRoles(createRoleList(roleName))
            .withInstitution(institution)
            .withUsername(username)
            .build();
        db.addUser(userDto);
        return userDto;
    }

    private List<RoleDto> createRoleList(String rolename) throws InvalidEntryInternalException {
        if (nonNull(rolename)) {
            RoleDto roleDto = RoleDto.newBuilder().withName(rolename).build();
            return Collections.singletonList(roleDto);
        } else {
            return Collections.emptyList();
        }
    }
}
