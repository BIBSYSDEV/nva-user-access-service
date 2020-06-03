package no.unit.nva.database;

import static java.util.Objects.isNull;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import nva.commons.utils.attempt.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserDbTest extends DatabaseTest {

    public static final String SOME_USERNAME = "someUser";
    public static final String SOME_INSTITUTION = "SomeInstitution";
    public static final List<RoleDb> SAMPLE_ROLES = createSampleRoles();



    public static final String GETTER_GET_PREFIX = "get";
    public static final String GETTER_IS_PREFIX = "is";
    public static final String NON_EMPTY_FIELD_REQUIREMENT = "Test requires all fields to be non-empty.";
    public static final String BLANK_STRING = " ";
    private UserDb dynamoFunctionalityTestUser;
    private UserDb sampleUser;

    @BeforeEach
    private void init() throws InvalidUserException {
        dynamoFunctionalityTestUser = new UserDb();
        sampleUser = UserDb.newBuilder().withUsername(SOME_USERNAME).build();
        initializeDatabase();
    }

    @Test
    public void setUsernameShouldAddUsernameToUserObject() throws InvalidUserException {
        dynamoFunctionalityTestUser.setUsername(SOME_USERNAME);
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(equalTo(SOME_USERNAME)));
    }

    @Test
    public void getUsernameShouldGetTheSetUsernameToUserObject() throws InvalidUserException {
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(nullValue()));

        dynamoFunctionalityTestUser.setUsername(SOME_USERNAME);
        assertThat(dynamoFunctionalityTestUser.getUsername(), is(equalTo(SOME_USERNAME)));
    }

    @Test
    public void getTypeShouldReturnConstantTypeValue() {
        assertThat(dynamoFunctionalityTestUser.getType(), is(equalTo(UserDb.TYPE)));
    }

    @Test
    public void setTypeShouldNotChangeTheReturnedTypeValue() {
        dynamoFunctionalityTestUser.setType("NotExpectedType");
        assertThat(dynamoFunctionalityTestUser.getType(), is(equalTo(UserDb.TYPE)));
    }

    @Test
    public void getHashKeyKeyShouldReturnTypeAndUsernameConcatenation()  {
        String expectedHashKey = String.join(UserDb.FIELD_DELIMITER, UserDb.TYPE, SOME_USERNAME);
        assertThat(sampleUser.getPrimaryHashKey(), is(equalTo(expectedHashKey)));
    }

    @Test
    public void builderShouldSetTheHashKeyBasedOnusername() throws InvalidUserException {
        sampleUser.setPrimaryHashKey("SomeOtherHashKey");
        String expectedHashKey = String.join(UserDb.FIELD_DELIMITER, UserDb.TYPE, SOME_USERNAME);
        assertThat(sampleUser.getPrimaryHashKey(), is(equalTo(expectedHashKey)));
    }

    @Test
    public void userDbShouldBeWriteableToDatabase() throws InvalidUserException {
        DynamoDBMapper mapper = clientToLocalDatabase();
        assertDoesNotThrow(() -> mapper.save(sampleUser));
    }

    @Test
    public void userDbShouldBeReadFromDatabaseWithoutDataLoss() throws InvalidUserException {
        UserDb insertedUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(SAMPLE_ROLES)
            .build();
        DynamoDBMapper mapper = clientToLocalDatabase();
        mapper.save(insertedUser);
        assertThatNoPublicFieldIsNull(insertedUser);
        UserDb savedUser = mapper.load(UserDb.class, insertedUser.getPrimaryHashKey());
        assertThat(savedUser, is(equalTo(insertedUser)));
    }

    @Test
    public void builderShouldThrowExceptionWhenAtLeastOneInputRoleIsInvalid() throws InvalidUserException {
        List<RoleDb> roles = SAMPLE_ROLES;
        RoleDb invalidRole = new RoleDb();
        invalidRole.setName(BLANK_STRING);
        roles.add(invalidRole);

        UserDb insertedUser = UserDb.newBuilder()
            .withUsername(SOME_USERNAME)
            .withInstitution(SOME_INSTITUTION)
            .withRoles(roles)
            .build();
    }


    private void assertThatNoPublicFieldIsNull(UserDb insertedUser) {
        Method[] methods = insertedUser.getClass().getMethods();
        Stream<MethodInvocationResult> getterInvocations = Arrays.stream(methods)
            .filter(this::isAGetter)
            .map(attempt(method -> invokeMethod(insertedUser, method)))
            .map(eff -> eff.orElseThrow(fail -> new RuntimeException(fail.getException())));

        List<MethodInvocationResult> emptyArgs = getterInvocations.filter(this::isEmpty).collect(
            Collectors.toList());
        assertThat(NON_EMPTY_FIELD_REQUIREMENT, emptyArgs, is(empty()));
    }

    private MethodInvocationResult invokeMethod(UserDb insertedUser, Method method)
        throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(insertedUser);
        return new MethodInvocationResult(method.getName(), result);
    }

    private boolean isEmpty(MethodInvocationResult mir) {
        if (isNull(mir.result)) {
            return true;
        } else {
            if (mir.result instanceof Collection<?>) {
                Collection col = (Collection) mir.result;
                return col.isEmpty();
            } else if (mir.result instanceof Map<?, ?>) {
                Map map = (Map) mir.result;
                return map.isEmpty();
            } else {
                return false;
            }
        }
    }

    private boolean isAGetter(Method m) {
        return m.getName().startsWith(GETTER_GET_PREFIX) || m.getName().startsWith(GETTER_IS_PREFIX);
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

    private static RoleDb newRole(String str) throws InvalidRoleException {
        return RoleDb.newBuilder().withName(str).build();
    }

    private static class MethodInvocationResult {

        public final String methodName;
        public final Object result;

        public MethodInvocationResult(String methodName, Object result) {
            this.methodName = methodName;
            this.result = result;
        }

        public String toString() {
            return this.methodName;
        }
    }
}