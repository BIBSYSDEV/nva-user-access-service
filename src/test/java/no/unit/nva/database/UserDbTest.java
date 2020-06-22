package no.unit.nva.database;

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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.database.UserDb.Builder;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import nva.commons.utils.attempt.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UserDbTest extends DatabaseTest {

    public static final String SOME_USERNAME = "someUser";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    public static final List<RoleDb> SAMPLE_ROLES = createSampleRoles();

    private UserDb dynamoFunctionalityTestUser;
    private UserDb sampleUser;

    @BeforeEach
    private void init() throws InvalidUserInternalException {
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
    public void builderShouldSetTheHashKeyBasedOnusername() throws InvalidUserInternalException {
        sampleUser.setPrimaryHashKey("SomeOtherHashKey");
        String expectedHashKey = String.join(UserDb.FIELD_DELIMITER, UserDb.TYPE, SOME_USERNAME);
        assertThat(sampleUser.getPrimaryHashKey(), is(equalTo(expectedHashKey)));
    }

    @Test
    void userDbShouldBeWriteableToDatabase() {
        DynamoDBMapper mapper = clientToLocalDatabase();
        assertDoesNotThrow(() -> mapper.save(sampleUser));
    }

    @Test
    void userDbShouldBeReadFromDatabaseWithoutDataLoss() throws InvalidUserInternalException {
        UserDb insertedUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();
        DynamoDBMapper mapper = clientToLocalDatabase();
        mapper.save(insertedUser);
        assertThat(insertedUser, doesNotHaveNullFields());
        UserDb savedUser = mapper.load(UserDb.class, insertedUser.getPrimaryHashKey());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @ParameterizedTest(name = "builder should throw exception when username is:\"{0}\"")
    @NullAndEmptySource
    void builderShouldThrowExceptionWhenUsernameIsNotValid(String invalidUsername) {

        Executable action = () -> UserDb.newBuilder()
            .withUsername(invalidUsername)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();

        InvalidUserInternalException exception = assertThrows(InvalidUserInternalException.class, action);
        assertThat(exception.getMessage(), containsString(UserDb.INVALID_USER_EMPTY_USERNAME));
    }

    @Test
    void copyShouldReturnBuilderWithFilledInFields() throws InvalidUserInternalException {
        UserDb originalUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
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
        InvalidUserInternalException exception = assertThrows(InvalidUserInternalException.class, action);
        assertThat(exception.getMessage(), containsString(UserDb.INVALID_PRIMARY_HASH_KEY));
    }

    private DynamoDBMapper clientToLocalDatabase() {
        return new DynamoDBMapper(localDynamo);
    }

    private static List<RoleDb> createSampleRoles() {
        return Stream.of("Role1", "Role2")
            .map(attempt(UserDbTest::newRole))
            .map(Try::get)
            .collect(Collectors.toList());
    }

    private static RoleDb newRole(String str) throws InvalidRoleInternalException {
        return RoleDb.newBuilder().withName(str).build();
    }
}