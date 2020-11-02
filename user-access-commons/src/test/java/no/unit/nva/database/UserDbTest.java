package no.unit.nva.database;

import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;
import static no.unit.nva.model.DoesNotHaveNullFields.doesNotHaveNullFields;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.database.UserDb.Builder;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import nva.commons.utils.attempt.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UserDbTest extends DatabaseAccessor {

    public static final String SOME_USERNAME = "someUser";
    public static final String SOME_GIVEN_NAME = "givenName";
    public static final String SOME_FAMILY_NAME = "familyName";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    public static final List<RoleDb> SAMPLE_ROLES = createSampleRoles();

    private UserDb dynamoFunctionalityTestUser;
    private UserDb sampleUser;

    @Test
    public void builderShouldSetTheHashKeyBasedOnusername() throws InvalidEntryInternalException {
        sampleUser.setPrimaryHashKey("SomeOtherHashKey");
        String expectedHashKey = String.join(UserDb.FIELD_DELIMITER, UserDb.TYPE, SOME_USERNAME);
        assertThat(sampleUser.getPrimaryHashKey(), is(equalTo(expectedHashKey)));
    }

    @BeforeEach
    public void init() throws InvalidEntryInternalException {
        dynamoFunctionalityTestUser = new UserDb();
        sampleUser = UserDb.newBuilder().withUsername(SOME_USERNAME).build();
        initializeTestDatabase();
    }

    @Test
    void userDbHasABuilder() {
        Builder builder = UserDb.newBuilder();
        assertNotNull(builder);
    }

    @Test
    void setUsernameShouldAddUsernameToUserObject() {
        dynamoFunctionalityTestUser.setUsername(SOME_USERNAME);
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(equalTo(SOME_USERNAME)));
    }

    @Test
    void getUsernameShouldGetTheSetUsernameToUserObject() {
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(nullValue()));

        dynamoFunctionalityTestUser.setUsername(SOME_USERNAME);
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(equalTo(SOME_USERNAME)));
    }

    @Test
    void getTypeShouldReturnConstantTypeValue() {
        assertThat(dynamoFunctionalityTestUser.getType(), is(equalTo(UserDb.TYPE)));
    }

    @Test
    void setTypeShouldNotChangeTheReturnedTypeValue() {
        dynamoFunctionalityTestUser.setType("NotExpectedType");
        assertThat(dynamoFunctionalityTestUser.getType(), is(equalTo(UserDb.TYPE)));
    }

    @Test
    void getHashKeyKeyShouldReturnTypeAndUsernameConcatenation() {
        String expectedHashKey = String.join(UserDb.FIELD_DELIMITER, UserDb.TYPE, SOME_USERNAME);
        assertThat(sampleUser.getPrimaryHashKey(), is(equalTo(expectedHashKey)));
    }

    @Test
    void userDbShouldBeWriteableToDatabase() {
        Table table = DatabaseServiceImpl.createTable(initializeTestDatabase(), envWithTableName);
        assertDoesNotThrow(() -> table.putItem(sampleUser.toItem()));
    }

    @Test
    void userDbShouldBeReadFromDatabaseWithoutDataLoss() throws InvalidEntryInternalException {
        UserDb insertedUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
            .withGivenName(SOME_GIVEN_NAME)
            .withFamilyName(SOME_FAMILY_NAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();
        Table table = clientToLocalDatabase();
        table.putItem(insertedUser.toItem());
        assertThat(insertedUser, doesNotHaveNullFields());
        Item item = table.getItem(DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY, insertedUser.getPrimaryHashKey(),
            PRIMARY_KEY_RANGE_KEY, insertedUser.getPrimaryRangeKey());
        UserDb savedUser = UserDb.fromItem(item, UserDb.class);
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @ParameterizedTest(name = "builder should throw exception when username is:\"{0}\"")
    @NullAndEmptySource
    void builderShouldThrowExceptionWhenUsernameIsNotValid(String invalidUsername) {

        Executable action = () -> UserDb.newBuilder()
            .withUsername(invalidUsername)
            .withGivenName(SOME_GIVEN_NAME)
            .withFamilyName(SOME_FAMILY_NAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();

        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);
        assertThat(exception.getMessage(), containsString(UserDb.INVALID_USER_EMPTY_USERNAME));
    }

    @Test
    void copyShouldReturnBuilderWithFilledInFields() throws InvalidEntryInternalException {
        UserDb originalUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
            .withGivenName(SOME_GIVEN_NAME)
            .withFamilyName(SOME_FAMILY_NAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();
        UserDb copy = originalUser.copy().build();
        assertThat(copy, is(equalTo(originalUser)));

        assertThat(copy, is(not(sameInstance(originalUser))));
    }

    @Test
    void setPrimaryHashKeyThrowsExceptionWhenKeyDoesNotStartWithType() {
        UserDb userDb = new UserDb();
        Executable action = () -> userDb.setPrimaryHashKey("SomeKey");
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);
        assertThat(exception.getMessage(), containsString(UserDb.INVALID_PRIMARY_HASH_KEY));
    }

    private static List<RoleDb> createSampleRoles() {
        return Stream.of("Role1", "Role2")
            .map(attempt(UserDbTest::newRole))
            .map(Try::get)
            .collect(Collectors.toList());
    }

    private static RoleDb newRole(String str) throws InvalidEntryInternalException {
        return RoleDb.newBuilder().withName(str).build();
    }

    private Table clientToLocalDatabase() {
        return DatabaseServiceImpl.createTable(initializeTestDatabase(), envWithTableName);
    }
}